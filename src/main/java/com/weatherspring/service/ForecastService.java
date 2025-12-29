package com.weatherspring.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.weatherspring.client.WeatherApiClient;
import com.weatherspring.dto.ForecastDto;
import com.weatherspring.dto.external.ForecastApiResponse;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.mapper.ForecastMapper;
import com.weatherspring.model.ForecastRecord;
import com.weatherspring.model.Location;
import com.weatherspring.repository.ForecastRecordRepository;
import com.weatherspring.validation.DateRangeValidator;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing weather forecast data.
 *
 * <p>Integrates with external Weather API and manages forecast records.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Validated
public class ForecastService {

  private static final int MAX_FORECAST_DAYS = 14;

  private final WeatherApiClient weatherApiClient;
  private final ForecastRecordRepository forecastRecordRepository;
  private final LocationService locationService;
  private final ForecastMapper forecastMapper;

  /**
   * Fetches weather forecast for a location name from external API.
   *
   * @param locationName the location name
   * @param days number of forecast days (1-14)
   * @param saveToDatabase whether to save the forecast data to database
   * @return list of forecast DTOs
   * @throws IllegalArgumentException if locationName is null/blank or days is not between 1 and 14
   * @throws com.weatherspring.exception.WeatherApiException if the external API call fails
   * @throws org.springframework.dao.DataAccessException if database save fails (when saveToDatabase
   *     is true)
   */
  @Observed(name = "forecast.get", contextualName = "get-forecast")
  @Transactional(propagation = Propagation.REQUIRED)
  @Cacheable(
      value = "forecasts",
      key = "'forecast:byName:' + #locationName + ':' + #days",
      unless = "#result == null || #result.isEmpty()")
  public List<ForecastDto> getForecast(
      @NotBlank String locationName, @Min(1) @Max(14) int days, boolean saveToDatabase) {
    log.info("Fetching {}-day forecast for location: {}", days, locationName);

    ForecastApiResponse apiResponse = weatherApiClient.getForecast(locationName, days);

    if (saveToDatabase) {
      saveForecastRecords(apiResponse);
    }

    List<ForecastDto> result = forecastMapper.toDtoFromApi(apiResponse, locationName);
    log.debug(
        "Successfully fetched {}-day forecast for location: {} ({} forecast records)",
        days,
        locationName,
        result.size());
    return result;
  }

  /**
   * Fetches weather forecast for a saved location ID.
   *
   * @param locationId the location ID
   * @param days number of forecast days (1-14)
   * @param saveToDatabase whether to save the forecast data to database
   * @return list of forecast DTOs
   * @throws IllegalArgumentException if locationId is null or days is not between 1 and 14
   * @throws com.weatherspring.exception.LocationNotFoundException if location with given ID does
   *     not exist
   * @throws com.weatherspring.exception.WeatherApiException if the external API call fails
   * @throws org.springframework.dao.DataAccessException if database save fails (when saveToDatabase
   *     is true)
   */
  @Observed(name = "forecast.get.by.location.id", contextualName = "get-forecast-by-location")
  @Transactional(propagation = Propagation.REQUIRED)
  @Cacheable(
      value = "forecasts",
      key = "'forecast:byId:' + #locationId + ':' + #days",
      unless = "#result == null || #result.isEmpty()")
  public List<ForecastDto> getForecastByLocationId(
      @NotNull Long locationId, @Min(1) @Max(14) int days, boolean saveToDatabase) {
    log.info("Fetching {}-day forecast for location ID: {}", days, locationId);

    Location location = locationService.getLocationEntityById(locationId);
    String locationQuery = location.getName() + "," + location.getCountry();

    ForecastApiResponse apiResponse = weatherApiClient.getForecast(locationQuery, days);

    if (saveToDatabase) {
      List<ForecastRecord> forecastRecords = forecastMapper.fromWeatherApi(apiResponse, location);
      upsertForecastRecords(forecastRecords, locationId);
      log.info("Saved {} forecast records for location ID: {}", forecastRecords.size(), locationId);
    }

    List<ForecastDto> result = forecastMapper.toDtoFromApi(apiResponse, location.getName());
    log.debug(
        "Successfully fetched {}-day forecast for location ID: {} ({}) - {} forecast records",
        days,
        locationId,
        location.getName(),
        result.size());
    return result;
  }

  /**
   * Retrieves stored forecast data for a location.
   *
   * @param locationId the location ID
   * @return list of forecast DTOs
   * @throws IllegalArgumentException if locationId is null
   * @throws com.weatherspring.exception.LocationNotFoundException if location with given ID does
   *     not exist
   * @throws org.springframework.dao.DataAccessException if database query fails
   */
  @Transactional(readOnly = true)
  @Observed(name = "forecast.get.stored", contextualName = "get-stored-forecasts")
  public List<ForecastDto> getStoredForecasts(@NotNull Long locationId) {
    log.debug("Fetching stored forecasts for location ID: {}", locationId);

    locationService.getLocationEntityById(locationId);

    List<ForecastDto> result =
        forecastRecordRepository.findByLocationId(locationId).stream()
            .map(forecastMapper::toDto)
            .toList();

    log.debug(
        "Successfully retrieved {} stored forecast records for location ID: {}",
        result.size(),
        locationId);
    return result;
  }

  /**
   * Retrieves future forecasts for a location.
   *
   * @param locationId the location ID
   * @return list of future forecast DTOs
   * @throws IllegalArgumentException if locationId is null
   * @throws com.weatherspring.exception.LocationNotFoundException if location with given ID does
   *     not exist
   * @throws org.springframework.dao.DataAccessException if database query fails
   */
  @Transactional(readOnly = true)
  @Observed(name = "forecast.get.future", contextualName = "get-future-forecasts")
  public List<ForecastDto> getFutureForecasts(@NotNull Long locationId) {
    log.debug("Fetching future forecasts for location ID: {}", locationId);

    locationService.getLocationEntityById(locationId);

    LocalDate today = LocalDate.now();

    List<ForecastDto> result =
        forecastRecordRepository.findFutureForecastsByLocationId(locationId, today).stream()
            .map(forecastMapper::toDto)
            .toList();

    log.debug(
        "Successfully retrieved {} future forecast records for location ID: {} (after {})",
        result.size(),
        locationId,
        today);
    return result;
  }

  /**
   * Retrieves forecasts within a date range.
   *
   * @param locationId the location ID
   * @param startDate start of the date range
   * @param endDate end of the date range
   * @return list of forecast DTOs
   * @throws IllegalArgumentException if locationId, startDate, or endDate is null, or if startDate
   *     is after endDate
   * @throws com.weatherspring.exception.LocationNotFoundException if location with given ID does
   *     not exist
   * @throws org.springframework.dao.DataAccessException if database query fails
   */
  @Transactional(readOnly = true)
  @Observed(name = "forecast.get.by.date.range", contextualName = "get-forecasts-by-date")
  public List<ForecastDto> getForecastsByDateRange(
      @NotNull Long locationId, @NotNull LocalDate startDate, @NotNull LocalDate endDate) {
    // Validate date range to prevent unbounded queries (max 365 days, not too far in past)
    DateRangeValidator.validateForecastRange(startDate, endDate);

    log.debug(
        "Fetching forecasts for location ID {} from {} to {}", locationId, startDate, endDate);

    locationService.getLocationEntityById(locationId);

    List<ForecastDto> result =
        forecastRecordRepository
            .findByLocationIdAndForecastDateBetween(locationId, startDate, endDate)
            .stream()
            .map(forecastMapper::toDto)
            .toList();

    log.debug(
        "Successfully retrieved {} forecast records for location ID: {} in date range {} to {}",
        result.size(),
        locationId,
        startDate,
        endDate);
    return result;
  }

  /**
   * Saves forecast records from external API response.
   *
   * <p><strong>Optimized for bulk operations:</strong> Prevents N+1 queries by checking for
   * existing forecasts in a single database query before saving. This approach:
   *
   * <ul>
   *   <li>Extracts all forecast dates from records to be saved
   *   <li>Queries existing records in bulk (single SELECT)
   *   <li>Filters out duplicates using a Set lookup (O(1) per record)
   *   <li>Saves only new records in batch (single INSERT batch)
   * </ul>
   *
   * <p>This eliminates the N+1 problem that would occur if we saved individually and caught
   * exceptions for each duplicate.
   *
   * @param apiResponse the external API response
   */
  private void saveForecastRecords(ForecastApiResponse apiResponse) {
    WeatherApiResponse.LocationInfo locationInfo = apiResponse.getLocation();

    Location location =
        locationService.findOrCreateLocation(
            locationInfo.getName(),
            locationInfo.getCountry(),
            locationInfo.getLat(),
            locationInfo.getLon(),
            locationInfo.getRegion());

    List<ForecastRecord> forecastRecords = forecastMapper.fromWeatherApi(apiResponse, location);

    // Extract all forecast dates to check for duplicates
    List<LocalDate> forecastDates =
        forecastRecords.stream().map(ForecastRecord::getForecastDate).toList();

    // Bulk query to find existing records (single SELECT instead of N queries)
    List<ForecastRecord> existingRecords =
        forecastRecordRepository.findByLocationIdAndForecastDateIn(location.getId(), forecastDates);

    // Build a set of existing dates for O(1) lookup
    Set<LocalDate> existingDates =
        existingRecords.stream().map(ForecastRecord::getForecastDate).collect(Collectors.toSet());

    // Filter out duplicates - only keep new records
    List<ForecastRecord> newRecords =
        forecastRecords.stream()
            .filter(record -> !existingDates.contains(record.getForecastDate()))
            .toList();

    if (!newRecords.isEmpty()) {
      forecastRecordRepository.saveAll(newRecords);
      log.info(
          "Saved {} new forecast records for location: {} ({} duplicates skipped)",
          newRecords.size(),
          location.getName(),
          forecastRecords.size() - newRecords.size());
    } else {
      log.info(
          "No new forecast records to save for location: {} (all {} records already exist)",
          location.getName(),
          forecastRecords.size());
    }
  }

  /**
   * Saves forecast records, skipping duplicates. This prevents unique constraint violations when
   * saving forecasts that may already exist in the database.
   *
   * @param forecastRecords the forecast records to save
   * @param locationId the location ID
   */
  private void upsertForecastRecords(List<ForecastRecord> forecastRecords, Long locationId) {
    if (forecastRecords.isEmpty()) {
      return;
    }

    // Extract all forecast dates to check for duplicates
    List<LocalDate> forecastDates =
        forecastRecords.stream().map(ForecastRecord::getForecastDate).toList();

    // Bulk query to find existing records (single SELECT instead of N queries)
    List<ForecastRecord> existingRecords =
        forecastRecordRepository.findByLocationIdAndForecastDateIn(locationId, forecastDates);

    // Build a set of existing dates for O(1) lookup
    Set<LocalDate> existingDates =
        existingRecords.stream().map(ForecastRecord::getForecastDate).collect(Collectors.toSet());

    // Filter out duplicates - only keep new records
    List<ForecastRecord> newRecords =
        forecastRecords.stream()
            .filter(record -> !existingDates.contains(record.getForecastDate()))
            .toList();

    if (!newRecords.isEmpty()) {
      forecastRecordRepository.saveAll(newRecords);
      log.info(
          "Saved {} new forecast records for location ID: {} ({} duplicates skipped)",
          newRecords.size(),
          locationId,
          forecastRecords.size() - newRecords.size());
    } else {
      log.info(
          "No new forecast records to save for location ID: {} (all {} records already exist)",
          locationId,
          forecastRecords.size());
    }
  }

  /**
   * Deletes old forecast records before a specific date.
   *
   * <p>This method runs in a separate transaction (REQUIRES_NEW) to ensure cleanup operations don't
   * interfere with other transactions.
   *
   * @param cutoffDate the cutoff date
   * @return the number of records deleted
   * @throws IllegalArgumentException if cutoffDate is null
   * @throws org.springframework.dao.DataAccessException if database delete operation fails
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public long deleteOldForecasts(@NotNull LocalDate cutoffDate) {
    log.info("Deleting forecast records before: {}", cutoffDate);
    Long deletedCount = forecastRecordRepository.deleteByForecastDateBefore(cutoffDate);
    log.info("Successfully deleted {} forecast records before {}", deletedCount, cutoffDate);
    return deletedCount;
  }
}

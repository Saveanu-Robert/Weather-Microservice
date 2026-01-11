package com.weatherspring.service;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.weatherspring.client.WeatherApiClient;
import com.weatherspring.dto.WeatherDto;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.mapper.WeatherMapper;
import com.weatherspring.model.Location;
import com.weatherspring.model.WeatherRecord;
import com.weatherspring.repository.WeatherRecordRepository;
import com.weatherspring.validation.DateRangeValidator;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Fetches current weather from external API and manages historical weather records.
 *
 * <p>This service handles two distinct responsibilities:
 * <ul>
 *   <li>Fetching live weather data from external API (with optional database persistence)
 *   <li>Querying historical weather records stored in the database
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Validated
public class WeatherService {

  private final WeatherApiClient weatherApiClient;
  private final WeatherRecordRepository weatherRecordRepository;
  private final LocationService locationService;
  private final WeatherMapper weatherMapper;

  /**
   * Fetches current weather from external API for a location name.
   *
   * <p>When saveToDatabase=true, creates the location record if it doesn't exist and saves
   * the weather snapshot. Duplicate records are safely ignored (concurrent requests for same
   * location/time).
   *
   * @param locationName location name or "city,country" format
   * @param saveToDatabase true to persist weather snapshot and location to database
   * @return current weather data from API
   */
  @Observed(name = "weather.get.current", contextualName = "get-current-weather")
  @Transactional(propagation = Propagation.REQUIRED)
  @Cacheable(
      value = "currentWeather",
      key = "'weather:byName:' + #locationName",
      unless = "#result == null")
  public WeatherDto getCurrentWeather(@NotBlank String locationName, boolean saveToDatabase) {
    WeatherApiResponse apiResponse = weatherApiClient.getCurrentWeather(locationName);

    if (saveToDatabase) {
      saveWeatherRecord(apiResponse);
    }

    WeatherDto result = weatherMapper.toDtoFromApi(apiResponse);
    log.debug(
        "Successfully fetched current weather for location: {} (temp: {}°C, condition: {})",
        locationName,
        result.temperature(),
        result.condition());
    return result;
  }

  /**
   * Fetches current weather from external API for a saved location.
   *
   * <p>Uses the location's name and country to query the API. When saveToDatabase=true,
   * saves the weather snapshot linked to this location ID.
   *
   * @param locationId ID of the saved location
   * @param saveToDatabase true to persist weather snapshot to database
   * @return current weather data from API
   */
  @Observed(name = "weather.get.by.location.id", contextualName = "get-weather-by-location")
  @Transactional(propagation = Propagation.REQUIRED)
  @Cacheable(
      value = "currentWeather",
      key = "'weather:byId:' + #locationId",
      unless = "#result == null")
  public WeatherDto getCurrentWeatherByLocationId(
      @NotNull Long locationId, boolean saveToDatabase) {
    Location location = locationService.getLocationEntityById(locationId);
    String locationQuery = location.getName() + "," + location.getCountry();

    WeatherApiResponse apiResponse = weatherApiClient.getCurrentWeather(locationQuery);

    if (saveToDatabase) {
      WeatherRecord weatherRecord = weatherMapper.fromWeatherApi(apiResponse, location);
      weatherRecordRepository.save(weatherRecord);
      log.info("Saved weather record for location ID: {}", locationId);
    }

    WeatherDto result = weatherMapper.toDtoFromApi(apiResponse);
    log.debug(
        "Successfully fetched current weather for location ID: {} ({}) - temp: {}°C",
        locationId,
        location.getName(),
        result.temperature());
    return result;
  }

  /**
   * Retrieves historical weather records from database with pagination.
   *
   * <p>Returns only records that were previously saved via saveToDatabase=true.
   * Results are sorted by timestamp descending (newest first).
   *
   * @param locationId location ID to query records for
   * @param pageable pagination parameters (page, size, sort)
   * @return page of historical weather records
   */
  @Transactional(readOnly = true)
  @Observed(name = "weather.get.history", contextualName = "get-weather-history")
  public Page<WeatherDto> getWeatherHistory(@NotNull Long locationId, @NotNull Pageable pageable) {
    log.debug("Fetching weather history for location ID: {}", locationId);

    locationService.getLocationEntityById(locationId);

    Page<WeatherDto> result =
        weatherRecordRepository.findByLocationId(locationId, pageable).map(weatherMapper::toDto);

    log.debug(
        "Successfully retrieved {} weather history records for location ID: {} (page {}/{})",
        result.getNumberOfElements(),
        locationId,
        result.getNumber() + 1,
        result.getTotalPages());
    return result;
  }

  /**
   * Retrieves historical weather records within a date range.
   *
   * <p>Date range is validated to prevent performance issues: maximum 90 days, no older than 1 year.
   * Returns only records previously saved to database.
   *
   * @param locationId location ID to query records for
   * @param startDate range start (inclusive)
   * @param endDate range end (inclusive)
   * @return list of weather records in the specified date range
   */
  @Transactional(readOnly = true)
  @Observed(
      name = "weather.get.history.by.date.range",
      contextualName = "get-weather-history-by-date")
  public List<WeatherDto> getWeatherHistoryByDateRange(
      @NotNull Long locationId, @NotNull LocalDateTime startDate, @NotNull LocalDateTime endDate) {
    DateRangeValidator.validateWeatherHistoryRange(startDate, endDate);

    log.debug(
        "Fetching weather history for location ID {} from {} to {}",
        locationId,
        startDate,
        endDate);

    locationService.getLocationEntityById(locationId);

    List<WeatherDto> result =
        weatherRecordRepository
            .findByLocationIdAndTimestampBetween(locationId, startDate, endDate)
            .stream()
            .map(weatherMapper::toDto)
            .toList();

    log.debug(
        "Successfully retrieved {} weather records for location ID: {} in date range {} to {}",
        result.size(),
        locationId,
        startDate,
        endDate);
    return result;
  }

  /**
   * Persists weather data from API response to database.
   *
   * <p>Creates the location if it doesn't exist. Duplicate weather records (same location and time)
   * are silently ignored to handle concurrent requests fetching the same weather data.
   */
  private void saveWeatherRecord(WeatherApiResponse apiResponse) {
    WeatherApiResponse.LocationInfo locationInfo = apiResponse.getLocation();

    Location location =
        locationService.findOrCreateLocation(
            locationInfo.getName(),
            locationInfo.getCountry(),
            locationInfo.getLat(),
            locationInfo.getLon(),
            locationInfo.getRegion());

    try {
      WeatherRecord weatherRecord = weatherMapper.fromWeatherApi(apiResponse, location);
      WeatherRecord saved = weatherRecordRepository.save(weatherRecord);

      log.info(
          "Saved weather record with ID: {} for location: {}", saved.getId(), location.getName());
    } catch (org.springframework.dao.DataIntegrityViolationException e) {
      // Concurrent request already saved this record - safe to ignore
      log.debug("Duplicate weather record for location {} - ignoring", location.getName());
    }
  }

  /**
   * Deletes old weather records before a cutoff date.
   *
   * <p>Runs in a separate transaction (REQUIRES_NEW) so cleanup doesn't hold locks
   * or interfere with concurrent weather fetches.
   *
   * @param cutoffDate delete all records before this date
   * @return number of records deleted
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public long deleteOldWeatherRecords(@NotNull LocalDateTime cutoffDate) {
    log.info("Deleting weather records before: {}", cutoffDate);
    Long deletedCount = weatherRecordRepository.deleteByTimestampBefore(cutoffDate);
    log.info("Successfully deleted {} weather records before {}", deletedCount, cutoffDate);
    return deletedCount;
  }
}

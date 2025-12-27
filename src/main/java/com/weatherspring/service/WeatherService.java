package com.weatherspring.service;

import com.weatherspring.client.WeatherApiClient;
import com.weatherspring.dto.WeatherDto;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.mapper.WeatherMapper;
import com.weatherspring.model.Location;
import com.weatherspring.model.WeatherRecord;
import com.weatherspring.repository.WeatherRecordRepository;
import com.weatherspring.validation.DateRangeValidator;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Fetches current weather from external API and manages historical weather records.
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
     * Gets current weather for a location. Optionally saves the weather data to the database.
     */
    @Observed(name = "weather.get.current", contextualName = "get-current-weather")
    @Transactional(propagation = Propagation.REQUIRED)
    @Cacheable(value = "currentWeather", key = "'weather:byName:' + #locationName", unless = "#result == null")
    public WeatherDto getCurrentWeather(@NotBlank String locationName, boolean saveToDatabase) {
        WeatherApiResponse apiResponse = weatherApiClient.getCurrentWeather(locationName);

        if (saveToDatabase) {
            saveWeatherRecord(apiResponse);
        }

        WeatherDto result = weatherMapper.toDtoFromApi(apiResponse);
        log.debug("Successfully fetched current weather for location: {} (temp: {}°C, condition: {})",
                locationName, result.temperature(), result.condition());
        return result;
    }

    /**
     * Gets current weather using a saved location ID.
     */
    @Observed(name = "weather.get.by.location.id", contextualName = "get-weather-by-location")
    @Transactional(propagation = Propagation.REQUIRED)
    @Cacheable(value = "currentWeather", key = "'weather:byId:' + #locationId", unless = "#result == null")
    public WeatherDto getCurrentWeatherByLocationId(@NotNull Long locationId, boolean saveToDatabase) {
        Location location = locationService.getLocationEntityById(locationId);
        String locationQuery = location.getName() + "," + location.getCountry();

        WeatherApiResponse apiResponse = weatherApiClient.getCurrentWeather(locationQuery);

        if (saveToDatabase) {
            WeatherRecord weatherRecord = weatherMapper.fromWeatherApi(apiResponse, location);
            weatherRecordRepository.save(weatherRecord);
            log.info("Saved weather record for location ID: {}", locationId);
        }

        WeatherDto result = weatherMapper.toDtoFromApi(apiResponse);
        log.debug("Successfully fetched current weather for location ID: {} ({}) - temp: {}°C",
                locationId, location.getName(), result.temperature());
        return result;
    }

    /**
     * Gets historical weather records for a location with pagination.
     */
    @Transactional(readOnly = true)
    @Observed(name = "weather.get.history", contextualName = "get-weather-history")
    public Page<WeatherDto> getWeatherHistory(@NotNull Long locationId, @NotNull Pageable pageable) {
        log.debug("Fetching weather history for location ID: {}", locationId);

        locationService.getLocationEntityById(locationId);

        Page<WeatherDto> result = weatherRecordRepository.findByLocationId(locationId, pageable)
                .map(weatherMapper::toDto);

        log.debug("Successfully retrieved {} weather history records for location ID: {} (page {}/{})",
                result.getNumberOfElements(), locationId, result.getNumber() + 1, result.getTotalPages());
        return result;
    }

    /**
     * Gets weather records for a location within a date range (max 90 days, not older than 1 year).
     */
    @Transactional(readOnly = true)
    @Observed(name = "weather.get.history.by.date.range", contextualName = "get-weather-history-by-date")
    public List<WeatherDto> getWeatherHistoryByDateRange(
            @NotNull Long locationId, @NotNull LocalDateTime startDate, @NotNull LocalDateTime endDate) {
        DateRangeValidator.validateWeatherHistoryRange(startDate, endDate);

        log.debug("Fetching weather history for location ID {} from {} to {}",
                    locationId, startDate, endDate);

        locationService.getLocationEntityById(locationId);

        List<WeatherDto> result = weatherRecordRepository
                .findByLocationIdAndTimestampBetween(locationId, startDate, endDate)
                .stream()
                .map(weatherMapper::toDto)
                .toList();

        log.debug("Successfully retrieved {} weather records for location ID: {} in date range {} to {}",
                result.size(), locationId, startDate, endDate);
        return result;
    }

    /**
     * Saves weather data from API response. Handles concurrent requests safely by
     * catching duplicate record violations.
     */
    private void saveWeatherRecord(WeatherApiResponse apiResponse) {
        WeatherApiResponse.LocationInfo locationInfo = apiResponse.getLocation();

        Location location = locationService.findOrCreateLocation(
                locationInfo.getName(),
                locationInfo.getCountry(),
                locationInfo.getLat(),
                locationInfo.getLon(),
                locationInfo.getRegion()
        );

        try {
            WeatherRecord weatherRecord = weatherMapper.fromWeatherApi(apiResponse, location);
            WeatherRecord saved = weatherRecordRepository.save(weatherRecord);

            log.info("Saved weather record with ID: {} for location: {}",
                       saved.getId(), location.getName());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Concurrent request already saved this record - safe to ignore
            log.debug("Duplicate weather record for location {} - ignoring", location.getName());
        }
    }

    /**
     * Deletes weather records older than the cutoff date.
     * Runs in a separate transaction to avoid interfering with other operations.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long deleteOldWeatherRecords(@NotNull LocalDateTime cutoffDate) {
        log.info("Deleting weather records before: {}", cutoffDate);
        Long deletedCount = weatherRecordRepository.deleteByTimestampBefore(cutoffDate);
        log.info("Successfully deleted {} weather records before {}", deletedCount, cutoffDate);
        return deletedCount;
    }
}

package com.weatherspring.service;

import com.weatherspring.client.WeatherApiClient;
import com.weatherspring.dto.ForecastDto;
import com.weatherspring.dto.external.ForecastApiResponse;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.mapper.ForecastMapper;
import com.weatherspring.model.ForecastRecord;
import com.weatherspring.model.Location;
import com.weatherspring.repository.ForecastRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service for managing weather forecast data.
 *
 * <p>Integrates with external Weather API and manages forecast records.</p>
 */
@Service
@Transactional(readOnly = true)
public class ForecastService {

    private static final Logger logger = LoggerFactory.getLogger(ForecastService.class);
    private static final int MAX_FORECAST_DAYS = 14;

    private final WeatherApiClient weatherApiClient;
    private final ForecastRecordRepository forecastRecordRepository;
    private final LocationService locationService;
    private final ForecastMapper forecastMapper;

    public ForecastService(
            WeatherApiClient weatherApiClient,
            ForecastRecordRepository forecastRecordRepository,
            LocationService locationService,
            ForecastMapper forecastMapper) {
        this.weatherApiClient = weatherApiClient;
        this.forecastRecordRepository = forecastRecordRepository;
        this.locationService = locationService;
        this.forecastMapper = forecastMapper;
    }

    /**
     * Fetches weather forecast for a location name from external API.
     *
     * @param locationName the location name
     * @param days number of forecast days (1-14)
     * @param saveToDatabase whether to save the forecast data to database
     * @return list of forecast DTOs
     */
    @Transactional
    @Cacheable(value = "forecasts", key = "#locationName + '_' + #days", unless = "#result == null || #result.isEmpty()")
    public List<ForecastDto> getForecast(String locationName, int days, boolean saveToDatabase) {
        Objects.requireNonNull(locationName, "Location name cannot be null");
        if (locationName.isBlank()) {
            throw new IllegalArgumentException("Location name cannot be blank");
        }

        logger.info("Fetching {}-day forecast for location: {}", days, locationName);

        validateForecastDays(days);

        ForecastApiResponse apiResponse = weatherApiClient.getForecast(locationName, days);

        if (saveToDatabase) {
            saveForecastRecords(apiResponse);
        }

        return forecastMapper.toDtoFromApi(apiResponse, locationName);
    }

    /**
     * Fetches weather forecast for a saved location ID.
     *
     * @param locationId the location ID
     * @param days number of forecast days (1-14)
     * @param saveToDatabase whether to save the forecast data to database
     * @return list of forecast DTOs
     */
    @Transactional
    public List<ForecastDto> getForecastByLocationId(Long locationId, int days, boolean saveToDatabase) {
        Objects.requireNonNull(locationId, "Location ID cannot be null");

        logger.info("Fetching {}-day forecast for location ID: {}", days, locationId);

        validateForecastDays(days);

        Location location = locationService.getLocationEntityById(locationId);
        String locationQuery = location.getName() + "," + location.getCountry();

        ForecastApiResponse apiResponse = weatherApiClient.getForecast(locationQuery, days);

        if (saveToDatabase) {
            List<ForecastRecord> forecastRecords = forecastMapper.fromWeatherApi(apiResponse, location);
            forecastRecordRepository.saveAll(forecastRecords);
            logger.info("Saved {} forecast records for location ID: {}", forecastRecords.size(), locationId);
        }

        return forecastMapper.toDtoFromApi(apiResponse, location.getName());
    }

    /**
     * Retrieves stored forecast data for a location.
     *
     * @param locationId the location ID
     * @return list of forecast DTOs
     */
    public List<ForecastDto> getStoredForecasts(Long locationId) {
        Objects.requireNonNull(locationId, "Location ID cannot be null");

        logger.debug("Fetching stored forecasts for location ID: {}", locationId);

        locationService.getLocationEntityById(locationId);

        return forecastRecordRepository.findByLocationId(locationId)
                .stream()
                .map(forecastMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves future forecasts for a location.
     *
     * @param locationId the location ID
     * @return list of future forecast DTOs
     */
    public List<ForecastDto> getFutureForecasts(Long locationId) {
        Objects.requireNonNull(locationId, "Location ID cannot be null");

        logger.debug("Fetching future forecasts for location ID: {}", locationId);

        locationService.getLocationEntityById(locationId);

        LocalDate today = LocalDate.now();

        return forecastRecordRepository.findFutureForecastsByLocationId(locationId, today)
                .stream()
                .map(forecastMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves forecasts within a date range.
     *
     * @param locationId the location ID
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @return list of forecast DTOs
     */
    public List<ForecastDto> getForecastsByDateRange(
            Long locationId, LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(locationId, "Location ID cannot be null");
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        logger.debug("Fetching forecasts for location ID {} from {} to {}",
                    locationId, startDate, endDate);

        locationService.getLocationEntityById(locationId);

        return forecastRecordRepository
                .findByLocationIdAndForecastDateBetween(locationId, startDate, endDate)
                .stream()
                .map(forecastMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Saves forecast records from external API response.
     *
     * @param apiResponse the external API response
     */
    private void saveForecastRecords(ForecastApiResponse apiResponse) {
        WeatherApiResponse.LocationInfo locationInfo = apiResponse.getLocation();

        Location location = locationService.findOrCreateLocation(
                locationInfo.getName(),
                locationInfo.getCountry(),
                locationInfo.getLat(),
                locationInfo.getLon(),
                locationInfo.getRegion()
        );

        List<ForecastRecord> forecastRecords = forecastMapper.fromWeatherApi(apiResponse, location);
        forecastRecordRepository.saveAll(forecastRecords);

        logger.info("Saved {} forecast records for location: {}",
                   forecastRecords.size(), location.getName());
    }

    /**
     * Deletes old forecast records before a specific date.
     *
     * @param cutoffDate the cutoff date
     */
    @Transactional
    public void deleteOldForecasts(LocalDate cutoffDate) {
        Objects.requireNonNull(cutoffDate, "Cutoff date cannot be null");

        logger.info("Deleting forecast records before: {}", cutoffDate);
        forecastRecordRepository.deleteByForecastDateBefore(cutoffDate);
    }

    /**
     * Validates that forecast days is within acceptable range.
     *
     * @param days the number of forecast days
     * @throws IllegalArgumentException if days is out of range
     */
    private void validateForecastDays(int days) {
        if (days < 1 || days > MAX_FORECAST_DAYS) {
            throw new IllegalArgumentException(
                "Forecast days must be between 1 and " + MAX_FORECAST_DAYS);
        }
    }
}

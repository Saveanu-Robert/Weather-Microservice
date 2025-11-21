package com.weatherspring.service;

import com.weatherspring.client.WeatherApiClient;
import com.weatherspring.dto.WeatherDto;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.mapper.WeatherMapper;
import com.weatherspring.model.Location;
import com.weatherspring.model.WeatherRecord;
import com.weatherspring.repository.WeatherRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service for managing weather data.
 *
 * <p>Integrates with external Weather API and manages historical weather records.</p>
 */
@Service
@Transactional(readOnly = true)
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    private final WeatherApiClient weatherApiClient;
    private final WeatherRecordRepository weatherRecordRepository;
    private final LocationService locationService;
    private final WeatherMapper weatherMapper;

    public WeatherService(
            WeatherApiClient weatherApiClient,
            WeatherRecordRepository weatherRecordRepository,
            LocationService locationService,
            WeatherMapper weatherMapper) {
        this.weatherApiClient = weatherApiClient;
        this.weatherRecordRepository = weatherRecordRepository;
        this.locationService = locationService;
        this.weatherMapper = weatherMapper;
    }

    /**
     * Fetches current weather for a location name from external API.
     *
     * @param locationName the location name
     * @param saveToDatabase whether to save the weather data to database
     * @return current weather DTO
     */
    @Transactional
    @Cacheable(value = "currentWeather", key = "#locationName", unless = "#result == null")
    public WeatherDto getCurrentWeather(String locationName, boolean saveToDatabase) {
        Objects.requireNonNull(locationName, "Location name cannot be null");
        if (locationName.isBlank()) {
            throw new IllegalArgumentException("Location name cannot be blank");
        }

        logger.info("Fetching current weather for location: {}", locationName);

        WeatherApiResponse apiResponse = weatherApiClient.getCurrentWeather(locationName);

        if (saveToDatabase) {
            saveWeatherRecord(apiResponse);
        }

        return weatherMapper.toDtoFromApi(apiResponse);
    }

    /**
     * Fetches current weather for a saved location ID.
     *
     * @param locationId the location ID
     * @param saveToDatabase whether to save the weather data to database
     * @return current weather DTO
     */
    @Transactional
    public WeatherDto getCurrentWeatherByLocationId(Long locationId, boolean saveToDatabase) {
        Objects.requireNonNull(locationId, "Location ID cannot be null");

        logger.info("Fetching current weather for location ID: {}", locationId);

        Location location = locationService.getLocationEntityById(locationId);
        String locationQuery = location.getName() + "," + location.getCountry();

        WeatherApiResponse apiResponse = weatherApiClient.getCurrentWeather(locationQuery);

        if (saveToDatabase) {
            WeatherRecord weatherRecord = weatherMapper.fromWeatherApi(apiResponse, location);
            weatherRecordRepository.save(weatherRecord);
            logger.info("Saved weather record for location ID: {}", locationId);
        }

        return weatherMapper.toDtoFromApi(apiResponse);
    }

    /**
     * Retrieves historical weather records for a location.
     *
     * @param locationId the location ID
     * @param pageable pagination information
     * @return page of weather DTOs
     */
    public Page<WeatherDto> getWeatherHistory(Long locationId, Pageable pageable) {
        Objects.requireNonNull(locationId, "Location ID cannot be null");
        Objects.requireNonNull(pageable, "Pageable cannot be null");

        logger.debug("Fetching weather history for location ID: {}", locationId);

        locationService.getLocationEntityById(locationId);

        return weatherRecordRepository.findByLocationId(locationId, pageable)
                .map(weatherMapper::toDto);
    }

    /**
     * Retrieves weather records within a date range.
     *
     * @param locationId the location ID
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @return list of weather DTOs
     */
    public List<WeatherDto> getWeatherHistoryByDateRange(
            Long locationId, LocalDateTime startDate, LocalDateTime endDate) {
        Objects.requireNonNull(locationId, "Location ID cannot be null");
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        logger.debug("Fetching weather history for location ID {} from {} to {}",
                    locationId, startDate, endDate);

        locationService.getLocationEntityById(locationId);

        return weatherRecordRepository
                .findByLocationIdAndTimestampBetween(locationId, startDate, endDate)
                .stream()
                .map(weatherMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Saves a weather record from external API response.
     *
     * @param apiResponse the external API response
     * @return the saved weather record
     */
    private WeatherRecord saveWeatherRecord(WeatherApiResponse apiResponse) {
        WeatherApiResponse.LocationInfo locationInfo = apiResponse.getLocation();

        Location location = locationService.findOrCreateLocation(
                locationInfo.getName(),
                locationInfo.getCountry(),
                locationInfo.getLat(),
                locationInfo.getLon(),
                locationInfo.getRegion()
        );

        WeatherRecord weatherRecord = weatherMapper.fromWeatherApi(apiResponse, location);
        WeatherRecord saved = weatherRecordRepository.save(weatherRecord);

        logger.info("Saved weather record with ID: {} for location: {}",
                   saved.getId(), location.getName());

        return saved;
    }

    /**
     * Deletes old weather records before a specific date.
     *
     * @param cutoffDate the cutoff date
     */
    @Transactional
    public void deleteOldWeatherRecords(LocalDateTime cutoffDate) {
        Objects.requireNonNull(cutoffDate, "Cutoff date cannot be null");

        logger.info("Deleting weather records before: {}", cutoffDate);
        weatherRecordRepository.deleteByTimestampBefore(cutoffDate);
    }
}

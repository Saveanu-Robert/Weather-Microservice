package com.weatherspring.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.annotation.Nullable;

import org.springframework.stereotype.Component;

import com.weatherspring.dto.WeatherDto;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.model.Location;
import com.weatherspring.model.WeatherRecord;

import lombok.extern.slf4j.Slf4j;

/**
 * Converts between WeatherRecord entities, DTOs, and external API responses.
 *
 * <p>Handles timestamp parsing from WeatherAPI.com's specific date format and validates
 * that location relationships are properly loaded to prevent lazy loading exceptions.
 */
@Slf4j
@Component
public class WeatherMapper {

  /**
   * Date-time format used by WeatherAPI.com for the localtime field.
   *
   * <p>Format: {@code yyyy-MM-dd HH:mm} (e.g., "2025-12-27 14:30")
   *
   * <p><strong>API-Specific:</strong> This format is specific to WeatherAPI.com's response
   * structure. If the external API changes its date format, this formatter must be updated
   * accordingly.
   *
   * @see <a href="https://www.weatherapi.com/docs/">WeatherAPI.com Documentation</a>
   */
  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  /**
   * Converts a WeatherRecord entity to WeatherDto.
   *
   * @param weatherRecord the weather record entity
   * @return the weather DTO, or null if input is null
   * @throws IllegalArgumentException if weatherRecord has null location (lazy loading issue)
   */
  @Nullable
  public WeatherDto toDto(@Nullable WeatherRecord weatherRecord) {
    if (weatherRecord == null) {
      return null;
    }

    // Critical null check: location could be null if not properly fetched (lazy loading)
    if (weatherRecord.getLocation() == null) {
      log.error(
          "WeatherRecord {} has null location - likely lazy loading issue", weatherRecord.getId());
      throw new IllegalArgumentException(
          "WeatherRecord location is null. Ensure @EntityGraph or JOIN FETCH is used to load location.");
    }

    return new WeatherDto(
        weatherRecord.getId(),
        weatherRecord.getLocation().getId(),
        weatherRecord.getLocation().getName(),
        weatherRecord.getTemperature(),
        weatherRecord.getFeelsLike(),
        weatherRecord.getHumidity(),
        weatherRecord.getWindSpeed(),
        weatherRecord.getWindDirection(),
        weatherRecord.getCondition(),
        weatherRecord.getDescription(),
        weatherRecord.getPressureMb(),
        weatherRecord.getPrecipitationMm(),
        weatherRecord.getCloudCoverage(),
        weatherRecord.getUvIndex(),
        weatherRecord.getTimestamp());
  }

  /**
   * Converts WeatherAPI response to WeatherRecord entity.
   *
   * @param apiResponse the external API response
   * @param location the associated location entity
   * @return the weather record entity, or null if inputs are invalid
   */
  @Nullable
  public WeatherRecord fromWeatherApi(@Nullable WeatherApiResponse apiResponse, Location location) {
    if (apiResponse == null || apiResponse.getCurrent() == null) {
      return null;
    }

    WeatherApiResponse.CurrentWeather current = apiResponse.getCurrent();
    LocalDateTime timestamp = parseTimestamp(apiResponse.getLocation().getLocaltime());

    return WeatherRecord.builder()
        .location(location)
        .temperature(current.getTempC())
        .feelsLike(current.getFeelslikeC())
        .humidity(current.getHumidity())
        .windSpeed(current.getWindKph())
        .windDirection(current.getWindDir())
        .condition(current.getCondition().getText())
        .description(current.getCondition().getText())
        .pressureMb(current.getPressureMb())
        .precipitationMm(current.getPrecipMm())
        .cloudCoverage(current.getCloud())
        .uvIndex(current.getUv())
        .timestamp(timestamp != null ? timestamp : LocalDateTime.now())
        .build();
  }

  /**
   * Creates a WeatherDto from WeatherAPI response (without persisting).
   *
   * @param apiResponse the external API response
   * @return the weather DTO, or null if input is invalid
   */
  @Nullable
  public WeatherDto toDtoFromApi(@Nullable WeatherApiResponse apiResponse) {
    if (apiResponse == null || apiResponse.getCurrent() == null) {
      return null;
    }

    WeatherApiResponse.CurrentWeather current = apiResponse.getCurrent();
    WeatherApiResponse.LocationInfo location = apiResponse.getLocation();
    LocalDateTime timestamp = parseTimestamp(location.getLocaltime());

    return new WeatherDto(
        null, // id
        null, // locationId
        location.getName(),
        current.getTempC(),
        current.getFeelslikeC(),
        current.getHumidity(),
        current.getWindKph(),
        current.getWindDir(),
        current.getCondition().getText(),
        current.getCondition().getText(),
        current.getPressureMb(),
        current.getPrecipMm(),
        current.getCloud(),
        current.getUv(),
        timestamp != null ? timestamp : LocalDateTime.now());
  }

  /**
   * Parses timestamp from WeatherAPI format.
   *
   * @param localtimeStr the localtime string from API
   * @return parsed LocalDateTime or null if parsing fails
   */
  private LocalDateTime parseTimestamp(String localtimeStr) {
    if (localtimeStr == null || localtimeStr.isEmpty()) {
      return null;
    }

    try {
      return LocalDateTime.parse(localtimeStr, DATETIME_FORMATTER);
    } catch (java.time.format.DateTimeParseException e) {
      log.warn("Failed to parse timestamp '{}', using current time", localtimeStr, e);
      return LocalDateTime.now();
    }
  }
}

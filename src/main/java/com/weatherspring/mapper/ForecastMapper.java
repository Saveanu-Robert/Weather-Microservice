package com.weatherspring.mapper;

import java.time.LocalDate;
import java.util.List;

import jakarta.annotation.Nullable;

import org.springframework.stereotype.Component;

import com.weatherspring.dto.ForecastDto;
import com.weatherspring.dto.external.ForecastApiResponse;
import com.weatherspring.model.ForecastRecord;
import com.weatherspring.model.Location;

import lombok.extern.slf4j.Slf4j;

/**
 * Converts between ForecastRecord entities, DTOs, and external API responses.
 *
 * <p>Handles null-safe conversion and validates that location relationships are properly
 * loaded to prevent lazy loading exceptions.
 */
@Slf4j
@Component
public class ForecastMapper {

  /**
   * Converts a ForecastRecord entity to ForecastDto.
   *
   * @param forecastRecord the forecast record entity
   * @return the forecast DTO, or null if input is null
   * @throws IllegalArgumentException if forecastRecord has null location (lazy loading issue)
   */
  @Nullable
  public ForecastDto toDto(@Nullable ForecastRecord forecastRecord) {
    if (forecastRecord == null) {
      return null;
    }

    // Critical null check: location could be null if not properly fetched (lazy loading)
    if (forecastRecord.getLocation() == null) {
      log.error(
          "ForecastRecord {} has null location - likely lazy loading issue",
          forecastRecord.getId());
      throw new IllegalArgumentException(
          "ForecastRecord location is null. Ensure @EntityGraph or JOIN FETCH is used to load location.");
    }

    return new ForecastDto(
        forecastRecord.getId(),
        forecastRecord.getLocation().getId(),
        forecastRecord.getLocation().getName(),
        forecastRecord.getForecastDate(),
        forecastRecord.getMaxTemperature(),
        forecastRecord.getMinTemperature(),
        forecastRecord.getAvgTemperature(),
        forecastRecord.getMaxWindSpeed(),
        forecastRecord.getAvgHumidity(),
        forecastRecord.getCondition(),
        forecastRecord.getDescription(),
        forecastRecord.getPrecipitationMm(),
        forecastRecord.getPrecipitationProbability(),
        forecastRecord.getUvIndex(),
        forecastRecord.getSunriseTime(),
        forecastRecord.getSunsetTime());
  }

  /**
   * Converts WeatherAPI forecast response to list of ForecastRecord entities.
   *
   * @param apiResponse the external API response
   * @param location the associated location entity
   * @return list of forecast record entities
   */
  public List<ForecastRecord> fromWeatherApi(ForecastApiResponse apiResponse, Location location) {
    if (apiResponse == null
        || apiResponse.getForecast() == null
        || apiResponse.getForecast().getForecastday() == null) {
      return List.of();
    }

    return apiResponse.getForecast().getForecastday().stream()
        .map(day -> fromForecastDay(day, location))
        .toList();
  }

  /**
   * Converts a single ForecastDay to ForecastRecord entity.
   *
   * @param forecastDay the forecast day from API
   * @param location the associated location entity
   * @return the forecast record entity, or null if inputs are invalid
   */
  @Nullable
  private ForecastRecord fromForecastDay(
      @Nullable ForecastApiResponse.ForecastDay forecastDay, Location location) {
    if (forecastDay == null || forecastDay.getDay() == null) {
      return null;
    }

    ForecastApiResponse.Day day = forecastDay.getDay();
    ForecastApiResponse.Astro astro = forecastDay.getAstro();

    return ForecastRecord.builder()
        .location(location)
        .forecastDate(LocalDate.parse(forecastDay.getDate()))
        .maxTemperature(day.getMaxtempC())
        .minTemperature(day.getMintempC())
        .avgTemperature(day.getAvgtempC())
        .maxWindSpeed(day.getMaxwindKph())
        .avgHumidity(day.getAvghumidity())
        .condition(day.getCondition().getText())
        .description(day.getCondition().getText())
        .precipitationMm(day.getTotalprecipMm())
        .precipitationProbability(day.getDailyChanceOfRain())
        .uvIndex(day.getUv())
        .sunriseTime(astro != null ? astro.getSunrise() : null)
        .sunsetTime(astro != null ? astro.getSunset() : null)
        .build();
  }

  /**
   * Converts WeatherAPI forecast response to list of ForecastDto (without persisting).
   *
   * @param apiResponse the external API response
   * @param locationName the location name
   * @return list of forecast DTOs
   */
  public List<ForecastDto> toDtoFromApi(ForecastApiResponse apiResponse, String locationName) {
    if (apiResponse == null
        || apiResponse.getForecast() == null
        || apiResponse.getForecast().getForecastday() == null) {
      return List.of();
    }

    return apiResponse.getForecast().getForecastday().stream()
        .map(day -> toDtoFromForecastDay(day, locationName))
        .toList();
  }

  /**
   * Converts a single ForecastDay to ForecastDto.
   *
   * @param forecastDay the forecast day from API
   * @param locationName the location name
   * @return the forecast DTO, or null if inputs are invalid
   */
  @Nullable
  private ForecastDto toDtoFromForecastDay(
      @Nullable ForecastApiResponse.ForecastDay forecastDay, String locationName) {
    if (forecastDay == null || forecastDay.getDay() == null) {
      return null;
    }

    ForecastApiResponse.Day day = forecastDay.getDay();
    ForecastApiResponse.Astro astro = forecastDay.getAstro();

    return new ForecastDto(
        null, // id
        null, // locationId
        locationName,
        LocalDate.parse(forecastDay.getDate()),
        day.getMaxtempC(),
        day.getMintempC(),
        day.getAvgtempC(),
        day.getMaxwindKph(),
        day.getAvghumidity(),
        day.getCondition().getText(),
        day.getCondition().getText(),
        day.getTotalprecipMm(),
        day.getDailyChanceOfRain(),
        day.getUv(),
        astro != null ? astro.getSunrise() : null,
        astro != null ? astro.getSunset() : null);
  }
}

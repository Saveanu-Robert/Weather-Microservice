package com.weatherspring.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Weather forecast information for a specific date.
 *
 * <p>Contains predicted weather conditions including temperature ranges, wind, precipitation,
 * and sunrise/sunset times. ID and locationId will be null for forecasts fetched directly from
 * the API without saving to database.
 */
@Schema(description = "Weather forecast information")
public record ForecastDto(
    @Schema(description = "Forecast record ID", example = "1") Long id,
    @Schema(description = "Location ID", example = "1") Long locationId,
    @Schema(description = "Location name", example = "London") String locationName,
    @Schema(description = "Forecast date", example = "2024-01-20") LocalDate forecastDate,
    @Schema(description = "Maximum temperature in Celsius", example = "18.5") Double maxTemperature,
    @Schema(description = "Minimum temperature in Celsius", example = "10.2") Double minTemperature,
    @Schema(description = "Average temperature in Celsius", example = "14.3") Double avgTemperature,
    @Schema(description = "Maximum wind speed in km/h", example = "25.0") Double maxWindSpeed,
    @Schema(description = "Average humidity percentage", example = "70") Integer avgHumidity,
    @Schema(description = "Weather condition", example = "Moderate rain") String condition,
    @Schema(
            description = "Detailed description",
            example = "Expect moderate rain throughout the day")
        String description,
    @Schema(description = "Total precipitation in mm", example = "12.5") Double precipitationMm,
    @Schema(description = "Precipitation probability percentage", example = "80")
        Integer precipitationProbability,
    @Schema(description = "UV index", example = "4.0") Double uvIndex,
    @Schema(description = "Sunrise time", example = "06:45") String sunriseTime,
    @Schema(description = "Sunset time", example = "18:30") String sunsetTime) {}

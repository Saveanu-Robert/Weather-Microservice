package com.weatherspring.dto;

import java.time.LocalDateTime;

import jakarta.annotation.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;

/** Current weather data returned from API responses. */
@Schema(description = "Current weather information")
public record WeatherDto(
    @Schema(description = "Weather record ID (null for unsaved records)", example = "1") @Nullable
        Long id,
    @Schema(description = "Location ID (null when fetched directly from API)", example = "1")
        @Nullable
        Long locationId,
    @Schema(description = "Location name", example = "London") String locationName,
    @Schema(description = "Temperature in Celsius", example = "15.5") Double temperature,
    @Schema(description = "Feels like temperature in Celsius", example = "13.2") Double feelsLike,
    @Schema(description = "Humidity percentage", example = "65") Integer humidity,
    @Schema(description = "Wind speed in km/h", example = "12.5") Double windSpeed,
    @Schema(description = "Wind direction", example = "NW") String windDirection,
    @Schema(description = "Weather condition", example = "Partly cloudy") String condition,
    @Schema(description = "Detailed description", example = "Partly cloudy with light winds")
        String description,
    @Schema(description = "Atmospheric pressure in mb", example = "1013.2") Double pressureMb,
    @Schema(description = "Precipitation in mm", example = "0.5") Double precipitationMm,
    @Schema(description = "Cloud coverage percentage", example = "40") Integer cloudCoverage,
    @Schema(description = "UV index", example = "3.5") Double uvIndex,
    @Schema(description = "Timestamp of weather data", example = "2024-01-15T14:30:00")
        LocalDateTime timestamp) {}

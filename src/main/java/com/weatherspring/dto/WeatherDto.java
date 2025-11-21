package com.weatherspring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for current weather data.
 */
@Value
@Builder
@Schema(description = "Current weather information")
public class WeatherDto {

    @Schema(description = "Weather record ID", example = "1")
    private Long id;

    @Schema(description = "Location ID", example = "1")
    private Long locationId;

    @Schema(description = "Location name", example = "London")
    private String locationName;

    @Schema(description = "Temperature in Celsius", example = "15.5")
    private Double temperature;

    @Schema(description = "Feels like temperature in Celsius", example = "13.2")
    private Double feelsLike;

    @Schema(description = "Humidity percentage", example = "65")
    private Integer humidity;

    @Schema(description = "Wind speed in km/h", example = "12.5")
    private Double windSpeed;

    @Schema(description = "Wind direction", example = "NW")
    private String windDirection;

    @Schema(description = "Weather condition", example = "Partly cloudy")
    private String condition;

    @Schema(description = "Detailed description", example = "Partly cloudy with light winds")
    private String description;

    @Schema(description = "Atmospheric pressure in mb", example = "1013.2")
    private Double pressureMb;

    @Schema(description = "Precipitation in mm", example = "0.5")
    private Double precipitationMm;

    @Schema(description = "Cloud coverage percentage", example = "40")
    private Integer cloudCoverage;

    @Schema(description = "UV index", example = "3.5")
    private Double uvIndex;

    @Schema(description = "Timestamp of weather data", example = "2024-01-15T14:30:00")
    private LocalDateTime timestamp;
}

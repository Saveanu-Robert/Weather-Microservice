package com.weatherspring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO for weather forecast data.
 */
@Value
@Builder
@Schema(description = "Weather forecast information")
public class ForecastDto {

    @Schema(description = "Forecast record ID", example = "1")
    private Long id;

    @Schema(description = "Location ID", example = "1")
    private Long locationId;

    @Schema(description = "Location name", example = "London")
    private String locationName;

    @Schema(description = "Forecast date", example = "2024-01-20")
    private LocalDate forecastDate;

    @Schema(description = "Maximum temperature in Celsius", example = "18.5")
    private Double maxTemperature;

    @Schema(description = "Minimum temperature in Celsius", example = "10.2")
    private Double minTemperature;

    @Schema(description = "Average temperature in Celsius", example = "14.3")
    private Double avgTemperature;

    @Schema(description = "Maximum wind speed in km/h", example = "25.0")
    private Double maxWindSpeed;

    @Schema(description = "Average humidity percentage", example = "70")
    private Integer avgHumidity;

    @Schema(description = "Weather condition", example = "Moderate rain")
    private String condition;

    @Schema(description = "Detailed description", example = "Expect moderate rain throughout the day")
    private String description;

    @Schema(description = "Total precipitation in mm", example = "12.5")
    private Double precipitationMm;

    @Schema(description = "Precipitation probability percentage", example = "80")
    private Integer precipitationProbability;

    @Schema(description = "UV index", example = "4.0")
    private Double uvIndex;

    @Schema(description = "Sunrise time", example = "06:45")
    private String sunriseTime;

    @Schema(description = "Sunset time", example = "18:30")
    private String sunsetTime;
}

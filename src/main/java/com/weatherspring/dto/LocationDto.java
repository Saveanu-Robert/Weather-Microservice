package com.weatherspring.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/** Location data returned from API responses. */
@Schema(description = "Location information")
public record LocationDto(
    @Schema(description = "Location ID", example = "1") Long id,
    @Schema(description = "Location name", example = "London") String name,
    @Schema(description = "Country name", example = "United Kingdom") String country,
    @Schema(description = "Latitude coordinate", example = "51.5074") Double latitude,
    @Schema(description = "Longitude coordinate", example = "-0.1278") Double longitude,
    @Schema(description = "Region/State", example = "City of London, Greater London") String region,
    @Schema(description = "Creation timestamp", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,
    @Schema(description = "Last update timestamp", example = "2024-01-15T10:30:00")
        LocalDateTime updatedAt) {}

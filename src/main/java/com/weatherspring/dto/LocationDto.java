package com.weatherspring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for location data transfer.
 */
@Value
@Builder
@Schema(description = "Location information")
public class LocationDto {

    @Schema(description = "Location ID", example = "1")
    private Long id;

    @NotBlank(message = "Location name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Location name", example = "London", required = true)
    private String name;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
    @Schema(description = "Country name", example = "United Kingdom", required = true)
    private String country;

    @NotNull(message = "Latitude is required")
    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    @Schema(description = "Latitude coordinate", example = "51.5074", required = true)
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Min(value = -180, message = "Longitude must be between -180 and 180")
    @Max(value = 180, message = "Longitude must be between -180 and 180")
    @Schema(description = "Longitude coordinate", example = "-0.1278", required = true)
    private Double longitude;

    @Schema(description = "Region/State", example = "City of London, Greater London")
    private String region;

    @Schema(description = "Creation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
}

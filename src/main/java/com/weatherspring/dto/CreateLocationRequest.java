package com.weatherspring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for creating a new location.
 */
@Value
@Builder
@Schema(description = "Request to create a new location")
public class CreateLocationRequest {

    @NotBlank(message = "Location name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Location name", example = "Paris", required = true)
    private String name;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
    @Schema(description = "Country name", example = "France", required = true)
    private String country;

    @NotNull(message = "Latitude is required")
    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    @Schema(description = "Latitude coordinate", example = "48.8566", required = true)
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Min(value = -180, message = "Longitude must be between -180 and 180")
    @Max(value = 180, message = "Longitude must be between -180 and 180")
    @Schema(description = "Longitude coordinate", example = "2.3522", required = true)
    private Double longitude;

    @Schema(description = "Region/State", example = "Île-de-France")
    private String region;
}

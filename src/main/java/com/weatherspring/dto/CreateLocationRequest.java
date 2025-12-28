package com.weatherspring.dto;

import com.weatherspring.validation.ValidCountryName;
import com.weatherspring.validation.ValidLatitude;
import com.weatherspring.validation.ValidLocationName;
import com.weatherspring.validation.ValidLongitude;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request to create or update a location. */
@Schema(description = "Request to create a new location")
public record CreateLocationRequest(
    @ValidLocationName @Schema(description = "Location name", example = "Paris", required = true)
        String name,
    @ValidCountryName @Schema(description = "Country name", example = "France", required = true)
        String country,
    @ValidLatitude
        @Schema(description = "Latitude coordinate", example = "48.8566", required = true)
        Double latitude,
    @ValidLongitude
        @Schema(description = "Longitude coordinate", example = "2.3522", required = true)
        Double longitude,
    @Schema(description = "Region/State", example = "Île-de-France") String region) {}

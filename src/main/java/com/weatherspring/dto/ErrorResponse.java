package com.weatherspring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

/**
 * Standard error response DTO.
 */
@Value
@Builder
@Schema(description = "Error response")
public class ErrorResponse {

    @Schema(description = "Error code", example = "LOCATION_NOT_FOUND")
    String code;

    @Schema(description = "Error message", example = "Location not found with ID: 123")
    String message;

    @Schema(description = "Timestamp of the error", example = "2024-01-15T14:30:00Z")
    Instant timestamp;

    /**
     * Creates an ErrorResponse with the current timestamp.
     *
     * @param code error code
     * @param message error message
     * @return ErrorResponse with current timestamp
     */
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, Instant.now());
    }
}

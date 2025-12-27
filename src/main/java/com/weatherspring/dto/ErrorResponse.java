package com.weatherspring.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Error information returned when requests fail.
 * Includes error code, message, path, and optional validation errors.
 */
@Schema(description = "Detailed error response")
public record ErrorResponse(
    @Schema(description = "Error code", example = "LOCATION_NOT_FOUND")
    String code,

    @Schema(description = "Error message", example = "Location not found with ID: 123")
    String message,

    @Schema(description = "Request path that caused the error", example = "/api/locations/123")
    String path,

    @Schema(description = "Additional error details", example = "{\"locationId\": \"123\", \"attempted\": \"2024-01-15T14:30:00Z\"}")
    Map<String, Object> details,

    @Schema(description = "Validation errors for constraint violations")
    List<ValidationError> validationErrors,

    @Schema(description = "Timestamp of the error", example = "2024-01-15T14:30:00Z")
    Instant timestamp
) {
    /**
     * Validation error detail.
     */
    @Schema(description = "Validation error detail")
    public record ValidationError(
        @Schema(description = "Field name that failed validation", example = "email")
        String field,

        @Schema(description = "Rejected value", example = "invalid-email")
        Object rejectedValue,

        @Schema(description = "Validation error message", example = "must be a well-formed email address")
        String message
    ) {}

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null, null, null, Instant.now());
    }

    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(code, message, path, null, null, Instant.now());
    }

    public static ErrorResponse of(String code, String message, String path, Map<String, Object> details) {
        return new ErrorResponse(code, message, path, details, null, Instant.now());
    }

    public static ErrorResponse ofValidation(String code, String message, String path, List<ValidationError> validationErrors) {
        return new ErrorResponse(code, message, path, null, validationErrors, Instant.now());
    }
}

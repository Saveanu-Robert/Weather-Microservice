package com.weatherspring.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Error information returned when requests fail. Includes error code, message, path, and optional
 * validation errors.
 */
@Schema(description = "Detailed error response")
public record ErrorResponse(
    @Schema(description = "Error code", example = "LOCATION_NOT_FOUND") String code,
    @Schema(description = "Error message", example = "Location not found with ID: 123")
        String message,
    @Schema(description = "Request path that caused the error", example = "/api/locations/123")
        String path,
    @Schema(
            description = "Additional error details",
            example = "{\"locationId\": \"123\", \"attempted\": \"2024-01-15T14:30:00Z\"}")
        Map<String, Object> details,
    @Schema(description = "Validation errors for constraint violations")
        List<ValidationError> validationErrors,
    @Schema(description = "Timestamp of the error", example = "2024-01-15T14:30:00Z")
        Instant timestamp) {
  /** Validation error detail. */
  @Schema(description = "Validation error detail")
  public record ValidationError(
      @Schema(description = "Field name that failed validation", example = "email") String field,
      @Schema(description = "Rejected value", example = "invalid-email") Object rejectedValue,
      @Schema(
              description = "Validation error message",
              example = "must be a well-formed email address")
          String message) {}

  /**
   * Creates an error response with code and message only.
   *
   * @param code the error code
   * @param message the error message
   * @return a new error response
   */
  public static ErrorResponse of(String code, String message) {
    return new ErrorResponse(code, message, null, null, null, Instant.now());
  }

  /**
   * Creates an error response with code, message, and path.
   *
   * @param code the error code
   * @param message the error message
   * @param path the request path that caused the error
   * @return a new error response
   */
  public static ErrorResponse of(String code, String message, String path) {
    return new ErrorResponse(code, message, path, null, null, Instant.now());
  }

  /**
   * Creates an error response with code, message, path, and additional details.
   *
   * @param code the error code
   * @param message the error message
   * @param path the request path that caused the error
   * @param details additional error details
   * @return a new error response
   */
  public static ErrorResponse of(
      String code, String message, String path, Map<String, Object> details) {
    return new ErrorResponse(code, message, path, details, null, Instant.now());
  }

  /**
   * Creates an error response for validation failures.
   *
   * @param code the error code
   * @param message the error message
   * @param path the request path that caused the error
   * @param validationErrors list of validation errors
   * @return a new error response with validation errors
   */
  public static ErrorResponse ofValidation(
      String code, String message, String path, List<ValidationError> validationErrors) {
    return new ErrorResponse(code, message, path, null, validationErrors, Instant.now());
  }
}

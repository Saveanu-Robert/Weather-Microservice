package com.weatherspring.exception;

import com.weatherspring.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 *
 * <p>Handles all exceptions and returns appropriate HTTP responses with error details.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles LocationNotFoundException.
     */
    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLocationNotFound(LocationNotFoundException ex) {
        logger.warn("Location not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code("LOCATION_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles WeatherDataNotFoundException.
     */
    @ExceptionHandler(WeatherDataNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWeatherDataNotFound(WeatherDataNotFoundException ex) {
        logger.warn("Weather data not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code("WEATHER_DATA_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles WeatherApiException.
     */
    @ExceptionHandler(WeatherApiException.class)
    public ResponseEntity<ErrorResponse> handleWeatherApiException(WeatherApiException ex) {
        logger.error("Weather API error: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code("WEATHER_API_ERROR")
                .message("Failed to fetch weather data from external API: " + ex.getMessage())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Handles IllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Invalid argument: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_ARGUMENT")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        logger.warn("Validation error: {}", ex.getMessage());

        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String message = error.getDefaultMessage();
                    return fieldName + ": " + message;
                })
                .collect(Collectors.joining(", "));

        ErrorResponse error = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message(errorMessage)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles type mismatch errors (e.g., invalid path variable types).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        logger.warn("Type mismatch error: {}", ex.getMessage());

        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ErrorResponse error = ErrorResponse.builder()
                .code("TYPE_MISMATCH")
                .message(message)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred", ex);

        ErrorResponse error = ErrorResponse.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

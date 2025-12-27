package com.weatherspring.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application using RFC 7807 ProblemDetail.
 *
 * <p>Handles all exceptions and returns RFC 7807 compliant HTTP responses with error details.
 * Migrated from custom ErrorResponse to Spring's ProblemDetail for standardization.</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${app.error.problem-base-url}")
    private String problemBaseUrl;

    /**
     * Handles LocationNotFoundException.
     */
    @ExceptionHandler(LocationNotFoundException.class)
    public ProblemDetail handleLocationNotFound(LocationNotFoundException ex) {
        log.warn("Location not found: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problem.setType(URI.create(problemBaseUrl + "/location-not-found"));
        problem.setTitle("Location Not Found");
        problem.setProperty("category", ex.getCategory());
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    /**
     * Handles WeatherDataNotFoundException.
     */
    @ExceptionHandler(WeatherDataNotFoundException.class)
    public ProblemDetail handleWeatherDataNotFound(WeatherDataNotFoundException ex) {
        log.warn("Weather data not found: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problem.setType(URI.create(problemBaseUrl + "/weather-data-not-found"));
        problem.setTitle("Weather Data Not Found");
        problem.setProperty("category", ex.getCategory());
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    /**
     * Handles WeatherApiException.
     */
    @ExceptionHandler(WeatherApiException.class)
    public ProblemDetail handleWeatherApiException(WeatherApiException ex) {
        log.error("Weather API error: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Failed to fetch weather data from external API: " + ex.getMessage()
        );
        problem.setType(URI.create(problemBaseUrl + "/weather-api-error"));
        problem.setTitle("Weather API Error");
        problem.setProperty("category", ex.getCategory());
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    /**
     * Handles IllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setType(URI.create(problemBaseUrl + "/invalid-argument"));
        problem.setTitle("Invalid Argument");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    /**
     * Handles validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed for one or more fields"
        );
        problem.setType(URI.create(problemBaseUrl + "/validation-error"));
        problem.setTitle("Validation Error");
        problem.setProperty("errors", errors);
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    /**
     * Handles constraint violation errors from method parameter validation.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());

        Map<String, String> violations = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                violations.put(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                )
        );

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Constraint validation failed"
        );
        problem.setType(URI.create(problemBaseUrl + "/constraint-violation"));
        problem.setTitle("Constraint Violation");
        problem.setProperty("violations", violations);
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    /**
     * Handles generic Jakarta Bean Validation exceptions.
     *
     * <p>This handler catches {@link ValidationException} and its subclasses that
     * are not already handled by more specific handlers (e.g., {@link ConstraintViolationException}).
     * This ensures all validation-related exceptions are properly handled.</p>
     */
    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidationException(ValidationException ex) {
        log.warn("Validation exception: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage() != null ? ex.getMessage() : "Validation failed"
        );
        problem.setType(URI.create(problemBaseUrl + "/validation-error"));
        problem.setTitle("Validation Error");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    /**
     * Handles type mismatch errors (e.g., invalid path variable types).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch error: {}", ex.getMessage());

        String detail = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                detail
        );
        problem.setType(URI.create(problemBaseUrl + "/type-mismatch"));
        problem.setTitle("Type Mismatch");
        problem.setProperty("parameter", ex.getName());
        problem.setProperty("value", ex.getValue());
        problem.setProperty("expectedType",
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    /**
     * Handles any WeatherServiceException not caught by specific handlers.
     * This serves as a fallback for the sealed exception hierarchy.
     */
    @ExceptionHandler(WeatherServiceException.class)
    public ProblemDetail handleWeatherServiceException(WeatherServiceException ex) {
        log.error("Weather service error: {}", ex.getMessage(), ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage()
        );
        problem.setType(URI.create(problemBaseUrl + "/weather-service-error"));
        problem.setTitle("Weather Service Error");
        problem.setProperty("category", ex.getCategory());
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    /**
     * Handles all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."
        );
        problem.setType(URI.create(problemBaseUrl + "/internal-server-error"));
        problem.setTitle("Internal Server Error");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }
}

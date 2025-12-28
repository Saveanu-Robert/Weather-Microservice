package com.weatherspring.exception;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for the application using RFC 7807 ProblemDetail.
 *
 * <p>Handles all exceptions and returns RFC 7807 compliant HTTP responses with error details.
 * Migrated from custom ErrorResponse to Spring's ProblemDetail for standardization.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @Value("${app.error.problem-base-url}")
  private String problemBaseUrl;

  @ExceptionHandler(LocationNotFoundException.class)
  public ProblemDetail handleLocationNotFound(LocationNotFoundException ex) {
    log.warn("Location not found: {}", ex.getMessage());

    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problem.setType(URI.create(problemBaseUrl + "/location-not-found"));
    problem.setTitle("Location Not Found");
    problem.setProperty("category", ex.getCategory());
    problem.setProperty("timestamp", Instant.now());

    return problem;
  }

  @ExceptionHandler(WeatherDataNotFoundException.class)
  public ProblemDetail handleWeatherDataNotFound(WeatherDataNotFoundException ex) {
    log.warn("Weather data not found: {}", ex.getMessage());

    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problem.setType(URI.create(problemBaseUrl + "/weather-data-not-found"));
    problem.setTitle("Weather Data Not Found");
    problem.setProperty("category", ex.getCategory());
    problem.setProperty("timestamp", Instant.now());

    return problem;
  }

  @ExceptionHandler(WeatherApiException.class)
  public ProblemDetail handleWeatherApiException(WeatherApiException ex) {
    log.error("Weather API error: {}", ex.getMessage());

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Failed to fetch weather data from external API: " + ex.getMessage());
    problem.setType(URI.create(problemBaseUrl + "/weather-api-error"));
    problem.setTitle("Weather API Error");
    problem.setProperty("category", ex.getCategory());
    problem.setProperty("timestamp", Instant.now());

    return problem;
  }

  @ExceptionHandler(RequestNotPermitted.class)
  public ProblemDetail handleRateLimitExceeded(RequestNotPermitted ex) {
    log.warn("Rate limit exceeded: {}", ex.getMessage());

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.TOO_MANY_REQUESTS,
            "Rate limit exceeded. Please try again later.");
    problem.setType(URI.create(problemBaseUrl + "/rate-limit-exceeded"));
    problem.setTitle("Rate Limit Exceeded");
    problem.setProperty("timestamp", Instant.now());

    return problem;
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    log.warn("Data integrity violation: {}", ex.getMessage());

    // Extract user-friendly message
    String detail = "A database constraint was violated. The operation could not be completed.";
    if (ex.getMessage() != null && ex.getMessage().contains("Unique index")) {
      detail = "This record already exists in the database.";
    }

    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
    problem.setType(URI.create(problemBaseUrl + "/data-integrity-violation"));
    problem.setTitle("Data Integrity Violation");
    problem.setProperty("timestamp", Instant.now());

    return problem;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Invalid argument: {}", ex.getMessage());

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problem.setType(URI.create(problemBaseUrl + "/invalid-argument"));
    problem.setTitle("Invalid Argument");
    problem.setProperty("timestamp", Instant.now());

    return problem;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
    log.warn("Validation error: {}", ex.getMessage());

    Map<String, String> errors = new LinkedHashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String message = error.getDefaultMessage();
              errors.put(fieldName, message);
            });

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Validation failed for one or more fields");
    problem.setType(URI.create(problemBaseUrl + "/validation-error"));
    problem.setTitle("Validation Error");
    problem.setProperty("errors", errors);
    problem.setProperty("timestamp", Instant.now());

    return problem;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
    log.warn("Constraint violation: {}", ex.getMessage());

    Map<String, String> violations = new LinkedHashMap<>();
    ex.getConstraintViolations()
        .forEach(
            violation ->
                violations.put(violation.getPropertyPath().toString(), violation.getMessage()));

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Constraint validation failed");
    problem.setType(URI.create(problemBaseUrl + "/constraint-violation"));
    problem.setTitle("Constraint Violation");
    problem.setProperty("violations", violations);
    problem.setProperty("timestamp", Instant.now());

    return problem;
  }

  /**
   * Handles generic Jakarta Bean Validation exceptions.
   *
   * <p>This handler catches {@link ValidationException} and its subclasses that are not already
   * handled by more specific handlers (e.g., {@link ConstraintViolationException}). This ensures
   * all validation-related exceptions are properly handled.
   */
  @ExceptionHandler(ValidationException.class)
  public ProblemDetail handleValidationException(ValidationException ex) {
    log.warn("Validation exception: {}", ex.getMessage());

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage() != null ? ex.getMessage() : "Validation failed");
    problem.setType(URI.create(problemBaseUrl + "/validation-error"));
    problem.setTitle("Validation Error");
    problem.setProperty("timestamp", Instant.now());

    return problem;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    log.warn("Type mismatch error: {}", ex.getMessage());

    String detail =
        String.format(
            "Invalid value '%s' for parameter '%s'. Expected type: %s",
            ex.getValue(),
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    problem.setType(URI.create(problemBaseUrl + "/type-mismatch"));
    problem.setTitle("Type Mismatch");
    problem.setProperty("parameter", ex.getName());
    problem.setProperty("value", ex.getValue());
    problem.setProperty(
        "expectedType",
        ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
    problem.setProperty("timestamp", Instant.now());

    return problem;
  }

  /**
   * Handles any WeatherServiceException not caught by specific handlers. This serves as a fallback
   * for the sealed exception hierarchy.
   */
  @ExceptionHandler(WeatherServiceException.class)
  public ProblemDetail handleWeatherServiceException(WeatherServiceException ex) {
    log.error("Weather service error: {}", ex.getMessage(), ex);

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    problem.setType(URI.create(problemBaseUrl + "/weather-service-error"));
    problem.setTitle("Weather Service Error");
    problem.setProperty("category", ex.getCategory());
    problem.setProperty("timestamp", Instant.now());

    return problem;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGenericException(Exception ex) {
    log.error("Unexpected error occurred", ex);

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please try again later.");
    problem.setType(URI.create(problemBaseUrl + "/internal-server-error"));
    problem.setTitle("Internal Server Error");
    problem.setProperty("timestamp", Instant.now());

    return problem;
  }
}

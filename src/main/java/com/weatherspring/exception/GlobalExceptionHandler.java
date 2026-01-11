package com.weatherspring.exception;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
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

  /**
   * Handles exceptions when a requested location cannot be found.
   *
   * <p>Returns a 404 Not Found response when a location lookup fails. This typically occurs when
   * searching for a location by ID or name that doesn't exist in the database.
   *
   * @param ex the location not found exception containing details about the missing location
   * @return problem detail with 404 status and location error information
   */
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

  /**
   * Handles exceptions when weather data cannot be found for a location.
   *
   * <p>Returns a 404 Not Found response when weather or forecast data is unavailable. This can
   * happen when requesting data for a location that has no recorded weather information.
   *
   * @param ex the weather data not found exception containing details about the missing data
   * @return problem detail with 404 status and weather data error information
   */
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

  /**
   * Handles exceptions when the external weather API fails to respond.
   *
   * <p>Returns a 503 Service Unavailable response when communication with the weather API fails.
   * This occurs when the API is down, returns errors, or experiences timeouts.
   *
   * @param ex the weather API exception containing details about the API failure
   * @return problem detail with 503 status and API error information
   */
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

  /**
   * Handles exceptions when rate limits are exceeded.
   *
   * <p>Returns a 429 Too Many Requests response when clients exceed the configured rate limit.
   * This protects the service from being overwhelmed by too many requests from a single source.
   *
   * @param ex the request not permitted exception indicating rate limit violation
   * @return problem detail with 429 status and rate limit information
   */
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

  /**
   * Handles database constraint violations.
   *
   * <p>Returns a 409 Conflict response when database operations violate constraints like unique
   * indexes or foreign keys. Common when trying to create duplicate records or violate data
   * relationships.
   *
   * @param ex the data integrity violation exception from the database
   * @return problem detail with 409 status and constraint violation information
   */
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

  /**
   * Handles illegal argument exceptions from application logic.
   *
   * <p>Returns a 400 Bad Request response when method parameters are invalid. This catches
   * programmatic validation failures that occur outside the bean validation framework.
   *
   * @param ex the illegal argument exception containing details about the invalid parameter
   * @return problem detail with 400 status and argument error information
   */
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

  /**
   * Handles validation failures on request body objects.
   *
   * <p>Returns a 400 Bad Request response when validation annotations on DTO fields fail. Collects
   * all field-level validation errors and returns them in a map for the client to process.
   *
   * @param ex the method argument validation exception containing all field validation failures
   * @return problem detail with 400 status and a map of field errors
   */
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

  /**
   * Handles constraint violations on method parameters.
   *
   * <p>Returns a 400 Bad Request response when validation constraints on method parameters fail.
   * This catches violations on parameters annotated with constraints like @NotNull, @Min, @Max.
   *
   * @param ex the constraint violation exception containing all parameter violations
   * @return problem detail with 400 status and a map of constraint violations
   */
  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
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
   * Handles HandlerMethodValidationException (Spring Framework 6.1+).
   *
   * <p>This exception is thrown for method parameter validation starting in Spring Framework 6.1 /
   * Spring Boot 3.2+. It covers validation of request parameters, path variables, and request
   * headers. This replaces the older ConstraintViolationException for these validation scenarios.
   */
  @ExceptionHandler(HandlerMethodValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ProblemDetail handleHandlerMethodValidation(HandlerMethodValidationException ex) {
    log.warn("Method validation error: {}", ex.getMessage());

    Map<String, String> violations = new LinkedHashMap<>();
    ex.getAllValidationResults()
        .forEach(
            result -> {
              String parameterName = result.getMethodParameter().getParameterName();
              result
                  .getResolvableErrors()
                  .forEach(
                      error -> {
                        String message = error.getDefaultMessage();
                        violations.put(parameterName, message != null ? message : "Validation failed");
                      });
            });

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Method validation failed");
    problem.setType(URI.create(problemBaseUrl + "/method-validation-error"));
    problem.setTitle("Method Validation Error");
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

  /**
   * Handles type conversion failures on method parameters.
   *
   * <p>Returns a 400 Bad Request response when Spring cannot convert a request parameter to the
   * expected type. For example, passing "abc" when an integer is expected.
   *
   * @param ex the type mismatch exception containing conversion failure details
   * @return problem detail with 400 status and type conversion error information
   */
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

  /**
   * Handles ServletException by unwrapping and delegating to specific handlers.
   *
   * <p>In Spring Boot 3.5+, validation exceptions on controller method parameters are wrapped in
   * ServletException. This handler unwraps the root cause and delegates to the appropriate
   * handler.
   */
  @ExceptionHandler(ServletException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ProblemDetail handleServletException(ServletException ex) {
    Throwable rootCause = ex.getRootCause();

    // Delegate to specific handler if root cause is a known exception type
    if (rootCause instanceof HandlerMethodValidationException) {
      return handleHandlerMethodValidation((HandlerMethodValidationException) rootCause);
    } else if (rootCause instanceof ConstraintViolationException) {
      return handleConstraintViolation((ConstraintViolationException) rootCause);
    } else if (rootCause instanceof ValidationException) {
      return handleValidationException((ValidationException) rootCause);
    }

    // Otherwise, treat as generic error
    log.error("Servlet error occurred", ex);

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "A servlet error occurred. Please try again later.");
    problem.setType(URI.create(problemBaseUrl + "/servlet-error"));
    problem.setTitle("Servlet Error");
    problem.setProperty("timestamp", Instant.now());

    return problem;
  }

  /**
   * Handles all unhandled exceptions as a fallback.
   *
   * <p>Returns a 500 Internal Server Error response for any exception not caught by specific
   * handlers. This ensures all errors result in a proper RFC 7807 response rather than exposing
   * stack traces.
   *
   * @param ex the unhandled exception
   * @return problem detail with 500 status and generic error message
   */
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

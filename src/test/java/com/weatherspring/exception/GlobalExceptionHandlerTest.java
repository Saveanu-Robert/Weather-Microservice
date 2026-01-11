package com.weatherspring.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;

/** Unit tests for GlobalExceptionHandler with RFC 7807 ProblemDetail support. */
class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler exceptionHandler;

  @BeforeEach
  void setUp() {
    exceptionHandler = new GlobalExceptionHandler();
    ReflectionTestUtils.setField(
        exceptionHandler, "problemBaseUrl", "https://weatherspring.com/problems");
  }

  @Test
  void handleLocationNotFound_ReturnsProblemDetailWithNotFound() {
    // Arrange
    LocationNotFoundException exception = new LocationNotFoundException(1L);

    // Act
    ProblemDetail problem = exceptionHandler.handleLocationNotFound(exception);

    // Assert
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(problem.getType())
        .isEqualTo(URI.create("https://weatherspring.com/problems/location-not-found"));
    assertThat(problem.getTitle()).isEqualTo("Location Not Found");
    assertThat(problem.getDetail()).contains("Location not found");
    assertThat(problem.getProperties()).containsKey("category");
    assertThat(problem.getProperties()).containsKey("timestamp");
    assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
    assertThat(problem.getProperties().get("category")).isEqualTo(exception.getCategory());
  }

  @Test
  void handleWeatherDataNotFound_ReturnsProblemDetailWithNotFound() {
    // Arrange
    WeatherDataNotFoundException exception =
        new WeatherDataNotFoundException("No weather data available");

    // Act
    ProblemDetail problem = exceptionHandler.handleWeatherDataNotFound(exception);

    // Assert
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(problem.getType())
        .isEqualTo(URI.create("https://weatherspring.com/problems/weather-data-not-found"));
    assertThat(problem.getTitle()).isEqualTo("Weather Data Not Found");
    assertThat(problem.getDetail()).isEqualTo("No weather data available");
    assertThat(problem.getProperties()).containsKey("category");
    assertThat(problem.getProperties()).containsKey("timestamp");
    assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
    assertThat(problem.getProperties().get("category")).isEqualTo(exception.getCategory());
  }

  @Test
  void handleWeatherApiException_ReturnsProblemDetailWithServiceUnavailable() {
    // Arrange
    WeatherApiException exception = new WeatherApiException("API connection timeout");

    // Act
    ProblemDetail problem = exceptionHandler.handleWeatherApiException(exception);

    // Assert
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
    assertThat(problem.getType())
        .isEqualTo(URI.create("https://weatherspring.com/problems/weather-api-error"));
    assertThat(problem.getTitle()).isEqualTo("Weather API Error");
    assertThat(problem.getDetail()).contains("Failed to fetch weather data");
    assertThat(problem.getDetail()).contains("API connection timeout");
    assertThat(problem.getProperties()).containsKey("category");
    assertThat(problem.getProperties()).containsKey("timestamp");
    assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
    assertThat(problem.getProperties().get("category")).isEqualTo(exception.getCategory());
  }

  @Test
  void handleIllegalArgument_ReturnsProblemDetailWithBadRequest() {
    // Arrange
    IllegalArgumentException exception =
        new IllegalArgumentException("Days must be between 1 and 14");

    // Act
    ProblemDetail problem = exceptionHandler.handleIllegalArgument(exception);

    // Assert
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getType())
        .isEqualTo(URI.create("https://weatherspring.com/problems/invalid-argument"));
    assertThat(problem.getTitle()).isEqualTo("Invalid Argument");
    assertThat(problem.getDetail()).isEqualTo("Days must be between 1 and 14");
    assertThat(problem.getProperties()).containsKey("timestamp");
    assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
  }

  @Test
  void handleTypeMismatch_ReturnsProblemDetailWithTypeInfo() {
    // Arrange
    MethodArgumentTypeMismatchException exception =
        new MethodArgumentTypeMismatchException(
            "invalid", Long.class, "locationId", null, new NumberFormatException());

    // Act
    ProblemDetail problem = exceptionHandler.handleTypeMismatch(exception);

    // Assert
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getType())
        .isEqualTo(URI.create("https://weatherspring.com/problems/type-mismatch"));
    assertThat(problem.getTitle()).isEqualTo("Type Mismatch");
    assertThat(problem.getDetail()).contains("Invalid value");
    assertThat(problem.getDetail()).contains("locationId");
    assertThat(problem.getDetail()).contains("Long");
    assertThat(problem.getProperties()).containsKey("parameter");
    assertThat(problem.getProperties()).containsKey("value");
    assertThat(problem.getProperties()).containsKey("expectedType");
    assertThat(problem.getProperties()).containsKey("timestamp");
    assertThat(problem.getProperties().get("parameter")).isEqualTo("locationId");
    assertThat(problem.getProperties().get("value")).isEqualTo("invalid");
    assertThat(problem.getProperties().get("expectedType")).isEqualTo("Long");
    assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
  }

  @Test
  void handleGenericException_ReturnsProblemDetailWithInternalServerError() {
    // Arrange
    Exception exception = new RuntimeException("Unexpected database error");

    // Act
    ProblemDetail problem = exceptionHandler.handleGenericException(exception);

    // Assert
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(problem.getType())
        .isEqualTo(URI.create("https://weatherspring.com/problems/internal-server-error"));
    assertThat(problem.getTitle()).isEqualTo("Internal Server Error");
    assertThat(problem.getDetail()).contains("unexpected error occurred");
    assertThat(problem.getProperties()).containsKey("timestamp");
    assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
  }

  @Test
  void handleRateLimitExceeded_ReturnsProblemDetailWithTooManyRequests() {
    // Arrange
    RequestNotPermitted exception = mock(RequestNotPermitted.class);
    when(exception.getMessage()).thenReturn("Rate limit exceeded for weatherApi");

    // Act
    ProblemDetail problem = exceptionHandler.handleRateLimitExceeded(exception);

    // Assert
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    assertThat(problem.getType())
        .isEqualTo(URI.create("https://weatherspring.com/problems/rate-limit-exceeded"));
    assertThat(problem.getTitle()).isEqualTo("Rate Limit Exceeded");
    assertThat(problem.getDetail()).isEqualTo("Rate limit exceeded. Please try again later.");
    assertThat(problem.getProperties()).containsKey("timestamp");
    assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
  }

  @Test
  void handleDataIntegrityViolation_ReturnsProblemDetailWithConflict() {
    // Arrange
    DataIntegrityViolationException exception =
        new DataIntegrityViolationException("could not execute statement");

    // Act
    ProblemDetail problem = exceptionHandler.handleDataIntegrityViolation(exception);

    // Assert
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    assertThat(problem.getType())
        .isEqualTo(URI.create("https://weatherspring.com/problems/data-integrity-violation"));
    assertThat(problem.getTitle()).isEqualTo("Data Integrity Violation");
    assertThat(problem.getDetail())
        .isEqualTo("A database constraint was violated. The operation could not be completed.");
    assertThat(problem.getProperties()).containsKey("timestamp");
    assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
  }

  @Test
  void handleDataIntegrityViolation_WithUniqueConstraint_ReturnsUserFriendlyMessage() {
    // Arrange
    DataIntegrityViolationException exception =
        new DataIntegrityViolationException("Unique index or primary key violation");

    // Act
    ProblemDetail problem = exceptionHandler.handleDataIntegrityViolation(exception);

    // Assert
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    assertThat(problem.getType())
        .isEqualTo(URI.create("https://weatherspring.com/problems/data-integrity-violation"));
    assertThat(problem.getTitle()).isEqualTo("Data Integrity Violation");
    assertThat(problem.getDetail()).isEqualTo("This record already exists in the database.");
    assertThat(problem.getProperties()).containsKey("timestamp");
    assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
  }

  @Test
  void handleValidationException_ReturnsProblemDetailWithValidationErrors() throws Exception {
    // Arrange
    Object target = new Object();
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "location");
    bindingResult.addError(new FieldError("location", "name", "must not be blank"));
    bindingResult.addError(new FieldError("location", "latitude", "must be between -90 and 90"));

    // Create a real MethodParameter using a dummy method
    java.lang.reflect.Method method =
        GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyMethod", Object.class);
    MethodParameter methodParameter = new MethodParameter(method, 0);

    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(methodParameter, bindingResult);

    // Act
    ProblemDetail problem = exceptionHandler.handleValidationException(exception);

    // Assert
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getType())
        .isEqualTo(URI.create("https://weatherspring.com/problems/validation-error"));
    assertThat(problem.getTitle()).isEqualTo("Validation Error");
    assertThat(problem.getDetail()).isEqualTo("Validation failed for one or more fields");
    assertThat(problem.getProperties()).containsKey("errors");
    assertThat(problem.getProperties()).containsKey("timestamp");
    assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);

    @SuppressWarnings("unchecked")
    Map<String, String> errors = (Map<String, String>) problem.getProperties().get("errors");
    assertThat(errors).hasSize(2);
    assertThat(errors).containsEntry("name", "must not be blank");
    assertThat(errors).containsEntry("latitude", "must be between -90 and 90");
  }

  // Dummy method used for creating MethodParameter in tests
  @SuppressWarnings("unused")
  private void dummyMethod(Object param) {}

  @Test
  void handleConstraintViolation_ReturnsProblemDetailWithViolations() {
    // Arrange
    ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
    Path path1 = mock(Path.class);
    when(path1.toString()).thenReturn("getDays");
    when(violation1.getPropertyPath()).thenReturn(path1);
    when(violation1.getMessage()).thenReturn("must be between 1 and 14");

    ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
    Path path2 = mock(Path.class);
    when(path2.toString()).thenReturn("locationId");
    when(violation2.getPropertyPath()).thenReturn(path2);
    when(violation2.getMessage()).thenReturn("must not be null");

    Set<ConstraintViolation<?>> violations = Set.of(violation1, violation2);
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    // Act
    ProblemDetail problem = exceptionHandler.handleConstraintViolation(exception);

    // Assert
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getType())
        .isEqualTo(URI.create("https://weatherspring.com/problems/constraint-violation"));
    assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
    assertThat(problem.getDetail()).isEqualTo("Constraint validation failed");
    assertThat(problem.getProperties()).containsKey("violations");
    assertThat(problem.getProperties()).containsKey("timestamp");
    assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);

    @SuppressWarnings("unchecked")
    Map<String, String> violationsMap =
        (Map<String, String>) problem.getProperties().get("violations");
    assertThat(violationsMap).hasSize(2);
    assertThat(violationsMap).containsEntry("getDays", "must be between 1 and 14");
    assertThat(violationsMap).containsEntry("locationId", "must not be null");
  }

  @Test
  void handleHandlerMethodValidation_ReturnsProblemDetailWithViolations() {
    // Arrange
    MethodParameter methodParameter = mock(MethodParameter.class);
    when(methodParameter.getParameterName()).thenReturn("days");

    HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);
    org.springframework.validation.method.ParameterValidationResult validationResult =
        mock(org.springframework.validation.method.ParameterValidationResult.class);

    when(validationResult.getMethodParameter()).thenReturn(methodParameter);

    org.springframework.context.support.DefaultMessageSourceResolvable error =
        new org.springframework.context.support.DefaultMessageSourceResolvable(
            new String[] {"code"}, null, "must be between 1 and 14");
    when(validationResult.getResolvableErrors()).thenReturn(List.of(error));

    when(exception.getAllValidationResults()).thenReturn(List.of(validationResult));

    // Act
    ProblemDetail problem = exceptionHandler.handleHandlerMethodValidation(exception);

    // Assert
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getType())
        .isEqualTo(URI.create("https://weatherspring.com/problems/method-validation-error"));
    assertThat(problem.getTitle()).isEqualTo("Method Validation Error");
    assertThat(problem.getDetail()).isEqualTo("Method validation failed");
    assertThat(problem.getProperties()).containsKey("violations");
    assertThat(problem.getProperties()).containsKey("timestamp");
    assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);

    @SuppressWarnings("unchecked")
    Map<String, String> violations = (Map<String, String>) problem.getProperties().get("violations");
    assertThat(violations).containsEntry("days", "must be between 1 and 14");
  }

  @Test
  void handleServletException_WithHandlerMethodValidationException_DelegatesToHandler() {
    // Arrange
    MethodParameter methodParameter = mock(MethodParameter.class);
    when(methodParameter.getParameterName()).thenReturn("locationId");

    HandlerMethodValidationException rootCause = mock(HandlerMethodValidationException.class);
    org.springframework.validation.method.ParameterValidationResult validationResult =
        mock(org.springframework.validation.method.ParameterValidationResult.class);

    when(validationResult.getMethodParameter()).thenReturn(methodParameter);

    org.springframework.context.support.DefaultMessageSourceResolvable error =
        new org.springframework.context.support.DefaultMessageSourceResolvable(
            new String[] {"code"}, null, "must not be null");
    when(validationResult.getResolvableErrors()).thenReturn(List.of(error));

    when(rootCause.getAllValidationResults()).thenReturn(List.of(validationResult));

    ServletException exception = new ServletException("Validation failed", rootCause);

    // Act
    ProblemDetail problem = exceptionHandler.handleServletException(exception);

    // Assert
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getType())
        .isEqualTo(URI.create("https://weatherspring.com/problems/method-validation-error"));
    assertThat(problem.getTitle()).isEqualTo("Method Validation Error");
    assertThat(problem.getDetail()).isEqualTo("Method validation failed");
    assertThat(problem.getProperties()).containsKey("violations");
    assertThat(problem.getProperties()).containsKey("timestamp");
  }

  @Test
  void handleServletException_WithConstraintViolationException_DelegatesToHandler() {
    // Arrange
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("parameterName");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("validation failed");

    Set<ConstraintViolation<?>> violations = Set.of(violation);
    ConstraintViolationException rootCause = new ConstraintViolationException(violations);

    ServletException exception = new ServletException("Constraint violation", rootCause);

    // Act
    ProblemDetail problem = exceptionHandler.handleServletException(exception);

    // Assert
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getType())
        .isEqualTo(URI.create("https://weatherspring.com/problems/constraint-violation"));
    assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
    assertThat(problem.getDetail()).isEqualTo("Constraint validation failed");
    assertThat(problem.getProperties()).containsKey("violations");
    assertThat(problem.getProperties()).containsKey("timestamp");
  }

  @Test
  void handleServletException_WithGenericCause_ReturnsGenericServletError() {
    // Arrange
    ServletException exception = new ServletException("Generic servlet error");

    // Act
    ProblemDetail problem = exceptionHandler.handleServletException(exception);

    // Assert
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(problem.getType())
        .isEqualTo(URI.create("https://weatherspring.com/problems/servlet-error"));
    assertThat(problem.getTitle()).isEqualTo("Servlet Error");
    assertThat(problem.getDetail()).isEqualTo("A servlet error occurred. Please try again later.");
    assertThat(problem.getProperties()).containsKey("timestamp");
    assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
  }
}

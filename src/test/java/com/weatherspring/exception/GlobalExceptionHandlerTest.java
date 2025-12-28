package com.weatherspring.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
}

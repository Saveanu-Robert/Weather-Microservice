package com.weatherspring.exception;

import com.weatherspring.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleLocationNotFound_ReturnsNotFoundResponse() {
        // Arrange
        LocationNotFoundException exception = new LocationNotFoundException(1L);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleLocationNotFound(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("LOCATION_NOT_FOUND");
        assertThat(response.getBody().getMessage()).contains("Location not found");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleWeatherDataNotFound_ReturnsNotFoundResponse() {
        // Arrange
        WeatherDataNotFoundException exception = new WeatherDataNotFoundException("No weather data available");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleWeatherDataNotFound(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("WEATHER_DATA_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("No weather data available");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleWeatherApiException_ReturnsServiceUnavailableResponse() {
        // Arrange
        WeatherApiException exception = new WeatherApiException("API connection timeout");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleWeatherApiException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("WEATHER_API_ERROR");
        assertThat(response.getBody().getMessage()).contains("Failed to fetch weather data");
        assertThat(response.getBody().getMessage()).contains("API connection timeout");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleIllegalArgument_ReturnsBadRequestResponse() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Days must be between 1 and 14");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_ARGUMENT");
        assertThat(response.getBody().getMessage()).isEqualTo("Days must be between 1 and 14");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleTypeMismatch_ReturnsBadRequestWithTypeInfo() {
        // Arrange
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "invalid",
                Long.class,
                "locationId",
                null,
                new NumberFormatException()
        );

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTypeMismatch(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("TYPE_MISMATCH");
        assertThat(response.getBody().getMessage()).contains("Invalid value");
        assertThat(response.getBody().getMessage()).contains("locationId");
        assertThat(response.getBody().getMessage()).contains("Long");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleGenericException_ReturnsInternalServerErrorResponse() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected database error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getMessage()).contains("unexpected error occurred");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
}

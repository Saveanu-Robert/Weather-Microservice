package com.weatherspring.client;

import com.weatherspring.dto.external.ForecastApiResponse;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.exception.WeatherApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WeatherApiClient.
 */
@ExtendWith(MockitoExtension.class)
class WeatherApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    private WeatherApiClient weatherApiClient;

    private final String baseUrl = "https://api.weatherapi.com/v1";
    private final String apiKey = "test-api-key";

    @BeforeEach
    void setUp() {
        weatherApiClient = new WeatherApiClient(restTemplate, baseUrl, apiKey);
    }

    @Test
    void getCurrentWeather_WithValidLocation_ReturnsWeatherApiResponse() {
        // Arrange
        WeatherApiResponse mockResponse = new WeatherApiResponse();
        when(restTemplate.getForObject(anyString(), eq(WeatherApiResponse.class)))
                .thenReturn(mockResponse);

        // Act
        WeatherApiResponse result = weatherApiClient.getCurrentWeather("London");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockResponse);
        verify(restTemplate).getForObject(contains("London"), eq(WeatherApiResponse.class));
    }

    @Test
    void getCurrentWeather_WithNullResponse_ThrowsWeatherApiException() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(WeatherApiResponse.class)))
                .thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> weatherApiClient.getCurrentWeather("London"))
                .isInstanceOf(WeatherApiException.class)
                .hasMessageContaining("empty response");
    }

    @Test
    void getCurrentWeather_WithHttpClientError_ThrowsWeatherApiException() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(WeatherApiResponse.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST,
                    "Bad Request", null, null, null));

        // Act & Assert
        assertThatThrownBy(() -> weatherApiClient.getCurrentWeather("InvalidLocation"))
                .isInstanceOf(WeatherApiException.class)
                .hasMessageContaining("Invalid location");
    }

    @Test
    void getCurrentWeather_WithHttpServerError_ThrowsWeatherApiException() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(WeatherApiResponse.class)))
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error", null, null, null));

        // Act & Assert
        assertThatThrownBy(() -> weatherApiClient.getCurrentWeather("London"))
                .isInstanceOf(WeatherApiException.class)
                .hasMessageContaining("server error");
    }

    @Test
    void getCurrentWeather_WithGenericException_ThrowsWeatherApiException() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(WeatherApiResponse.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Act & Assert
        assertThatThrownBy(() -> weatherApiClient.getCurrentWeather("London"))
                .isInstanceOf(WeatherApiException.class)
                .hasMessageContaining("Failed to fetch weather data");
    }

    @Test
    void getForecast_WithValidParameters_ReturnsForecastApiResponse() {
        // Arrange
        ForecastApiResponse mockResponse = new ForecastApiResponse();
        when(restTemplate.getForObject(anyString(), eq(ForecastApiResponse.class)))
                .thenReturn(mockResponse);

        // Act
        ForecastApiResponse result = weatherApiClient.getForecast("London", 3);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockResponse);
        verify(restTemplate).getForObject(contains("London"), eq(ForecastApiResponse.class));
        verify(restTemplate).getForObject(contains("days=3"), eq(ForecastApiResponse.class));
    }

    @Test
    void getForecast_WithInvalidDays_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThatThrownBy(() -> weatherApiClient.getForecast("London", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 14");

        assertThatThrownBy(() -> weatherApiClient.getForecast("London", 15))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 14");
    }

    @Test
    void getForecast_WithNullResponse_ThrowsWeatherApiException() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(ForecastApiResponse.class)))
                .thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> weatherApiClient.getForecast("London", 3))
                .isInstanceOf(WeatherApiException.class)
                .hasMessageContaining("empty response");
    }

    @Test
    void searchLocation_CallsGetCurrentWeather() {
        // Arrange
        WeatherApiResponse mockResponse = new WeatherApiResponse();
        when(restTemplate.getForObject(anyString(), eq(WeatherApiResponse.class)))
                .thenReturn(mockResponse);

        // Act
        WeatherApiResponse result = weatherApiClient.searchLocation("Paris");

        // Assert
        assertThat(result).isNotNull();
        verify(restTemplate).getForObject(contains("Paris"), eq(WeatherApiResponse.class));
    }
}

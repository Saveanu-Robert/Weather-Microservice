package com.weatherspring.client;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import com.weatherspring.dto.external.ForecastApiResponse;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.exception.WeatherApiException;

/** Unit tests for WeatherApiClient with RestClient. */
@ExtendWith(MockitoExtension.class)
class WeatherApiClientTest {

  @Mock private RestClient restClient;

  @Mock private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

  @Mock private RestClient.ResponseSpec responseSpec;

  private WeatherApiClient weatherApiClient;

  private final String apiKey = "test-api-key";

  @BeforeEach
  void setUp() {
    weatherApiClient = new WeatherApiClient(restClient, apiKey);
  }

  @Test
  void getCurrentWeather_WithValidLocation_ReturnsWeatherApiResponse() {
    // Arrange
    WeatherApiResponse mockResponse = new WeatherApiResponse();

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.body(WeatherApiResponse.class)).thenReturn(mockResponse);

    // Act
    WeatherApiResponse result = weatherApiClient.getCurrentWeather("London");

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(mockResponse);
    verify(restClient).get();
    verify(responseSpec).body(WeatherApiResponse.class);
  }

  @Test
  void getCurrentWeather_WithNullResponse_ThrowsWeatherApiException() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.body(WeatherApiResponse.class)).thenReturn(null);

    // Act & Assert
    assertThatThrownBy(() -> weatherApiClient.getCurrentWeather("London"))
        .isInstanceOf(WeatherApiException.class)
        .hasMessageContaining("empty response");
  }

  @Test
  void getCurrentWeather_WithGenericException_ThrowsWeatherApiException() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.body(WeatherApiResponse.class))
        .thenThrow(new org.springframework.web.client.RestClientException("Network error"));

    // Act & Assert
    assertThatThrownBy(() -> weatherApiClient.getCurrentWeather("London"))
        .isInstanceOf(WeatherApiException.class)
        .hasMessageContaining("Failed to fetch weather data");
  }

  @Test
  void getForecast_WithValidParameters_ReturnsForecastApiResponse() {
    // Arrange
    ForecastApiResponse mockResponse = new ForecastApiResponse();

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.body(ForecastApiResponse.class)).thenReturn(mockResponse);

    // Act
    ForecastApiResponse result = weatherApiClient.getForecast("London", 3);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(mockResponse);
    verify(restClient).get();
    verify(responseSpec).body(ForecastApiResponse.class);
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
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.body(ForecastApiResponse.class)).thenReturn(null);

    // Act & Assert
    assertThatThrownBy(() -> weatherApiClient.getForecast("London", 3))
        .isInstanceOf(WeatherApiException.class)
        .hasMessageContaining("empty response");
  }

  @Test
  void getForecast_WithGenericException_ThrowsWeatherApiException() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.body(ForecastApiResponse.class))
        .thenThrow(new org.springframework.web.client.RestClientException("Network timeout"));

    // Act & Assert
    assertThatThrownBy(() -> weatherApiClient.getForecast("Paris", 7))
        .isInstanceOf(WeatherApiException.class)
        .hasMessageContaining("Failed to fetch forecast data");
  }

}

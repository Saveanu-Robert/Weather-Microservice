package com.weatherspring.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.weatherspring.dto.external.ForecastApiResponse;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.exception.WeatherApiException;
import com.weatherspring.validation.ValidationConstants;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

/**
 * Connects to WeatherAPI.com to fetch current weather and forecasts. Uses circuit breaker and retry
 * patterns for reliability.
 */
@Slf4j
@Component
public class WeatherApiClient {

  private final RestClient restClient;
  private final String apiKey;

  /**
   * Constructs a new WeatherApiClient with the given REST client and API key.
   *
   * @param weatherRestClient the configured REST client for API communication
   * @param apiKey the API key for authenticating with WeatherAPI.com
   */
  public WeatherApiClient(
      RestClient weatherRestClient, @Value("${weather.api.key}") String apiKey) {
    this.restClient = weatherRestClient;
    this.apiKey = apiKey;
  }

  /**
   * Gets current weather for a location. Protected by circuit breaker (opens at 50% failure rate)
   * with up to 3 retry attempts using exponential backoff.
   */
  @CircuitBreaker(name = "weatherApiCurrent", fallbackMethod = "getCurrentWeatherFallback")
  @Retry(name = "weatherApiCurrent")
  @io.github.resilience4j.ratelimiter.annotation.RateLimiter(name = "weatherApiCurrent")
  public WeatherApiResponse getCurrentWeather(String location) {
    log.debug("Fetching current weather for location: {}", location);

    try {
      WeatherApiResponse response =
          restClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/current.json")
                          .queryParam("key", apiKey)
                          .queryParam("q", location)
                          .queryParam("aqi", "no")
                          .build())
              .retrieve()
              .onStatus(
                  HttpStatusCode::is4xxClientError,
                  (request, clientResponse) -> {
                    log.error(
                        "Client error fetching weather for location {}: {}",
                        location,
                        clientResponse.getStatusCode());
                    throw new WeatherApiException("Invalid location or API request: " + location);
                  })
              .onStatus(
                  HttpStatusCode::is5xxServerError,
                  (request, serverResponse) -> {
                    log.error("Server error from WeatherAPI: {}", serverResponse.getStatusCode());
                    throw new WeatherApiException("Weather API server error");
                  })
              .body(WeatherApiResponse.class);

      if (response == null) {
        log.error("Received null response from WeatherAPI for location: {}", location);
        throw new WeatherApiException("Failed to fetch weather data: empty response");
      }

      log.info("Successfully fetched current weather for: {}", location);
      return response;

    } catch (WeatherApiException e) {
      throw e;
    } catch (org.springframework.web.client.RestClientException e) {
      log.error("REST client error fetching weather for location: {}", location, e);
      throw new WeatherApiException("Failed to fetch weather data: " + e.getMessage(), e);
    }
  }

  /**
   * Gets weather forecast for a location (1-14 days). Uses 2 retries instead of 3 since forecasts
   * have larger payloads and take longer.
   */
  @CircuitBreaker(name = "weatherApiForecast", fallbackMethod = "getForecastFallback")
  @Retry(name = "weatherApiForecast")
  @io.github.resilience4j.ratelimiter.annotation.RateLimiter(name = "weatherApiForecast")
  public ForecastApiResponse getForecast(String location, int days) {
    log.debug("Fetching {}-day forecast for location: {}", days, location);

    if (days < ValidationConstants.FORECAST_DAYS_MIN
        || days > ValidationConstants.FORECAST_DAYS_MAX) {
      throw new IllegalArgumentException(ValidationConstants.FORECAST_DAYS_RANGE_MESSAGE);
    }

    try {
      ForecastApiResponse response =
          restClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/forecast.json")
                          .queryParam("key", apiKey)
                          .queryParam("q", location)
                          .queryParam("days", days)
                          .queryParam("aqi", "no")
                          .queryParam("alerts", "no")
                          .build())
              .retrieve()
              .onStatus(
                  HttpStatusCode::is4xxClientError,
                  (request, clientResponse) -> {
                    log.error(
                        "Client error fetching forecast for location {}: {}",
                        location,
                        clientResponse.getStatusCode());
                    throw new WeatherApiException("Invalid location or API request: " + location);
                  })
              .onStatus(
                  HttpStatusCode::is5xxServerError,
                  (request, serverResponse) -> {
                    log.error("Server error from WeatherAPI: {}", serverResponse.getStatusCode());
                    throw new WeatherApiException("Weather API server error");
                  })
              .body(ForecastApiResponse.class);

      if (response == null) {
        log.error("Received null forecast response from WeatherAPI for location: {}", location);
        throw new WeatherApiException("Failed to fetch forecast data: empty response");
      }

      log.info("Successfully fetched {}-day forecast for: {}", days, location);
      return response;

    } catch (WeatherApiException e) {
      throw e;
    } catch (org.springframework.web.client.RestClientException e) {
      log.error("REST client error fetching forecast for location: {}", location, e);
      throw new WeatherApiException("Failed to fetch forecast data: " + e.getMessage(), e);
    }
  }

  /** Called when circuit breaker opens or retries are exhausted for current weather requests. */
  private WeatherApiResponse getCurrentWeatherFallback(String location, Throwable throwable) {
    log.error(
        "Circuit breaker fallback triggered for getCurrentWeather. Location: {}, Error: {}",
        location,
        throwable.getMessage());
    throw new WeatherApiException(
        "Weather service is currently unavailable. Please try again later. Location: " + location,
        throwable);
  }

  /** Called when circuit breaker opens or retries are exhausted for forecast requests. */
  private ForecastApiResponse getForecastFallback(String location, int days, Throwable throwable) {
    log.error(
        "Circuit breaker fallback triggered for getForecast. Location: {}, Days: {}, Error: {}",
        location,
        days,
        throwable.getMessage());
    throw new WeatherApiException(
        "Weather forecast service is currently unavailable. Please try again later. Location: "
            + location,
        throwable);
  }
}

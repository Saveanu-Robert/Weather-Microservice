package com.weatherspring.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.weatherspring.dto.ForecastDto;
import com.weatherspring.dto.LocationDto;
import com.weatherspring.dto.WeatherDto;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;

/**
 * Composite service using virtual threads for efficient parallel data fetching.
 *
 * <p>This service demonstrates concurrent operations using CompletableFuture with virtual thread
 * executor (Java 21+ Project Loom) for massive scalability.
 *
 * <p>Benefits of virtual threads:
 *
 * <ul>
 *   <li>Handles 10,000+ concurrent requests with minimal overhead
 *   <li>No need for complex async programming models
 *   <li>Simple blocking code that scales like async
 *   <li>Perfect for I/O-bound operations like API calls
 * </ul>
 *
 * @since Java 21 (Project Loom)
 */
@Service
@Slf4j
@Validated
public class CompositeWeatherService {

  private final WeatherService weatherService;
  private final ForecastService forecastService;
  private final LocationService locationService;
  private final ExecutorService virtualExecutor;

  /**
   * Constructor with dependency injection.
   *
   * @param weatherService the weather service
   * @param forecastService the forecast service
   * @param locationService the location service
   * @param virtualExecutor the virtual thread executor for composite operations
   */
  public CompositeWeatherService(
      WeatherService weatherService,
      ForecastService forecastService,
      LocationService locationService,
      @Qualifier("compositeExecutor") ExecutorService virtualExecutor) {
    this.weatherService = weatherService;
    this.forecastService = forecastService;
    this.locationService = locationService;
    this.virtualExecutor = virtualExecutor;
  }

  /**
   * Composite result containing weather and forecast data.
   *
   * @param weather current weather data
   * @param forecasts list of forecast data
   */
  @Schema(
      description =
          "Composite result containing current weather and forecast data fetched concurrently")
  public record WeatherWithForecast(
      @Schema(description = "Current weather information") WeatherDto weather,
      @Schema(description = "List of forecast data for the requested number of days")
          List<ForecastDto> forecasts) {}

  /**
   * Fetches current weather and forecast data concurrently using virtual threads.
   *
   * <p>Uses CompletableFuture with virtual thread executor for concurrent execution:
   *
   * <ul>
   *   <li>Both tasks run concurrently on separate virtual threads
   *   <li>Waits for both to complete using allOf()
   *   <li>Returns combined result
   * </ul>
   *
   * <p><strong>No @Transactional:</strong> This method orchestrates parallel service calls, each
   * with their own transaction management. Adding @Transactional here would:
   *
   * <ul>
   *   <li>Hold database connections open during blocking .join() calls
   *   <li>Cause connection pool exhaustion under load
   *   <li>Provide no benefit since no direct database operations occur here
   * </ul>
   *
   * @param locationName the location name
   * @param forecastDays number of forecast days (1-14)
   * @param saveToDatabase whether to save data to database
   * @return composite result with weather and forecast
   */
  @Observed(name = "composite.weather.and.forecast", contextualName = "get-weather-and-forecast")
  public WeatherWithForecast getWeatherAndForecast(
      @NotBlank String locationName, @Min(1) @Max(14) int forecastDays, boolean saveToDatabase) {

    log.info("Fetching weather and forecast concurrently for: {}", locationName);

    // Execute tasks concurrently on virtual threads
    CompletableFuture<WeatherDto> weatherFuture =
        CompletableFuture.supplyAsync(
            () -> weatherService.getCurrentWeather(locationName, saveToDatabase), virtualExecutor);

    CompletableFuture<List<ForecastDto>> forecastFuture =
        CompletableFuture.supplyAsync(
            () -> forecastService.getForecast(locationName, forecastDays, saveToDatabase),
            virtualExecutor);

    // Wait for both to complete concurrently with timeout, then extract results
    try {
      CompletableFuture.allOf(weatherFuture, forecastFuture).orTimeout(30, TimeUnit.SECONDS).join();

      // Extract results using getNow() since allOf().join() guarantees completion
      WeatherDto weather = weatherFuture.getNow(null);
      List<ForecastDto> forecasts = forecastFuture.getNow(null);

      if (weather == null || forecasts == null) {
        throw new IllegalStateException("Future completed with null result");
      }

      log.info(
          "Successfully fetched weather and {}-day forecast for: {}", forecastDays, locationName);

      return new WeatherWithForecast(weather, forecasts);
    } catch (Exception e) {
      // Cancel incomplete futures on timeout or error
      weatherFuture.cancel(true);
      forecastFuture.cancel(true);
      throw e;
    }
  }

  /**
   * Fetches current weather and forecast by location ID using virtual threads.
   *
   * <p><strong>No @Transactional:</strong> Orchestrates parallel service calls with their own
   * transactions. Blocking .join() calls should not hold database connections open.
   *
   * @param locationId the location ID
   * @param forecastDays number of forecast days (1-14)
   * @param saveToDatabase whether to save data to database
   * @return composite result with weather and forecast
   */
  @Observed(
      name = "composite.weather.and.forecast.by.id",
      contextualName = "get-weather-and-forecast-by-id")
  public WeatherWithForecast getWeatherAndForecastByLocationId(
      @NotNull Long locationId, @Min(1) @Max(14) int forecastDays, boolean saveToDatabase) {

    log.info("Fetching weather and forecast concurrently for location ID: {}", locationId);

    CompletableFuture<WeatherDto> weatherFuture =
        CompletableFuture.supplyAsync(
            () -> weatherService.getCurrentWeatherByLocationId(locationId, saveToDatabase),
            virtualExecutor);

    CompletableFuture<List<ForecastDto>> forecastFuture =
        CompletableFuture.supplyAsync(
            () -> forecastService.getForecastByLocationId(locationId, forecastDays, saveToDatabase),
            virtualExecutor);

    // Wait for both to complete concurrently with timeout, then extract results
    try {
      CompletableFuture.allOf(weatherFuture, forecastFuture).orTimeout(30, TimeUnit.SECONDS).join();

      // Extract results using getNow() since allOf().join() guarantees completion
      WeatherDto weather = weatherFuture.getNow(null);
      List<ForecastDto> forecasts = forecastFuture.getNow(null);

      if (weather == null || forecasts == null) {
        throw new IllegalStateException("Future completed with null result");
      }

      log.info(
          "Successfully fetched weather and {}-day forecast for location ID: {}",
          forecastDays,
          locationId);

      return new WeatherWithForecast(weather, forecasts);
    } catch (Exception e) {
      // Cancel incomplete futures on timeout or error
      weatherFuture.cancel(true);
      forecastFuture.cancel(true);
      throw e;
    }
  }

  /**
   * Complete location information with current weather and forecast.
   *
   * @param location location information
   * @param weather current weather data
   * @param forecasts forecast data
   */
  @Schema(
      description =
          "Complete location information including location details, current weather, and forecasts")
  public record CompleteLocationInfo(
      @Schema(description = "Location details (name, country, coordinates, etc.)")
          LocationDto location,
      @Schema(description = "Current weather information for this location") WeatherDto weather,
      @Schema(description = "List of forecast data for the requested number of days")
          List<ForecastDto> forecasts) {}

  /**
   * Fetches complete location information including weather and forecast concurrently.
   *
   * <p>This demonstrates a 3-way concurrent fetch using virtual threads.
   *
   * <p><strong>No @Transactional:</strong> Orchestrates parallel service calls with their own
   * transactions. Blocking .join() calls should not hold database connections open.
   *
   * @param locationId the location ID
   * @param forecastDays number of forecast days (1-14)
   * @param saveToDatabase whether to save weather data to database
   * @return complete location information
   */
  @Observed(
      name = "composite.complete.location.info",
      contextualName = "get-complete-location-info")
  public CompleteLocationInfo getCompleteLocationInfo(
      @NotNull Long locationId, @Min(1) @Max(14) int forecastDays, boolean saveToDatabase) {

    log.info("Fetching complete location info concurrently for location ID: {}", locationId);

    // Execute 3 tasks concurrently on virtual threads
    CompletableFuture<LocationDto> locationFuture =
        CompletableFuture.supplyAsync(
            () -> locationService.getLocationById(locationId), virtualExecutor);

    CompletableFuture<WeatherDto> weatherFuture =
        CompletableFuture.supplyAsync(
            () -> weatherService.getCurrentWeatherByLocationId(locationId, saveToDatabase),
            virtualExecutor);

    CompletableFuture<List<ForecastDto>> forecastFuture =
        CompletableFuture.supplyAsync(
            () -> forecastService.getForecastByLocationId(locationId, forecastDays, saveToDatabase),
            virtualExecutor);

    // Wait for all three to complete concurrently with timeout, then extract results
    try {
      CompletableFuture.allOf(locationFuture, weatherFuture, forecastFuture)
          .orTimeout(30, TimeUnit.SECONDS)
          .join();

      // Extract results using getNow() since allOf().join() guarantees completion
      LocationDto location = locationFuture.getNow(null);
      WeatherDto weather = weatherFuture.getNow(null);
      List<ForecastDto> forecasts = forecastFuture.getNow(null);

      if (location == null || weather == null || forecasts == null) {
        throw new IllegalStateException("Future completed with null result");
      }

      log.info("Successfully fetched complete info for location: {}", location.name());

      return new CompleteLocationInfo(location, weather, forecasts);
    } catch (Exception e) {
      // Cancel incomplete futures on timeout or error
      locationFuture.cancel(true);
      weatherFuture.cancel(true);
      forecastFuture.cancel(true);
      throw e;
    }
  }

  /**
   * Fetches weather data for multiple locations concurrently.
   *
   * <p>Uses virtual threads to fetch weather for all locations in parallel.
   *
   * <p><strong>No @Transactional:</strong> Orchestrates parallel service calls with their own
   * transactions. Blocking .join() calls should not hold database connections open.
   *
   * @param locationNames list of location names
   * @param saveToDatabase whether to save data to database
   * @return list of weather data for all locations
   */
  @Observed(name = "composite.bulk.weather", contextualName = "get-bulk-weather")
  public List<WeatherDto> getBulkWeather(
      @NotEmpty List<String> locationNames, boolean saveToDatabase) {

    log.info("Fetching weather for {} locations concurrently", locationNames.size());

    // Create a CompletableFuture for each location
    List<CompletableFuture<WeatherDto>> futures =
        locationNames.stream()
            .map(
                name ->
                    CompletableFuture.supplyAsync(
                        () -> weatherService.getCurrentWeather(name, saveToDatabase),
                        virtualExecutor))
            .toList();

    // Wait for all futures to complete and collect results
    List<WeatherDto> results = futures.stream().map(CompletableFuture::join).toList();

    log.info("Successfully fetched weather for {} locations", results.size());

    return results;
  }
}

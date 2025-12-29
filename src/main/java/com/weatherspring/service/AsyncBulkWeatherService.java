package com.weatherspring.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.weatherspring.dto.BulkOperationResult;
import com.weatherspring.dto.ForecastDto;
import com.weatherspring.dto.WeatherDto;
import com.weatherspring.util.BatchingUtils;
import com.weatherspring.util.ExceptionHandlers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for async bulk weather operations using Spring @Async with virtual threads.
 *
 * <p>All methods return {@link CompletableFuture} and execute asynchronously on virtual threads,
 * allowing the caller to continue processing while data is being fetched.
 *
 * <p>Benefits:
 *
 * <ul>
 *   <li>Non-blocking - caller can do other work while waiting
 *   <li>Composable - can combine multiple async operations
 *   <li>Virtual threads - no thread pool exhaustion
 *   <li>Built-in error handling via CompletableFuture
 * </ul>
 */
@Service
@Slf4j
@Validated
public class AsyncBulkWeatherService {

  private final WeatherService weatherService;
  private final ForecastService forecastService;
  private final LocationService locationService;
  private final MeterRegistry meterRegistry;
  private final int timeoutSeconds;

  // Metrics
  private final Counter weatherSuccessCounter;
  private final Counter weatherFailureCounter;
  private final Counter forecastSuccessCounter;
  private final Counter forecastFailureCounter;
  private final Counter updateSuccessCounter;
  private final Counter updateFailureCounter;
  private final Timer weatherTimer;
  private final Timer forecastTimer;
  private final Timer updateTimer;

  /**
   * Constructs a new AsyncBulkWeatherService with required dependencies.
   *
   * @param weatherService service for weather operations
   * @param forecastService service for forecast operations
   * @param locationService service for location operations
   * @param meterRegistry registry for metrics
   * @param timeoutSeconds timeout for async operations in seconds
   */
  public AsyncBulkWeatherService(
      WeatherService weatherService,
      ForecastService forecastService,
      LocationService locationService,
      MeterRegistry meterRegistry,
      @Value("${app.async.timeout-seconds:60}") int timeoutSeconds) {
    this.weatherService = weatherService;
    this.forecastService = forecastService;
    this.locationService = locationService;
    this.meterRegistry = meterRegistry;
    this.timeoutSeconds = timeoutSeconds;

    // Initialize counters
    this.weatherSuccessCounter =
        Counter.builder("async.bulk.weather.success")
            .description("Number of successful bulk weather fetches")
            .register(meterRegistry);
    this.weatherFailureCounter =
        Counter.builder("async.bulk.weather.failure")
            .description("Number of failed bulk weather fetches")
            .register(meterRegistry);
    this.forecastSuccessCounter =
        Counter.builder("async.bulk.forecast.success")
            .description("Number of successful bulk forecast fetches")
            .register(meterRegistry);
    this.forecastFailureCounter =
        Counter.builder("async.bulk.forecast.failure")
            .description("Number of failed bulk forecast fetches")
            .register(meterRegistry);
    this.updateSuccessCounter =
        Counter.builder("async.bulk.update.success")
            .description("Number of successful bulk weather updates")
            .register(meterRegistry);
    this.updateFailureCounter =
        Counter.builder("async.bulk.update.failure")
            .description("Number of failed bulk weather updates")
            .register(meterRegistry);

    // Initialize timers
    this.weatherTimer =
        Timer.builder("async.bulk.weather.duration")
            .description("Duration of bulk weather fetch operations")
            .register(meterRegistry);
    this.forecastTimer =
        Timer.builder("async.bulk.forecast.duration")
            .description("Duration of bulk forecast fetch operations")
            .register(meterRegistry);
    this.updateTimer =
        Timer.builder("async.bulk.update.duration")
            .description("Duration of bulk weather update operations")
            .register(meterRegistry);
  }

  /**
   * Asynchronously fetches current weather for multiple locations.
   *
   * <p>Returns immediately with a CompletableFuture that will complete when all weather data has
   * been fetched.
   *
   * @param locationNames list of location names
   * @param saveToDatabase whether to save to database
   * @return future containing list of weather data
   * @throws IllegalArgumentException if locationNames is empty
   */
  @Observed(name = "async.bulk.weather", contextualName = "async-bulk-weather")
  public CompletableFuture<List<WeatherDto>> fetchBulkWeatherAsync(
      @NotEmpty List<String> locationNames, boolean saveToDatabase) {

    return CompletableFuture.supplyAsync(
        () ->
            weatherTimer.record(
                () -> {
                  log.info(
                      "Starting async bulk weather fetch for {} locations", locationNames.size());

                  // Record batch size
                  meterRegistry.gauge("async.bulk.weather.batch.size", locationNames.size());

                  // Process in batches to prevent memory exhaustion
                  List<List<String>> batches = BatchingUtils.partition(locationNames);
                  int batchCount = batches.size();

                  if (batchCount > 1) {
                    log.info(
                        "Processing {} locations in {} batches of up to {} items",
                        locationNames.size(),
                        batchCount,
                        BatchingUtils.DEFAULT_BATCH_SIZE);
                  }

                  // Process each batch concurrently
                  List<CompletableFuture<List<WeatherDto>>> batchFutures =
                      batches.stream()
                          .map(batch -> processBatchWeather(batch, saveToDatabase))
                          .toList();

                  // Wait for all batches to complete and aggregate results (with timeout)
                  CompletableFuture<Void> allBatches =
                      CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]))
                          .orTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);

                  List<WeatherDto> results =
                      allBatches
                          .thenApply(
                              v ->
                                  batchFutures.stream()
                                      .map(CompletableFuture::join)
                                      .flatMap(List::stream)
                                      .toList())
                          .join();

                  // Record metrics
                  long successCount = results.size();
                  long failureCount = locationNames.size() - successCount;
                  weatherSuccessCounter.increment(successCount);
                  if (failureCount > 0) {
                    weatherFailureCounter.increment(failureCount);
                  }

                  return results;
                }));
  }

  /**
   * Processes a single batch of weather fetches concurrently.
   *
   * <p><strong>Failure Handling:</strong> Returns {@code null} for failed fetches (exceptions
   * caught in {@code exceptionally} handler). Nulls are filtered out before returning, allowing
   * metrics to distinguish failures from successful empty results.
   */
  private CompletableFuture<List<WeatherDto>> processBatchWeather(
      List<String> batch, boolean saveToDatabase) {

    List<CompletableFuture<WeatherDto>> futures =
        batch.stream()
            .map(
                name ->
                    CompletableFuture.supplyAsync(
                            () -> weatherService.getCurrentWeather(name, saveToDatabase))
                        .exceptionally(
                            ex -> {
                              log.error("Failed to fetch weather for location: {}", name, ex);
                              return null; // null indicates fetch failure (filtered out later)
                            }))
            .toList();

    CompletableFuture<Void> allOf =
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

    return allOf.thenApply(
        v ->
            futures.stream()
                .map(CompletableFuture::join)
                .filter(result -> result != null) // Filter out failed operations
                .toList());
  }

  /**
   * Asynchronously fetches forecasts for multiple locations.
   *
   * @param locationNames list of location names
   * @param days number of forecast days (1-14)
   * @param saveToDatabase whether to save to database
   * @return future containing list of forecast lists
   */
  @Observed(name = "async.bulk.forecast", contextualName = "async-bulk-forecast")
  public CompletableFuture<List<List<ForecastDto>>> fetchBulkForecastAsync(
      @NotEmpty List<String> locationNames, @Min(1) @Max(14) int days, boolean saveToDatabase) {

    return CompletableFuture.supplyAsync(
        () ->
            forecastTimer.record(
                () -> {
                  log.info(
                      "Starting async bulk forecast fetch for {} locations, {} days",
                      locationNames.size(),
                      days);

                  // Record batch size
                  meterRegistry.gauge("async.bulk.forecast.batch.size", locationNames.size());

                  // Process in batches to prevent memory exhaustion
                  List<List<String>> batches = BatchingUtils.partition(locationNames);
                  int batchCount = batches.size();

                  if (batchCount > 1) {
                    log.info(
                        "Processing {} locations in {} batches of up to {} items",
                        locationNames.size(),
                        batchCount,
                        BatchingUtils.DEFAULT_BATCH_SIZE);
                  }

                  // Process each batch concurrently
                  List<CompletableFuture<List<List<ForecastDto>>>> batchFutures =
                      batches.stream()
                          .map(batch -> processBatchForecast(batch, days, saveToDatabase))
                          .toList();

                  // Wait for all batches to complete and aggregate results (with timeout)
                  CompletableFuture<Void> allBatches =
                      CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]))
                          .orTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);

                  List<List<ForecastDto>> results =
                      allBatches
                          .thenApply(
                              v ->
                                  batchFutures.stream()
                                      .map(CompletableFuture::join)
                                      .flatMap(List::stream)
                                      .toList())
                          .join();

                  // Record metrics
                  // Count successes as the number of results returned (nulls filtered out at line
                  // 263)
                  // An empty forecast list is still a successful fetch, not a failure
                  long successCount = results.size();
                  long failureCount = locationNames.size() - successCount;
                  forecastSuccessCounter.increment(successCount);
                  if (failureCount > 0) {
                    forecastFailureCounter.increment(failureCount);
                  }

                  return results;
                }));
  }

  /**
   * Processes a single batch of forecast fetches concurrently.
   *
   * <p><strong>Failure Handling:</strong> Returns {@code null} for failed fetches (exceptions
   * caught in {@code exceptionally} handler). Nulls are filtered out before returning. An empty
   * forecast list means successful fetch with no forecasts, while {@code null} means the fetch
   * itself failed.
   */
  private CompletableFuture<List<List<ForecastDto>>> processBatchForecast(
      List<String> batch, int days, boolean saveToDatabase) {

    List<CompletableFuture<List<ForecastDto>>> futures =
        batch.stream()
            .map(
                name ->
                    CompletableFuture.supplyAsync(
                            () -> forecastService.getForecast(name, days, saveToDatabase))
                        .exceptionally(
                            ex -> {
                              log.error("Failed to fetch forecast for location: {}", name, ex);
                              return null; // null indicates fetch failure (filtered out later)
                            }))
            .toList();

    CompletableFuture<Void> allOf =
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

    return allOf.thenApply(
        v ->
            futures.stream()
                .map(CompletableFuture::join)
                .filter(result -> result != null) // Filter out failed operations
                .toList());
  }

  /**
   * Bulk update weather data for multiple location IDs asynchronously.
   *
   * <p>Useful for batch jobs that need to refresh weather data for all tracked locations.
   *
   * @param locationIds list of location IDs
   * @param saveToDatabase whether to save to database
   * @return future containing number of successfully updated locations
   */
  @Observed(name = "async.bulk.update", contextualName = "async-bulk-update")
  public CompletableFuture<Integer> bulkUpdateWeatherAsync(
      @NotEmpty List<Long> locationIds, boolean saveToDatabase) {

    log.info("Starting async bulk weather update for {} locations", locationIds.size());

    // Record batch size
    meterRegistry.gauge("async.bulk.update.batch.size", locationIds.size());

    // Process in batches to prevent memory exhaustion
    List<List<Long>> batches = BatchingUtils.partition(locationIds);
    int batchCount = batches.size();

    if (batchCount > 1) {
      log.info(
          "Processing {} location IDs in {} batches of up to {} items",
          locationIds.size(),
          batchCount,
          BatchingUtils.DEFAULT_BATCH_SIZE);
    }

    // Process each batch concurrently
    List<CompletableFuture<Integer>> batchFutures =
        batches.stream()
            .map(batch -> processBatchUpdate(batch, saveToDatabase))
            .toList();

    // Wait for all batches to complete and sum the results (with timeout)
    // FIXED: Don't use .join() inside async - properly compose futures instead
    CompletableFuture<Void> allBatches =
        CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]))
            .orTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);

    return allBatches
        .thenApply(
            v -> {
              // Sum up results from all batches
              int totalSuccess =
                  batchFutures.stream()
                      .map(CompletableFuture::join)
                      .mapToInt(Integer::intValue)
                      .sum();

              // Record metrics
              int totalFailure = locationIds.size() - totalSuccess;
              updateSuccessCounter.increment(totalSuccess);
              if (totalFailure > 0) {
                updateFailureCounter.increment(totalFailure);
              }

              log.info(
                  "Bulk weather update completed: {}/{} successful",
                  totalSuccess,
                  locationIds.size());

              return totalSuccess;
            })
        .exceptionally(
            ex -> {
              log.error("Bulk weather update failed: {}", ex.getMessage(), ex);
              updateFailureCounter.increment(locationIds.size());
              return 0;
            });
  }

  /** Processes a single batch of weather updates concurrently. */
  private CompletableFuture<Integer> processBatchUpdate(List<Long> batch, boolean saveToDatabase) {

    List<CompletableFuture<WeatherDto>> futures =
        batch.stream()
            .map(
                id ->
                    CompletableFuture.supplyAsync(
                            () -> weatherService.getCurrentWeatherByLocationId(id, saveToDatabase))
                        .exceptionally(
                            ExceptionHandlers.warnWithMessageAndReturnNull(
                                "Failed to update weather for location ID {}: {}", id)))
            .toList();

    CompletableFuture<Void> allOf =
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

    return allOf.thenApply(
        v ->
            (int)
                futures.stream()
                    .map(CompletableFuture::join)
                    .filter(result -> result != null)
                    .count());
  }

  /**
   * Refresh all forecast data for multiple locations asynchronously.
   *
   * @param locationIds list of location IDs
   * @param days number of forecast days (1-14)
   * @param saveToDatabase whether to save to database
   * @return future containing number of successfully updated forecasts
   */
  @Observed(name = "async.bulk.forecast.refresh", contextualName = "async-bulk-forecast-refresh")
  public CompletableFuture<Integer> bulkRefreshForecastsAsync(
      @NotEmpty List<Long> locationIds, @Min(1) @Max(14) int days, boolean saveToDatabase) {

    log.info(
        "Starting async bulk forecast refresh for {} locations, {} days",
        locationIds.size(),
        days);

    // Record batch size
    meterRegistry.gauge("async.bulk.forecast.refresh.batch.size", locationIds.size());

    // Process in batches to prevent memory exhaustion
    List<List<Long>> batches = BatchingUtils.partition(locationIds);
    int batchCount = batches.size();

    if (batchCount > 1) {
      log.info(
          "Processing {} location IDs in {} batches of up to {} items",
          locationIds.size(),
          batchCount,
          BatchingUtils.DEFAULT_BATCH_SIZE);
    }

    // Process each batch concurrently
    List<CompletableFuture<Integer>> batchFutures =
        batches.stream()
            .map(batch -> processBatchForecastRefresh(batch, days, saveToDatabase))
            .toList();

    // Wait for all batches to complete and sum the results (with timeout)
    // FIXED: Don't use .join() inside async - properly compose futures instead
    CompletableFuture<Void> allBatches =
        CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]))
            .orTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);

    return allBatches
        .thenApply(
            v -> {
              // Sum up results from all batches
              int totalSuccess =
                  batchFutures.stream()
                      .map(CompletableFuture::join)
                      .mapToInt(Integer::intValue)
                      .sum();

              // Record metrics
              int totalFailure = locationIds.size() - totalSuccess;
              forecastSuccessCounter.increment(totalSuccess);
              if (totalFailure > 0) {
                forecastFailureCounter.increment(totalFailure);
              }

              log.info(
                  "Bulk forecast refresh completed: {}/{} successful",
                  totalSuccess,
                  locationIds.size());

              return totalSuccess;
            })
        .exceptionally(
            ex -> {
              log.error("Bulk forecast refresh failed: {}", ex.getMessage(), ex);
              forecastFailureCounter.increment(locationIds.size());
              return 0;
            });
  }

  /** Processes a single batch of forecast refreshes concurrently. */
  private CompletableFuture<Integer> processBatchForecastRefresh(
      List<Long> batch, int days, boolean saveToDatabase) {

    List<CompletableFuture<List<ForecastDto>>> futures =
        batch.stream()
            .map(
                id ->
                    CompletableFuture.supplyAsync(
                            () -> forecastService.getForecastByLocationId(id, days, saveToDatabase))
                        .exceptionally(
                            ExceptionHandlers.warnWithMessageAndReturnNull(
                                "Failed to refresh forecast for location ID {}: {}", id)))
            .toList();

    CompletableFuture<Void> allOf =
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

    return allOf.thenApply(
        v ->
            (int)
                futures.stream()
                    .map(CompletableFuture::join)
                    .filter(result -> result != null)
                    .count());
  }

  /**
   * Comprehensive bulk data refresh for all saved locations.
   *
   * <p>Fetches both current weather and forecast for all locations in the system. Returns a summary
   * of the operation.
   *
   * @param forecastDays number of forecast days (1-14)
   * @return future containing operation result summary
   */
  @Observed(name = "async.refresh.all", contextualName = "async-refresh-all-locations")
  public CompletableFuture<BulkOperationResult> refreshAllLocationsAsync(int forecastDays) {

    log.info("Starting async refresh of all locations with {}-day forecast", forecastDays);

    // Get all location IDs
    var allLocations = locationService.getAllLocations();
    var locationIds = allLocations.stream().map(loc -> loc.id()).toList();

    if (locationIds.isEmpty()) {
      log.info("No locations to refresh");
      return CompletableFuture.completedFuture(new BulkOperationResult(0, 0, 0));
    }

    // Refresh both weather and forecast for all locations concurrently
    CompletableFuture<Integer> weatherFuture = bulkUpdateWeatherAsync(locationIds, true);

    CompletableFuture<Integer> forecastFuture =
        bulkRefreshForecastsAsync(locationIds, forecastDays, true);

    return weatherFuture.thenCombine(
        forecastFuture,
        (weatherSuccess, forecastSuccess) -> {
          int totalOps = locationIds.size() * 2; // weather + forecast for each
          int totalSuccess = weatherSuccess + forecastSuccess;
          int totalFailure = totalOps - totalSuccess;

          log.info("Refresh all completed: {}/{} operations successful", totalSuccess, totalOps);

          return new BulkOperationResult(totalSuccess, totalFailure, totalOps);
        });
  }
}

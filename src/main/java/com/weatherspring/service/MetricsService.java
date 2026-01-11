package com.weatherspring.service;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for tracking custom business metrics.
 *
 * <p>Provides methods to record domain-specific metrics for monitoring and observability. All
 * metrics are exposed via Micrometer and can be scraped by Prometheus.
 */
@Service
@Slf4j
public class MetricsService {

  private final Counter weatherApiCallsTotal;
  private final Counter forecastApiCallsTotal;
  private final Counter cacheHitsTotal;
  private final Counter cacheMissesTotal;
  private final Counter locationsCreatedTotal;
  private final Counter weatherRecordsSavedTotal;
  private final Counter forecastRecordsSavedTotal;
  private final Timer externalApiResponseTime;

  /**
   * Constructs a new MetricsService and initializes all custom metrics.
   *
   * @param meterRegistry the meter registry for metric registration
   */
  public MetricsService(MeterRegistry meterRegistry) {
    this.weatherApiCallsTotal =
        Counter.builder("weather.api.calls.total")
            .description("Total number of weather API calls")
            .tag("api", "weatherapi")
            .register(meterRegistry);

    this.forecastApiCallsTotal =
        Counter.builder("forecast.api.calls.total")
            .description("Total number of forecast API calls")
            .tag("api", "weatherapi")
            .register(meterRegistry);

    this.cacheHitsTotal =
        Counter.builder("cache.hits.total")
            .description("Total number of cache hits")
            .register(meterRegistry);

    this.cacheMissesTotal =
        Counter.builder("cache.misses.total")
            .description("Total number of cache misses")
            .register(meterRegistry);

    this.locationsCreatedTotal =
        Counter.builder("locations.created.total")
            .description("Total number of locations created")
            .register(meterRegistry);

    this.weatherRecordsSavedTotal =
        Counter.builder("weather.records.saved.total")
            .description("Total number of weather records saved to database")
            .register(meterRegistry);

    this.forecastRecordsSavedTotal =
        Counter.builder("forecast.records.saved.total")
            .description("Total number of forecast records saved to database")
            .register(meterRegistry);

    this.externalApiResponseTime =
        Timer.builder("external.api.response.time")
            .description("External API response time")
            .tag("api", "weatherapi")
            .register(meterRegistry);
  }

  /**
   * Records a call to the weather API.
   *
   * <p>Increments the counter tracking total weather API calls. Use this whenever fetching current
   * weather conditions from the external weather service.
   */
  public void recordWeatherApiCall() {
    weatherApiCallsTotal.increment();
  }

  /**
   * Records a call to the forecast API.
   *
   * <p>Increments the counter tracking total forecast API calls. Use this whenever fetching
   * multi-day forecast data from the external weather service.
   */
  public void recordForecastApiCall() {
    forecastApiCallsTotal.increment();
  }

  /**
   * Records a successful cache lookup.
   *
   * <p>Increments the counter when requested data is found in the cache. Use this to track cache
   * effectiveness and avoid unnecessary API calls.
   */
  public void recordCacheHit() {
    cacheHitsTotal.increment();
  }

  /**
   * Records a failed cache lookup.
   *
   * <p>Increments the counter when requested data is not found in the cache. This indicates the
   * system needs to fetch fresh data from the external API.
   */
  public void recordCacheMiss() {
    cacheMissesTotal.increment();
  }

  /**
   * Records the creation of a new location.
   *
   * <p>Increments the counter when a location is successfully added to the database. Use this to
   * monitor location growth and user activity.
   */
  public void recordLocationCreated() {
    locationsCreatedTotal.increment();
  }

  /**
   * Records weather records saved to the database.
   *
   * <p>Increments the counter by the specified amount when weather records are persisted. Use this
   * to track data collection volume and database write activity.
   *
   * @param count number of weather records saved
   */
  public void recordWeatherRecordsSaved(int count) {
    weatherRecordsSavedTotal.increment(count);
  }

  /**
   * Records forecast records saved to the database.
   *
   * <p>Increments the counter by the specified amount when forecast records are persisted. Use this
   * to track forecast data collection and database write volume.
   *
   * @param count number of forecast records saved
   */
  public void recordForecastRecordsSaved(int count) {
    forecastRecordsSavedTotal.increment(count);
  }

  /**
   * Records the response time of an external API call.
   *
   * <p>Tracks how long external API requests take to complete. Use this to monitor API performance
   * and identify slow responses that may impact user experience.
   *
   * @param durationMillis API call duration in milliseconds
   */
  public void recordExternalApiResponseTime(long durationMillis) {
    externalApiResponseTime.record(durationMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * Executes a task and records its execution time as external API response time.
   *
   * @param task the task to execute
   * @param <T> return type
   * @return the task result
   */
  public <T> T recordExternalApiCall(java.util.function.Supplier<T> task) {
    return externalApiResponseTime.record(task);
  }
}

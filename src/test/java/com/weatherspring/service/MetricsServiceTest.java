package com.weatherspring.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/** Unit tests for MetricsService. */
class MetricsServiceTest {

  private MeterRegistry meterRegistry;
  private MetricsService metricsService;

  @BeforeEach
  void setUp() {
    meterRegistry = new SimpleMeterRegistry();
    metricsService = new MetricsService(meterRegistry);
  }

  @Test
  void recordWeatherApiCall_IncrementsCounter() {
    // Act
    metricsService.recordWeatherApiCall();
    metricsService.recordWeatherApiCall();

    // Assert
    Counter counter = meterRegistry.find("weather.api.calls.total").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(2.0);
  }

  @Test
  void recordForecastApiCall_IncrementsCounter() {
    // Act
    metricsService.recordForecastApiCall();

    // Assert
    Counter counter = meterRegistry.find("forecast.api.calls.total").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void recordCacheHit_IncrementsCounter() {
    // Act
    metricsService.recordCacheHit();
    metricsService.recordCacheHit();
    metricsService.recordCacheHit();

    // Assert
    Counter counter = meterRegistry.find("cache.hits.total").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(3.0);
  }

  @Test
  void recordCacheMiss_IncrementsCounter() {
    // Act
    metricsService.recordCacheMiss();

    // Assert
    Counter counter = meterRegistry.find("cache.misses.total").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void recordLocationCreated_IncrementsCounter() {
    // Act
    metricsService.recordLocationCreated();

    // Assert
    Counter counter = meterRegistry.find("locations.created.total").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void recordWeatherRecordsSaved_IncrementsCounterByGivenAmount() {
    // Act
    metricsService.recordWeatherRecordsSaved(5);
    metricsService.recordWeatherRecordsSaved(3);

    // Assert
    Counter counter = meterRegistry.find("weather.records.saved.total").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(8.0);
  }

  @Test
  void recordForecastRecordsSaved_IncrementsCounterByGivenAmount() {
    // Act
    metricsService.recordForecastRecordsSaved(10);

    // Assert
    Counter counter = meterRegistry.find("forecast.records.saved.total").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(10.0);
  }

  @Test
  void recordExternalApiResponseTime_RecordsTimerValue() {
    // Act
    metricsService.recordExternalApiResponseTime(150);
    metricsService.recordExternalApiResponseTime(200);

    // Assert
    Timer timer = meterRegistry.find("external.api.response.time").timer();
    assertThat(timer).isNotNull();
    assertThat(timer.count()).isEqualTo(2);
    assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(350.0);
  }

  @Test
  void recordExternalApiCall_ExecutesTaskAndRecordsTime() {
    // Arrange
    Supplier<String> task =
        () -> {
          try {
            Thread.sleep(10); // Simulate some work
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
          return "result";
        };

    // Act
    String result = metricsService.recordExternalApiCall(task);

    // Assert
    assertThat(result).isEqualTo("result");
    Timer timer = meterRegistry.find("external.api.response.time").timer();
    assertThat(timer).isNotNull();
    assertThat(timer.count()).isEqualTo(1);
    assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isGreaterThan(0);
  }

  @Test
  void metricsHaveCorrectTags() {
    // Act
    metricsService.recordWeatherApiCall();
    metricsService.recordForecastApiCall();

    // Assert
    Counter weatherCounter =
        meterRegistry.find("weather.api.calls.total").tag("api", "weatherapi").counter();
    assertThat(weatherCounter).isNotNull();

    Counter forecastCounter =
        meterRegistry.find("forecast.api.calls.total").tag("api", "weatherapi").counter();
    assertThat(forecastCounter).isNotNull();
  }

  @Test
  void allMetricsAreRegistered() {
    // Assert - verify all expected metrics exist in registry
    assertThat(meterRegistry.find("weather.api.calls.total").counter()).isNotNull();
    assertThat(meterRegistry.find("forecast.api.calls.total").counter()).isNotNull();
    assertThat(meterRegistry.find("cache.hits.total").counter()).isNotNull();
    assertThat(meterRegistry.find("cache.misses.total").counter()).isNotNull();
    assertThat(meterRegistry.find("locations.created.total").counter()).isNotNull();
    assertThat(meterRegistry.find("weather.records.saved.total").counter()).isNotNull();
    assertThat(meterRegistry.find("forecast.records.saved.total").counter()).isNotNull();
    assertThat(meterRegistry.find("external.api.response.time").timer()).isNotNull();
  }
}

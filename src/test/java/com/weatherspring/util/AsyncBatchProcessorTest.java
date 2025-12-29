package com.weatherspring.util;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/** Unit tests for AsyncBatchProcessor. */
class AsyncBatchProcessorTest {

  private SimpleMeterRegistry meterRegistry;
  private AsyncBatchProcessor<String, String> processor;

  @BeforeEach
  void setUp() {
    meterRegistry = new SimpleMeterRegistry();
    processor = new AsyncBatchProcessor<>(meterRegistry, "test-operation");
  }

  @Test
  void constructor_CreatesMetricsCorrectly() {
    // Verify that metrics are created with correct names
    assertThat(meterRegistry.getMeters()).isNotEmpty();
    assertThat(meterRegistry.find("async.batch.test-operation.duration").timer()).isNotNull();
    assertThat(meterRegistry.find("async.batch.test-operation.success").counter()).isNotNull();
    assertThat(meterRegistry.find("async.batch.test-operation.failure").counter()).isNotNull();
  }

  @Test
  void processAsync_WithSuccessfulItems_ReturnsAllResults() {
    // Arrange
    List<String> items = Arrays.asList("A", "B", "C");
    Function<String, CompletableFuture<String>> processorFunc =
        item -> CompletableFuture.completedFuture(item.toLowerCase());

    // Act
    CompletableFuture<List<String>> result = processor.processAsync(items, processorFunc);
    List<String> results = result.join();

    // Assert
    assertThat(results).hasSize(3);
    assertThat(results).containsExactly("a", "b", "c");
  }

  @Test
  void processAsync_WithFailedItems_FiltersOutNullResults() {
    // Arrange
    List<String> items = Arrays.asList("A", "B", "C", "D");
    Function<String, CompletableFuture<String>> processorFunc =
        item -> {
          if (item.equals("B") || item.equals("D")) {
            return CompletableFuture.completedFuture(null); // Simulate failure
          }
          return CompletableFuture.completedFuture(item.toLowerCase());
        };

    // Act
    CompletableFuture<List<String>> result = processor.processAsync(items, processorFunc);
    List<String> results = result.join();

    // Assert
    assertThat(results).hasSize(2);
    assertThat(results).containsExactly("a", "c");
  }

  @Test
  void processAsync_WithLargeList_ProcessesInMultipleBatches() {
    // Arrange
    SimpleMeterRegistry largeMeterRegistry = new SimpleMeterRegistry();
    AsyncBatchProcessor<Integer, Integer> largeProcessor =
        new AsyncBatchProcessor<>(largeMeterRegistry, "large-batch");

    // Create a list larger than DEFAULT_BATCH_SIZE (50)
    List<Integer> items = java.util.stream.IntStream.range(0, 120).boxed().toList();
    Function<Integer, CompletableFuture<Integer>> processorFunc =
        item -> CompletableFuture.completedFuture(item * 2);

    // Act
    CompletableFuture<List<Integer>> result = largeProcessor.processAsync(items, processorFunc);
    List<Integer> results = result.join();

    // Assert
    assertThat(results).hasSize(120);
    assertThat(results.get(0)).isEqualTo(0);
    assertThat(results.get(119)).isEqualTo(238);
  }

  @Test
  void processAsync_WithEmptyList_ReturnsEmptyList() {
    // Arrange
    List<String> items = Arrays.asList();
    Function<String, CompletableFuture<String>> processorFunc =
        item -> CompletableFuture.completedFuture(item);

    // Act
    CompletableFuture<List<String>> result = processor.processAsync(items, processorFunc);
    List<String> results = result.join();

    // Assert
    assertThat(results).isEmpty();
  }

  @Test
  void processAsync_WithSingleItem_ProcessesSuccessfully() {
    // Arrange
    SimpleMeterRegistry singleMeterRegistry = new SimpleMeterRegistry();
    AsyncBatchProcessor<String, String> singleProcessor =
        new AsyncBatchProcessor<>(singleMeterRegistry, "single-item");

    List<String> items = Arrays.asList("single");
    Function<String, CompletableFuture<String>> processorFunc =
        item -> CompletableFuture.completedFuture(item.toUpperCase());

    // Act
    CompletableFuture<List<String>> result = singleProcessor.processAsync(items, processorFunc);
    List<String> results = result.join();

    // Assert
    assertThat(results).hasSize(1);
    assertThat(results.get(0)).isEqualTo("SINGLE");
  }

  @Test
  void processForCount_WithSuccessfulItems_ReturnsCount() {
    // Arrange
    SimpleMeterRegistry countMeterRegistry = new SimpleMeterRegistry();
    AsyncBatchProcessor<String, String> countProcessor =
        new AsyncBatchProcessor<>(countMeterRegistry, "count-test");

    List<String> items = Arrays.asList("A", "B", "C", "D", "E");
    Function<String, CompletableFuture<String>> processorFunc =
        item -> CompletableFuture.completedFuture(item.toLowerCase());

    // Act
    CompletableFuture<Integer> result = countProcessor.processForCount(items, processorFunc);
    Integer count = result.join();

    // Assert
    assertThat(count).isEqualTo(5);
  }

  @Test
  void processForCount_WithSomeFailures_ReturnsSuccessCount() {
    // Arrange
    SimpleMeterRegistry partialMeterRegistry = new SimpleMeterRegistry();
    AsyncBatchProcessor<String, String> partialProcessor =
        new AsyncBatchProcessor<>(partialMeterRegistry, "partial-count");

    List<String> items = Arrays.asList("A", "B", "C", "D", "E");
    Function<String, CompletableFuture<String>> processorFunc =
        item -> {
          if (item.equals("B") || item.equals("D")) {
            return CompletableFuture.completedFuture(null); // Simulate failure
          }
          return CompletableFuture.completedFuture(item.toLowerCase());
        };

    // Act
    CompletableFuture<Integer> result = partialProcessor.processForCount(items, processorFunc);
    Integer count = result.join();

    // Assert
    assertThat(count).isEqualTo(3); // Only A, C, E succeeded
  }

  @Test
  void processAsync_RecordsSuccessMetrics() {
    // Arrange
    SimpleMeterRegistry metricsMeterRegistry = new SimpleMeterRegistry();
    AsyncBatchProcessor<String, String> metricsProcessor =
        new AsyncBatchProcessor<>(metricsMeterRegistry, "metrics-test");

    List<String> items = Arrays.asList("A", "B", "C");
    Function<String, CompletableFuture<String>> processorFunc =
        item -> CompletableFuture.completedFuture(item.toLowerCase());

    // Act
    CompletableFuture<List<String>> result = metricsProcessor.processAsync(items, processorFunc);
    result.join();

    // Assert - verify success metrics were recorded
    assertThat(metricsMeterRegistry.get("async.batch.metrics-test.success").counter().count())
        .isEqualTo(3.0);
  }

  @Test
  void processAsync_WithPartialFailures_RecordsFailureMetrics() {
    // Arrange
    SimpleMeterRegistry failureMeterRegistry = new SimpleMeterRegistry();
    AsyncBatchProcessor<String, String> failureProcessor =
        new AsyncBatchProcessor<>(failureMeterRegistry, "failure-metrics");

    List<String> items = Arrays.asList("A", "B", "C", "D", "E");
    Function<String, CompletableFuture<String>> processorFunc =
        item -> {
          if (item.equals("B") || item.equals("D")) {
            return CompletableFuture.completedFuture(null); // Simulate failure
          }
          return CompletableFuture.completedFuture(item.toLowerCase());
        };

    // Act
    CompletableFuture<List<String>> result = failureProcessor.processAsync(items, processorFunc);
    result.join();

    // Assert - verify both success and failure metrics
    assertThat(failureMeterRegistry.get("async.batch.failure-metrics.success").counter().count())
        .isEqualTo(3.0);
    assertThat(failureMeterRegistry.get("async.batch.failure-metrics.failure").counter().count())
        .isEqualTo(2.0);
  }

  @Test
  void processAsync_WithAllFailures_RecordsOnlyFailureMetrics() {
    // Arrange
    SimpleMeterRegistry allFailMeterRegistry = new SimpleMeterRegistry();
    AsyncBatchProcessor<String, String> allFailProcessor =
        new AsyncBatchProcessor<>(allFailMeterRegistry, "all-fail");

    List<String> items = Arrays.asList("A", "B", "C");
    Function<String, CompletableFuture<String>> processorFunc =
        item -> CompletableFuture.completedFuture(null); // All fail

    // Act
    CompletableFuture<List<String>> result = allFailProcessor.processAsync(items, processorFunc);
    result.join();

    // Assert
    assertThat(allFailMeterRegistry.get("async.batch.all-fail.success").counter().count())
        .isEqualTo(0.0);
    assertThat(allFailMeterRegistry.get("async.batch.all-fail.failure").counter().count())
        .isEqualTo(3.0);
  }

  @Test
  void processAsync_RecordsGaugeForBatchSize() {
    // Arrange
    SimpleMeterRegistry gaugeMeterRegistry = new SimpleMeterRegistry();
    AsyncBatchProcessor<String, String> gaugeProcessor =
        new AsyncBatchProcessor<>(gaugeMeterRegistry, "gauge-test");

    List<String> items = Arrays.asList("A", "B", "C", "D", "E");
    Function<String, CompletableFuture<String>> processorFunc =
        item -> CompletableFuture.completedFuture(item.toLowerCase());

    // Act
    CompletableFuture<List<String>> result = gaugeProcessor.processAsync(items, processorFunc);
    result.join();

    // Assert - verify gauge was recorded
    assertThat(gaugeMeterRegistry.find("async.batch.gauge-test.size").gauge()).isNotNull();
  }
}

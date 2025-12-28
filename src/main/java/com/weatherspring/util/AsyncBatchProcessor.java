package com.weatherspring.util;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

/**
 * Generic utility for processing large lists asynchronously in batches.
 *
 * <p>This class encapsulates the common pattern of:
 *
 * <ul>
 *   <li>Partitioning input into batches
 *   <li>Processing each batch concurrently
 *   <li>Aggregating results
 *   <li>Tracking metrics
 * </ul>
 *
 * @param <T> the input type
 * @param <R> the result type
 */
@Slf4j
public class AsyncBatchProcessor<T, R> {

  private final MeterRegistry meterRegistry;
  private final String operationName;
  private final Timer timer;
  private final Counter successCounter;
  private final Counter failureCounter;

  /**
   * Creates a new async batch processor with metric tracking.
   *
   * @param meterRegistry the meter registry for metrics
   * @param operationName the name of the operation (used in metrics)
   */
  public AsyncBatchProcessor(MeterRegistry meterRegistry, String operationName) {
    this.meterRegistry = meterRegistry;
    this.operationName = operationName;

    // Initialize metrics
    this.timer =
        Timer.builder("async.batch." + operationName + ".duration")
            .description("Duration of " + operationName + " batch operations")
            .register(meterRegistry);

    this.successCounter =
        Counter.builder("async.batch." + operationName + ".success")
            .description("Number of successful " + operationName + " operations")
            .register(meterRegistry);

    this.failureCounter =
        Counter.builder("async.batch." + operationName + ".failure")
            .description("Number of failed " + operationName + " operations")
            .register(meterRegistry);
  }

  /**
   * Processes a list of items in batches asynchronously.
   *
   * @param items the items to process
   * @param processor function that processes a single item and returns CompletableFuture
   * @return CompletableFuture containing list of successfully processed results
   */
  public CompletableFuture<List<R>> processAsync(
      List<T> items, Function<T, CompletableFuture<R>> processor) {
    return CompletableFuture.supplyAsync(
        () ->
            timer.record(
                () -> {
                  log.info("Starting async {} for {} items", operationName, items.size());

                  // Record batch size as gauge
                  meterRegistry.gauge("async.batch." + operationName + ".size", items.size());

                  // Partition into batches
                  List<List<T>> batches = BatchingUtils.partition(items);
                  int batchCount = batches.size();

                  if (batchCount > 1) {
                    log.info(
                        "Processing {} items in {} batches of up to {} items",
                        items.size(),
                        batchCount,
                        BatchingUtils.DEFAULT_BATCH_SIZE);
                  }

                  // Process each batch concurrently
                  List<CompletableFuture<List<R>>> batchFutures =
                      batches.stream().map(batch -> processBatch(batch, processor)).toList();

                  // Wait for all batches to complete
                  CompletableFuture<Void> allBatches =
                      CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]));

                  // Aggregate results
                  List<R> results =
                      allBatches
                          .thenApply(
                              v ->
                                  batchFutures.stream()
                                      .map(CompletableFuture::join)
                                      .flatMap(List::stream)
                                      .toList())
                          .join();

                  // Track metrics
                  long successCount = results.size();
                  long failureCount = items.size() - successCount;
                  successCounter.increment(successCount);
                  if (failureCount > 0) {
                    failureCounter.increment(failureCount);
                  }

                  log.info(
                      "Async {} completed: {}/{} successful",
                      operationName,
                      successCount,
                      items.size());
                  return results;
                }));
  }

  /**
   * Processes a single batch concurrently.
   *
   * @param batch the batch of items
   * @param processor function to process each item
   * @return CompletableFuture containing list of results from this batch
   */
  private CompletableFuture<List<R>> processBatch(
      List<T> batch, Function<T, CompletableFuture<R>> processor) {
    List<CompletableFuture<R>> futures = batch.stream().map(processor).toList();

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
   * Processes items and returns count of successes.
   *
   * <p>Useful when you don't need the actual results, just success count.
   *
   * @param items the items to process
   * @param processor function that processes a single item
   * @return CompletableFuture containing count of successful operations
   */
  public CompletableFuture<Integer> processForCount(
      List<T> items, Function<T, CompletableFuture<R>> processor) {
    return processAsync(items, processor).thenApply(List::size);
  }
}

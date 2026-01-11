package com.weatherspring.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for batching operations to prevent memory exhaustion.
 *
 * <p>Provides methods to partition large collections into smaller batches for processing. This is
 * essential for bulk operations to prevent OutOfMemoryError when dealing with large datasets.
 *
 * @since 1.0
 */
public final class BatchingUtils {

  /**
   * Default batch size for bulk operations.
   *
   * <p>Set to 50 based on the following considerations:
   *
   * <ul>
   *   <li>Balance between API rate limiting (WeatherAPI.com allows 1M calls/month)
   *   <li>Memory usage per batch: ~50 weather records ≈ 10-20KB
   *   <li>Database batch insert optimal performance range (20-100 records)
   *   <li>Virtual thread overhead acceptable for this size
   * </ul>
   *
   * <p><strong>Tuning:</strong> Applications can override by calling {@link #partition(List, int)}
   * with a custom batch size. For heavy loads, consider reducing to 20-30. For lighter loads with
   * fewer API calls, increase to 80-100.
   */
  public static final int DEFAULT_BATCH_SIZE = 50;

  /**
   * Maximum recommended batch size to prevent memory pressure.
   *
   * <p>Set to 100 based on:
   *
   * <ul>
   *   <li>JVM heap analysis: 100 records ≈ 40KB max per batch
   *   <li>Database connection pool limits (default: 10 connections)
   *   <li>API timeout considerations (larger batches = longer processing time)
   * </ul>
   *
   * <p><strong>Note:</strong> Exceeding this limit will throw {@link IllegalArgumentException} via
   * {@link #validateBatchSize(int)}. If you need larger batches, consider processing in multiple
   * stages or increasing JVM heap size.
   */
  public static final int MAX_BATCH_SIZE = 100;

  private BatchingUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Partitions a list into smaller batches of the specified size.
   *
   * <p>The last batch may contain fewer elements than the batch size if the list size is not evenly
   * divisible by the batch size.
   *
   * <p>Example:
   *
   * <pre>{@code
   * List<String> locations = Arrays.asList("A", "B", "C", "D", "E");
   * List<List<String>> batches = partition(locations, 2);
   * // Result: [[A, B], [C, D], [E]]
   * }</pre>
   *
   * @param <T> the type of elements in the list
   * @param list the list to partition
   * @param batchSize the maximum size of each batch
   * @return list of batches, each containing up to batchSize elements
   * @throws IllegalArgumentException if batchSize is less than 1
   */
  public static <T> List<List<T>> partition(List<T> list, int batchSize) {
    if (batchSize < 1) {
      throw new IllegalArgumentException("Batch size must be at least 1");
    }

    List<List<T>> batches = new ArrayList<>();
    int totalSize = list.size();

    for (int i = 0; i < totalSize; i += batchSize) {
      int end = Math.min(i + batchSize, totalSize);
      batches.add(list.subList(i, end));
    }

    return batches;
  }

  /**
   * Partitions a list into batches using the default batch size.
   *
   * <p>Uses {@link #DEFAULT_BATCH_SIZE} as the batch size.
   *
   * @param <T> the type of elements in the list
   * @param list the list to partition
   * @return list of batches, each containing up to DEFAULT_BATCH_SIZE elements
   * @see #partition(List, int)
   */
  public static <T> List<List<T>> partition(List<T> list) {
    return partition(list, DEFAULT_BATCH_SIZE);
  }

  /**
   * Calculates the number of batches required for the given list size.
   *
   * @param totalSize the total number of elements
   * @param batchSize the size of each batch
   * @return the number of batches required
   * @throws IllegalArgumentException if batchSize is less than 1
   */
  public static int calculateBatchCount(int totalSize, int batchSize) {
    if (batchSize < 1) {
      throw new IllegalArgumentException("Batch size must be at least 1");
    }
    return (int) Math.ceil((double) totalSize / batchSize);
  }

  /**
   * Validates that the batch size is within acceptable limits.
   *
   * @param batchSize the batch size to validate
   * @throws IllegalArgumentException if batch size is invalid
   */
  public static void validateBatchSize(int batchSize) {
    if (batchSize < 1) {
      throw new IllegalArgumentException("Batch size must be at least 1");
    }
    if (batchSize > MAX_BATCH_SIZE) {
      throw new IllegalArgumentException(
          String.format(
              "Batch size %d exceeds maximum allowed size of %d", batchSize, MAX_BATCH_SIZE));
    }
  }
}

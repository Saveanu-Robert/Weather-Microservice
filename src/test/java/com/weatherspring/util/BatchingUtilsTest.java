package com.weatherspring.util;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Unit tests for BatchingUtils. */
class BatchingUtilsTest {

  @Test
  void constructor_ThrowsUnsupportedOperationException() {
    // Act & Assert
    assertThatThrownBy(
            () -> {
              var constructor = BatchingUtils.class.getDeclaredConstructor();
              constructor.setAccessible(true);
              constructor.newInstance();
            })
        .hasCauseInstanceOf(UnsupportedOperationException.class)
        .getRootCause()
        .hasMessageContaining("Utility class cannot be instantiated");
  }

  @Test
  void partition_WithCustomBatchSize_PartitionsCorrectly() {
    // Arrange
    List<String> items = Arrays.asList("A", "B", "C", "D", "E", "F", "G");

    // Act
    List<List<String>> batches = BatchingUtils.partition(items, 3);

    // Assert
    assertThat(batches).hasSize(3);
    assertThat(batches.get(0)).containsExactly("A", "B", "C");
    assertThat(batches.get(1)).containsExactly("D", "E", "F");
    assertThat(batches.get(2)).containsExactly("G");
  }

  @Test
  void partition_WithInvalidBatchSize_ThrowsException() {
    // Arrange
    List<String> items = Arrays.asList("A", "B", "C");

    // Act & Assert
    assertThatThrownBy(() -> BatchingUtils.partition(items, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Batch size must be at least 1");

    assertThatThrownBy(() -> BatchingUtils.partition(items, -1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Batch size must be at least 1");
  }

  @Test
  void partition_WithDefaultBatchSize_UsesDefault() {
    // Arrange
    List<Integer> items = java.util.stream.IntStream.range(0, 120).boxed().toList();

    // Act
    List<List<Integer>> batches = BatchingUtils.partition(items);

    // Assert - with 120 items and default batch size of 50, should have 3 batches
    assertThat(batches).hasSize(3);
    assertThat(batches.get(0)).hasSize(50);
    assertThat(batches.get(1)).hasSize(50);
    assertThat(batches.get(2)).hasSize(20);
  }

  @Test
  void partition_WithEmptyList_ReturnsEmptyList() {
    // Arrange
    List<String> items = Arrays.asList();

    // Act
    List<List<String>> batches = BatchingUtils.partition(items, 10);

    // Assert
    assertThat(batches).isEmpty();
  }

  @Test
  void partition_WithSingleItem_ReturnsSingleBatch() {
    // Arrange
    List<String> items = Arrays.asList("single");

    // Act
    List<List<String>> batches = BatchingUtils.partition(items, 10);

    // Assert
    assertThat(batches).hasSize(1);
    assertThat(batches.get(0)).containsExactly("single");
  }

  @Test
  void partition_WithBatchSizeEqualToListSize_ReturnsSingleBatch() {
    // Arrange
    List<String> items = Arrays.asList("A", "B", "C", "D", "E");

    // Act
    List<List<String>> batches = BatchingUtils.partition(items, 5);

    // Assert
    assertThat(batches).hasSize(1);
    assertThat(batches.get(0)).containsExactly("A", "B", "C", "D", "E");
  }

  @Test
  void partition_WithBatchSizeLargerThanList_ReturnsSingleBatch() {
    // Arrange
    List<String> items = Arrays.asList("A", "B", "C");

    // Act
    List<List<String>> batches = BatchingUtils.partition(items, 100);

    // Assert
    assertThat(batches).hasSize(1);
    assertThat(batches.get(0)).containsExactly("A", "B", "C");
  }

  @Test
  void calculateBatchCount_WithValidInputs_ReturnsCorrectCount() {
    // Act & Assert
    assertThat(BatchingUtils.calculateBatchCount(100, 50)).isEqualTo(2);
    assertThat(BatchingUtils.calculateBatchCount(120, 50)).isEqualTo(3);
    assertThat(BatchingUtils.calculateBatchCount(50, 50)).isEqualTo(1);
    assertThat(BatchingUtils.calculateBatchCount(51, 50)).isEqualTo(2);
    assertThat(BatchingUtils.calculateBatchCount(0, 50)).isEqualTo(0);
  }

  @Test
  void calculateBatchCount_WithInvalidBatchSize_ThrowsException() {
    // Act & Assert
    assertThatThrownBy(() -> BatchingUtils.calculateBatchCount(100, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Batch size must be at least 1");

    assertThatThrownBy(() -> BatchingUtils.calculateBatchCount(100, -5))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Batch size must be at least 1");
  }

  @Test
  void validateBatchSize_WithValidSize_DoesNotThrow() {
    // Act & Assert - should not throw
    assertThatCode(() -> BatchingUtils.validateBatchSize(1)).doesNotThrowAnyException();
    assertThatCode(() -> BatchingUtils.validateBatchSize(50)).doesNotThrowAnyException();
    assertThatCode(() -> BatchingUtils.validateBatchSize(100)).doesNotThrowAnyException();
  }

  @Test
  void validateBatchSize_WithTooSmallSize_ThrowsException() {
    // Act & Assert
    assertThatThrownBy(() -> BatchingUtils.validateBatchSize(0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Batch size must be at least 1");

    assertThatThrownBy(() -> BatchingUtils.validateBatchSize(-10))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Batch size must be at least 1");
  }

  @Test
  void validateBatchSize_WithTooLargeSize_ThrowsException() {
    // Act & Assert
    assertThatThrownBy(() -> BatchingUtils.validateBatchSize(101))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("exceeds maximum allowed size of 100");

    assertThatThrownBy(() -> BatchingUtils.validateBatchSize(1000))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("exceeds maximum allowed size of 100");
  }

  @Test
  void validateBatchSize_WithMaxSize_DoesNotThrow() {
    // Act & Assert - exactly at MAX_BATCH_SIZE should be valid
    assertThatCode(() -> BatchingUtils.validateBatchSize(BatchingUtils.MAX_BATCH_SIZE))
        .doesNotThrowAnyException();
  }

  @Test
  void validateBatchSize_WithJustOverMaxSize_ThrowsException() {
    // Act & Assert
    assertThatThrownBy(() -> BatchingUtils.validateBatchSize(BatchingUtils.MAX_BATCH_SIZE + 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("exceeds maximum allowed size");
  }

  @Test
  void partition_WithBatchSizeOne_CreatesIndividualBatches() {
    // Arrange
    List<String> items = Arrays.asList("A", "B", "C");

    // Act
    List<List<String>> batches = BatchingUtils.partition(items, 1);

    // Assert
    assertThat(batches).hasSize(3);
    assertThat(batches.get(0)).containsExactly("A");
    assertThat(batches.get(1)).containsExactly("B");
    assertThat(batches.get(2)).containsExactly("C");
  }

  @Test
  void calculateBatchCount_WithBoundaryValues_HandlesCorrectly() {
    // Test edge cases for ceiling calculation
    assertThat(BatchingUtils.calculateBatchCount(1, 1)).isEqualTo(1);
    assertThat(BatchingUtils.calculateBatchCount(1, 10)).isEqualTo(1);
    assertThat(BatchingUtils.calculateBatchCount(10, 1)).isEqualTo(10);
    assertThat(BatchingUtils.calculateBatchCount(99, 10)).isEqualTo(10);
    assertThat(BatchingUtils.calculateBatchCount(100, 10)).isEqualTo(10);
    assertThat(BatchingUtils.calculateBatchCount(101, 10)).isEqualTo(11);
  }
}

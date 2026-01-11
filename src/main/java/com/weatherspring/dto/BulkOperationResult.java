package com.weatherspring.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Result of a bulk operation containing successful and failed operation counts.
 *
 * <p>This DTO provides summary information for batch/bulk operations, including:
 *
 * <ul>
 *   <li>Number of successful operations
 *   <li>Number of failed operations
 *   <li>Total number of operations attempted
 *   <li>Success rate calculation
 * </ul>
 *
 * @param successCount number of successful operations
 * @param failureCount number of failed operations
 * @param totalCount total number of operations attempted
 */
@Schema(description = "Result summary of a bulk operation")
public record BulkOperationResult(
    @Schema(description = "Number of successful operations", example = "45") int successCount,
    @Schema(description = "Number of failed operations", example = "5") int failureCount,
    @Schema(description = "Total number of operations attempted", example = "50") int totalCount) {
  /**
   * Checks if all operations were successful.
   *
   * @return true if no failures occurred
   */
  public boolean allSuccessful() {
    return failureCount == 0;
  }

  /**
   * Calculates the success rate as a decimal between 0.0 and 1.0.
   *
   * @return success rate (0.0 to 1.0), or 0.0 if no operations attempted
   */
  public double successRate() {
    return totalCount > 0 ? (double) successCount / totalCount : 0.0;
  }

  /**
   * Calculates the success rate as a percentage between 0 and 100.
   *
   * @return success rate as a percentage (0.0 to 100.0)
   */
  public double successPercentage() {
    return successRate() * 100.0;
  }
}

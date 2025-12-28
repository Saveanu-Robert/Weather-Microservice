package com.weatherspring.validation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Validator for date range queries to prevent unbounded or excessive queries.
 *
 * <p>Enforces maximum date range limits to protect database performance and prevent out-of-memory
 * errors from loading too much data.
 *
 * <p><strong>Default Limits:</strong>
 *
 * <ul>
 *   <li>Weather history: 90 days maximum (configurable)
 *   <li>Forecast range: 365 days maximum (configurable)
 * </ul>
 *
 * @since 1.0
 */
public final class DateRangeValidator {

  /**
   * Maximum number of days allowed for weather history queries. Prevents excessive database loads
   * and memory consumption.
   */
  public static final long MAX_WEATHER_HISTORY_DAYS = 90;

  /**
   * Maximum number of days allowed for forecast queries. Prevents excessive database loads and
   * memory consumption.
   */
  public static final long MAX_FORECAST_RANGE_DAYS = 365;

  private DateRangeValidator() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Validates that a date range for weather history is within acceptable limits.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @throws IllegalArgumentException if the range is invalid or exceeds limits
   */
  public static void validateWeatherHistoryRange(LocalDateTime startDate, LocalDateTime endDate) {
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start date and end date cannot be null");
    }

    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date must be before or equal to end date");
    }

    long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
    if (daysBetween > MAX_WEATHER_HISTORY_DAYS) {
      throw new IllegalArgumentException(
          String.format(
              "Date range exceeds maximum of %d days (requested: %d days). "
                  + "Please narrow your query range.",
              MAX_WEATHER_HISTORY_DAYS, daysBetween));
    }

    // Prevent querying far into the past (e.g., more than 1 year ago)
    LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
    if (startDate.isBefore(oneYearAgo)) {
      throw new IllegalArgumentException(
          "Start date cannot be more than 1 year in the past. "
              + "Historical data older than 1 year is archived.");
    }
  }

  /**
   * Validates that a date range for forecasts is within acceptable limits.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @throws IllegalArgumentException if the range is invalid or exceeds limits
   */
  public static void validateForecastRange(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start date and end date cannot be null");
    }

    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date must be before or equal to end date");
    }

    long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
    if (daysBetween > MAX_FORECAST_RANGE_DAYS) {
      throw new IllegalArgumentException(
          String.format(
              "Date range exceeds maximum of %d days (requested: %d days). "
                  + "Please narrow your query range.",
              MAX_FORECAST_RANGE_DAYS, daysBetween));
    }

    // Prevent querying forecasts too far in the past
    LocalDate today = LocalDate.now();
    if (endDate.isBefore(today.minusDays(30))) {
      throw new IllegalArgumentException(
          "End date cannot be more than 30 days in the past. "
              + "Use weather history API for past data.");
    }
  }
}

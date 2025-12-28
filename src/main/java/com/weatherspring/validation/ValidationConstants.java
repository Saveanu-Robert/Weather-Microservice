package com.weatherspring.validation;

/**
 * Centralized validation constants for the application.
 *
 * <p>This class provides a single source of truth for all validation-related constants used
 * throughout the application, improving maintainability and consistency.
 *
 * <p>Constants are organized by domain:
 *
 * <ul>
 *   <li>Location validation - name length, geographic bounds
 *   <li>Forecast validation - day limits
 * </ul>
 */
public final class ValidationConstants {

  private ValidationConstants() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  // Location Name Validation
  public static final int LOCATION_NAME_MIN_LENGTH = 2;
  public static final int LOCATION_NAME_MAX_LENGTH = 100;
  public static final String LOCATION_NAME_SIZE_MESSAGE =
      "Name must be between "
          + LOCATION_NAME_MIN_LENGTH
          + " and "
          + LOCATION_NAME_MAX_LENGTH
          + " characters";

  // Country Name Validation
  public static final int COUNTRY_NAME_MIN_LENGTH = 2;
  public static final int COUNTRY_NAME_MAX_LENGTH = 100;
  public static final String COUNTRY_NAME_SIZE_MESSAGE =
      "Country must be between "
          + COUNTRY_NAME_MIN_LENGTH
          + " and "
          + COUNTRY_NAME_MAX_LENGTH
          + " characters";

  // Latitude Validation
  public static final long LATITUDE_MIN = -90;
  public static final long LATITUDE_MAX = 90;
  public static final String LATITUDE_RANGE_MESSAGE =
      "Latitude must be between " + LATITUDE_MIN + " and " + LATITUDE_MAX;

  // Longitude Validation
  public static final long LONGITUDE_MIN = -180;
  public static final long LONGITUDE_MAX = 180;
  public static final String LONGITUDE_RANGE_MESSAGE =
      "Longitude must be between " + LONGITUDE_MIN + " and " + LONGITUDE_MAX;

  // Forecast Days Validation
  public static final int FORECAST_DAYS_MIN = 1;
  public static final int FORECAST_DAYS_MAX = 14;
  public static final String FORECAST_DAYS_RANGE_MESSAGE =
      "Forecast days must be between " + FORECAST_DAYS_MIN + " and " + FORECAST_DAYS_MAX;
}

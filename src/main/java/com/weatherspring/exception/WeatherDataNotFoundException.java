package com.weatherspring.exception;

/**
 * Exception thrown when requested weather data is not found.
 *
 * <p>This is a final subclass of the sealed {@link WeatherServiceException} hierarchy, indicating
 * that historical weather data is not available in the database.
 */
public final class WeatherDataNotFoundException extends WeatherServiceException {

  /**
   * Constructs a new weather data not found exception with an error message.
   *
   * @param message the error message
   */
  public WeatherDataNotFoundException(String message) {
    super(message);
  }

  @Override
  public String getCategory() {
    return "WEATHER_DATA_NOT_FOUND";
  }
}

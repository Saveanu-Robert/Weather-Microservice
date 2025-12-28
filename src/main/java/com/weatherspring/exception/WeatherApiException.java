package com.weatherspring.exception;

/**
 * Exception thrown when communication with the external Weather API fails.
 *
 * <p>This is a final subclass of the sealed {@link WeatherServiceException} hierarchy, indicating
 * an external API communication failure. This exception is configured as a failure in the
 * Resilience4j circuit breaker.
 */
public final class WeatherApiException extends WeatherServiceException {

  /**
   * Constructs a new weather API exception with an error message.
   *
   * @param message the error message
   */
  public WeatherApiException(String message) {
    super(message);
  }

  /**
   * Constructs a new weather API exception with a message and cause.
   *
   * @param message the error message
   * @param cause the underlying cause of the exception
   */
  public WeatherApiException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public String getCategory() {
    return "WEATHER_API_ERROR";
  }
}

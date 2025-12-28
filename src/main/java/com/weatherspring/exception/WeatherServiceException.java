package com.weatherspring.exception;

/**
 * Sealed base exception for all weather service domain exceptions.
 *
 * <p>This sealed hierarchy (Java 17+) provides compile-time exhaustiveness checking when handling
 * exceptions, ensuring all possible exception types are considered.
 *
 * <p>Permitted subtypes:
 *
 * <ul>
 *   <li>{@link LocationNotFoundException} - Location not found in database
 *   <li>{@link WeatherDataNotFoundException} - Weather data not available
 *   <li>{@link WeatherApiException} - External API communication failure
 * </ul>
 *
 * @see LocationNotFoundException
 * @see WeatherDataNotFoundException
 * @see WeatherApiException
 */
public abstract sealed class WeatherServiceException extends RuntimeException
    permits LocationNotFoundException, WeatherDataNotFoundException, WeatherApiException {

  /**
   * Constructs a new weather service exception with the specified detail message.
   *
   * @param message the detail message
   */
  protected WeatherServiceException(String message) {
    super(message);
  }

  /**
   * Constructs a new weather service exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause of this exception
   */
  protected WeatherServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Returns the exception category for logging and monitoring purposes.
   *
   * @return the exception category
   */
  public abstract String getCategory();
}

package com.weatherspring.exception;

/**
 * Exception thrown when a requested location is not found in the database.
 *
 * <p>This is a final subclass of the sealed {@link WeatherServiceException} hierarchy, indicating a
 * location lookup failure.
 */
public final class LocationNotFoundException extends WeatherServiceException {

  /**
   * Constructs a new location not found exception with a location ID.
   *
   * @param locationId the ID of the location that was not found
   */
  public LocationNotFoundException(Long locationId) {
    super("Location not found with ID: " + locationId);
  }

  /**
   * Constructs a new location not found exception with a custom message.
   *
   * @param message the error message
   */
  public LocationNotFoundException(String message) {
    super(message);
  }

  @Override
  public String getCategory() {
    return "LOCATION_NOT_FOUND";
  }
}

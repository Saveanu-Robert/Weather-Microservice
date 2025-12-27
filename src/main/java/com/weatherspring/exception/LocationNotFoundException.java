package com.weatherspring.exception;

/**
 * Exception thrown when a requested location is not found in the database.
 *
 * <p>This is a final subclass of the sealed {@link WeatherServiceException} hierarchy,
 * indicating a location lookup failure.</p>
 */
public final class LocationNotFoundException extends WeatherServiceException {

    public LocationNotFoundException(Long locationId) {
        super("Location not found with ID: " + locationId);
    }

    public LocationNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getCategory() {
        return "LOCATION_NOT_FOUND";
    }
}

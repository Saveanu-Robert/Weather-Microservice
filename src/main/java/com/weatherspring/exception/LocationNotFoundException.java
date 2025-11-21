package com.weatherspring.exception;

/**
 * Exception thrown when a requested location is not found.
 */
public class LocationNotFoundException extends RuntimeException {

    public LocationNotFoundException(Long locationId) {
        super("Location not found with ID: " + locationId);
    }

    public LocationNotFoundException(String message) {
        super(message);
    }
}

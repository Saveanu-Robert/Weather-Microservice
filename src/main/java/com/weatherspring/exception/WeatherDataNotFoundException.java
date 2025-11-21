package com.weatherspring.exception;

/**
 * Exception thrown when requested weather data is not found.
 */
public class WeatherDataNotFoundException extends RuntimeException {

    public WeatherDataNotFoundException(String message) {
        super(message);
    }
}

package com.weatherspring.exception;

/**
 * Exception thrown when communication with the external Weather API fails.
 *
 * <p>This is a final subclass of the sealed {@link WeatherServiceException} hierarchy,
 * indicating an external API communication failure. This exception is configured
 * as a failure in the Resilience4j circuit breaker.</p>
 */
public final class WeatherApiException extends WeatherServiceException {

    public WeatherApiException(String message) {
        super(message);
    }

    public WeatherApiException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getCategory() {
        return "WEATHER_API_ERROR";
    }
}

package com.weatherspring.client;

import com.weatherspring.dto.external.ForecastApiResponse;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.exception.WeatherApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Client for interacting with WeatherAPI.com external service.
 *
 * <p>Provides methods to fetch current weather and forecast data from the external API.
 * Includes error handling and logging for API calls.</p>
 */
@Component
public class WeatherApiClient {

    private static final Logger logger = LoggerFactory.getLogger(WeatherApiClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public WeatherApiClient(
            RestTemplate restTemplate,
            @Value("${weather.api.base-url}") String baseUrl,
            @Value("${weather.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    /**
     * Fetches current weather data for a specific location.
     *
     * @param location the location name (city, coordinates, etc.)
     * @return current weather data from the API
     * @throws WeatherApiException if the API call fails
     */
    public WeatherApiResponse getCurrentWeather(String location) {
        logger.debug("Fetching current weather for location: {}", location);

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/current.json")
                .queryParam("key", apiKey)
                .queryParam("q", location)
                .queryParam("aqi", "no")
                .toUriString();

        try {
            WeatherApiResponse response = restTemplate.getForObject(url, WeatherApiResponse.class);

            if (response == null) {
                logger.error("Received null response from WeatherAPI for location: {}", location);
                throw new WeatherApiException("Failed to fetch weather data: empty response");
            }

            logger.info("Successfully fetched current weather for: {}", location);
            return response;

        } catch (HttpClientErrorException e) {
            logger.error("Client error fetching weather for location {}: {} - {}",
                        location, e.getStatusCode(), e.getResponseBodyAsString());
            throw new WeatherApiException("Invalid location or API request: " + location, e);

        } catch (HttpServerErrorException e) {
            logger.error("Server error from WeatherAPI: {} - {}",
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new WeatherApiException("Weather API server error", e);

        } catch (Exception e) {
            logger.error("Unexpected error fetching weather for location: {}", location, e);
            throw new WeatherApiException("Failed to fetch weather data: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches weather forecast for a specific location.
     *
     * @param location the location name (city, coordinates, etc.)
     * @param days number of forecast days (1-14)
     * @return forecast data from the API
     * @throws WeatherApiException if the API call fails
     */
    public ForecastApiResponse getForecast(String location, int days) {
        logger.debug("Fetching {}-day forecast for location: {}", days, location);

        if (days < 1 || days > 14) {
            throw new IllegalArgumentException("Forecast days must be between 1 and 14");
        }

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/forecast.json")
                .queryParam("key", apiKey)
                .queryParam("q", location)
                .queryParam("days", days)
                .queryParam("aqi", "no")
                .queryParam("alerts", "no")
                .toUriString();

        try {
            ForecastApiResponse response = restTemplate.getForObject(url, ForecastApiResponse.class);

            if (response == null) {
                logger.error("Received null forecast response from WeatherAPI for location: {}", location);
                throw new WeatherApiException("Failed to fetch forecast data: empty response");
            }

            logger.info("Successfully fetched {}-day forecast for: {}", days, location);
            return response;

        } catch (HttpClientErrorException e) {
            logger.error("Client error fetching forecast for location {}: {} - {}",
                        location, e.getStatusCode(), e.getResponseBodyAsString());
            throw new WeatherApiException("Invalid location or API request: " + location, e);

        } catch (HttpServerErrorException e) {
            logger.error("Server error from WeatherAPI: {} - {}",
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new WeatherApiException("Weather API server error", e);

        } catch (Exception e) {
            logger.error("Unexpected error fetching forecast for location: {}", location, e);
            throw new WeatherApiException("Failed to fetch forecast data: " + e.getMessage(), e);
        }
    }

    /**
     * Searches for locations matching the query.
     *
     * @param query the search query
     * @return location information from the API
     * @throws WeatherApiException if the API call fails
     */
    public WeatherApiResponse searchLocation(String query) {
        logger.debug("Searching for location: {}", query);
        return getCurrentWeather(query);
    }
}

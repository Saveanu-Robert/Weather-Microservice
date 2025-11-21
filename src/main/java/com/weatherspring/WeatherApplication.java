package com.weatherspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main application class for the Weather Microservice.
 *
 * <p>This microservice provides weather data and forecasts by integrating
 * with external weather APIs and maintaining historical weather records.</p>
 *
 * <p>Features:</p>
 * <ul>
 *     <li>Current weather data retrieval</li>
 *     <li>Weather forecasts (up to 14 days)</li>
 *     <li>Historical weather data storage</li>
 *     <li>Location management</li>
 *     <li>Caching for improved performance</li>
 * </ul>
 *
 * @author Weather Service Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
public class WeatherApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherApplication.class, args);
    }
}

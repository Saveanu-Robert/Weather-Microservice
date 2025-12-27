package com.weatherspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main entry point for the Weather Service application.
 *
 * This service fetches weather data from external APIs, stores historical records,
 * and provides forecasts for up to 14 days. It uses caching to improve performance
 * and reduce API calls.
 */
@SpringBootApplication
@EnableCaching
public class WeatherApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherApplication.class, args);
    }
}

package com.weatherspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main entry point for the Weather Service application.
 *
 * <p>This service fetches weather data from external APIs, stores historical records, and provides
 * forecasts for up to 14 days. It uses caching to improve performance and reduce API calls.
 */
@SpringBootApplication
@EnableCaching
public class WeatherApplication {

  /**
   * Main method to start the Spring Boot application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(WeatherApplication.class);

    // Register shutdown hook for graceful cleanup
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("\nShutting down WeatherSpring gracefully...");
                }));

    app.run(args);
  }
}

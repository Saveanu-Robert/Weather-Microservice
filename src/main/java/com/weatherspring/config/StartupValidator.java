package com.weatherspring.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Validates critical application configuration on startup.
 *
 * <p>Performs fail-fast validation of required configuration properties
 * to ensure the application is properly configured before accepting requests.</p>
 */
@Component
public class StartupValidator {

    private static final Logger logger = LoggerFactory.getLogger(StartupValidator.class);

    @Value("${weather.api.key}")
    private String apiKey;

    /**
     * Validates configuration after application is fully started.
     *
     * @throws IllegalStateException if configuration is invalid
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        logger.info("Validating application configuration...");

        validateApiKey();

        logger.info("Application configuration validation completed successfully");
    }

    /**
     * Validates that the API key is properly configured.
     *
     * @throws IllegalStateException if API key is missing or invalid
     */
    private void validateApiKey() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            String message = "Weather API key is not configured. " +
                "Please set WEATHER_API_KEY environment variable or weather.api.key property.";
            logger.error(message);
            throw new IllegalStateException(message);
        }

        // Check for placeholder values that indicate the key hasn't been set
        if (apiKey.equalsIgnoreCase("your-api-key-here") ||
            apiKey.equalsIgnoreCase("demo-api-key") ||
            apiKey.equalsIgnoreCase("replace-me")) {
            String message = "Weather API key is set to a placeholder value: " + apiKey + ". " +
                "Please configure a valid API key from https://www.weatherapi.com/";
            logger.error(message);
            throw new IllegalStateException(message);
        }

        // Basic format validation (WeatherAPI keys are typically 32 characters)
        if (apiKey.length() < 20) {
            logger.warn("Weather API key seems unusually short ({}  characters). " +
                "Please verify it's a valid key.", apiKey.length());
        }

        logger.info("Weather API key validation passed");
    }
}

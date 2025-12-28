package com.weatherspring.config;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Validates required configuration properties at application startup.
 *
 * <p>This component ensures that all required configuration values are set before the application
 * starts accepting requests. If validation fails, the application will fail to start with a clear
 * error message.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ConfigurationValidator {

  @Value("${weather.api.key:}")
  private String weatherApiKey;

  @Value("${weather.api.base-url}")
  private String weatherApiBaseUrl;

  @Value("${spring.datasource.url:}")
  private String datasourceUrl;

  /**
   * Validates configuration at application startup.
   *
   * @throws IllegalStateException if required configuration is missing
   */
  @PostConstruct
  public void validateConfiguration() {
    log.info("Validating application configuration...");

    // Validate API key with enhanced checks
    validateApiKey();

    validateRequired(
        "weather.api.base-url", weatherApiBaseUrl, "Weather API base URL is required.");

    validateRequired(
        "spring.datasource.url",
        datasourceUrl,
        "Database URL is required. Set DATABASE_URL environment variable or configure spring.datasource.url.");

    log.info("Configuration validation successful");
  }

  /**
   * Validates the Weather API key with enhanced checks.
   *
   * <p>Performs multiple validations:
   *
   * <ul>
   *   <li>Checks if key is present and not blank
   *   <li>Detects common placeholder values
   *   <li>Validates minimum length (warns if < 20 characters)
   * </ul>
   *
   * @throws IllegalStateException if API key is missing, placeholder, or invalid
   */
  private void validateApiKey() {
    if (weatherApiKey == null || weatherApiKey.isBlank()) {
      String message =
          "Weather API key is not configured. "
              + "Please set WEATHER_API_KEY environment variable or weather.api.key property.";
      log.error(message);
      throw new IllegalStateException(message);
    }

    // Check for placeholder values that indicate the key hasn't been set
    if (weatherApiKey.equalsIgnoreCase("your-api-key-here")
        || weatherApiKey.equalsIgnoreCase("demo-api-key")
        || weatherApiKey.equalsIgnoreCase("replace-me")) {
      String message =
          "Weather API key is set to a placeholder value: "
              + weatherApiKey
              + ". "
              + "Please configure a valid API key from https://www.weatherapi.com/";
      log.error(message);
      throw new IllegalStateException(message);
    }

    // Basic format validation (WeatherAPI keys are typically 32 characters)
    if (weatherApiKey.length() < 20) {
      log.warn(
          "Weather API key seems unusually short ({} characters). "
              + "Please verify it's a valid key.",
          weatherApiKey.length());
    }

    log.debug("Weather API key validation passed");
  }

  /**
   * Validates that a required property is set.
   *
   * @param propertyName the name of the property
   * @param value the value of the property
   * @param message error message if validation fails
   * @throws IllegalStateException if value is null or blank
   */
  private void validateRequired(String propertyName, String value, String message) {
    if (value == null || value.isBlank()) {
      String errorMsg =
          String.format("Configuration validation failed for '%s': %s", propertyName, message);
      log.error(errorMsg);
      throw new IllegalStateException(errorMsg);
    }
    log.debug("Configuration property '{}' is set", propertyName);
  }
}

package com.weatherspring.controller;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weatherspring.dto.WeatherDto;
import com.weatherspring.service.CompositeWeatherService;
import com.weatherspring.service.CompositeWeatherService.CompleteLocationInfo;
import com.weatherspring.service.CompositeWeatherService.WeatherWithForecast;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for composite weather queries using structured concurrency.
 *
 * <p>Demonstrates Java 21 structured concurrency for efficient parallel data fetching with virtual
 * threads.
 */
@RestController
@RequestMapping("/api/composite")
@Tag(
    name = "Composite Weather API",
    description = "Composite queries using structured concurrency (Java 21)")
@Slf4j
@RequiredArgsConstructor
@Validated
public class CompositeWeatherController {

  private final CompositeWeatherService compositeWeatherService;

  /**
   * Fetches current weather and forecast concurrently.
   *
   * @param locationName the location name
   * @param days number of forecast days (default 3)
   * @param save whether to save to database (default false)
   * @return weather and forecast data
   */
  @Operation(
      summary = "Get weather and forecast concurrently",
      description =
          "Fetches current weather and forecast data in parallel using Java 21 structured concurrency")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully fetched weather and forecast"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "500", description = "External API error")
      })
  @GetMapping("/weather-and-forecast")
  public ResponseEntity<WeatherWithForecast> getWeatherAndForecast(
      @Parameter(description = "Location name", required = true, example = "London")
          @RequestParam
          @NotBlank
          String locationName,
      @Parameter(description = "Number of forecast days (1-14)", example = "3")
          @RequestParam(defaultValue = "3")
          @Min(1)
          @Max(14)
          int days,
      @Parameter(description = "Save data to database", example = "false")
          @RequestParam(defaultValue = "false")
          boolean save) {

    WeatherWithForecast result =
        compositeWeatherService.getWeatherAndForecast(locationName, days, save);
    return ResponseEntity.ok(result);
  }

  /**
   * Fetches current weather and forecast by location ID concurrently.
   *
   * @param locationId the location ID
   * @param days number of forecast days (default 3)
   * @param save whether to save to database (default false)
   * @return weather and forecast data
   */
  @Operation(
      summary = "Get weather and forecast by location ID",
      description =
          "Fetches weather and forecast for a saved location using structured concurrency")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetched data"),
        @ApiResponse(responseCode = "404", description = "Location not found"),
        @ApiResponse(responseCode = "500", description = "External API error")
      })
  @GetMapping("/weather-and-forecast/{locationId}")
  public ResponseEntity<WeatherWithForecast> getWeatherAndForecastByLocationId(
      @Parameter(description = "Location ID", required = true, example = "1")
          @PathVariable
          @NotNull
          @Positive
          Long locationId,
      @Parameter(description = "Number of forecast days (1-14)", example = "3")
          @RequestParam(defaultValue = "3")
          @Min(1)
          @Max(14)
          int days,
      @Parameter(description = "Save data to database", example = "false")
          @RequestParam(defaultValue = "false")
          boolean save) {

    WeatherWithForecast result =
        compositeWeatherService.getWeatherAndForecastByLocationId(locationId, days, save);
    return ResponseEntity.ok(result);
  }

  /**
   * Fetches complete location information including weather and forecast.
   *
   * @param locationId the location ID
   * @param days number of forecast days (default 3)
   * @param save whether to save weather data to database (default false)
   * @return complete location information
   */
  @Operation(
      summary = "Get complete location information",
      description =
          "Fetches location details, current weather, and forecast concurrently (3-way parallel fetch)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully fetched complete information"),
        @ApiResponse(responseCode = "404", description = "Location not found"),
        @ApiResponse(responseCode = "500", description = "External API error")
      })
  @GetMapping("/complete-info/{locationId}")
  public ResponseEntity<CompleteLocationInfo> getCompleteLocationInfo(
      @Parameter(description = "Location ID", required = true, example = "1")
          @PathVariable
          @NotNull
          @Positive
          Long locationId,
      @Parameter(description = "Number of forecast days (1-14)", example = "3")
          @RequestParam(defaultValue = "3")
          @Min(1)
          @Max(14)
          int days,
      @Parameter(description = "Save weather data to database", example = "false")
          @RequestParam(defaultValue = "false")
          boolean save) {

    CompleteLocationInfo result =
        compositeWeatherService.getCompleteLocationInfo(locationId, days, save);
    return ResponseEntity.ok(result);
  }

  /**
   * Fetches weather data for multiple locations concurrently.
   *
   * @param locations list of location names to fetch weather for
   * @param save whether to save data to database (default false)
   * @return list of weather data
   */
  @Operation(
      summary = "Get weather for multiple locations in parallel",
      description =
          "Fetches current weather for multiple locations concurrently using virtual threads")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully fetched weather for all locations"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "500", description = "External API error")
      })
  @GetMapping("/bulk-weather")
  public ResponseEntity<List<WeatherDto>> getBulkWeather(
      @Parameter(
              description = "Comma-separated list of location names",
              required = true,
              example = "London,Paris,New York")
          @RequestParam
          @NotEmpty
          @Size(max = 100, message = "Maximum 100 locations per request")
          List<String> locations,
      @Parameter(description = "Save data to database", example = "false")
          @RequestParam(defaultValue = "false")
          boolean save) {

    List<WeatherDto> results = compositeWeatherService.getBulkWeather(locations, save);
    return ResponseEntity.ok(results);
  }
}

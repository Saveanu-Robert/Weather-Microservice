package com.weatherspring.controller;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weatherspring.dto.ForecastDto;
import com.weatherspring.service.ForecastService;
import com.weatherspring.validation.ValidationConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST endpoints for weather forecast data.
 *
 * <p>Provides access to forecast data from external API and database:
 * <ul>
 *   <li>Fetch forecasts (1-14 days) from external API with optional database persistence
 *   <li>Query stored forecast records by date range
 *   <li>Retrieve future forecasts only
 * </ul>
 */
@RestController
@RequestMapping("/api/forecast")
@Tag(name = "Weather Forecast", description = "APIs for weather forecast data")
@RequiredArgsConstructor
@Validated
public class ForecastController {

  private final ForecastService forecastService;

  /**
   * Retrieves weather forecast for a location by name.
   *
   * @param location the location name to fetch forecast for
   * @param days the number of forecast days (1-14)
   * @param save whether to save the forecast data to database
   * @return list of forecast data for the specified days
   */
  @Operation(
      summary = "Get weather forecast by location name",
      description = "Fetches weather forecast for a location from external API")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Forecast data retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid location name or days parameter"),
        @ApiResponse(responseCode = "503", description = "External API unavailable")
      })
  @GetMapping
  public ResponseEntity<List<ForecastDto>> getForecast(
      @Parameter(description = "Location name (e.g., 'London', 'Paris')", required = true)
          @RequestParam
          @NotBlank(message = "Location cannot be blank")
          String location,
      @Parameter(description = "Number of forecast days (1-14)")
          @RequestParam(defaultValue = "3")
          @Min(
              value = ValidationConstants.FORECAST_DAYS_MIN,
              message = "Days must be at least " + ValidationConstants.FORECAST_DAYS_MIN)
          @Max(
              value = ValidationConstants.FORECAST_DAYS_MAX,
              message = "Days cannot exceed " + ValidationConstants.FORECAST_DAYS_MAX)
          int days,
      @Parameter(description = "Whether to save forecast data to database")
          @RequestParam(defaultValue = "true")
          boolean save) {
    List<ForecastDto> forecast = forecastService.getForecast(location, days, save);
    return ResponseEntity.ok(forecast);
  }

  /**
   * Retrieves weather forecast for a saved location by its ID.
   *
   * @param locationId the ID of the saved location
   * @param days the number of forecast days (1-14)
   * @param save whether to save the forecast data to database
   * @return list of forecast data for the specified days
   */
  @Operation(
      summary = "Get weather forecast by location ID",
      description = "Fetches weather forecast for a saved location")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Forecast data retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Location not found"),
        @ApiResponse(responseCode = "400", description = "Invalid days parameter"),
        @ApiResponse(responseCode = "503", description = "External API unavailable")
      })
  @GetMapping("/location/{locationId}")
  public ResponseEntity<List<ForecastDto>> getForecastByLocationId(
      @Parameter(description = "Location ID") @PathVariable @Positive Long locationId,
      @Parameter(description = "Number of forecast days (1-14)")
          @RequestParam(defaultValue = "3")
          @Min(
              value = ValidationConstants.FORECAST_DAYS_MIN,
              message = "Days must be at least " + ValidationConstants.FORECAST_DAYS_MIN)
          @Max(
              value = ValidationConstants.FORECAST_DAYS_MAX,
              message = "Days cannot exceed " + ValidationConstants.FORECAST_DAYS_MAX)
          int days,
      @Parameter(description = "Whether to save forecast data to database")
          @RequestParam(defaultValue = "true")
          boolean save) {
    List<ForecastDto> forecast = forecastService.getForecastByLocationId(locationId, days, save);
    return ResponseEntity.ok(forecast);
  }

  /**
   * Retrieves all stored forecast data for a location from the database.
   *
   * @param locationId the ID of the location
   * @return list of stored forecast records
   */
  @Operation(
      summary = "Get stored forecast data",
      description = "Retrieves all stored forecast data for a location from database")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Stored forecast retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Location not found")
      })
  @GetMapping("/stored/location/{locationId}")
  public ResponseEntity<List<ForecastDto>> getStoredForecasts(
      @Parameter(description = "Location ID") @PathVariable @Positive Long locationId) {
    List<ForecastDto> forecasts = forecastService.getStoredForecasts(locationId);
    return ResponseEntity.ok(forecasts);
  }

  /**
   * Retrieves future forecast data for a location from the database.
   *
   * @param locationId the ID of the location
   * @return list of future forecast records (dates after today)
   */
  @Operation(
      summary = "Get future forecasts",
      description = "Retrieves future forecast data for a location from database")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Future forecasts retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Location not found")
      })
  @GetMapping("/future/location/{locationId}")
  public ResponseEntity<List<ForecastDto>> getFutureForecasts(
      @Parameter(description = "Location ID") @PathVariable @Positive Long locationId) {
    List<ForecastDto> forecasts = forecastService.getFutureForecasts(locationId);
    return ResponseEntity.ok(forecasts);
  }

  /**
   * Retrieves forecast data for a location within a specific date range.
   *
   * @param locationId the ID of the location
   * @param startDate the start date of the range (inclusive)
   * @param endDate the end date of the range (inclusive)
   * @return list of forecast records within the specified date range
   */
  @Operation(
      summary = "Get forecasts by date range",
      description = "Retrieves forecast data for a location within a specific date range")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Forecasts retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Location not found"),
        @ApiResponse(responseCode = "400", description = "Invalid date range")
      })
  @GetMapping("/range/location/{locationId}")
  public ResponseEntity<List<ForecastDto>> getForecastsByDateRange(
      @Parameter(description = "Location ID") @PathVariable @Positive Long locationId,
      @Parameter(description = "Start date (ISO format)")
          @RequestParam
          @NotNull
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @Parameter(description = "End date (ISO format)")
          @RequestParam
          @NotNull
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate) {
    List<ForecastDto> forecasts =
        forecastService.getForecastsByDateRange(locationId, startDate, endDate);
    return ResponseEntity.ok(forecasts);
  }
}

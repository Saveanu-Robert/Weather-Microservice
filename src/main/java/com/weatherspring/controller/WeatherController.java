package com.weatherspring.controller;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weatherspring.dto.WeatherDto;
import com.weatherspring.service.WeatherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST endpoints for current and historical weather data.
 *
 * <p>Fetches live data from external API and queries historical snapshots from database.
 * Historical data is only available for records previously saved via saveToDatabase=true.
 */
@RestController
@RequestMapping("/api/weather")
@Tag(name = "Weather Data", description = "APIs for current weather and historical weather data")
@RequiredArgsConstructor
@Validated
public class WeatherController {

  private final WeatherService weatherService;

  /**
   * Retrieves current weather data for a location by name.
   *
   * @param location the location name to fetch weather for
   * @param save whether to save the weather data to database
   * @return current weather data
   */
  @Operation(
      summary = "Get current weather by location name",
      description = "Fetches current weather data for a location from external API")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Weather data retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid location name"),
        @ApiResponse(responseCode = "503", description = "External API unavailable")
      })
  @GetMapping("/current")
  public ResponseEntity<WeatherDto> getCurrentWeather(
      @Parameter(description = "Location name (e.g., 'London', 'New York')", required = true)
          @RequestParam
          @NotBlank(message = "Location cannot be blank")
          String location,
      @Parameter(description = "Whether to save weather data to database")
          @RequestParam(defaultValue = "true")
          boolean save) {
    WeatherDto weather = weatherService.getCurrentWeather(location, save);
    return ResponseEntity.ok(weather);
  }

  /**
   * Retrieves current weather data for a saved location by its ID.
   *
   * @param locationId the ID of the saved location
   * @param save whether to save the weather data to database
   * @return current weather data
   */
  @Operation(
      summary = "Get current weather by location ID",
      description = "Fetches current weather data for a saved location")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Weather data retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Location not found"),
        @ApiResponse(responseCode = "503", description = "External API unavailable")
      })
  @GetMapping("/current/location/{locationId}")
  public ResponseEntity<WeatherDto> getCurrentWeatherByLocationId(
      @Parameter(description = "Location ID") @PathVariable @Positive Long locationId,
      @Parameter(description = "Whether to save weather data to database")
          @RequestParam(defaultValue = "true")
          boolean save) {
    WeatherDto weather = weatherService.getCurrentWeatherByLocationId(locationId, save);
    return ResponseEntity.ok(weather);
  }

  /**
   * Retrieves historical weather records for a location with pagination.
   *
   * @param locationId the ID of the location
   * @param pageable pagination parameters including page number, size, and sort order
   * @return page of historical weather records
   */
  @Operation(
      summary = "Get weather history for a location",
      description = "Retrieves historical weather records for a location with pagination")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Weather history retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Location not found")
      })
  @GetMapping("/history/location/{locationId}")
  public ResponseEntity<Page<WeatherDto>> getWeatherHistory(
      @Parameter(description = "Location ID") @PathVariable @Positive Long locationId,
      @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
          Pageable pageable) {
    Page<WeatherDto> weatherHistory = weatherService.getWeatherHistory(locationId, pageable);
    return ResponseEntity.ok(weatherHistory);
  }

  /**
   * Retrieves weather records for a location within a specific date range.
   *
   * @param locationId the ID of the location
   * @param startDate the start date and time of the range (inclusive)
   * @param endDate the end date and time of the range (inclusive)
   * @return list of weather records within the specified date range
   */
  @Operation(
      summary = "Get weather history by date range",
      description = "Retrieves weather records for a location within a specific date range")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Weather history retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Location not found"),
        @ApiResponse(responseCode = "400", description = "Invalid date range")
      })
  @GetMapping("/history/location/{locationId}/range")
  public ResponseEntity<List<WeatherDto>> getWeatherHistoryByDateRange(
      @Parameter(description = "Location ID") @PathVariable @Positive Long locationId,
      @Parameter(description = "Start date and time (ISO format)")
          @RequestParam
          @NotNull
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime startDate,
      @Parameter(description = "End date and time (ISO format)")
          @RequestParam
          @NotNull
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime endDate) {
    List<WeatherDto> weatherHistory =
        weatherService.getWeatherHistoryByDateRange(locationId, startDate, endDate);
    return ResponseEntity.ok(weatherHistory);
  }
}

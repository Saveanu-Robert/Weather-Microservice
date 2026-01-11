package com.weatherspring.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weatherspring.dto.BulkOperationResult;
import com.weatherspring.dto.ForecastDto;
import com.weatherspring.dto.WeatherDto;
import com.weatherspring.service.AsyncBulkWeatherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for async bulk operations.
 *
 * <p>All endpoints return {@link CompletableFuture} which allows:
 *
 * <ul>
 *   <li>Non-blocking request processing - frees up request threads immediately
 *   <li>Better scalability under high load
 *   <li>Client can poll or wait for completion
 *   <li>Perfect for batch/bulk operations that take time
 * </ul>
 *
 * <p>Spring MVC automatically handles CompletableFuture return types by:
 *
 * <ul>
 *   <li>Returning 200 OK immediately with async processing
 *   <li>Completing the response when the future completes
 *   <li>Handling errors if the future fails
 * </ul>
 */
@RestController
@RequestMapping("/api/async")
@Tag(
    name = "Async Bulk Operations",
    description = "Asynchronous bulk weather operations using virtual threads")
@Slf4j
@RequiredArgsConstructor
@Validated
public class AsyncBulkController {

  private final AsyncBulkWeatherService asyncBulkWeatherService;

  /**
   * Asynchronously fetch weather for multiple locations.
   *
   * @param locations comma-separated list of location names
   * @param save whether to save to database (default false)
   * @return future containing weather data for all locations
   */
  @Operation(
      summary = "Async bulk weather fetch",
      description =
          "Asynchronously fetches current weather for multiple locations. "
              + "Returns immediately and completes when all data is fetched.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully initiated async fetch"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
      })
  @GetMapping("/weather/bulk")
  public CompletableFuture<List<WeatherDto>> bulkWeatherAsync(
      @Parameter(
              description = "Comma-separated list of location names",
              required = true,
              example = "London,Paris,Tokyo,New York")
          @RequestParam
          @NotEmpty
          @Size(max = 100, message = "Maximum 100 locations per request")
          List<String> locations,
      @Parameter(description = "Save data to database", example = "false")
          @RequestParam(defaultValue = "false")
          boolean save) {

    log.info("Received async bulk weather request for {} locations", locations.size());

    return asyncBulkWeatherService.fetchBulkWeatherAsync(locations, save);
  }

  /**
   * Asynchronously fetch forecasts for multiple locations.
   *
   * @param locations comma-separated list of location names
   * @param days number of forecast days (1-14)
   * @param save whether to save to database (default false)
   * @return future containing forecast data for all locations
   */
  @Operation(
      summary = "Async bulk forecast fetch",
      description =
          "Asynchronously fetches forecasts for multiple locations. "
              + "Each location gets its own forecast list.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully initiated async fetch"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
      })
  @GetMapping("/forecast/bulk")
  public CompletableFuture<List<List<ForecastDto>>> bulkForecastAsync(
      @Parameter(
              description = "Comma-separated list of location names",
              required = true,
              example = "London,Paris,Tokyo")
          @RequestParam
          @NotEmpty
          @Size(max = 100, message = "Maximum 100 locations per request")
          List<String> locations,
      @Parameter(description = "Number of forecast days (1-14)", example = "3")
          @RequestParam(defaultValue = "3")
          @Min(1)
          @Max(14)
          int days,
      @Parameter(description = "Save data to database", example = "false")
          @RequestParam(defaultValue = "false")
          boolean save) {

    log.info(
        "Received async bulk forecast request for {} locations, {} days", locations.size(), days);

    return asyncBulkWeatherService.fetchBulkForecastAsync(locations, days, save);
  }

  /**
   * Asynchronously update weather for multiple location IDs.
   *
   * @param locationIds list of location IDs
   * @param save whether to save to database (default true)
   * @return future containing number of successful updates
   */
  @Operation(
      summary = "Async bulk weather update",
      description =
          "Asynchronously updates current weather for saved locations. "
              + "Returns count of successful updates.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Update initiated"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
      })
  @PostMapping("/weather/update")
  public CompletableFuture<Integer> bulkUpdateWeatherAsync(
      @Parameter(
              description = "List of location IDs to update",
              required = true,
              example = "[1, 2, 3, 4, 5]")
          @RequestBody
          @NotEmpty
          @Size(max = 100, message = "Maximum 100 location IDs per request")
          List<Long> locationIds,
      @Parameter(description = "Save data to database", example = "true")
          @RequestParam(defaultValue = "true")
          boolean save) {

    log.info("Received async bulk weather update request for {} locations", locationIds.size());

    return asyncBulkWeatherService.bulkUpdateWeatherAsync(locationIds, save);
  }

  /**
   * Asynchronously refresh forecasts for multiple location IDs.
   *
   * @param locationIds list of location IDs
   * @param days number of forecast days (1-14)
   * @param save whether to save to database (default true)
   * @return future containing number of successful refreshes
   */
  @Operation(
      summary = "Async bulk forecast refresh",
      description =
          "Asynchronously refreshes forecasts for saved locations. "
              + "Returns count of successful refreshes.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Refresh initiated"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
      })
  @PostMapping("/forecast/refresh")
  public CompletableFuture<Integer> bulkRefreshForecastsAsync(
      @Parameter(
              description = "List of location IDs to refresh",
              required = true,
              example = "[1, 2, 3]")
          @RequestBody
          @NotEmpty
          @Size(max = 100, message = "Maximum 100 location IDs per request")
          List<Long> locationIds,
      @Parameter(description = "Number of forecast days (1-14)", example = "7")
          @RequestParam(defaultValue = "7")
          @Min(1)
          @Max(14)
          int days,
      @Parameter(description = "Save data to database", example = "true")
          @RequestParam(defaultValue = "true")
          boolean save) {

    log.info(
        "Received async bulk forecast refresh request for {} locations, {} days",
        locationIds.size(),
        days);

    return asyncBulkWeatherService.bulkRefreshForecastsAsync(locationIds, days, save);
  }

  /**
   * Asynchronously refresh all data for all locations in the system.
   *
   * @param forecastDays number of forecast days (1-14)
   * @return future containing operation result summary
   */
  @Operation(
      summary = "Async refresh all locations",
      description =
          "Asynchronously refreshes both weather and forecast for ALL saved locations. "
              + "This is a comprehensive bulk operation that updates the entire system. "
              + "Returns a summary with success/failure counts.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Refresh initiated"),
        @ApiResponse(responseCode = "500", description = "Refresh failed")
      })
  @PostMapping("/refresh-all")
  public CompletableFuture<BulkOperationResult> refreshAllAsync(
      @Parameter(description = "Number of forecast days (1-14)", example = "7")
          @RequestParam(defaultValue = "7")
          @Min(1)
          @Max(14)
          int forecastDays) {

    log.info("Received async refresh all locations request with {} forecast days", forecastDays);

    return asyncBulkWeatherService
        .refreshAllLocationsAsync(forecastDays)
        .thenApply(
            result -> {
              log.info(
                  "Refresh all completed: success={}, failure={}, total={}",
                  result.successCount(),
                  result.failureCount(),
                  result.totalCount());
              return result;
            });
  }
}

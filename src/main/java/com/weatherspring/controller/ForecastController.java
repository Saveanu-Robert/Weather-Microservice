package com.weatherspring.controller;

import com.weatherspring.dto.ForecastDto;
import com.weatherspring.service.ForecastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for weather forecast endpoints.
 */
@RestController
@RequestMapping("/api/forecast")
@Tag(name = "Weather Forecast", description = "APIs for weather forecast data")
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @Operation(summary = "Get weather forecast by location name",
               description = "Fetches weather forecast for a location from external API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Forecast data retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid location name or days parameter"),
        @ApiResponse(responseCode = "503", description = "External API unavailable")
    })
    @GetMapping
    public ResponseEntity<List<ForecastDto>> getForecast(
            @Parameter(description = "Location name (e.g., 'London', 'Paris')", required = true)
            @RequestParam String location,
            @Parameter(description = "Number of forecast days (1-14)")
            @RequestParam(defaultValue = "3") int days,
            @Parameter(description = "Whether to save forecast data to database")
            @RequestParam(defaultValue = "true") boolean save) {
        List<ForecastDto> forecast = forecastService.getForecast(location, days, save);
        return ResponseEntity.ok(forecast);
    }

    @Operation(summary = "Get weather forecast by location ID",
               description = "Fetches weather forecast for a saved location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Forecast data retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Location not found"),
        @ApiResponse(responseCode = "400", description = "Invalid days parameter"),
        @ApiResponse(responseCode = "503", description = "External API unavailable")
    })
    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<ForecastDto>> getForecastByLocationId(
            @Parameter(description = "Location ID") @PathVariable Long locationId,
            @Parameter(description = "Number of forecast days (1-14)")
            @RequestParam(defaultValue = "3") int days,
            @Parameter(description = "Whether to save forecast data to database")
            @RequestParam(defaultValue = "true") boolean save) {
        List<ForecastDto> forecast = forecastService.getForecastByLocationId(locationId, days, save);
        return ResponseEntity.ok(forecast);
    }

    @Operation(summary = "Get stored forecast data",
               description = "Retrieves all stored forecast data for a location from database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stored forecast retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Location not found")
    })
    @GetMapping("/stored/location/{locationId}")
    public ResponseEntity<List<ForecastDto>> getStoredForecasts(
            @Parameter(description = "Location ID") @PathVariable Long locationId) {
        List<ForecastDto> forecasts = forecastService.getStoredForecasts(locationId);
        return ResponseEntity.ok(forecasts);
    }

    @Operation(summary = "Get future forecasts",
               description = "Retrieves future forecast data for a location from database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Future forecasts retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Location not found")
    })
    @GetMapping("/future/location/{locationId}")
    public ResponseEntity<List<ForecastDto>> getFutureForecasts(
            @Parameter(description = "Location ID") @PathVariable Long locationId) {
        List<ForecastDto> forecasts = forecastService.getFutureForecasts(locationId);
        return ResponseEntity.ok(forecasts);
    }

    @Operation(summary = "Get forecasts by date range",
               description = "Retrieves forecast data for a location within a specific date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Forecasts retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Location not found"),
        @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    @GetMapping("/range/location/{locationId}")
    public ResponseEntity<List<ForecastDto>> getForecastsByDateRange(
            @Parameter(description = "Location ID") @PathVariable Long locationId,
            @Parameter(description = "Start date (ISO format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ForecastDto> forecasts =
            forecastService.getForecastsByDateRange(locationId, startDate, endDate);
        return ResponseEntity.ok(forecasts);
    }
}

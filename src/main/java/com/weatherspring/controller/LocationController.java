package com.weatherspring.controller;

import com.weatherspring.dto.CreateLocationRequest;
import com.weatherspring.dto.LocationDto;
import com.weatherspring.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for location management endpoints.
 */
@RestController
@RequestMapping("/api/locations")
@Tag(name = "Location Management", description = "APIs for managing weather locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @Operation(summary = "Create a new location", description = "Creates a new location for weather tracking")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Location created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Location already exists")
    })
    @PostMapping
    public ResponseEntity<LocationDto> createLocation(
            @Valid @RequestBody CreateLocationRequest request) {
        LocationDto location = locationService.createLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(location);
    }

    @Operation(summary = "Get location by ID", description = "Retrieves a location by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Location found"),
        @ApiResponse(responseCode = "404", description = "Location not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getLocationById(
            @Parameter(description = "Location ID") @PathVariable Long id) {
        LocationDto location = locationService.getLocationById(id);
        return ResponseEntity.ok(location);
    }

    @Operation(summary = "Get all locations", description = "Retrieves all saved locations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Locations retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<LocationDto>> getAllLocations() {
        List<LocationDto> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }

    @Operation(summary = "Search locations by name", description = "Searches for locations matching the given name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<List<LocationDto>> searchLocations(
            @Parameter(description = "Location name to search for")
            @RequestParam String name) {
        List<LocationDto> locations = locationService.searchLocationsByName(name);
        return ResponseEntity.ok(locations);
    }

    @Operation(summary = "Update a location", description = "Updates an existing location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Location updated successfully"),
        @ApiResponse(responseCode = "404", description = "Location not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<LocationDto> updateLocation(
            @Parameter(description = "Location ID") @PathVariable Long id,
            @Valid @RequestBody CreateLocationRequest request) {
        LocationDto location = locationService.updateLocation(id, request);
        return ResponseEntity.ok(location);
    }

    @Operation(summary = "Delete a location", description = "Deletes a location by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Location deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Location not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(
            @Parameter(description = "Location ID") @PathVariable Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}

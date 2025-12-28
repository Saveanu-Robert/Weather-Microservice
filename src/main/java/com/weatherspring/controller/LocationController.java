package com.weatherspring.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weatherspring.dto.CreateLocationRequest;
import com.weatherspring.dto.LocationDto;
import com.weatherspring.service.LocationService;
import com.weatherspring.validation.ValidationConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST endpoints for managing location data.
 *
 * <p>Locations are created automatically when fetching weather with saveToDatabase=true,
 * or can be managed explicitly through these endpoints for organization and tracking.
 */
@RestController
@RequestMapping("/api/locations")
@Tag(name = "Location Management", description = "APIs for managing weather locations")
@RequiredArgsConstructor
@Validated
public class LocationController {

  private final LocationService locationService;

  /**
   * Creates a new location for weather tracking.
   *
   * @param request the location creation request with name, country, latitude, and longitude
   * @return the created location data
   */
  @Operation(
      summary = "Create a new location",
      description = "Creates a new location for weather tracking")
  @ApiResponses(
      value = {
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

  /**
   * Retrieves a location by its unique identifier.
   *
   * @param id the location ID
   * @return the location data
   */
  @Operation(
      summary = "Get location by ID",
      description = "Retrieves a location by its unique identifier")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Location found"),
        @ApiResponse(responseCode = "404", description = "Location not found")
      })
  @GetMapping("/{id}")
  public ResponseEntity<LocationDto> getLocationById(
      @Parameter(description = "Location ID") @PathVariable @Positive Long id) {
    LocationDto location = locationService.getLocationById(id);
    return ResponseEntity.ok(location);
  }

  /**
   * Retrieves all saved locations.
   *
   * @return list of all location data
   */
  @Operation(summary = "Get all locations", description = "Retrieves all saved locations")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Locations retrieved successfully")
      })
  @GetMapping
  public ResponseEntity<List<LocationDto>> getAllLocations() {
    List<LocationDto> locations = locationService.getAllLocations();
    return ResponseEntity.ok(locations);
  }

  /**
   * Searches for locations matching the given name.
   *
   * @param name the location name to search for (partial match supported)
   * @return list of matching locations
   */
  @Operation(
      summary = "Search locations by name",
      description = "Searches for locations matching the given name")
  @ApiResponses(
      value = {@ApiResponse(responseCode = "200", description = "Search completed successfully")})
  @GetMapping("/search")
  public ResponseEntity<List<LocationDto>> searchLocations(
      @Parameter(description = "Location name to search for")
          @RequestParam
          @NotBlank(message = "Location name cannot be blank")
          @Size(
              min = 1,
              max = ValidationConstants.LOCATION_NAME_MAX_LENGTH,
              message =
                  "Location name must be between 1 and "
                      + ValidationConstants.LOCATION_NAME_MAX_LENGTH
                      + " characters")
          String name) {
    List<LocationDto> locations = locationService.searchLocationsByName(name);
    return ResponseEntity.ok(locations);
  }

  /**
   * Searches for locations matching the given name with pagination support.
   *
   * @param name the location name to search for (partial match supported)
   * @param pageable pagination parameters including page number, size, and sort order
   * @return page of matching locations
   */
  @Operation(
      summary = "Search locations by name (paginated)",
      description = "Searches for locations matching the given name with pagination support")
  @ApiResponses(
      value = {@ApiResponse(responseCode = "200", description = "Search completed successfully")})
  @GetMapping("/search/page")
  public ResponseEntity<Page<LocationDto>> searchLocationsPaginated(
      @Parameter(description = "Location name to search for")
          @RequestParam
          @NotBlank(message = "Location name cannot be blank")
          @Size(
              min = 1,
              max = ValidationConstants.LOCATION_NAME_MAX_LENGTH,
              message =
                  "Location name must be between 1 and "
                      + ValidationConstants.LOCATION_NAME_MAX_LENGTH
                      + " characters")
          String name,
      @Parameter(description = "Pagination parameters")
          @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
          Pageable pageable) {
    Page<LocationDto> locations = locationService.searchLocationsByName(name, pageable);
    return ResponseEntity.ok(locations);
  }

  /**
   * Updates an existing location.
   *
   * @param id the ID of the location to update
   * @param request the updated location data
   * @return the updated location data
   */
  @Operation(summary = "Update a location", description = "Updates an existing location")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Location updated successfully"),
        @ApiResponse(responseCode = "404", description = "Location not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
      })
  @PutMapping("/{id}")
  public ResponseEntity<LocationDto> updateLocation(
      @Parameter(description = "Location ID") @PathVariable @Positive Long id,
      @Valid @RequestBody CreateLocationRequest request) {
    LocationDto location = locationService.updateLocation(id, request);
    return ResponseEntity.ok(location);
  }

  /**
   * Deletes a location by ID.
   *
   * @param id the ID of the location to delete
   * @return no content response
   */
  @Operation(summary = "Delete a location", description = "Deletes a location by ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Location deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Location not found")
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteLocation(
      @Parameter(description = "Location ID") @PathVariable @Positive Long id) {
    locationService.deleteLocation(id);
    return ResponseEntity.noContent().build();
  }
}

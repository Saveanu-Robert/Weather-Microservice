package com.weatherspring.service;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.weatherspring.annotation.CacheEvictingOperation;
import com.weatherspring.dto.CreateLocationRequest;
import com.weatherspring.dto.LocationDto;
import com.weatherspring.exception.LocationNotFoundException;
import com.weatherspring.mapper.LocationMapper;
import com.weatherspring.model.Location;
import com.weatherspring.repository.LocationRepository;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing location data.
 *
 * <p>Provides CRUD operations for locations with caching support.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Validated
public class LocationService {

  private final LocationRepository locationRepository;
  private final LocationMapper locationMapper;
  private final MetricsService metricsService;

  /**
   * Creates a new location.
   *
   * <p><strong>Cache Strategy:</strong> Uses {@code allEntries = true} to evict the entire
   * locations cache because this operation affects:
   *
   * <ul>
   *   <li>{@code getAllLocations()} - the new location will appear in the full list
   *   <li>All paginated queries - the new location may appear on any page depending on page size
   *   <li>Cannot selectively evict paginated caches as page keys are dynamic ({@code page:X:Y})
   * </ul>
   *
   * @param request the create location request
   * @return the created location DTO
   * @throws IllegalArgumentException if request is null or if location already exists with the same
   *     name and country
   * @throws jakarta.validation.ValidationException if request validation fails
   * @throws org.springframework.dao.DataAccessException if database save fails
   */
  @Transactional(propagation = Propagation.REQUIRED)
  @Observed(name = "location.create", contextualName = "create-location")
  @CacheEvictingOperation(cacheNames = "locations", allEntries = true)
  public LocationDto createLocation(@Valid @NotNull CreateLocationRequest request) {
    log.info("Creating new location: {} in {}", request.name(), request.country());

    if (locationRepository.existsByNameAndCountry(request.name(), request.country())) {
      log.warn("Location already exists: {} in {}", request.name(), request.country());
      throw new IllegalArgumentException(
          String.format("Location already exists: %s in %s", request.name(), request.country()));
    }

    Location location = locationMapper.toEntity(request);
    Location savedLocation = locationRepository.save(location);

    metricsService.recordLocationCreated();
    log.info("Successfully created location with ID: {}", savedLocation.getId());
    LocationDto result = locationMapper.toDto(savedLocation);
    log.debug(
        "Successfully created location: {} (lat: {}, lon: {})",
        result.name(),
        result.latitude(),
        result.longitude());
    return result;
  }

  /**
   * Retrieves a location by ID.
   *
   * @param id the location ID
   * @return the location DTO
   * @throws IllegalArgumentException if id is null
   * @throws com.weatherspring.exception.LocationNotFoundException if location with given ID does
   *     not exist
   * @throws org.springframework.dao.DataAccessException if database query fails
   */
  @Transactional(readOnly = true)
  @Observed(name = "location.get.by.id", contextualName = "get-location-by-id")
  @Cacheable(value = "locations", key = "'location:byId:' + #id")
  public LocationDto getLocationById(@NotNull Long id) {
    log.debug("Fetching location with ID: {}", id);

    Location location =
        locationRepository.findById(id).orElseThrow(() -> new LocationNotFoundException(id));

    return locationMapper.toDto(location);
  }

  /**
   * Retrieves all locations.
   *
   * @return list of all location DTOs
   */
  @Transactional(readOnly = true)
  @Cacheable(value = "locations", key = "'location:all'")
  public List<LocationDto> getAllLocations() {
    log.debug("Fetching all locations");

    List<LocationDto> result =
        locationRepository.findAll().stream().map(locationMapper::toDto).toList();

    log.debug("Successfully retrieved {} locations", result.size());
    return result;
  }

  /**
   * Retrieves all locations with pagination.
   *
   * @param pageable pagination information
   * @return page of location DTOs
   */
  @Transactional(readOnly = true)
  @Cacheable(
      value = "locations",
      key = "'location:page:' + #pageable.pageNumber + ':' + #pageable.pageSize")
  public Page<LocationDto> getAllLocations(@NotNull Pageable pageable) {
    log.debug(
        "Fetching locations page {} with size {}",
        pageable.getPageNumber(),
        pageable.getPageSize());

    Page<LocationDto> result = locationRepository.findAll(pageable).map(locationMapper::toDto);

    log.debug(
        "Successfully retrieved {} locations (page {} of {})",
        result.getNumberOfElements(),
        result.getNumber() + 1,
        result.getTotalPages());
    return result;
  }

  /**
   * Searches for locations by name.
   *
   * @param name the location name to search for
   * @return list of matching location DTOs
   */
  @Transactional(readOnly = true)
  @Observed(name = "location.search", contextualName = "search-locations")
  public List<LocationDto> searchLocationsByName(@NotBlank String name) {
    log.debug("Searching locations by name: {}", name);

    List<LocationDto> result =
        locationRepository.findByNameContaining(name).stream().map(locationMapper::toDto).toList();

    log.debug("Successfully found {} locations matching name: {}", result.size(), name);
    return result;
  }

  /**
   * Searches for locations by name with pagination.
   *
   * @param name the location name to search for
   * @param pageable pagination information
   * @return page of matching location DTOs
   */
  @Transactional(readOnly = true)
  @Observed(name = "location.search.page", contextualName = "search-locations-paginated")
  public Page<LocationDto> searchLocationsByName(
      @NotBlank String name, @NotNull Pageable pageable) {
    log.debug(
        "Searching locations by name '{}' - page {} with size {}",
        name,
        pageable.getPageNumber(),
        pageable.getPageSize());

    Page<LocationDto> result =
        locationRepository.findByNameContaining(name, pageable).map(locationMapper::toDto);

    log.debug(
        "Successfully found {} locations matching name '{}' (page {} of {})",
        result.getNumberOfElements(),
        name,
        result.getNumber() + 1,
        result.getTotalPages());
    return result;
  }

  /**
   * Retrieves a location entity by ID (for internal use).
   *
   * @param id the location ID
   * @return the location entity
   * @throws LocationNotFoundException if location not found
   */
  @Transactional(readOnly = true)
  public Location getLocationEntityById(@NotNull Long id) {
    return locationRepository.findById(id).orElseThrow(() -> new LocationNotFoundException(id));
  }

  /**
   * Finds or creates a location based on name and country.
   *
   * <p>This method handles race conditions by using database-level unique constraints. If
   * concurrent creation attempts occur, the constraint violation is caught and a retry query is
   * performed.
   *
   * @param name the location name
   * @param country the country name
   * @param latitude the latitude
   * @param longitude the longitude
   * @param region the region
   * @return the location entity
   * @throws IllegalArgumentException if name, country, latitude, or longitude is null
   * @throws org.springframework.dao.DataAccessException if database operation fails after retry
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public Location findOrCreateLocation(
      @NotNull String name,
      @NotNull String country,
      @NotNull Double latitude,
      @NotNull Double longitude,
      String region) {
    log.debug("Finding or creating location: {} in {}", name, country);

    // First attempt to find existing location
    Optional<Location> existing = locationRepository.findByNameAndCountry(name, country);
    if (existing.isPresent()) {
      return existing.get();
    }

    // Try to create new location
    try {
      Location newLocation =
          Location.builder()
              .name(name)
              .country(country)
              .latitude(latitude)
              .longitude(longitude)
              .region(region)
              .build();
      Location saved = locationRepository.save(newLocation);
      log.info("Created new location with ID: {}", saved.getId());
      return saved;
    } catch (org.springframework.dao.DataIntegrityViolationException e) {
      // Unique constraint violation - another thread created it concurrently
      log.debug(
          "Concurrent location creation detected for {} in {}, retrying query", name, country);

      // Retry the query - the location must exist now
      return locationRepository
          .findByNameAndCountry(name, country)
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      "Location creation failed and retry query found nothing for: "
                          + name
                          + " in "
                          + country));
    }
  }

  /**
   * Updates an existing location.
   *
   * <p><strong>Cache Strategy:</strong> Uses {@code allEntries = true} to evict the entire
   * locations cache because updates to location name or coordinates affect:
   *
   * <ul>
   *   <li>The specific location cache entry (by ID)
   *   <li>{@code getAllLocations()} - the updated location data
   *   <li>All paginated queries - the updated location appears on its current page
   *   <li>Search results - name changes affect {@code findByNameContaining()} results
   * </ul>
   *
   * @param id the location ID
   * @param request the update request
   * @return the updated location DTO
   * @throws IllegalArgumentException if id or request is null
   * @throws com.weatherspring.exception.LocationNotFoundException if location with given ID does
   *     not exist
   * @throws jakarta.validation.ValidationException if request validation fails
   * @throws org.springframework.dao.DataAccessException if database save fails
   */
  @Transactional(propagation = Propagation.REQUIRED)
  @Observed(name = "location.update", contextualName = "update-location")
  @CacheEvictingOperation(cacheNames = "locations", allEntries = true)
  public LocationDto updateLocation(
      @NotNull Long id, @Valid @NotNull CreateLocationRequest request) {
    log.info("Updating location with ID: {}", id);

    Location location =
        locationRepository.findById(id).orElseThrow(() -> new LocationNotFoundException(id));

    Location updatedLocation = locationMapper.updateEntityFromRequest(location, request);
    updatedLocation = locationRepository.save(updatedLocation);

    log.info("Successfully updated location with ID: {}", id);
    LocationDto result = locationMapper.toDto(updatedLocation);
    log.debug(
        "Successfully updated location ID: {} - new values: {} (lat: {}, lon: {})",
        id,
        result.name(),
        result.latitude(),
        result.longitude());
    return result;
  }

  /**
   * Deletes a location by ID.
   *
   * <p><strong>Cache Strategy:</strong> Uses {@code allEntries = true} to evict the entire
   * locations cache because deletion affects:
   *
   * <ul>
   *   <li>The specific location cache entry (by ID) - must be removed
   *   <li>{@code getAllLocations()} - the deleted location no longer appears
   *   <li>All paginated queries - page contents shift, potentially affecting multiple pages
   *   <li>Search results - the deleted location no longer matches any searches
   * </ul>
   *
   * @param id the location ID
   * @throws IllegalArgumentException if id is null
   * @throws com.weatherspring.exception.LocationNotFoundException if location with given ID does
   *     not exist
   * @throws org.springframework.dao.DataAccessException if database delete operation fails
   */
  @Transactional(propagation = Propagation.REQUIRED)
  @Observed(name = "location.delete", contextualName = "delete-location")
  @CacheEvictingOperation(cacheNames = "locations", allEntries = true)
  public void deleteLocation(@NotNull Long id) {
    log.info("Deleting location with ID: {}", id);

    if (!locationRepository.existsById(id)) {
      throw new LocationNotFoundException(id);
    }

    locationRepository.deleteById(id);
    log.info("Successfully deleted location with ID: {}", id);
  }
}

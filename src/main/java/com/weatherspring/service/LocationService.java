package com.weatherspring.service;

import com.weatherspring.dto.CreateLocationRequest;
import com.weatherspring.dto.LocationDto;
import com.weatherspring.exception.LocationNotFoundException;
import com.weatherspring.mapper.LocationMapper;
import com.weatherspring.model.Location;
import com.weatherspring.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service for managing location data.
 *
 * <p>Provides CRUD operations for locations with caching support.</p>
 */
@Service
@Transactional(readOnly = true)
public class LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public LocationService(LocationRepository locationRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }

    /**
     * Creates a new location.
     *
     * @param request the create location request
     * @return the created location DTO
     */
    @Transactional
    @CacheEvict(value = "locations", allEntries = true)
    public LocationDto createLocation(CreateLocationRequest request) {
        Objects.requireNonNull(request, "Create location request cannot be null");

        logger.info("Creating new location: {} in {}", request.getName(), request.getCountry());

        if (locationRepository.existsByNameAndCountry(request.getName(), request.getCountry())) {
            logger.warn("Location already exists: {} in {}", request.getName(), request.getCountry());
            throw new IllegalArgumentException(
                "Location already exists: " + request.getName() + " in " + request.getCountry());
        }

        Location location = locationMapper.toEntity(request);
        Location savedLocation = locationRepository.save(location);

        logger.info("Successfully created location with ID: {}", savedLocation.getId());
        return locationMapper.toDto(savedLocation);
    }

    /**
     * Retrieves a location by ID.
     *
     * @param id the location ID
     * @return the location DTO
     * @throws LocationNotFoundException if location not found
     */
    @Cacheable(value = "locations", key = "#id")
    public LocationDto getLocationById(Long id) {
        Objects.requireNonNull(id, "Location ID cannot be null");

        logger.debug("Fetching location with ID: {}", id);

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));

        return locationMapper.toDto(location);
    }

    /**
     * Retrieves all locations.
     *
     * @return list of all location DTOs
     */
    public List<LocationDto> getAllLocations() {
        logger.debug("Fetching all locations");

        return locationRepository.findAll().stream()
                .map(locationMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Searches for locations by name.
     *
     * @param name the location name to search for
     * @return list of matching location DTOs
     */
    public List<LocationDto> searchLocationsByName(String name) {
        Objects.requireNonNull(name, "Search name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Search name cannot be blank");
        }

        logger.debug("Searching locations by name: {}", name);

        return locationRepository.findByNameContaining(name).stream()
                .map(locationMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a location entity by ID (for internal use).
     *
     * @param id the location ID
     * @return the location entity
     * @throws LocationNotFoundException if location not found
     */
    public Location getLocationEntityById(Long id) {
        Objects.requireNonNull(id, "Location ID cannot be null");

        return locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));
    }

    /**
     * Finds or creates a location based on name and country.
     *
     * @param name the location name
     * @param country the country name
     * @param latitude the latitude
     * @param longitude the longitude
     * @param region the region
     * @return the location entity
     */
    @Transactional
    public Location findOrCreateLocation(String name, String country, Double latitude,
                                        Double longitude, String region) {
        Objects.requireNonNull(name, "Location name cannot be null");
        Objects.requireNonNull(country, "Country cannot be null");
        Objects.requireNonNull(latitude, "Latitude cannot be null");
        Objects.requireNonNull(longitude, "Longitude cannot be null");

        logger.debug("Finding or creating location: {} in {}", name, country);

        return locationRepository.findByNameAndCountry(name, country)
                .orElseGet(() -> {
                    Location newLocation = Location.builder()
                            .name(name)
                            .country(country)
                            .latitude(latitude)
                            .longitude(longitude)
                            .region(region)
                            .build();
                    Location saved = locationRepository.save(newLocation);
                    logger.info("Created new location with ID: {}", saved.getId());
                    return saved;
                });
    }

    /**
     * Updates an existing location.
     *
     * @param id the location ID
     * @param request the update request
     * @return the updated location DTO
     * @throws LocationNotFoundException if location not found
     */
    @Transactional
    @CacheEvict(value = "locations", key = "#id")
    public LocationDto updateLocation(Long id, CreateLocationRequest request) {
        Objects.requireNonNull(id, "Location ID cannot be null");
        Objects.requireNonNull(request, "Update location request cannot be null");

        logger.info("Updating location with ID: {}", id);

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));

        Location updatedLocation = locationMapper.updateEntityFromRequest(location, request);
        updatedLocation = locationRepository.save(updatedLocation);

        logger.info("Successfully updated location with ID: {}", id);
        return locationMapper.toDto(updatedLocation);
    }

    /**
     * Deletes a location by ID.
     *
     * @param id the location ID
     * @throws LocationNotFoundException if location not found
     */
    @Transactional
    @CacheEvict(value = "locations", key = "#id")
    public void deleteLocation(Long id) {
        Objects.requireNonNull(id, "Location ID cannot be null");

        logger.info("Deleting location with ID: {}", id);

        if (!locationRepository.existsById(id)) {
            throw new LocationNotFoundException(id);
        }

        locationRepository.deleteById(id);
        logger.info("Successfully deleted location with ID: {}", id);
    }
}

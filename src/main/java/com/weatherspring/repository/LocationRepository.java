package com.weatherspring.repository;

import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.weatherspring.model.Location;

/**
 * Database operations for location entities.
 *
 * <p>Provides queries for finding locations by name/country and checking existence.
 * The unique constraint on (name, country) prevents duplicate locations.
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

  /**
   * Finds a location by its name and country.
   *
   * @param name the location name
   * @param country the country name
   * @return an Optional containing the location if found
   */
  Optional<Location> findByNameAndCountry(@NotNull String name, @NotNull String country);

  /**
   * Finds all locations in a specific country.
   *
   * @param country the country name
   * @return list of locations in the country
   */
  List<Location> findByCountry(@NotNull String country);

  /**
   * Finds all locations in a specific country with pagination.
   *
   * @param country the country name
   * @param pageable pagination parameters
   * @return page of locations in the country
   */
  Page<Location> findByCountry(@NotNull String country, @NotNull Pageable pageable);

  /**
   * Searches for locations with names containing the given string (case-insensitive).
   *
   * @param name the name to search for
   * @return list of matching locations
   */
  @Query("SELECT l FROM Location l WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :name, '%'))")
  List<Location> findByNameContaining(@NotNull @Param("name") String name);

  /**
   * Searches for locations with names containing the given string with pagination (case-insensitive).
   *
   * @param name the name to search for
   * @param pageable pagination parameters
   * @return page of matching locations
   */
  @Query("SELECT l FROM Location l WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :name, '%'))")
  Page<Location> findByNameContaining(
      @NotNull @Param("name") String name, @NotNull Pageable pageable);

  /**
   * Checks if a location with the given name and country already exists.
   *
   * @param name the location name
   * @param country the country name
   * @return true if a location with this name and country exists
   */
  boolean existsByNameAndCountry(@NotNull String name, @NotNull String country);
}

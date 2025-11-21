package com.weatherspring.repository;

import com.weatherspring.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Location entities.
 *
 * <p>Provides CRUD operations and custom queries for location data.</p>
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    /**
     * Finds a location by its name and country.
     *
     * @param name the location name
     * @param country the country name
     * @return Optional containing the location if found
     */
    Optional<Location> findByNameAndCountry(String name, String country);

    /**
     * Finds all locations in a specific country.
     *
     * @param country the country name
     * @return list of locations in the country
     */
    List<Location> findByCountry(String country);

    /**
     * Finds locations by name (case-insensitive partial match).
     *
     * @param name the location name to search for
     * @return list of matching locations
     */
    @Query("SELECT l FROM Location l WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Location> findByNameContaining(@Param("name") String name);

    /**
     * Checks if a location exists with the given name and country.
     *
     * @param name the location name
     * @param country the country name
     * @return true if location exists, false otherwise
     */
    boolean existsByNameAndCountry(String name, String country);
}

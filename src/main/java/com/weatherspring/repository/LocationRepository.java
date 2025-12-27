package com.weatherspring.repository;

import com.weatherspring.model.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Database operations for locations.
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByNameAndCountry(@NotNull String name, @NotNull String country);

    List<Location> findByCountry(@NotNull String country);

    Page<Location> findByCountry(@NotNull String country, @NotNull Pageable pageable);

    // Case-insensitive partial match search
    @Query("SELECT l FROM Location l WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Location> findByNameContaining(@NotNull @Param("name") String name);

    @Query("SELECT l FROM Location l WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Location> findByNameContaining(@NotNull @Param("name") String name, @NotNull Pageable pageable);

    boolean existsByNameAndCountry(@NotNull String name, @NotNull String country);
}

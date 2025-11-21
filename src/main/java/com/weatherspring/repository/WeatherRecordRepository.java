package com.weatherspring.repository;

import com.weatherspring.model.WeatherRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing WeatherRecord entities.
 *
 * <p>Provides CRUD operations and custom queries for historical weather data.</p>
 */
@Repository
public interface WeatherRecordRepository extends JpaRepository<WeatherRecord, Long> {

    /**
     * Finds all weather records for a specific location.
     *
     * @param locationId the location ID
     * @param pageable pagination information
     * @return page of weather records
     */
    Page<WeatherRecord> findByLocationId(Long locationId, Pageable pageable);

    /**
     * Finds weather records for a location within a date range.
     *
     * @param locationId the location ID
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @return list of weather records in the range
     */
    @Query("SELECT w FROM WeatherRecord w WHERE w.location.id = :locationId " +
           "AND w.timestamp BETWEEN :startDate AND :endDate ORDER BY w.timestamp DESC")
    List<WeatherRecord> findByLocationIdAndTimestampBetween(
        @Param("locationId") Long locationId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Finds the most recent weather record for a location.
     *
     * @param locationId the location ID
     * @return Optional containing the most recent weather record
     */
    @Query("SELECT w FROM WeatherRecord w WHERE w.location.id = :locationId " +
           "ORDER BY w.timestamp DESC LIMIT 1")
    Optional<WeatherRecord> findMostRecentByLocationId(@Param("locationId") Long locationId);

    /**
     * Deletes old weather records before a specific date.
     *
     * @param date the cutoff date
     */
    @Transactional
    void deleteByTimestampBefore(LocalDateTime date);
}

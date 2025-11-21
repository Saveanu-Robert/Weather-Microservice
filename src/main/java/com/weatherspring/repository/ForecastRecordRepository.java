package com.weatherspring.repository;

import com.weatherspring.model.ForecastRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing ForecastRecord entities.
 *
 * <p>Provides CRUD operations and custom queries for weather forecast data.</p>
 */
@Repository
public interface ForecastRecordRepository extends JpaRepository<ForecastRecord, Long> {

    /**
     * Finds all forecast records for a specific location.
     *
     * @param locationId the location ID
     * @return list of forecast records ordered by date
     */
    @Query("SELECT f FROM ForecastRecord f WHERE f.location.id = :locationId " +
           "ORDER BY f.forecastDate ASC")
    List<ForecastRecord> findByLocationId(@Param("locationId") Long locationId);

    /**
     * Finds forecast records for a location within a date range.
     *
     * @param locationId the location ID
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @return list of forecast records in the range
     */
    @Query("SELECT f FROM ForecastRecord f WHERE f.location.id = :locationId " +
           "AND f.forecastDate BETWEEN :startDate AND :endDate " +
           "ORDER BY f.forecastDate ASC")
    List<ForecastRecord> findByLocationIdAndForecastDateBetween(
        @Param("locationId") Long locationId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Finds a forecast record for a specific location and date.
     *
     * @param locationId the location ID
     * @param forecastDate the forecast date
     * @return Optional containing the forecast record if found
     */
    Optional<ForecastRecord> findByLocationIdAndForecastDate(Long locationId, LocalDate forecastDate);

    /**
     * Finds all future forecasts for a location.
     *
     * @param locationId the location ID
     * @param currentDate the current date
     * @return list of future forecast records
     */
    @Query("SELECT f FROM ForecastRecord f WHERE f.location.id = :locationId " +
           "AND f.forecastDate >= :currentDate ORDER BY f.forecastDate ASC")
    List<ForecastRecord> findFutureForecastsByLocationId(
        @Param("locationId") Long locationId,
        @Param("currentDate") LocalDate currentDate
    );

    /**
     * Deletes old forecast records before a specific date.
     *
     * @param date the cutoff date
     */
    @Transactional
    void deleteByForecastDateBefore(LocalDate date);
}

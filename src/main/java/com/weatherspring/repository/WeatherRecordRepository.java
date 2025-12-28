package com.weatherspring.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.weatherspring.model.WeatherRecord;

/**
 * Repository for managing WeatherRecord entities.
 *
 * <p>Provides CRUD operations and custom queries for historical weather data. Uses @EntityGraph to
 * prevent N+1 queries by eagerly fetching the location relationship.
 */
@Repository
public interface WeatherRecordRepository extends JpaRepository<WeatherRecord, Long> {

  /**
   * Finds all weather records for a specific location. Uses @EntityGraph to eagerly fetch the
   * location in a single query, preventing N+1 problem.
   *
   * @param locationId the location ID
   * @param pageable pagination information
   * @return page of weather records with locations eagerly loaded
   */
  @EntityGraph(attributePaths = {"location"})
  Page<WeatherRecord> findByLocationId(@NotNull Long locationId, @NotNull Pageable pageable);

  /**
   * Finds weather records for a location within a date range. Uses JOIN FETCH to eagerly load
   * location, preventing N+1 queries.
   *
   * @param locationId the location ID
   * @param startDate start of the date range
   * @param endDate end of the date range
   * @return list of weather records with locations eagerly loaded
   */
  @Query(
      "SELECT w FROM WeatherRecord w JOIN FETCH w.location WHERE w.location.id = :locationId "
          + "AND w.timestamp BETWEEN :startDate AND :endDate ORDER BY w.timestamp DESC")
  List<WeatherRecord> findByLocationIdAndTimestampBetween(
      @NotNull @Param("locationId") Long locationId,
      @NotNull @Param("startDate") LocalDateTime startDate,
      @NotNull @Param("endDate") LocalDateTime endDate);

  /**
   * Finds weather records for a location within a date range (paginated). Uses @EntityGraph to
   * eagerly load location, preventing N+1 queries.
   *
   * @param locationId the location ID
   * @param startDate start of the date range
   * @param endDate end of the date range
   * @param pageable pagination information
   * @return page of weather records with locations eagerly loaded
   */
  @EntityGraph(attributePaths = {"location"})
  @Query(
      "SELECT w FROM WeatherRecord w WHERE w.location.id = :locationId "
          + "AND w.timestamp BETWEEN :startDate AND :endDate ORDER BY w.timestamp DESC")
  Page<WeatherRecord> findByLocationIdAndTimestampBetween(
      @NotNull @Param("locationId") Long locationId,
      @NotNull @Param("startDate") LocalDateTime startDate,
      @NotNull @Param("endDate") LocalDateTime endDate,
      @NotNull Pageable pageable);

  /**
   * Finds the most recent weather record for a location. Uses JOIN FETCH to eagerly load location
   * in a single query.
   *
   * @param locationId the location ID
   * @return Optional containing the most recent weather record with location eagerly loaded
   */
  @Query(
      "SELECT w FROM WeatherRecord w JOIN FETCH w.location WHERE w.location.id = :locationId "
          + "ORDER BY w.timestamp DESC LIMIT 1")
  Optional<WeatherRecord> findMostRecentByLocationId(@NotNull @Param("locationId") Long locationId);

  /**
   * Deletes old weather records before a specific date.
   *
   * <p>Note: Transaction management is handled by the calling service method ({@code
   * WeatherService.deleteOldWeatherRecords}).
   *
   * @param date the cutoff date
   * @return the number of records deleted
   */
  Long deleteByTimestampBefore(@NotNull LocalDateTime date);
}

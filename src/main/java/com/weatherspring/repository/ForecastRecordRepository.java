package com.weatherspring.repository;

import java.time.LocalDate;
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
import org.springframework.transaction.annotation.Transactional;

import com.weatherspring.model.ForecastRecord;

/**
 * Repository for managing ForecastRecord entities.
 *
 * <p>Provides CRUD operations and custom queries for weather forecast data. Uses @EntityGraph and
 * JOIN FETCH to prevent N+1 queries by eagerly fetching the location relationship.
 */
@Repository
public interface ForecastRecordRepository extends JpaRepository<ForecastRecord, Long> {

  /**
   * Finds all forecast records for a specific location. Uses JOIN FETCH to eagerly load location,
   * preventing N+1 queries.
   *
   * @param locationId the location ID
   * @return list of forecast records with locations eagerly loaded
   */
  @Query(
      "SELECT f FROM ForecastRecord f JOIN FETCH f.location WHERE f.location.id = :locationId "
          + "ORDER BY f.forecastDate ASC")
  List<ForecastRecord> findByLocationId(@NotNull @Param("locationId") Long locationId);

  /**
   * Finds all forecast records for a specific location (paginated). Uses @EntityGraph to eagerly
   * load location, preventing N+1 queries.
   *
   * @param locationId the location ID
   * @param pageable pagination information
   * @return page of forecast records with locations eagerly loaded
   */
  @EntityGraph(attributePaths = {"location"})
  @Query(
      "SELECT f FROM ForecastRecord f WHERE f.location.id = :locationId ORDER BY f.forecastDate ASC")
  Page<ForecastRecord> findByLocationId(
      @NotNull @Param("locationId") Long locationId, @NotNull Pageable pageable);

  /**
   * Finds forecast records for a location within a date range. Uses JOIN FETCH to eagerly load
   * location, preventing N+1 queries.
   *
   * @param locationId the location ID
   * @param startDate start of the date range
   * @param endDate end of the date range
   * @return list of forecast records with locations eagerly loaded
   */
  @Query(
      "SELECT f FROM ForecastRecord f JOIN FETCH f.location WHERE f.location.id = :locationId "
          + "AND f.forecastDate BETWEEN :startDate AND :endDate "
          + "ORDER BY f.forecastDate ASC")
  List<ForecastRecord> findByLocationIdAndForecastDateBetween(
      @NotNull @Param("locationId") Long locationId,
      @NotNull @Param("startDate") LocalDate startDate,
      @NotNull @Param("endDate") LocalDate endDate);

  /**
   * Finds forecast records for a location within a date range (paginated). Uses @EntityGraph to
   * eagerly load location, preventing N+1 queries.
   *
   * @param locationId the location ID
   * @param startDate start of the date range
   * @param endDate end of the date range
   * @param pageable pagination information
   * @return page of forecast records with locations eagerly loaded
   */
  @EntityGraph(attributePaths = {"location"})
  @Query(
      "SELECT f FROM ForecastRecord f WHERE f.location.id = :locationId "
          + "AND f.forecastDate BETWEEN :startDate AND :endDate "
          + "ORDER BY f.forecastDate ASC")
  Page<ForecastRecord> findByLocationIdAndForecastDateBetween(
      @NotNull @Param("locationId") Long locationId,
      @NotNull @Param("startDate") LocalDate startDate,
      @NotNull @Param("endDate") LocalDate endDate,
      @NotNull Pageable pageable);

  /**
   * Finds a forecast record for a specific location and date. Uses @EntityGraph to eagerly fetch
   * location in a single query.
   *
   * @param locationId the location ID
   * @param forecastDate the forecast date
   * @return Optional containing the forecast record with location eagerly loaded
   */
  @EntityGraph(attributePaths = {"location"})
  Optional<ForecastRecord> findByLocationIdAndForecastDate(
      @NotNull Long locationId, @NotNull LocalDate forecastDate);

  /**
   * Finds all future forecasts for a location. Uses JOIN FETCH to eagerly load location, preventing
   * N+1 queries.
   *
   * @param locationId the location ID
   * @param currentDate the current date
   * @return list of future forecast records with locations eagerly loaded
   */
  @Query(
      "SELECT f FROM ForecastRecord f JOIN FETCH f.location WHERE f.location.id = :locationId "
          + "AND f.forecastDate >= :currentDate ORDER BY f.forecastDate ASC")
  List<ForecastRecord> findFutureForecastsByLocationId(
      @NotNull @Param("locationId") Long locationId,
      @NotNull @Param("currentDate") LocalDate currentDate);

  /**
   * Finds all future forecasts for a location (paginated). Uses @EntityGraph to eagerly load
   * location, preventing N+1 queries.
   *
   * @param locationId the location ID
   * @param currentDate the current date
   * @param pageable pagination information
   * @return page of future forecast records with locations eagerly loaded
   */
  @EntityGraph(attributePaths = {"location"})
  @Query(
      "SELECT f FROM ForecastRecord f WHERE f.location.id = :locationId "
          + "AND f.forecastDate >= :currentDate ORDER BY f.forecastDate ASC")
  Page<ForecastRecord> findFutureForecastsByLocationId(
      @NotNull @Param("locationId") Long locationId,
      @NotNull @Param("currentDate") LocalDate currentDate,
      @NotNull Pageable pageable);

  /**
   * Finds existing forecast records for a location on multiple specific dates. Uses JOIN FETCH to
   * eagerly load location, preventing N+1 queries.
   *
   * <p>This method is essential for bulk operations to check which forecasts already exist before
   * attempting to save, avoiding unique constraint violations and N+1 queries.
   *
   * @param locationId the location ID
   * @param dates the collection of forecast dates to check
   * @return list of existing forecast records with locations eagerly loaded
   */
  @Query(
      "SELECT f FROM ForecastRecord f JOIN FETCH f.location WHERE f.location.id = :locationId "
          + "AND f.forecastDate IN :dates")
  List<ForecastRecord> findByLocationIdAndForecastDateIn(
      @NotNull @Param("locationId") Long locationId,
      @NotNull @Param("dates") List<LocalDate> dates);

  /**
   * Deletes old forecast records before a specific date.
   *
   * @param date the cutoff date
   * @return the number of records deleted
   */
  @Transactional
  Long deleteByForecastDateBefore(@NotNull LocalDate date);
}

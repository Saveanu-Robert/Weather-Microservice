package com.weatherspring.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.weatherspring.annotation.Auditable;
import com.weatherspring.listener.AuditableEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Weather forecast for a specific date and location.
 *
 * <p>Stores predicted conditions including temperature ranges, precipitation probability, and
 * sunrise/sunset times. Each forecast is tied to a specific location and date through a unique
 * constraint that prevents duplicate forecasts.
 *
 * <p>Uses EAGER fetch for location since forecasts are almost always displayed with their location
 * name. This prevents N+1 query problems when loading multiple forecasts.
 */
@Entity
@Table(
    name = "forecast_records",
    indexes = {
      @Index(name = "idx_forecast_location_id", columnList = "location_id"),
      @Index(name = "idx_forecast_date", columnList = "forecast_date")
    },
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_location_forecast_date",
          columnNames = {"location_id", "forecast_date"})
    })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"location"})
@EqualsAndHashCode(of = "id", callSuper = false)
@Auditable
@EntityListeners(AuditableEntityListener.class)
public class ForecastRecord extends BaseAuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "location_id", nullable = false)
  private Location location;

  @Column(name = "forecast_date", nullable = false)
  private LocalDate forecastDate;

  @Column(name = "max_temperature", nullable = false)
  private Double maxTemperature;

  @Column(name = "min_temperature", nullable = false)
  private Double minTemperature;

  @Column(name = "avg_temperature")
  private Double avgTemperature;

  @Column(name = "max_wind_speed")
  private Double maxWindSpeed;

  @Column(name = "avg_humidity")
  private Integer avgHumidity;

  @Column(nullable = false, length = 100)
  private String condition;

  @Column(length = 500)
  private String description;

  @Column(name = "precipitation_mm")
  private Double precipitationMm;

  @Column(name = "precipitation_probability")
  private Integer precipitationProbability;

  @Column(name = "uv_index")
  private Double uvIndex;

  @Column(name = "sunrise_time")
  private String sunriseTime;

  @Column(name = "sunset_time")
  private String sunsetTime;
}

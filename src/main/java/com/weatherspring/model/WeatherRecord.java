package com.weatherspring.model;

import java.time.LocalDateTime;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import com.weatherspring.annotation.Auditable;
import com.weatherspring.listener.AuditableEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Historical snapshot of weather conditions at a specific location and time.
 *
 * <p>Each record captures the weather at a moment in time. Stores temperature, humidity, wind,
 * precipitation, and atmospheric pressure. Multiple records for the same location build up
 * historical weather trends.
 *
 * <p>Uses EAGER fetch for location since weather records are almost always displayed with their
 * location name. This prevents N+1 query problems when loading multiple records.
 */
@Entity
@Table(
    name = "weather_records",
    indexes = {
      @Index(name = "idx_weather_location_id", columnList = "location_id"),
      @Index(name = "idx_weather_timestamp", columnList = "timestamp")
    })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"location"})
@EqualsAndHashCode(of = "id", callSuper = false)
@Auditable
@EntityListeners(AuditableEntityListener.class)
public class WeatherRecord extends BaseAuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "location_id", nullable = false)
  private Location location;

  @Column(nullable = false)
  private Double temperature;

  @Column(name = "feels_like")
  private Double feelsLike;

  @Column(nullable = false)
  private Integer humidity;

  @Column(name = "wind_speed", nullable = false)
  private Double windSpeed;

  @Column(name = "wind_direction")
  private String windDirection;

  @Column(nullable = false, length = 100)
  private String condition;

  @Column(length = 500)
  private String description;

  @Column(name = "pressure_mb")
  private Double pressureMb;

  @Column(name = "precipitation_mm")
  private Double precipitationMm;

  @Column(name = "cloud_coverage")
  private Integer cloudCoverage;

  @Column(name = "uv_index")
  private Double uvIndex;

  @Column(nullable = false)
  private LocalDateTime timestamp;

  /**
   * Ensures timestamp is set before persisting.
   *
   * <p>Normally timestamp is set from API response, but this provides a fallback
   * to prevent constraint violations if timestamp is somehow null.
   */
  @PrePersist
  protected void onCreate() {
    if (timestamp == null) {
      timestamp = LocalDateTime.now();
    }
  }
}

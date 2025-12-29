package com.weatherspring.model;

import java.time.LocalDateTime;

import com.weatherspring.listener.AuditableEntityListener;
import jakarta.persistence.*;

import com.weatherspring.annotation.Auditable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Historical weather data snapshot for a specific location and time.
 *
 * <p>Stores temperature, humidity, wind, precipitation, and other weather conditions. Uses EAGER
 * fetch for location since weather records are almost always used with their location data.
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

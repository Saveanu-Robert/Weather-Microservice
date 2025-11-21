package com.weatherspring.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a historical weather record.
 *
 * <p>Stores weather data snapshots including temperature, humidity, wind speed,
 * and conditions for a specific location at a specific time.</p>
 */
@Entity
@Table(name = "weather_records", indexes = {
    @Index(name = "idx_weather_location_id", columnList = "location_id"),
    @Index(name = "idx_weather_timestamp", columnList = "timestamp")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "WeatherRecord{" +
                "id=" + id +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", condition='" + condition + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

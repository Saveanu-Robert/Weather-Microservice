package com.weatherspring.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a weather forecast for a future date.
 *
 * <p>Stores predicted weather conditions including temperature ranges,
 * precipitation probability, and general conditions for a specific date.</p>
 */
@Entity
@Table(name = "forecast_records", indexes = {
    @Index(name = "idx_forecast_location_id", columnList = "location_id"),
    @Index(name = "idx_forecast_date", columnList = "forecast_date")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForecastRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "ForecastRecord{" +
                "id=" + id +
                ", forecastDate=" + forecastDate +
                ", maxTemperature=" + maxTemperature +
                ", minTemperature=" + minTemperature +
                ", condition='" + condition + '\'' +
                '}';
    }
}

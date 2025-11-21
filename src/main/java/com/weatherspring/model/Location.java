package com.weatherspring.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a geographical location for weather tracking.
 *
 * <p>This entity stores location information including name, country, and coordinates.
 * It serves as a reference for weather data and forecasts.</p>
 */
@Entity
@Table(name = "locations", indexes = {
    @Index(name = "idx_location_name", columnList = "name"),
    @Index(name = "idx_location_country", columnList = "country")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Location{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}

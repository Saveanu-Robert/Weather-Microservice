package com.weatherspring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
 * Geographical location tracked for weather data and forecasts.
 *
 * <p>Each location is uniquely identified by the combination of name and country. Stores
 * coordinates for fetching weather data from the external API. The region field stores
 * state/province information when available.
 *
 * <p>Database indexes on name and country speed up location searches. The unique constraint
 * prevents duplicate locations in different formats (e.g., "London, UK" vs "London, United Kingdom").
 */
@Entity
@Table(
    name = "locations",
    indexes = {
      @Index(name = "idx_location_name", columnList = "name"),
      @Index(name = "idx_location_country", columnList = "country")
    },
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_location_name_country",
          columnNames = {"name", "country"})
    })
@Getter
// No @Setter - updates go through LocationMapper for controlled modifications
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id", callSuper = false)
@Auditable
@EntityListeners(AuditableEntityListener.class)
public class Location extends BaseAuditableEntity {

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
}

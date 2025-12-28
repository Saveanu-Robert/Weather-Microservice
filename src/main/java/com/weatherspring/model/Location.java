package com.weatherspring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.weatherspring.annotation.Auditable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Geographical location used for weather tracking and forecasts. Stores the location's name,
 * country, coordinates, and optional region information.
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
@jakarta.persistence.EntityListeners(com.weatherspring.listener.AuditableEntityListener.class)
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

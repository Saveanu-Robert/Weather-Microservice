package com.weatherspring.listener;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.weatherspring.model.Location;

/** Unit tests for AuditableEntityListener. */
class AuditableEntityListenerTest {

  private AuditableEntityListener listener;

  @BeforeEach
  void setUp() {
    listener = new AuditableEntityListener();
  }

  @Test
  void setCreationDate_WithAuditableEntity_SetsCreatedAtAndUpdatedAt() {
    // Arrange
    Location location =
        Location.builder()
            .name("London")
            .country("United Kingdom")
            .latitude(51.5074)
            .longitude(-0.1278)
            .build();

    LocalDateTime before = LocalDateTime.now();

    // Act
    listener.setCreationDate(location);

    LocalDateTime after = LocalDateTime.now();

    // Assert
    assertThat(location.getCreatedAt()).isNotNull();
    assertThat(location.getUpdatedAt()).isNotNull();
    assertThat(location.getCreatedAt()).isEqualTo(location.getUpdatedAt());
    assertThat(location.getCreatedAt()).isBetween(before, after);
  }

  @Test
  void setCreationDate_WithNonAuditableEntity_ThrowsIllegalStateException() {
    // Arrange
    Object nonAuditableEntity = new Object();

    // Act & Assert
    assertThatThrownBy(() -> listener.setCreationDate(nonAuditableEntity))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("must extend BaseAuditableEntity");
  }

  @Test
  void setUpdateDate_WithAuditableEntity_UpdatesUpdatedAtOnly() throws InterruptedException {
    // Arrange
    Location location =
        Location.builder()
            .name("Paris")
            .country("France")
            .latitude(48.8566)
            .longitude(2.3522)
            .build();

    // Set initial timestamps
    LocalDateTime initialCreatedAt = LocalDateTime.now().minusDays(1);
    location.setCreatedAt(initialCreatedAt);
    location.setUpdatedAt(initialCreatedAt);

    // Small delay to ensure timestamp difference
    Thread.sleep(10);

    LocalDateTime beforeUpdate = LocalDateTime.now();

    // Act
    listener.setUpdateDate(location);

    LocalDateTime afterUpdate = LocalDateTime.now();

    // Assert
    assertThat(location.getCreatedAt()).isEqualTo(initialCreatedAt); // Should not change
    assertThat(location.getUpdatedAt()).isNotNull();
    assertThat(location.getUpdatedAt()).isNotEqualTo(initialCreatedAt);
    assertThat(location.getUpdatedAt()).isBetween(beforeUpdate, afterUpdate);
  }

  @Test
  void setUpdateDate_WithNonAuditableEntity_ThrowsIllegalStateException() {
    // Arrange
    Object nonAuditableEntity = new Object();

    // Act & Assert
    assertThatThrownBy(() -> listener.setUpdateDate(nonAuditableEntity))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("must extend BaseAuditableEntity");
  }

  @Test
  void setCreationDate_WithAuditableEntity_SetsTimestampsWithinReasonableTime() {
    // Arrange
    Location location =
        Location.builder()
            .name("Tokyo")
            .country("Japan")
            .latitude(35.6762)
            .longitude(139.6503)
            .build();

    LocalDateTime before = LocalDateTime.now();

    // Act
    listener.setCreationDate(location);

    // Assert - timestamps should be very close to current time
    assertThat(location.getCreatedAt()).isAfter(before.minusSeconds(1));
    assertThat(location.getUpdatedAt()).isAfter(before.minusSeconds(1));
  }

  @Test
  void setUpdateDate_PreservesCreatedAt() {
    // Arrange
    Location location =
        Location.builder()
            .name("New York")
            .country("United States")
            .latitude(40.7128)
            .longitude(-74.0060)
            .build();

    LocalDateTime originalCreatedAt = LocalDateTime.now().minusMonths(1);
    LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(5);
    location.setCreatedAt(originalCreatedAt);
    location.setUpdatedAt(originalUpdatedAt);

    // Act
    listener.setUpdateDate(location);

    // Assert
    assertThat(location.getCreatedAt()).isEqualTo(originalCreatedAt);
    assertThat(location.getUpdatedAt()).isAfter(originalUpdatedAt);
    assertThat(location.getUpdatedAt()).isNotEqualTo(originalCreatedAt);
  }

  /** Test entity that does NOT extend BaseAuditableEntity to test error cases. */
  private static class NonAuditableTestEntity {
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  @Test
  void setCreationDate_WithCustomNonAuditableClass_IncludesClassNameInError() {
    // Arrange
    NonAuditableTestEntity entity = new NonAuditableTestEntity();

    // Act & Assert
    assertThatThrownBy(() -> listener.setCreationDate(entity))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("NonAuditableTestEntity")
        .hasMessageContaining("must extend BaseAuditableEntity");
  }

  @Test
  void setUpdateDate_WithCustomNonAuditableClass_IncludesClassNameInError() {
    // Arrange
    NonAuditableTestEntity entity = new NonAuditableTestEntity();

    // Act & Assert
    assertThatThrownBy(() -> listener.setUpdateDate(entity))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("NonAuditableTestEntity")
        .hasMessageContaining("must extend BaseAuditableEntity");
  }
}

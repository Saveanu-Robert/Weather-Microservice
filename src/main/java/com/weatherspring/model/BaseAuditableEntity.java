package com.weatherspring.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import lombok.Getter;

/**
 * Base class that automatically tracks when entities are created and updated.
 *
 * <p>Entities extending this class get createdAt and updatedAt timestamps managed automatically by
 * AuditableEntityListener.
 */
@MappedSuperclass
@Getter
public abstract class BaseAuditableEntity {

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // Public setters for AuditableEntityListener to update timestamps
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}

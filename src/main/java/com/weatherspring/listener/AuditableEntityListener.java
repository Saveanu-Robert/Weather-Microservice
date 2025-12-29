package com.weatherspring.listener;

import java.time.LocalDateTime;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import com.weatherspring.model.BaseAuditableEntity;

import lombok.extern.slf4j.Slf4j;

/**
 * JPA entity listener for automatic timestamp auditing.
 *
 * <p>This listener is automatically attached to entities annotated with {@link
 * com.weatherspring.annotation.Auditable}. It manages {@code createdAt} and {@code updatedAt}
 * timestamp fields using type-safe methods instead of reflection.
 *
 * <p><strong>Type-Safe Approach:</strong> Entities must extend {@link BaseAuditableEntity} to
 * benefit from automatic auditing. This provides:
 *
 * <ul>
 *   <li>Compile-time type checking
 *   <li>Better performance (no reflection overhead)
 *   <li>Clear contract - entities must extend the base class
 *   <li>Fails fast at startup if entity doesn't extend base class
 * </ul>
 *
 * <p>Lifecycle hooks:
 *
 * <ul>
 *   <li>{@code @PrePersist} - Sets both {@code createdAt} and {@code updatedAt} to current time
 *   <li>{@code @PreUpdate} - Updates only {@code updatedAt} to current time
 * </ul>
 */
@Slf4j
public class AuditableEntityListener {

  /**
   * Called before a new entity is persisted to the database. Sets {@code createdAt} and {@code
   * updatedAt} to the current timestamp.
   *
   * @param entity the entity being persisted (must extend {@link BaseAuditableEntity})
   * @throws IllegalStateException if entity doesn't extend BaseAuditableEntity
   */
  @PrePersist
  public void setCreationDate(Object entity) {
    if (!(entity instanceof BaseAuditableEntity auditableEntity)) {
      log.error(
          "Entity {} is marked @Auditable but doesn't extend BaseAuditableEntity",
          entity.getClass().getSimpleName());
      throw new IllegalStateException(
          "Entity "
              + entity.getClass().getSimpleName()
              + " must extend BaseAuditableEntity to use @Auditable annotation");
    }

    LocalDateTime now = LocalDateTime.now();
    auditableEntity.setCreatedAt(now);
    auditableEntity.setUpdatedAt(now);
  }

  /**
   * Called before an existing entity is updated in the database. Sets {@code updatedAt} to the
   * current timestamp.
   *
   * @param entity the entity being updated (must extend {@link BaseAuditableEntity})
   * @throws IllegalStateException if entity doesn't extend BaseAuditableEntity
   */
  @PreUpdate
  public void setUpdateDate(Object entity) {
    if (!(entity instanceof BaseAuditableEntity auditableEntity)) {
      log.error(
          "Entity {} is marked @Auditable but doesn't extend BaseAuditableEntity",
          entity.getClass().getSimpleName());
      throw new IllegalStateException(
          "Entity "
              + entity.getClass().getSimpleName()
              + " must extend BaseAuditableEntity to use @Auditable annotation");
    }

    auditableEntity.setUpdatedAt(LocalDateTime.now());
  }
}

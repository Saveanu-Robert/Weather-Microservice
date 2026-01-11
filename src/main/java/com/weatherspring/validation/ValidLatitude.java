package com.weatherspring.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Composite validation annotation for latitude coordinates.
 *
 * <p>Validates that a latitude value is:
 *
 * <ul>
 *   <li>Not null
 *   <li>Between -90 and 90 degrees
 * </ul>
 *
 * <p>This annotation combines multiple standard validation constraints into a single,
 * domain-specific annotation for improved code clarity and maintainability.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotNull(message = "Latitude is required")
@Min(value = ValidationConstants.LATITUDE_MIN, message = ValidationConstants.LATITUDE_RANGE_MESSAGE)
@Max(value = ValidationConstants.LATITUDE_MAX, message = ValidationConstants.LATITUDE_RANGE_MESSAGE)
@Constraint(validatedBy = {})
public @interface ValidLatitude {

  /**
   * The error message returned when validation fails.
   *
   * <p>You can override this to provide custom messages for specific use cases. For example:
   * {@code @ValidLatitude(message = "Latitude must be between -90 and 90 degrees")}
   *
   * @return the validation error message
   */
  String message() default "Invalid latitude coordinate";

  /**
   * Validation groups this constraint belongs to.
   *
   * <p>Groups allow you to apply different validation rules in different scenarios. For example,
   * you might validate required fields during creation but skip them during updates.
   *
   * @return array of validation groups
   */
  Class<?>[] groups() default {};

  /**
   * Additional metadata about the validation failure.
   *
   * <p>Payload allows you to attach custom metadata to constraint violations. This is useful for
   * categorizing errors by severity or providing additional context to error handlers.
   *
   * @return array of payload types
   */
  Class<? extends Payload>[] payload() default {};
}

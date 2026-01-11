package com.weatherspring.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Composite validation annotation for location names.
 *
 * <p>Validates that a location name is:
 *
 * <ul>
 *   <li>Not null or blank
 *   <li>Between 2 and 100 characters in length
 * </ul>
 *
 * <p>This annotation combines multiple standard validation constraints into a single,
 * domain-specific annotation for improved code clarity and maintainability.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "Location name is required")
@Size(
    min = ValidationConstants.LOCATION_NAME_MIN_LENGTH,
    max = ValidationConstants.LOCATION_NAME_MAX_LENGTH,
    message = ValidationConstants.LOCATION_NAME_SIZE_MESSAGE)
@Constraint(validatedBy = {})
public @interface ValidLocationName {

  /**
   * The error message returned when validation fails.
   *
   * <p>You can override this to provide custom messages for specific use cases. For example:
   * {@code @ValidLocationName(message = "Please enter a valid city or location name")}
   *
   * @return the validation error message
   */
  String message() default "Invalid location name";

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

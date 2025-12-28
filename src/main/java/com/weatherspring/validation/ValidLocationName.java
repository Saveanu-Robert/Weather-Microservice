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

  String message() default "Invalid location name";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}

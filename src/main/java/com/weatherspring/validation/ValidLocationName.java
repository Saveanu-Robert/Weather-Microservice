package com.weatherspring.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite validation annotation for location names.
 *
 * <p>Validates that a location name is:</p>
 * <ul>
 *   <li>Not null or blank</li>
 *   <li>Between 2 and 100 characters in length</li>
 * </ul>
 *
 * <p>This annotation combines multiple standard validation constraints
 * into a single, domain-specific annotation for improved code clarity
 * and maintainability.</p>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "Location name is required")
@Size(min = ValidationConstants.LOCATION_NAME_MIN_LENGTH,
      max = ValidationConstants.LOCATION_NAME_MAX_LENGTH,
      message = ValidationConstants.LOCATION_NAME_SIZE_MESSAGE)
@Constraint(validatedBy = {})
public @interface ValidLocationName {

    String message() default "Invalid location name";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

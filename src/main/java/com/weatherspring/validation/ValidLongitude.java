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
 * Composite validation annotation for longitude coordinates.
 *
 * <p>Validates that a longitude value is:
 *
 * <ul>
 *   <li>Not null
 *   <li>Between -180 and 180 degrees
 * </ul>
 *
 * <p>This annotation combines multiple standard validation constraints into a single,
 * domain-specific annotation for improved code clarity and maintainability.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotNull(message = "Longitude is required")
@Min(
    value = ValidationConstants.LONGITUDE_MIN,
    message = ValidationConstants.LONGITUDE_RANGE_MESSAGE)
@Max(
    value = ValidationConstants.LONGITUDE_MAX,
    message = ValidationConstants.LONGITUDE_RANGE_MESSAGE)
@Constraint(validatedBy = {})
public @interface ValidLongitude {

  String message() default "Invalid longitude coordinate";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}

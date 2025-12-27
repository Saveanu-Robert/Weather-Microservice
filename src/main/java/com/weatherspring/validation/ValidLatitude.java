package com.weatherspring.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite validation annotation for latitude coordinates.
 *
 * <p>Validates that a latitude value is:</p>
 * <ul>
 *   <li>Not null</li>
 *   <li>Between -90 and 90 degrees</li>
 * </ul>
 *
 * <p>This annotation combines multiple standard validation constraints
 * into a single, domain-specific annotation for improved code clarity
 * and maintainability.</p>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotNull(message = "Latitude is required")
@Min(value = ValidationConstants.LATITUDE_MIN, message = ValidationConstants.LATITUDE_RANGE_MESSAGE)
@Max(value = ValidationConstants.LATITUDE_MAX, message = ValidationConstants.LATITUDE_RANGE_MESSAGE)
@Constraint(validatedBy = {})
public @interface ValidLatitude {

    String message() default "Invalid latitude coordinate";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

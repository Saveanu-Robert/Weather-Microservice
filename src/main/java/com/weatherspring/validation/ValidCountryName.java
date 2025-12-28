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
 * Composite validation annotation for country names.
 *
 * <p>Validates that a country name is:
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
@NotBlank(message = "Country name is required")
@Size(
    min = ValidationConstants.COUNTRY_NAME_MIN_LENGTH,
    max = ValidationConstants.COUNTRY_NAME_MAX_LENGTH,
    message = ValidationConstants.COUNTRY_NAME_SIZE_MESSAGE)
@Constraint(validatedBy = {})
public @interface ValidCountryName {

  String message() default "Invalid country name";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}

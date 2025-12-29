package com.weatherspring.config;

import jakarta.persistence.EntityManager;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA configuration for enabling auditing.
 *
 * <p>This configuration is only loaded when JPA is available (full application context), not during
 * slice tests like @WebMvcTest.
 */
@Configuration
@EnableJpaAuditing
@ConditionalOnClass(EntityManager.class)
public class JpaConfig {}

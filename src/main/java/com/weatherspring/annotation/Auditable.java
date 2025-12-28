package com.weatherspring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.persistence.EntityListeners;

import com.weatherspring.listener.AuditableEntityListener;

/**
 * Marks an entity for automatic timestamp tracking. Sets createdAt when the entity is created and
 * updatedAt whenever it's modified.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EntityListeners(AuditableEntityListener.class)
public @interface Auditable {}

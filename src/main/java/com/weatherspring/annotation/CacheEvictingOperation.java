package com.weatherspring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Meta-annotation combining {@link Transactional} and {@link CacheEvict}.
 *
 * <p>This annotation is typically used on write operations (create, update, delete) that need to
 * evict cache entries after the transaction completes.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @CacheEvictingOperation(cacheNames = "users", key = "#id")
 * public void deleteUser(Long id) {
 *     userRepository.deleteById(id);
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Transactional
@CacheEvict
public @interface CacheEvictingOperation {

  @AliasFor(annotation = CacheEvict.class, attribute = "value")
  String[] cacheNames() default {};

  @AliasFor(annotation = CacheEvict.class, attribute = "key")
  String key() default "";

  @AliasFor(annotation = CacheEvict.class, attribute = "allEntries")
  boolean allEntries() default false;

  @AliasFor(annotation = CacheEvict.class, attribute = "beforeInvocation")
  boolean beforeInvocation() default false;
}

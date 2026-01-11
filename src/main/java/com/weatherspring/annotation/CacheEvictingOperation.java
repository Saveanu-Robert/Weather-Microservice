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

  /**
   * Names of the caches to clear. You can specify multiple caches if the operation affects data in
   * different cache regions.
   *
   * @return array of cache names
   */
  @AliasFor(annotation = CacheEvict.class, attribute = "value")
  String[] cacheNames() default {};

  /**
   * Spring Expression Language (SpEL) expression to compute the cache key. For example, use "#id"
   * to use the method's id parameter as the key.
   *
   * @return SpEL expression for the cache key
   */
  @AliasFor(annotation = CacheEvict.class, attribute = "key")
  String key() default "";

  /**
   * When true, clears all entries in the specified caches instead of just one specific key. Use
   * this for operations that affect multiple cache entries.
   *
   * @return true to clear all cache entries, false to clear only the specified key
   */
  @AliasFor(annotation = CacheEvict.class, attribute = "allEntries")
  boolean allEntries() default false;

  /**
   * When true, clears the cache before running the method instead of after. Normally you want
   * false so the cache is only cleared if the method succeeds.
   *
   * @return true to clear cache before method execution, false to clear after
   */
  @AliasFor(annotation = CacheEvict.class, attribute = "beforeInvocation")
  boolean beforeInvocation() default false;
}

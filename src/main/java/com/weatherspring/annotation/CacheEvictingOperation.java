package com.weatherspring.annotation;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation combining {@link Transactional} and {@link CacheEvict}.
 *
 * <p>This annotation is typically used on write operations (create, update, delete)
 * that need to evict cache entries after the transaction completes.</p>
 *
 * <p>Example usage:</p>
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
     * Names of the caches to evict.
     */
    @AliasFor(annotation = CacheEvict.class, attribute = "value")
    String[] cacheNames() default {};

    /**
     * Spring Expression Language (SpEL) expression for the cache key.
     */
    @AliasFor(annotation = CacheEvict.class, attribute = "key")
    String key() default "";

    /**
     * Whether to evict all entries from the cache.
     */
    @AliasFor(annotation = CacheEvict.class, attribute = "allEntries")
    boolean allEntries() default false;

    /**
     * Whether to evict entries before the method is invoked.
     */
    @AliasFor(annotation = CacheEvict.class, attribute = "beforeInvocation")
    boolean beforeInvocation() default false;
}

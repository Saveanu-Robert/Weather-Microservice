package com.weatherspring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Meta-annotation combining common annotations for read-only service classes.
 *
 * <p>This annotation combines the following standard annotations:
 *
 * <ul>
 *   <li>{@link Service @Service} - Marks the class as a Spring service component
 *   <li>{@link Transactional @Transactional(readOnly = true)} - Enables read-only transaction
 *       management
 * </ul>
 *
 * <p><strong>Note:</strong> Due to Lombok limitations, {@code @Slf4j} and
 * {@code @RequiredArgsConstructor} must be added separately to classes using this annotation.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * @ReadOnlyService
 * @Slf4j
 * @RequiredArgsConstructor
 * public class UserService {
 *     private final UserRepository userRepository;
 *     private final UserMapper userMapper;
 *
 *     public User findUserById(Long id) {
 *         log.info("Finding user with ID: {}", id);
 *         // Service logic...
 *     }
 * }
 * }</pre>
 *
 * <p>Benefits:
 *
 * <ul>
 *   <li>Reduces boilerplate from 4 annotations to 3
 *   <li>Self-documenting - clearly indicates read-only service pattern
 *   <li>Ensures consistent transactional configuration across all read-only services
 * </ul>
 *
 * <p>For services that perform write operations, use {@link Service @Service} with {@link
 * Transactional @Transactional} (without {@code readOnly = true}) instead.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
@Transactional(readOnly = true)
public @interface ReadOnlyService {

  /**
   * Alias for {@link Service#value()}. Allows specifying a custom bean name for the service.
   *
   * @return the bean name (default is empty string for auto-generated name)
   */
  @AliasFor(annotation = Service.class, attribute = "value")
  String value() default "";
}

package com.weatherspring.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test security configuration for unit and integration tests.
 *
 * <p>Provides a simplified security setup with in-memory users for testing.
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

  /**
   * Provides a password encoder for test users.
   *
   * <p>Uses BCrypt to hash passwords for the in-memory test users. This matches the production
   * password encoder to ensure tests behave realistically.
   *
   * @return BCrypt password encoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Provides in-memory test users for authentication testing.
   *
   * <p>Creates three test users with different roles: a regular user, an admin with elevated
   * permissions, and an actuator admin for monitoring endpoints. All users have the password
   * "password" for simplicity in tests.
   *
   * @param passwordEncoder the password encoder to hash test passwords
   * @return user details service with pre-configured test users
   */
  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails user =
        User.builder()
            .username("user")
            .password(passwordEncoder.encode("password"))
            .roles("USER")
            .build();

    UserDetails admin =
        User.builder()
            .username("admin")
            .password(passwordEncoder.encode("password"))
            .roles("USER", "ADMIN")
            .build();

    UserDetails actuator =
        User.builder()
            .username("actuator")
            .password(passwordEncoder.encode("password"))
            .roles("ACTUATOR_ADMIN")
            .build();

    return new InMemoryUserDetailsManager(user, admin, actuator);
  }

  /**
   * Configures a permissive security filter chain for testing.
   *
   * <p>Disables CSRF protection and allows all requests without authentication. This simplifies
   * testing by removing security barriers, letting tests focus on business logic rather than
   * authentication mechanics.
   *
   * @param http the HTTP security configuration
   * @return configured security filter chain
   * @throws Exception if configuration fails
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

    return http.build();
  }
}

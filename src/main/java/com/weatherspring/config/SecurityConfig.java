package com.weatherspring.config;

import java.util.List;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security setup with basic authentication.
 *
 * <p>Uses in-memory users for development/demo. In production, replace this with a proper
 * database-backed user store, LDAP, or OAuth2.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /**
   * Security filter chain with role-based access control.
   *
   * <p>Access rules:
   * <ul>
   *   <li>Actuator endpoints: ACTUATOR_ADMIN role
   *   <li>H2 console: ADMIN role
   *   <li>API docs (Swagger): public
   *   <li>DELETE /api/**: ADMIN role
   *   <li>POST/PUT /api/**: USER role
   *   <li>GET /api/**: public
   * </ul>
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(
            auth ->
                auth
                    // Actuator endpoints - require admin role
                    .requestMatchers(EndpointRequest.toAnyEndpoint())
                    .hasRole("ACTUATOR_ADMIN")

                    // H2 Console - only in dev profile with authentication
                    .requestMatchers("/h2-console/**")
                    .hasRole("ADMIN")

                    // API Documentation - public access
                    .requestMatchers(
                        "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api-docs/**")
                    .permitAll()

                    // DELETE operations - admin only
                    .requestMatchers(HttpMethod.DELETE, "/api/**")
                    .hasRole("ADMIN")

                    // POST/PUT operations - authenticated users
                    .requestMatchers(HttpMethod.POST, "/api/**")
                    .hasRole("USER")
                    .requestMatchers(HttpMethod.PUT, "/api/**")
                    .hasRole("USER")

                    // GET operations - public read access
                    .requestMatchers(HttpMethod.GET, "/api/**")
                    .permitAll()

                    // All other requests require authentication
                    .anyRequest()
                    .authenticated())
        .httpBasic(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for REST API
        .cors(Customizer.withDefaults()) // Enable CORS with custom configuration
        .headers(
            headers ->
                headers.frameOptions(frame -> frame.sameOrigin()) // Allow H2 console in same origin
            )
        .build();
  }

  /**
   * CORS configuration for cross-origin requests.
   *
   * <p>Allows requests from common development ports by default (localhost:3000, 4200, 8080).
   * In production, set CORS_ALLOWED_ORIGINS environment variable to your actual frontend URLs.
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    String allowedOriginsEnv = System.getenv("CORS_ALLOWED_ORIGINS");
    if (allowedOriginsEnv != null && !allowedOriginsEnv.isBlank()) {
      configuration.setAllowedOrigins(List.of(allowedOriginsEnv.split(",")));
    } else {
      // Default to common dev ports - change this in production
      configuration.setAllowedOrigins(
          List.of("http://localhost:3000", "http://localhost:4200", "http://localhost:8080"));
    }

    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));

    configuration.setAllowedHeaders(
        List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));

    configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));

    configuration.setAllowCredentials(true);

    String maxAgeEnv = System.getenv("CORS_MAX_AGE");
    long maxAge = 3600L;
    if (maxAgeEnv != null && !maxAgeEnv.isBlank()) {
      try {
        maxAge = Long.parseLong(maxAgeEnv);
      } catch (NumberFormatException e) {
        org.slf4j.LoggerFactory.getLogger(SecurityConfig.class)
            .warn("Invalid CORS_MAX_AGE value '{}', using default: 3600 seconds", maxAgeEnv);
      }
    }
    configuration.setMaxAge(maxAge);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    source.registerCorsConfiguration("/actuator/**", configuration);
    return source;
  }

  /**
   * Password encoder using BCrypt with strength 12.
   *
   * <p>Strength 12 provides strong security (2^12 = 4096 rounds) while maintaining
   * reasonable performance (~200ms per hash on modern hardware).
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  /**
   * Creates three in-memory users for development: user/user123, admin/admin123, and
   * actuator/actuator123.
   *
   * <p>IMPORTANT: This is for development only. In production, use environment variables to set
   * passwords or replace with a database/LDAP/OAuth2 user store.
   */
  @Bean
  public UserDetailsService userDetailsService() {
    String userUsername = System.getenv().getOrDefault("APP_USER_USERNAME", "user");
    String userPassword = System.getenv("APP_USER_PASSWORD");
    String adminUsername = System.getenv().getOrDefault("APP_ADMIN_USERNAME", "admin");
    String adminPassword = System.getenv("APP_ADMIN_PASSWORD");
    String actuatorUsername = System.getenv().getOrDefault("APP_ACTUATOR_USERNAME", "actuator");
    String actuatorPassword = System.getenv("APP_ACTUATOR_PASSWORD");

    boolean isProduction = "prod".equals(System.getenv("SPRING_PROFILES_ACTIVE"));
    if (isProduction
        && (userPassword == null || adminPassword == null || actuatorPassword == null)) {
      throw new IllegalStateException(
          "Production environment requires APP_USER_PASSWORD, APP_ADMIN_PASSWORD, and APP_ACTUATOR_PASSWORD "
              + "environment variables. Never use default credentials in production!");
    }

    // Fall back to default passwords in dev/test only
    if (userPassword == null) userPassword = "user123";
    if (adminPassword == null) adminPassword = "admin123";
    if (actuatorPassword == null) actuatorPassword = "actuator123";

    UserDetails user =
        User.builder()
            .username(userUsername)
            .password(passwordEncoder().encode(userPassword))
            .roles("USER")
            .build();

    UserDetails admin =
        User.builder()
            .username(adminUsername)
            .password(passwordEncoder().encode(adminPassword))
            .roles("USER", "ADMIN")
            .build();

    UserDetails actuator =
        User.builder()
            .username(actuatorUsername)
            .password(passwordEncoder().encode(actuatorPassword))
            .roles("ACTUATOR_ADMIN")
            .build();

    return new InMemoryUserDetailsManager(user, admin, actuator);
  }
}

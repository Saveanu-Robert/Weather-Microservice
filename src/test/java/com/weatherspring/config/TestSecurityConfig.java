package com.weatherspring.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test security configuration that disables authentication for integration tests.
 *
 * <p>This configuration allows integration tests to run without authentication,
 * making it easier to test controller behavior independently of security.</p>
 *
 * <p>In production, the real SecurityConfig is used which enforces proper
 * authentication and authorization.</p>
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    /**
     * Security filter chain for tests that permits all requests.
     *
     * <p>This overrides the production SecurityConfig when running tests,
     * allowing tests to focus on business logic rather than authentication.</p>
     *
     * @param http the HttpSecurity to configure
     * @return configured SecurityFilterChain that permits all requests
     * @throws Exception if configuration fails
     */
    @Bean
    @Primary
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    }
}

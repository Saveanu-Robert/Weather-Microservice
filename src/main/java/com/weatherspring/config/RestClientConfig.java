package com.weatherspring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for REST client used to communicate with external APIs.
 */
@Configuration
public class RestClientConfig {

    @Value("${weather.api.timeout:5000}")
    private long timeout;

    /**
     * Configures RestTemplate with timeout settings.
     *
     * @param builder RestTemplate builder
     * @return configured RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(timeout))
                .withReadTimeout(Duration.ofMillis(timeout));

        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);

        return builder
                .requestFactory(() -> requestFactory)
                .build();
    }
}

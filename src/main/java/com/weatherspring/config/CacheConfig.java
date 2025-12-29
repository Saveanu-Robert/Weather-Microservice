package com.weatherspring.config;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Configuration for caching with Caffeine.
 *
 * <p>Configures different caches with appropriate TTL values:
 *
 * <ul>
 *   <li>currentWeather - 5 minutes TTL (300 seconds)
 *   <li>forecasts - 1 hour TTL (3600 seconds)
 *   <li>locations - 15 minutes TTL (900 seconds)
 * </ul>
 */
@Configuration
public class CacheConfig {

  private static final int DEFAULT_CACHE_SIZE = 500;

  @Value("${weather.api.cache.current-weather-ttl:300}")
  private long currentWeatherTtl;

  @Value("${weather.api.cache.forecast-ttl:3600}")
  private long forecastTtl;

  @Value("${weather.api.cache.location-ttl:900}")
  private long locationTtl;

  /**
   * Configures the cache manager with separate Caffeine caches, each with its own TTL.
   *
   * @return configured cache manager with individual cache configurations
   */
  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();

    cacheManager.setCaches(
        Arrays.asList(
            buildCache("currentWeather", currentWeatherTtl),
            buildCache("forecasts", forecastTtl),
            buildCache("locations", locationTtl)));

    // Must call initializeCaches() or caches won't be available for lookup
    cacheManager.initializeCaches();

    return cacheManager;
  }

  /**
   * Builds a CaffeineCache with specified name and TTL.
   *
   * @param name the cache name
   * @param ttlSeconds the time-to-live in seconds
   * @return configured CaffeineCache
   */
  private CaffeineCache buildCache(String name, long ttlSeconds) {
    return new CaffeineCache(
        name,
        Caffeine.newBuilder()
            .maximumSize(DEFAULT_CACHE_SIZE)
            .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
            .recordStats()
            .build());
  }
}

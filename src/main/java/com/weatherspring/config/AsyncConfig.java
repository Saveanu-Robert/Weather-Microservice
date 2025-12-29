package com.weatherspring.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

/**
 * Async configuration using virtual threads for massive concurrency.
 *
 * <p>Configures Spring's @Async to use virtual threads instead of platform threads, allowing
 * thousands of concurrent async operations with minimal overhead.
 *
 * <p><strong>Note:</strong> The @ConfigurationProperties only apply to the {@link
 * #platformExecutor()} fallback executor. The primary executors ({@link #taskExecutor()} and {@link
 * #compositeExecutor()}) use virtual threads and don't require pool configuration.
 *
 * @since Java 21 (Project Loom)
 */
@Configuration
@EnableAsync
@ConfigurationProperties(prefix = "thread-pool.platform")
@Validated
@Getter
@Setter
public class AsyncConfig {

  // Platform thread pool configuration (only used by platformExecutor fallback)

  @Min(1)
  @Max(100)
  private int corePoolSize = 10;

  @Min(1)
  @Max(500)
  private int maxPoolSize = 50;

  @Min(0)
  @Max(10000)
  private int queueCapacity = 500;

  @Min(1)
  @Max(300)
  private int awaitTerminationSeconds = 30;

  /**
   * Validates configuration values on startup.
   *
   * @throws IllegalStateException if configuration is invalid
   */
  @PostConstruct
  public void validateConfig() {
    if (maxPoolSize < corePoolSize) {
      throw new IllegalStateException(
          String.format(
              "max-pool-size (%d) must be >= core-pool-size (%d)", maxPoolSize, corePoolSize));
    }
  }

  /**
   * Custom async executor using virtual threads.
   *
   * <p>Unlike traditional ThreadPoolTaskExecutor which uses platform threads, this executor creates
   * a new virtual thread for each task, providing:
   *
   * <ul>
   *   <li>No thread pool limits - handles 10,000+ concurrent tasks
   *   <li>Minimal memory overhead (~1KB per virtual thread vs 1MB for platform thread)
   *   <li>No need to tune core/max pool sizes
   *   <li>Perfect for I/O-bound async operations
   * </ul>
   *
   * @return executor that creates virtual threads per task
   */
  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    // Use virtual threads for async execution
    return Executors.newVirtualThreadPerTaskExecutor();
  }

  /**
   * Virtual thread executor for composite operations.
   *
   * <p>Used by CompositeWeatherService for parallel data fetching. Configured with graceful
   * shutdown to ensure all tasks complete on application stop.
   *
   * <p><strong>Important:</strong> Virtual thread executors implement AutoCloseable, not
   * ExecutorService shutdown. Using "close" instead of "shutdown" for proper resource cleanup.
   *
   * @return executor service with graceful shutdown support
   */
  @Bean(name = "compositeExecutor", destroyMethod = "close")
  public ExecutorService compositeExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }

  /**
   * Traditional platform thread executor for comparison/fallback.
   *
   * <p>This executor uses platform threads with a limited pool size. Use this only for CPU-bound
   * tasks that need pinning to OS threads.
   *
   * @return platform thread pool executor
   */
  @Bean(name = "platformExecutor")
  public Executor platformExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setQueueCapacity(queueCapacity);
    executor.setThreadNamePrefix("platform-async-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
    executor.initialize();
    return executor;
  }
}

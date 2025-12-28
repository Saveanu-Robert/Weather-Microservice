package com.weatherspring.util;

import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for common exception handling patterns in async operations.
 *
 * <p>Provides reusable exception handlers to reduce code duplication and ensure consistent error
 * handling across async operations.
 *
 * @since 1.0
 */
@Slf4j
public final class ExceptionHandlers {

  private ExceptionHandlers() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Creates an exception handler that logs the error at WARN level with exception message and
   * returns null.
   *
   * <p>Use this for batch operations where failures are expected and should not pollute logs with
   * ERROR level messages. Logs the exception message rather than full stack trace. Returns null to
   * allow batch continuation.
   *
   * <p>Usage with CompletableFuture:
   *
   * <pre>{@code
   * CompletableFuture.supplyAsync(() -> refreshData(id))
   *       .exceptionally(warnWithMessageAndReturnNull("Failed to refresh data for ID {}: {}", id));
   * }</pre>
   *
   * @param <T> the type of the result
   * @param warningTemplate the warning message template with TWO {} placeholders (first for
   *     context, second for exception message)
   * @param contextArg the context argument (e.g., ID, name)
   * @return exception handler function that returns null
   */
  public static <T> Function<Throwable, T> warnWithMessageAndReturnNull(
      String warningTemplate, Object contextArg) {
    return ex -> {
      String errorMessage =
          ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
      log.warn(warningTemplate, contextArg, errorMessage);
      return null;
    };
  }
}

package com.weatherspring.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

/**
 * Utility class for common exception handling patterns in async operations.
 *
 * <p>Provides reusable exception handlers to reduce code duplication and ensure
 * consistent error handling across async operations.</p>
 *
 * @since 1.0
 */
@Slf4j
public final class ExceptionHandlers {

    private ExceptionHandlers() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Creates an exception handler that logs the error and returns an internal server error response.
     *
     * <p>Usage with CompletableFuture:</p>
     * <pre>{@code
     * future.thenApply(ResponseEntity::ok)
     *       .exceptionally(logAndReturnError("Operation failed"));
     * }</pre>
     *
     * @param <T> the type of the response entity body
     * @param errorMessage the error message to log
     * @return exception handler function that returns 500 Internal Server Error
     */
    public static <T> Function<Throwable, ResponseEntity<T>> logAndReturnError(String errorMessage) {
        return ex -> {
            log.error(errorMessage, ex);
            return ResponseEntity.internalServerError().build();
        };
    }

    /**
     * Creates an exception handler that logs the error at ERROR level and returns null.
     *
     * <p>Use this for batch operations where individual failures should not stop the entire batch.
     * Returning null allows the batch to continue processing other items.</p>
     *
     * <p>Usage with CompletableFuture:</p>
     * <pre>{@code
     * CompletableFuture.supplyAsync(() -> processItem(id))
     *       .exceptionally(logAndReturnNull("Failed to process item: {}", id));
     * }</pre>
     *
     * @param <T> the type of the result
     * @param errorMessage the error message template (supports {} placeholders)
     * @param messageArgs arguments for the error message placeholders
     * @return exception handler function that returns null
     */
    public static <T> Function<Throwable, T> logAndReturnNull(String errorMessage, Object... messageArgs) {
        return ex -> {
            log.error(errorMessage, messageArgs, ex);
            return null;
        };
    }

    /**
     * Creates an exception handler that logs the error at WARN level with exception message and returns null.
     *
     * <p>Use this for batch operations where failures are expected and should not pollute
     * logs with ERROR level messages. Logs the exception message rather than full stack trace.
     * Returns null to allow batch continuation.</p>
     *
     * <p>Usage with CompletableFuture:</p>
     * <pre>{@code
     * CompletableFuture.supplyAsync(() -> refreshData(id))
     *       .exceptionally(warnWithMessageAndReturnNull("Failed to refresh data for ID {}: {}", id));
     * }</pre>
     *
     * @param <T> the type of the result
     * @param warningTemplate the warning message template with TWO {} placeholders (first for context, second for exception message)
     * @param contextArg the context argument (e.g., ID, name)
     * @return exception handler function that returns null
     */
    public static <T> Function<Throwable, T> warnWithMessageAndReturnNull(String warningTemplate, Object contextArg) {
        return ex -> {
            String errorMessage = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            log.warn(warningTemplate, contextArg, errorMessage);
            return null;
        };
    }

    /**
     * Creates an exception handler that logs the error at ERROR level with a custom message
     * and exception details, then returns null.
     *
     * <p>Use this when you need both the custom message and full exception logging.</p>
     *
     * @param <T> the type of the result
     * @param errorMessage the error message (without placeholders)
     * @param context additional context (e.g., location name, ID)
     * @return exception handler function that returns null
     */
    public static <T> Function<Throwable, T> logErrorAndReturnNull(String errorMessage, String context) {
        return ex -> {
            log.error("{}: {}", errorMessage, context, ex);
            return null;
        };
    }
}

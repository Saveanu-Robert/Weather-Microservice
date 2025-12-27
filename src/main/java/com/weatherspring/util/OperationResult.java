package com.weatherspring.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents the result of an operation that can either succeed or fail.
 *
 * <p>This is a type-safe alternative to returning null from exception handlers.
 * It allows tracking both successful results and error information without
 * silent failures.</p>
 *
 * @param <T> the type of successful result
 */
public sealed interface OperationResult<T> {

    /**
     * Creates a successful result.
     *
     * @param value the successful value
     * @param <T> the type of value
     * @return a Success result
     */
    static <T> OperationResult<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates a failure result.
     *
     * @param error the error that occurred
     * @param context additional context about the failure
     * @param <T> the type of value (if it had succeeded)
     * @return a Failure result
     */
    static <T> OperationResult<T> failure(Throwable error, String context) {
        return new Failure<>(error, context);
    }

    /**
     * Checks if this result is successful.
     *
     * @return true if successful, false otherwise
     */
    boolean isSuccess();

    /**
     * Checks if this result is a failure.
     *
     * @return true if failure, false otherwise
     */
    boolean isFailure();

    /**
     * Gets the value if successful.
     *
     * @return Optional containing value if success, empty if failure
     */
    Optional<T> getValue();

    /**
     * Gets the error if failed.
     *
     * @return Optional containing error if failure, empty if success
     */
    Optional<Throwable> getError();

    /**
     * Gets the context message if failed.
     *
     * @return Optional containing context if failure, empty if success
     */
    Optional<String> getContext();

    /**
     * Maps the value if successful.
     *
     * @param mapper function to transform the value
     * @param <R> the result type
     * @return new OperationResult with mapped value, or the same failure
     */
    <R> OperationResult<R> map(Function<T, R> mapper);

    /**
     * Executes action if successful.
     *
     * @param action consumer to execute with the value
     */
    void ifSuccess(Consumer<T> action);

    /**
     * Executes action if failed.
     *
     * @param action consumer to execute with error and context
     */
    void ifFailure(Consumer<Throwable> action);

    /**
     * Success result containing a value.
     */
    record Success<T>(T value) implements OperationResult<T> {
        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public Optional<T> getValue() {
            return Optional.of(value);
        }

        @Override
        public Optional<Throwable> getError() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getContext() {
            return Optional.empty();
        }

        @Override
        public <R> OperationResult<R> map(Function<T, R> mapper) {
            return new Success<>(mapper.apply(value));
        }

        @Override
        public void ifSuccess(Consumer<T> action) {
            action.accept(value);
        }

        @Override
        public void ifFailure(Consumer<Throwable> action) {
            // No-op for success
        }
    }

    /**
     * Failure result containing error information.
     */
    record Failure<T>(Throwable error, String context) implements OperationResult<T> {
        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public Optional<T> getValue() {
            return Optional.empty();
        }

        @Override
        public Optional<Throwable> getError() {
            return Optional.of(error);
        }

        @Override
        public Optional<String> getContext() {
            return Optional.of(context);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <R> OperationResult<R> map(Function<T, R> mapper) {
            return (OperationResult<R>) this;
        }

        @Override
        public void ifSuccess(Consumer<T> action) {
            // No-op for failure
        }

        @Override
        public void ifFailure(Consumer<Throwable> action) {
            action.accept(error);
        }
    }
}

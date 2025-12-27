package com.weatherspring.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

/**
 * HTTP request/response logging filter for debugging and monitoring.
 *
 * <p>Logs all incoming HTTP requests and outgoing responses with:</p>
 * <ul>
 *     <li>Request method and URI</li>
 *     <li>Response status code</li>
 *     <li>Request/response duration</li>
 *     <li>Optional request/response bodies (for debugging)</li>
 * </ul>
 *
 * <p>Uses content caching wrappers to allow reading request/response bodies
 * without consuming the input streams.</p>
 */
@Component
@Slf4j
public class LoggingFilter implements Filter {

    private static final int MAX_PAYLOAD_LENGTH = 1000; // Max chars to log from body

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest) ||
            !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        // Wrap request and response to cache content
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);

        Instant startTime = Instant.now();

        try {
            // Log request
            logRequest(requestWrapper);

            // Process request
            chain.doFilter(requestWrapper, responseWrapper);

        } finally {
            Duration duration = Duration.between(startTime, Instant.now());

            // Log response
            logResponse(requestWrapper, responseWrapper, duration);

            // IMPORTANT: Copy cached response content to actual response
            responseWrapper.copyBodyToResponse();
        }
    }

    /**
     * Logs HTTP request details.
     */
    private void logRequest(ContentCachingRequestWrapper request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUri = queryString != null ? uri + "?" + queryString : uri;

        log.info("HTTP Request: {} {}", method, fullUri);

        // Log request body for debugging (only for POST/PUT/PATCH)
        if (log.isDebugEnabled() && isBodyLoggable(method)) {
            String payload = getRequestPayload(request);
            if (!payload.isBlank()) {
                log.debug("Request Body: {}", truncate(payload));
            }
        }
    }

    /**
     * Logs HTTP response details.
     */
    private void logResponse(ContentCachingRequestWrapper request,
                            ContentCachingResponseWrapper response,
                            Duration duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        long durationMs = duration.toMillis();

        log.info("HTTP Response: {} {} - Status: {} - Duration: {}ms",
                method, uri, status, durationMs);

        // Log response body for debugging (only for errors)
        if (log.isDebugEnabled() && status >= 400) {
            String payload = getResponsePayload(response);
            if (!payload.isBlank()) {
                log.debug("Response Body: {}", truncate(payload));
            }
        }
    }

    /**
     * Extracts request payload from cached content.
     */
    private String getRequestPayload(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return "";
    }

    /**
     * Extracts response payload from cached content.
     */
    private String getResponsePayload(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return "";
    }

    /**
     * Checks if request body should be logged based on HTTP method.
     */
    private boolean isBodyLoggable(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

    /**
     * Truncates payload to maximum length to prevent excessive logging.
     */
    private String truncate(String payload) {
        if (payload.length() <= MAX_PAYLOAD_LENGTH) {
            return payload;
        }
        return payload.substring(0, MAX_PAYLOAD_LENGTH) + "... (truncated)";
    }
}

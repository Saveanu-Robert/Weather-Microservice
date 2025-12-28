package com.weatherspring.config;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Filter that adds a correlation ID to every request for distributed tracing.
 *
 * <p>The correlation ID is:
 *
 * <ul>
 *   <li>Read from the X-Correlation-ID header if present
 *   <li>Generated as a UUID if not present
 *   <li>Added to the MDC for logging
 *   <li>Added to the response header
 * </ul>
 *
 * <p>This enables request tracing across microservices and log aggregation systems.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements Filter {

  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
  private static final String CORRELATION_ID_MDC_KEY = "correlationId";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    if (!(request instanceof HttpServletRequest httpRequest)
        || !(response instanceof HttpServletResponse httpResponse)) {
      chain.doFilter(request, response);
      return;
    }

    try {
      // Get or generate correlation ID
      String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
      if (correlationId == null || correlationId.isBlank()) {
        correlationId = UUID.randomUUID().toString();
      }

      // Add to MDC for logging
      MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

      // Add to response headers
      httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

      chain.doFilter(request, response);
    } finally {
      // Clean up MDC to prevent memory leaks in thread pools
      MDC.remove(CORRELATION_ID_MDC_KEY);
    }
  }
}

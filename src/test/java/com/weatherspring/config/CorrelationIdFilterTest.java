package com.weatherspring.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

/** Unit tests for CorrelationIdFilter. */
@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
  private static final String CORRELATION_ID_MDC_KEY = "correlationId";

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  private CorrelationIdFilter correlationIdFilter;

  @BeforeEach
  void setUp() {
    correlationIdFilter = new CorrelationIdFilter();
    MDC.clear();
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  void doFilter_WithExistingCorrelationId_UsesThatId() throws Exception {
    // Arrange
    String existingId = "existing-correlation-id-123";
    when(request.getHeader(CORRELATION_ID_HEADER)).thenReturn(existingId);

    // Act
    correlationIdFilter.doFilter(request, response, filterChain);

    // Assert
    verify(response).setHeader(CORRELATION_ID_HEADER, existingId);
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilter_WithoutCorrelationId_GeneratesNewId() throws Exception {
    // Arrange
    when(request.getHeader(CORRELATION_ID_HEADER)).thenReturn(null);

    // Act
    correlationIdFilter.doFilter(request, response, filterChain);

    // Assert
    verify(response).setHeader(eq(CORRELATION_ID_HEADER), anyString());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilter_WithBlankCorrelationId_GeneratesNewId() throws Exception {
    // Arrange
    when(request.getHeader(CORRELATION_ID_HEADER)).thenReturn("");

    // Act
    correlationIdFilter.doFilter(request, response, filterChain);

    // Assert
    verify(response).setHeader(eq(CORRELATION_ID_HEADER), anyString());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilter_CleansUpMdcAfterProcessing() throws Exception {
    // Arrange
    String existingId = "test-correlation-id";
    when(request.getHeader(CORRELATION_ID_HEADER)).thenReturn(existingId);

    // Act
    correlationIdFilter.doFilter(request, response, filterChain);

    // Assert - MDC should be cleaned up
    assertThat(MDC.get(CORRELATION_ID_MDC_KEY)).isNull();
  }

  @Test
  void doFilter_CleansUpMdcEvenWhenExceptionThrown() throws Exception {
    // Arrange
    String existingId = "test-correlation-id";
    when(request.getHeader(CORRELATION_ID_HEADER)).thenReturn(existingId);
    doThrow(new RuntimeException("Test exception")).when(filterChain).doFilter(request, response);

    // Act & Assert
    try {
      correlationIdFilter.doFilter(request, response, filterChain);
    } catch (RuntimeException e) {
      // Expected exception
    }

    // MDC should still be cleaned up
    assertThat(MDC.get(CORRELATION_ID_MDC_KEY)).isNull();
  }

  @Test
  void doFilter_SetsResponseHeader() throws Exception {
    // Arrange
    String correlationId = "test-id-12345";
    when(request.getHeader(CORRELATION_ID_HEADER)).thenReturn(correlationId);

    // Act
    correlationIdFilter.doFilter(request, response, filterChain);

    // Assert
    verify(response).setHeader(CORRELATION_ID_HEADER, correlationId);
  }
}

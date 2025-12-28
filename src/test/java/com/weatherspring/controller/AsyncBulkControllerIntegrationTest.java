package com.weatherspring.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherspring.TestDataFactory;
import com.weatherspring.config.TestSecurityConfig;
import com.weatherspring.dto.BulkOperationResult;
import com.weatherspring.dto.ForecastDto;
import com.weatherspring.dto.WeatherDto;
import com.weatherspring.service.AsyncBulkWeatherService;

/**
 * Integration tests for AsyncBulkController.
 *
 * <p>Tests async bulk operations with CompletableFuture handling, validation constraints, and error
 * scenarios.
 */
@WebMvcTest(AsyncBulkController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@WithMockUser(
    username = "admin",
    roles = {"USER", "ADMIN"})
class AsyncBulkControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AsyncBulkWeatherService asyncBulkWeatherService;

  private WeatherDto weatherDto;
  private ForecastDto forecastDto;
  private List<WeatherDto> weatherList;
  private List<ForecastDto> forecastList;

  @BeforeEach
  void setUp() {
    weatherDto = TestDataFactory.createDefaultWeatherDto();
    forecastDto = TestDataFactory.createDefaultForecastDto();
    weatherList = Arrays.asList(weatherDto, weatherDto, weatherDto);
    forecastList = Arrays.asList(forecastDto, forecastDto);
  }

  // ==================== GET /api/async/weather/bulk ====================

  @Test
  void bulkWeatherAsync_WithValidLocations_ReturnsWeatherData() throws Exception {
    // Arrange
    List<String> locations = Arrays.asList("London", "Paris", "Tokyo");
    when(asyncBulkWeatherService.fetchBulkWeatherAsync(anyList(), anyBoolean()))
        .thenReturn(CompletableFuture.completedFuture(weatherList));

    // Act
    MvcResult mvcResult =
        mockMvc
            .perform(
                get("/api/async/weather/bulk")
                    .param("locations", "London", "Paris", "Tokyo")
                    .param("save", "false")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andExpect(status().isOk())
            .andReturn();

    // Wait for async processing and get result
    @SuppressWarnings("unchecked")
    List<WeatherDto> result = (List<WeatherDto>) mvcResult.getAsyncResult();

    // Assert
    assertThat(result).hasSize(3);
    assertThat(result.get(0).locationName()).isEqualTo("London");
    assertThat(result.get(0).temperature()).isEqualTo(15.5);
    verify(asyncBulkWeatherService).fetchBulkWeatherAsync(locations, false);
  }

  @Test
  void bulkWeatherAsync_WithSaveToDatabase_PassesSaveFlag() throws Exception {
    // Arrange
    List<String> locations = Arrays.asList("London");
    when(asyncBulkWeatherService.fetchBulkWeatherAsync(anyList(), eq(true)))
        .thenReturn(CompletableFuture.completedFuture(Collections.singletonList(weatherDto)));

    // Act
    MvcResult mvcResult =
        mockMvc
            .perform(
                get("/api/async/weather/bulk")
                    .param("locations", "London")
                    .param("save", "true")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

    // Assert
    mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk());

    verify(asyncBulkWeatherService)
        .fetchBulkWeatherAsync(Collections.singletonList("London"), true);
  }

  @Test
  void bulkWeatherAsync_WithEmptyLocationList_ReturnsBadRequest() throws Exception {
    // Act & Assert
    mockMvc
        .perform(
            get("/api/async/weather/bulk")
                .param("locations", "")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(asyncBulkWeatherService, never()).fetchBulkWeatherAsync(anyList(), anyBoolean());
  }

  @Test
  void bulkWeatherAsync_WithTooManyLocations_ReturnsBadRequest() throws Exception {
    // Arrange - Create list with 101 locations (exceeds max of 100)
    String[] locations = new String[101];
    Arrays.fill(locations, "London");

    // Act & Assert
    mockMvc
        .perform(
            get("/api/async/weather/bulk")
                .param("locations", locations)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(asyncBulkWeatherService, never()).fetchBulkWeatherAsync(anyList(), anyBoolean());
  }

  @Test
  void bulkWeatherAsync_WithServiceException_ReturnsInternalServerError() throws Exception {
    // Arrange
    List<String> locations = Arrays.asList("London");
    CompletableFuture<List<WeatherDto>> failedFuture = new CompletableFuture<>();
    failedFuture.completeExceptionally(new RuntimeException("API error"));

    when(asyncBulkWeatherService.fetchBulkWeatherAsync(anyList(), anyBoolean()))
        .thenReturn(failedFuture);

    // Act
    MvcResult mvcResult =
        mockMvc
            .perform(
                get("/api/async/weather/bulk")
                    .param("locations", "London")
                    .param("save", "false")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

    // Assert
    mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isInternalServerError());
  }

  // ==================== GET /api/async/forecast/bulk ====================

  @Test
  void bulkForecastAsync_WithValidLocations_ReturnsForecastData() throws Exception {
    // Arrange
    List<String> locations = Arrays.asList("London", "Paris");
    List<List<ForecastDto>> forecasts = Arrays.asList(forecastList, forecastList);

    when(asyncBulkWeatherService.fetchBulkForecastAsync(anyList(), anyInt(), anyBoolean()))
        .thenReturn(CompletableFuture.completedFuture(forecasts));

    // Act
    MvcResult mvcResult =
        mockMvc
            .perform(
                get("/api/async/forecast/bulk")
                    .param("locations", "London", "Paris")
                    .param("days", "3")
                    .param("save", "false")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andExpect(status().isOk())
            .andReturn();

    // Get async result
    @SuppressWarnings("unchecked")
    List<List<ForecastDto>> result = (List<List<ForecastDto>>) mvcResult.getAsyncResult();

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).hasSize(2);
    verify(asyncBulkWeatherService).fetchBulkForecastAsync(locations, 3, false);
  }

  @Test
  void bulkForecastAsync_WithDefaultDays_Uses3Days() throws Exception {
    // Arrange
    List<String> locations = Arrays.asList("London");
    when(asyncBulkWeatherService.fetchBulkForecastAsync(anyList(), eq(3), anyBoolean()))
        .thenReturn(CompletableFuture.completedFuture(Collections.singletonList(forecastList)));

    // Act
    MvcResult mvcResult =
        mockMvc
            .perform(
                get("/api/async/forecast/bulk")
                    .param("locations", "London")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

    // Assert
    mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk());

    verify(asyncBulkWeatherService)
        .fetchBulkForecastAsync(Collections.singletonList("London"), 3, false);
  }

  @Test
  void bulkForecastAsync_WithInvalidDays_ReturnsBadRequest() throws Exception {
    // Act & Assert - Days less than 1
    mockMvc
        .perform(
            get("/api/async/forecast/bulk")
                .param("locations", "London")
                .param("days", "0")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    // Act & Assert - Days more than 14
    mockMvc
        .perform(
            get("/api/async/forecast/bulk")
                .param("locations", "London")
                .param("days", "15")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(asyncBulkWeatherService, never())
        .fetchBulkForecastAsync(anyList(), anyInt(), anyBoolean());
  }

  @Test
  void bulkForecastAsync_WithMaximumDays_AcceptsRequest() throws Exception {
    // Arrange
    List<String> locations = Arrays.asList("London");
    when(asyncBulkWeatherService.fetchBulkForecastAsync(anyList(), eq(14), anyBoolean()))
        .thenReturn(CompletableFuture.completedFuture(Collections.singletonList(forecastList)));

    // Act
    MvcResult mvcResult =
        mockMvc
            .perform(
                get("/api/async/forecast/bulk")
                    .param("locations", "London")
                    .param("days", "14")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

    // Assert
    mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk());

    verify(asyncBulkWeatherService)
        .fetchBulkForecastAsync(Collections.singletonList("London"), 14, false);
  }

  // ==================== POST /api/async/weather/update ====================

  @Test
  void bulkUpdateWeatherAsync_WithValidLocationIds_ReturnsSuccessCount() throws Exception {
    // Arrange
    List<Long> locationIds = Arrays.asList(1L, 2L, 3L);
    when(asyncBulkWeatherService.bulkUpdateWeatherAsync(anyList(), anyBoolean()))
        .thenReturn(CompletableFuture.completedFuture(3));

    // Act
    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/api/async/weather/update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(locationIds))
                    .param("save", "true"))
            .andExpect(request().asyncStarted())
            .andExpect(status().isOk())
            .andReturn();

    // Get async result
    Integer result = (Integer) mvcResult.getAsyncResult();

    // Assert
    assertThat(result).isEqualTo(3);
    verify(asyncBulkWeatherService).bulkUpdateWeatherAsync(locationIds, true);
  }

  @Test
  void bulkUpdateWeatherAsync_WithDefaultSave_UsesTrue() throws Exception {
    // Arrange
    List<Long> locationIds = Arrays.asList(1L);
    when(asyncBulkWeatherService.bulkUpdateWeatherAsync(anyList(), eq(true)))
        .thenReturn(CompletableFuture.completedFuture(1));

    // Act
    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/api/async/weather/update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(locationIds)))
            .andExpect(request().asyncStarted())
            .andReturn();

    // Assert
    mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk());

    verify(asyncBulkWeatherService).bulkUpdateWeatherAsync(locationIds, true);
  }

  @Test
  void bulkUpdateWeatherAsync_WithEmptyList_ReturnsBadRequest() throws Exception {
    // Arrange
    List<Long> emptyList = Collections.emptyList();

    // Act & Assert
    mockMvc
        .perform(
            post("/api/async/weather/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyList)))
        .andExpect(status().isBadRequest());

    verify(asyncBulkWeatherService, never()).bulkUpdateWeatherAsync(anyList(), anyBoolean());
  }

  @Test
  void bulkUpdateWeatherAsync_WithTooManyIds_ReturnsBadRequest() throws Exception {
    // Arrange - Create list with 101 IDs (exceeds max of 100)
    List<Long> tooManyIds = new java.util.ArrayList<>();
    for (long i = 1; i <= 101; i++) {
      tooManyIds.add(i);
    }

    // Act & Assert
    mockMvc
        .perform(
            post("/api/async/weather/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tooManyIds)))
        .andExpect(status().isBadRequest());

    verify(asyncBulkWeatherService, never()).bulkUpdateWeatherAsync(anyList(), anyBoolean());
  }

  @Test
  void bulkUpdateWeatherAsync_WithPartialFailures_ReturnsPartialCount() throws Exception {
    // Arrange
    List<Long> locationIds = Arrays.asList(1L, 2L, 3L);
    when(asyncBulkWeatherService.bulkUpdateWeatherAsync(anyList(), anyBoolean()))
        .thenReturn(CompletableFuture.completedFuture(2)); // Only 2 succeeded

    // Act
    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/api/async/weather/update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(locationIds))
                    .param("save", "true"))
            .andExpect(request().asyncStarted())
            .andExpect(status().isOk())
            .andReturn();

    // Get async result
    Integer result = (Integer) mvcResult.getAsyncResult();

    // Assert
    assertThat(result).isEqualTo(2);
  }

  // ==================== POST /api/async/forecast/refresh ====================

  @Test
  void bulkRefreshForecastsAsync_WithValidLocationIds_ReturnsSuccessCount() throws Exception {
    // Arrange
    List<Long> locationIds = Arrays.asList(1L, 2L);
    when(asyncBulkWeatherService.bulkRefreshForecastsAsync(anyList(), anyInt(), anyBoolean()))
        .thenReturn(CompletableFuture.completedFuture(2));

    // Act
    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/api/async/forecast/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(locationIds))
                    .param("days", "7")
                    .param("save", "true"))
            .andExpect(request().asyncStarted())
            .andExpect(status().isOk())
            .andReturn();

    // Get async result
    Integer result = (Integer) mvcResult.getAsyncResult();

    // Assert
    assertThat(result).isEqualTo(2);
    verify(asyncBulkWeatherService).bulkRefreshForecastsAsync(locationIds, 7, true);
  }

  @Test
  void bulkRefreshForecastsAsync_WithDefaultValues_Uses7DaysAndTrue() throws Exception {
    // Arrange
    List<Long> locationIds = Arrays.asList(1L);
    when(asyncBulkWeatherService.bulkRefreshForecastsAsync(anyList(), eq(7), eq(true)))
        .thenReturn(CompletableFuture.completedFuture(1));

    // Act
    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/api/async/forecast/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(locationIds)))
            .andExpect(request().asyncStarted())
            .andReturn();

    // Assert
    mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk());

    verify(asyncBulkWeatherService).bulkRefreshForecastsAsync(locationIds, 7, true);
  }

  @Test
  void bulkRefreshForecastsAsync_WithInvalidDays_ReturnsBadRequest() throws Exception {
    // Arrange
    List<Long> locationIds = Arrays.asList(1L);

    // Act & Assert - Days less than 1
    mockMvc
        .perform(
            post("/api/async/forecast/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationIds))
                .param("days", "0"))
        .andExpect(status().isBadRequest());

    // Act & Assert - Days more than 14
    mockMvc
        .perform(
            post("/api/async/forecast/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationIds))
                .param("days", "15"))
        .andExpect(status().isBadRequest());

    verify(asyncBulkWeatherService, never())
        .bulkRefreshForecastsAsync(anyList(), anyInt(), anyBoolean());
  }

  // ==================== POST /api/async/refresh-all ====================

  @Test
  void refreshAllAsync_WithValidDays_ReturnsOperationResult() throws Exception {
    // Arrange
    BulkOperationResult expectedResult = new BulkOperationResult(8, 2, 10);
    when(asyncBulkWeatherService.refreshAllLocationsAsync(anyInt()))
        .thenReturn(CompletableFuture.completedFuture(expectedResult));

    // Act
    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/api/async/refresh-all")
                    .param("forecastDays", "7")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andExpect(status().isOk())
            .andReturn();

    // Get async result
    BulkOperationResult result = (BulkOperationResult) mvcResult.getAsyncResult();

    // Assert
    assertThat(result.successCount()).isEqualTo(8);
    assertThat(result.failureCount()).isEqualTo(2);
    assertThat(result.totalCount()).isEqualTo(10);
    verify(asyncBulkWeatherService).refreshAllLocationsAsync(7);
  }

  @Test
  void refreshAllAsync_WithDefaultDays_Uses7Days() throws Exception {
    // Arrange
    BulkOperationResult expectedResult = new BulkOperationResult(10, 0, 10);
    when(asyncBulkWeatherService.refreshAllLocationsAsync(eq(7)))
        .thenReturn(CompletableFuture.completedFuture(expectedResult));

    // Act
    MvcResult mvcResult =
        mockMvc
            .perform(post("/api/async/refresh-all").contentType(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andExpect(status().isOk())
            .andReturn();

    // Get async result
    BulkOperationResult result = (BulkOperationResult) mvcResult.getAsyncResult();

    // Assert
    assertThat(result.allSuccessful()).isTrue();
    verify(asyncBulkWeatherService).refreshAllLocationsAsync(7);
  }

  @Test
  void refreshAllAsync_WithInvalidDays_ReturnsBadRequest() throws Exception {
    // Act & Assert - Days less than 1
    mockMvc
        .perform(
            post("/api/async/refresh-all")
                .param("forecastDays", "0")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    // Act & Assert - Days more than 14
    mockMvc
        .perform(
            post("/api/async/refresh-all")
                .param("forecastDays", "15")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(asyncBulkWeatherService, never()).refreshAllLocationsAsync(anyInt());
  }

  @Test
  void refreshAllAsync_WithAllFailures_ReturnsFailureResult() throws Exception {
    // Arrange
    BulkOperationResult expectedResult = new BulkOperationResult(0, 10, 10);
    when(asyncBulkWeatherService.refreshAllLocationsAsync(anyInt()))
        .thenReturn(CompletableFuture.completedFuture(expectedResult));

    // Act
    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/api/async/refresh-all")
                    .param("forecastDays", "3")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andExpect(status().isOk())
            .andReturn();

    // Get async result
    BulkOperationResult result = (BulkOperationResult) mvcResult.getAsyncResult();

    // Assert
    assertThat(result.allSuccessful()).isFalse();
    assertThat(result.successRate()).isEqualTo(0.0);
  }

  @Test
  void refreshAllAsync_WithServiceException_ReturnsInternalServerError() throws Exception {
    // Arrange
    CompletableFuture<BulkOperationResult> failedFuture = new CompletableFuture<>();
    failedFuture.completeExceptionally(new RuntimeException("Database error"));

    when(asyncBulkWeatherService.refreshAllLocationsAsync(anyInt())).thenReturn(failedFuture);

    // Act
    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/api/async/refresh-all")
                    .param("forecastDays", "7")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

    // Assert
    mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isInternalServerError());
  }
}

package com.weatherspring.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.weatherspring.TestDataFactory;
import com.weatherspring.dto.BulkOperationResult;
import com.weatherspring.dto.ForecastDto;
import com.weatherspring.dto.LocationDto;
import com.weatherspring.dto.WeatherDto;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Unit tests for AsyncBulkWeatherService.
 *
 * <p>Tests async bulk operations with CompletableFuture handling, partial failures, and virtual
 * thread execution.
 */
@ExtendWith(MockitoExtension.class)
class AsyncBulkWeatherServiceTest {

  @Mock private WeatherService weatherService;

  @Mock private ForecastService forecastService;

  @Mock private LocationService locationService;

  private AsyncBulkWeatherService asyncBulkWeatherService;

  private WeatherDto weatherDto;
  private ForecastDto forecastDto;
  private LocationDto locationDto;

  @BeforeEach
  void setUp() {
    weatherDto = TestDataFactory.createDefaultWeatherDto();
    forecastDto = TestDataFactory.createDefaultForecastDto();
    locationDto = TestDataFactory.createTestLocationDto();

    // Create service with SimpleMeterRegistry for testing
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    asyncBulkWeatherService =
        new AsyncBulkWeatherService(
            weatherService, forecastService, locationService, meterRegistry, 300);
  }

  @Test
  void fetchBulkWeatherAsync_WithValidLocations_ReturnsWeatherData()
      throws ExecutionException, InterruptedException {
    // Arrange
    List<String> locations = Arrays.asList("London", "Paris", "Tokyo");
    when(weatherService.getCurrentWeather(anyString(), anyBoolean())).thenReturn(weatherDto);

    // Act
    CompletableFuture<List<WeatherDto>> future =
        asyncBulkWeatherService.fetchBulkWeatherAsync(locations, false);
    List<WeatherDto> results = future.get();

    // Assert
    assertThat(results).hasSize(3);
    assertThat(results).allMatch(dto -> dto != null);
    verify(weatherService, times(3)).getCurrentWeather(anyString(), eq(false));
  }

  @Test
  void fetchBulkWeatherAsync_WithPartialFailures_ReturnsSuccessfulResults()
      throws ExecutionException, InterruptedException {
    // Arrange
    List<String> locations = Arrays.asList("London", "InvalidLocation", "Paris");

    when(weatherService.getCurrentWeather("London", false)).thenReturn(weatherDto);
    when(weatherService.getCurrentWeather("InvalidLocation", false))
        .thenThrow(new RuntimeException("Location not found"));
    when(weatherService.getCurrentWeather("Paris", false)).thenReturn(weatherDto);

    // Act
    CompletableFuture<List<WeatherDto>> future =
        asyncBulkWeatherService.fetchBulkWeatherAsync(locations, false);
    List<WeatherDto> results = future.get();

    // Assert
    assertThat(results).hasSize(2); // Only successful results
    verify(weatherService, times(3)).getCurrentWeather(anyString(), eq(false));
  }

  @Test
  void fetchBulkWeatherAsync_WithSaveToDatabase_SavesData()
      throws ExecutionException, InterruptedException {
    // Arrange
    List<String> locations = Arrays.asList("London");
    when(weatherService.getCurrentWeather(anyString(), eq(true))).thenReturn(weatherDto);

    // Act
    CompletableFuture<List<WeatherDto>> future =
        asyncBulkWeatherService.fetchBulkWeatherAsync(locations, true);
    future.get();

    // Assert
    verify(weatherService).getCurrentWeather("London", true);
  }

  @Test
  void fetchBulkWeatherAsync_WithEmptyList_ReturnsEmptyResult()
      throws ExecutionException, InterruptedException {
    // Arrange
    List<String> locations = Collections.emptyList();

    // Act
    CompletableFuture<List<WeatherDto>> result =
        asyncBulkWeatherService.fetchBulkWeatherAsync(locations, false);

    // Assert
    assertThat(result.get()).isEmpty();
  }

  @Test
  void fetchBulkForecastAsync_WithValidLocations_ReturnsForecastData()
      throws ExecutionException, InterruptedException {
    // Arrange
    List<String> locations = Arrays.asList("London", "Paris");
    List<ForecastDto> forecastList = Arrays.asList(forecastDto);

    when(forecastService.getForecast(anyString(), eq(3), anyBoolean())).thenReturn(forecastList);

    // Act
    CompletableFuture<List<List<ForecastDto>>> future =
        asyncBulkWeatherService.fetchBulkForecastAsync(locations, 3, false);
    List<List<ForecastDto>> results = future.get();

    // Assert
    assertThat(results).hasSize(2);
    assertThat(results).allMatch(list -> list.size() == 1);
    verify(forecastService, times(2)).getForecast(anyString(), eq(3), eq(false));
  }

  @Test
  void fetchBulkForecastAsync_WithPartialFailures_ReturnsSuccessfulResults()
      throws ExecutionException, InterruptedException {
    // Arrange
    List<String> locations = Arrays.asList("London", "InvalidLocation");
    List<ForecastDto> forecastList = Arrays.asList(forecastDto);

    when(forecastService.getForecast("London", 7, false)).thenReturn(forecastList);
    when(forecastService.getForecast("InvalidLocation", 7, false))
        .thenThrow(new RuntimeException("Location not found"));

    // Act
    CompletableFuture<List<List<ForecastDto>>> future =
        asyncBulkWeatherService.fetchBulkForecastAsync(locations, 7, false);
    List<List<ForecastDto>> results = future.get();

    // Assert
    assertThat(results).hasSize(1); // Only successful results
    verify(forecastService, times(2)).getForecast(anyString(), eq(7), eq(false));
  }

  @Test
  void bulkUpdateWeatherAsync_WithValidLocationIds_ReturnsSuccessCount()
      throws ExecutionException, InterruptedException {
    // Arrange
    List<Long> locationIds = Arrays.asList(1L, 2L, 3L);
    when(weatherService.getCurrentWeatherByLocationId(anyLong(), anyBoolean()))
        .thenReturn(weatherDto);

    // Act
    CompletableFuture<Integer> future =
        asyncBulkWeatherService.bulkUpdateWeatherAsync(locationIds, true);
    Integer successCount = future.get();

    // Assert
    assertThat(successCount).isEqualTo(3);
    verify(weatherService, times(3)).getCurrentWeatherByLocationId(anyLong(), eq(true));
  }

  @Test
  void bulkUpdateWeatherAsync_WithPartialFailures_ReturnsPartialSuccessCount()
      throws ExecutionException, InterruptedException {
    // Arrange
    List<Long> locationIds = Arrays.asList(1L, 2L, 3L);

    when(weatherService.getCurrentWeatherByLocationId(1L, true)).thenReturn(weatherDto);
    when(weatherService.getCurrentWeatherByLocationId(2L, true))
        .thenThrow(new RuntimeException("Location not found"));
    when(weatherService.getCurrentWeatherByLocationId(3L, true)).thenReturn(weatherDto);

    // Act
    CompletableFuture<Integer> future =
        asyncBulkWeatherService.bulkUpdateWeatherAsync(locationIds, true);
    Integer successCount = future.get();

    // Assert
    assertThat(successCount).isEqualTo(2); // 2 succeeded, 1 failed
    verify(weatherService, times(3)).getCurrentWeatherByLocationId(anyLong(), eq(true));
  }

  @Test
  void bulkRefreshForecastsAsync_WithValidLocationIds_ReturnsSuccessCount()
      throws ExecutionException, InterruptedException {
    // Arrange
    List<Long> locationIds = Arrays.asList(1L, 2L);
    List<ForecastDto> forecastList = Arrays.asList(forecastDto);

    when(forecastService.getForecastByLocationId(anyLong(), eq(7), anyBoolean()))
        .thenReturn(forecastList);

    // Act
    CompletableFuture<Integer> future =
        asyncBulkWeatherService.bulkRefreshForecastsAsync(locationIds, 7, true);
    Integer successCount = future.get();

    // Assert
    assertThat(successCount).isEqualTo(2);
    verify(forecastService, times(2)).getForecastByLocationId(anyLong(), eq(7), eq(true));
  }

  @Test
  void bulkRefreshForecastsAsync_WithPartialFailures_ReturnsPartialSuccessCount()
      throws ExecutionException, InterruptedException {
    // Arrange
    List<Long> locationIds = Arrays.asList(1L, 2L, 3L);
    List<ForecastDto> forecastList = Arrays.asList(forecastDto);

    when(forecastService.getForecastByLocationId(1L, 14, true)).thenReturn(forecastList);
    when(forecastService.getForecastByLocationId(2L, 14, true))
        .thenThrow(new RuntimeException("API error"));
    when(forecastService.getForecastByLocationId(3L, 14, true)).thenReturn(forecastList);

    // Act
    CompletableFuture<Integer> future =
        asyncBulkWeatherService.bulkRefreshForecastsAsync(locationIds, 14, true);
    Integer successCount = future.get();

    // Assert
    assertThat(successCount).isEqualTo(2); // 2 succeeded, 1 failed
    verify(forecastService, times(3)).getForecastByLocationId(anyLong(), eq(14), eq(true));
  }

  @Test
  void refreshAllLocationsAsync_WithNoLocations_ReturnsZeroResult()
      throws ExecutionException, InterruptedException {
    // Arrange
    when(locationService.getAllLocations()).thenReturn(Collections.emptyList());

    // Act
    CompletableFuture<BulkOperationResult> future =
        asyncBulkWeatherService.refreshAllLocationsAsync(7);
    BulkOperationResult result = future.get();

    // Assert
    assertThat(result.totalCount()).isEqualTo(0);
    assertThat(result.successCount()).isEqualTo(0);
    assertThat(result.failureCount()).isEqualTo(0);
    assertThat(result.allSuccessful()).isTrue();
  }

  @Test
  void refreshAllLocationsAsync_WithLocations_RefreshesBothWeatherAndForecast()
      throws ExecutionException, InterruptedException {
    // Arrange
    List<LocationDto> locations = Arrays.asList(locationDto, locationDto);
    when(locationService.getAllLocations()).thenReturn(locations);
    when(weatherService.getCurrentWeatherByLocationId(anyLong(), eq(true))).thenReturn(weatherDto);

    List<ForecastDto> forecastList = Arrays.asList(forecastDto);
    when(forecastService.getForecastByLocationId(anyLong(), eq(7), eq(true)))
        .thenReturn(forecastList);

    // Act
    CompletableFuture<BulkOperationResult> future =
        asyncBulkWeatherService.refreshAllLocationsAsync(7);
    BulkOperationResult result = future.get();

    // Assert
    assertThat(result.totalCount()).isEqualTo(4); // 2 locations * 2 operations
    assertThat(result.successCount()).isEqualTo(4);
    assertThat(result.failureCount()).isEqualTo(0);
    assertThat(result.allSuccessful()).isTrue();
    assertThat(result.successRate()).isEqualTo(1.0);
  }

  @Test
  void refreshAllLocationsAsync_WithPartialFailures_ReturnsPartialResult()
      throws ExecutionException, InterruptedException {
    // Arrange
    LocationDto location1 = TestDataFactory.createTestLocationDtoWithId(1L);
    LocationDto location2 = TestDataFactory.createTestLocationDtoWithId(2L);
    List<LocationDto> locations = Arrays.asList(location1, location2);
    when(locationService.getAllLocations()).thenReturn(locations);

    // First location succeeds, second fails
    when(weatherService.getCurrentWeatherByLocationId(1L, true)).thenReturn(weatherDto);
    when(weatherService.getCurrentWeatherByLocationId(2L, true))
        .thenThrow(new RuntimeException("API error"));

    List<ForecastDto> forecastList = Arrays.asList(forecastDto);
    when(forecastService.getForecastByLocationId(anyLong(), eq(3), eq(true)))
        .thenReturn(forecastList);

    // Act
    CompletableFuture<BulkOperationResult> future =
        asyncBulkWeatherService.refreshAllLocationsAsync(3);
    BulkOperationResult result = future.get();

    // Assert
    assertThat(result.totalCount()).isEqualTo(4); // 2 locations * 2 operations
    assertThat(result.successCount()).isEqualTo(3); // 1 weather + 2 forecasts
    assertThat(result.failureCount()).isEqualTo(1);
    assertThat(result.allSuccessful()).isFalse();
    assertThat(result.successRate()).isEqualTo(0.75);
  }

  @Test
  void bulkOperationResult_CalculatesSuccessRate_Correctly() {
    // Arrange & Act
    BulkOperationResult result = new BulkOperationResult(8, 2, 10);

    // Assert
    assertThat(result.successRate()).isEqualTo(0.8);
    assertThat(result.successPercentage()).isEqualTo(80.0);
    assertThat(result.allSuccessful()).isFalse();
  }

  @Test
  void bulkOperationResult_WithZeroOperations_ReturnsZeroSuccessRate() {
    // Arrange & Act
    BulkOperationResult result = new BulkOperationResult(0, 0, 0);

    // Assert
    assertThat(result.successRate()).isEqualTo(0.0);
    assertThat(result.allSuccessful()).isTrue();
  }

  @Test
  void bulkOperationResult_WithAllSuccessful_ReturnsTrue() {
    // Arrange & Act
    BulkOperationResult result = new BulkOperationResult(10, 0, 10);

    // Assert
    assertThat(result.allSuccessful()).isTrue();
    assertThat(result.successRate()).isEqualTo(1.0);
    assertThat(result.successPercentage()).isEqualTo(100.0);
  }
}

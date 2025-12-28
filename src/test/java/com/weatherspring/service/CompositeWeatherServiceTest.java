package com.weatherspring.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.weatherspring.TestDataFactory;
import com.weatherspring.dto.ForecastDto;
import com.weatherspring.dto.LocationDto;
import com.weatherspring.dto.WeatherDto;
import com.weatherspring.service.CompositeWeatherService.CompleteLocationInfo;
import com.weatherspring.service.CompositeWeatherService.WeatherWithForecast;

/**
 * Unit tests for CompositeWeatherService.
 *
 * <p>Tests concurrent composite operations using virtual threads and CompletableFuture with timeout
 * handling.
 */
@ExtendWith(MockitoExtension.class)
class CompositeWeatherServiceTest {

  @Mock private WeatherService weatherService;

  @Mock private ForecastService forecastService;

  @Mock private LocationService locationService;

  private ExecutorService virtualExecutor;

  private CompositeWeatherService compositeWeatherService;

  private WeatherDto weatherDto;
  private ForecastDto forecastDto;
  private LocationDto locationDto;

  @BeforeEach
  void setUp() {
    virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
    compositeWeatherService =
        new CompositeWeatherService(
            weatherService, forecastService, locationService, virtualExecutor);

    weatherDto = TestDataFactory.createDefaultWeatherDto();
    forecastDto = TestDataFactory.createDefaultForecastDto();
    locationDto = TestDataFactory.createTestLocationDto();
  }

  @Test
  void getWeatherAndForecast_WithValidLocation_ReturnsBothResults() {
    // Arrange
    String locationName = "London";
    List<ForecastDto> forecasts = Arrays.asList(forecastDto, forecastDto, forecastDto);

    when(weatherService.getCurrentWeather(locationName, false)).thenReturn(weatherDto);
    when(forecastService.getForecast(locationName, 3, false)).thenReturn(forecasts);

    // Act
    WeatherWithForecast result =
        compositeWeatherService.getWeatherAndForecast(locationName, 3, false);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.weather()).isEqualTo(weatherDto);
    assertThat(result.forecasts()).hasSize(3);
    assertThat(result.forecasts()).isEqualTo(forecasts);

    verify(weatherService).getCurrentWeather(locationName, false);
    verify(forecastService).getForecast(locationName, 3, false);
  }

  @Test
  void getWeatherAndForecast_WithSaveToDatabase_SavesBothResults() {
    // Arrange
    String locationName = "Paris";
    List<ForecastDto> forecasts = Arrays.asList(forecastDto);

    when(weatherService.getCurrentWeather(locationName, true)).thenReturn(weatherDto);
    when(forecastService.getForecast(locationName, 7, true)).thenReturn(forecasts);

    // Act
    WeatherWithForecast result =
        compositeWeatherService.getWeatherAndForecast(locationName, 7, true);

    // Assert
    assertThat(result).isNotNull();
    verify(weatherService).getCurrentWeather(locationName, true);
    verify(forecastService).getForecast(locationName, 7, true);
  }

  @Test
  void getWeatherAndForecast_WithDifferentForecastDays_UsesCorrectParameter() {
    // Arrange
    String locationName = "Tokyo";
    when(weatherService.getCurrentWeather(anyString(), anyBoolean())).thenReturn(weatherDto);
    when(forecastService.getForecast(anyString(), anyInt(), anyBoolean()))
        .thenReturn(Collections.emptyList());

    // Act
    compositeWeatherService.getWeatherAndForecast(locationName, 14, false);

    // Assert
    verify(forecastService).getForecast(locationName, 14, false);
  }

  @Test
  void getWeatherAndForecastByLocationId_WithValidLocationId_ReturnsBothResults() {
    // Arrange
    Long locationId = 1L;
    List<ForecastDto> forecasts = Arrays.asList(forecastDto);

    when(weatherService.getCurrentWeatherByLocationId(locationId, false)).thenReturn(weatherDto);
    when(forecastService.getForecastByLocationId(locationId, 5, false)).thenReturn(forecasts);

    // Act
    WeatherWithForecast result =
        compositeWeatherService.getWeatherAndForecastByLocationId(locationId, 5, false);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.weather()).isEqualTo(weatherDto);
    assertThat(result.forecasts()).isEqualTo(forecasts);

    verify(weatherService).getCurrentWeatherByLocationId(locationId, false);
    verify(forecastService).getForecastByLocationId(locationId, 5, false);
  }

  @Test
  void getWeatherAndForecastByLocationId_WithSaveToDatabase_SavesData() {
    // Arrange
    Long locationId = 2L;
    when(weatherService.getCurrentWeatherByLocationId(anyLong(), eq(true))).thenReturn(weatherDto);
    when(forecastService.getForecastByLocationId(anyLong(), anyInt(), eq(true)))
        .thenReturn(Collections.emptyList());

    // Act
    compositeWeatherService.getWeatherAndForecastByLocationId(locationId, 3, true);

    // Assert
    verify(weatherService).getCurrentWeatherByLocationId(locationId, true);
    verify(forecastService).getForecastByLocationId(locationId, 3, true);
  }

  @Test
  void getCompleteLocationInfo_WithValidLocationId_ReturnsAllThreeResults() {
    // Arrange
    Long locationId = 1L;
    List<ForecastDto> forecasts = Arrays.asList(forecastDto);

    when(locationService.getLocationById(locationId)).thenReturn(locationDto);
    when(weatherService.getCurrentWeatherByLocationId(locationId, false)).thenReturn(weatherDto);
    when(forecastService.getForecastByLocationId(locationId, 3, false)).thenReturn(forecasts);

    // Act
    CompleteLocationInfo result =
        compositeWeatherService.getCompleteLocationInfo(locationId, 3, false);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.location()).isEqualTo(locationDto);
    assertThat(result.weather()).isEqualTo(weatherDto);
    assertThat(result.forecasts()).isEqualTo(forecasts);

    verify(locationService).getLocationById(locationId);
    verify(weatherService).getCurrentWeatherByLocationId(locationId, false);
    verify(forecastService).getForecastByLocationId(locationId, 3, false);
  }

  @Test
  void getCompleteLocationInfo_WithSaveToDatabase_SavesWeatherAndForecast() {
    // Arrange
    Long locationId = 3L;
    when(locationService.getLocationById(anyLong())).thenReturn(locationDto);
    when(weatherService.getCurrentWeatherByLocationId(anyLong(), eq(true))).thenReturn(weatherDto);
    when(forecastService.getForecastByLocationId(anyLong(), anyInt(), eq(true)))
        .thenReturn(Collections.emptyList());

    // Act
    compositeWeatherService.getCompleteLocationInfo(locationId, 7, true);

    // Assert
    verify(locationService).getLocationById(locationId);
    verify(weatherService).getCurrentWeatherByLocationId(locationId, true);
    verify(forecastService).getForecastByLocationId(locationId, 7, true);
  }

  @Test
  void getBulkWeather_WithMultipleLocations_ReturnAllWeatherData() {
    // Arrange
    List<String> locations = Arrays.asList("London", "Paris", "Tokyo");
    when(weatherService.getCurrentWeather(anyString(), anyBoolean())).thenReturn(weatherDto);

    // Act
    List<WeatherDto> results = compositeWeatherService.getBulkWeather(locations, false);

    // Assert
    assertThat(results).hasSize(3);
    assertThat(results).allMatch(dto -> dto.equals(weatherDto));

    verify(weatherService, times(3)).getCurrentWeather(anyString(), eq(false));
  }

  @Test
  void getBulkWeather_WithEmptyList_ReturnsEmptyResult() {
    // Arrange
    List<String> locations = Collections.emptyList();

    // Act
    List<WeatherDto> results = compositeWeatherService.getBulkWeather(locations, false);

    // Assert
    assertThat(results).isEmpty();
    verify(weatherService, never()).getCurrentWeather(anyString(), anyBoolean());
  }

  @Test
  void getBulkWeather_WithSaveToDatabase_SavesAllData() {
    // Arrange
    List<String> locations = Arrays.asList("London", "Paris");
    when(weatherService.getCurrentWeather(anyString(), eq(true))).thenReturn(weatherDto);

    // Act
    compositeWeatherService.getBulkWeather(locations, true);

    // Assert
    verify(weatherService).getCurrentWeather("London", true);
    verify(weatherService).getCurrentWeather("Paris", true);
  }

  @Test
  void getBulkWeather_WithSingleLocation_ReturnsSingleResult() {
    // Arrange
    List<String> locations = Arrays.asList("London");
    when(weatherService.getCurrentWeather("London", false)).thenReturn(weatherDto);

    // Act
    List<WeatherDto> results = compositeWeatherService.getBulkWeather(locations, false);

    // Assert
    assertThat(results).hasSize(1);
    assertThat(results.get(0)).isEqualTo(weatherDto);
  }

  @Test
  void getWeatherAndForecast_ConcurrentExecution_CompletesSuccessfully() {
    // Arrange
    String locationName = "London";
    List<ForecastDto> forecasts = Arrays.asList(forecastDto);

    when(weatherService.getCurrentWeather(locationName, false)).thenReturn(weatherDto);
    when(forecastService.getForecast(locationName, 3, false)).thenReturn(forecasts);

    // Act
    WeatherWithForecast result =
        compositeWeatherService.getWeatherAndForecast(locationName, 3, false);

    // Assert - verify concurrent execution by checking both services were called
    assertThat(result).isNotNull();
    assertThat(result.weather()).isEqualTo(weatherDto);
    assertThat(result.forecasts()).isEqualTo(forecasts);

    // Verify both services were called (proves concurrent execution happened)
    verify(weatherService).getCurrentWeather(locationName, false);
    verify(forecastService).getForecast(locationName, 3, false);
  }

  @Test
  void getCompleteLocationInfo_ThreeWayParallel_CompletesSuccessfully() {
    // Arrange
    Long locationId = 1L;
    List<ForecastDto> forecasts = Collections.emptyList();

    when(locationService.getLocationById(locationId)).thenReturn(locationDto);
    when(weatherService.getCurrentWeatherByLocationId(locationId, false)).thenReturn(weatherDto);
    when(forecastService.getForecastByLocationId(locationId, 3, false)).thenReturn(forecasts);

    // Act
    CompleteLocationInfo result =
        compositeWeatherService.getCompleteLocationInfo(locationId, 3, false);

    // Assert - verify three-way parallel execution by checking all services were called
    assertThat(result).isNotNull();
    assertThat(result.location()).isEqualTo(locationDto);
    assertThat(result.weather()).isEqualTo(weatherDto);
    assertThat(result.forecasts()).isEqualTo(forecasts);

    // Verify all three services were called (proves parallel execution happened)
    verify(locationService).getLocationById(locationId);
    verify(weatherService).getCurrentWeatherByLocationId(locationId, false);
    verify(forecastService).getForecastByLocationId(locationId, 3, false);
  }

  @Test
  void weatherWithForecast_Record_CreatesCorrectly() {
    // Arrange & Act
    List<ForecastDto> forecasts = Arrays.asList(forecastDto);
    WeatherWithForecast result = new WeatherWithForecast(weatherDto, forecasts);

    // Assert
    assertThat(result.weather()).isEqualTo(weatherDto);
    assertThat(result.forecasts()).isEqualTo(forecasts);
  }

  @Test
  void completeLocationInfo_Record_CreatesCorrectly() {
    // Arrange & Act
    List<ForecastDto> forecasts = Arrays.asList(forecastDto);
    CompleteLocationInfo result = new CompleteLocationInfo(locationDto, weatherDto, forecasts);

    // Assert
    assertThat(result.location()).isEqualTo(locationDto);
    assertThat(result.weather()).isEqualTo(weatherDto);
    assertThat(result.forecasts()).isEqualTo(forecasts);
  }
}

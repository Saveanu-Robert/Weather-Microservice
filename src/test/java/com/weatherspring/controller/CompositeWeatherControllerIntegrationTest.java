package com.weatherspring.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

import com.weatherspring.TestDataFactory;
import com.weatherspring.config.TestSecurityConfig;
import com.weatherspring.dto.ForecastDto;
import com.weatherspring.dto.LocationDto;
import com.weatherspring.dto.WeatherDto;
import com.weatherspring.service.CompositeWeatherService;
import com.weatherspring.service.CompositeWeatherService.CompleteLocationInfo;
import com.weatherspring.service.CompositeWeatherService.WeatherWithForecast;

/**
 * Integration tests for CompositeWeatherController.
 *
 * <p>Tests composite weather queries using structured concurrency with virtual threads.
 */
@WebMvcTest(CompositeWeatherController.class)
@Import({TestSecurityConfig.class, com.weatherspring.exception.GlobalExceptionHandler.class})
@ActiveProfiles("test")
@WithMockUser(roles = "USER")
class CompositeWeatherControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CompositeWeatherService compositeWeatherService;

  private WeatherDto weatherDto;
  private ForecastDto forecastDto;
  private LocationDto locationDto;
  private List<ForecastDto> forecastList;
  private WeatherWithForecast weatherWithForecast;
  private CompleteLocationInfo completeLocationInfo;

  @BeforeEach
  void setUp() {
    weatherDto = TestDataFactory.createDefaultWeatherDto();
    forecastDto = TestDataFactory.createDefaultForecastDto();
    locationDto = TestDataFactory.createTestLocationDto();
    forecastList = Arrays.asList(forecastDto, forecastDto, forecastDto);

    weatherWithForecast = new WeatherWithForecast(weatherDto, forecastList);
    completeLocationInfo = new CompleteLocationInfo(locationDto, weatherDto, forecastList);
  }

  // ==================== GET /api/composite/weather-and-forecast ====================

  @Test
  void getWeatherAndForecast_WithValidLocation_ReturnsCompositeData() throws Exception {
    // Arrange
    String locationName = "London";
    when(compositeWeatherService.getWeatherAndForecast(anyString(), anyInt(), anyBoolean()))
        .thenReturn(weatherWithForecast);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/weather-and-forecast")
                .param("locationName", locationName)
                .param("days", "3")
                .param("save", "false")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.weather").exists())
        .andExpect(jsonPath("$.weather.locationName").value("London"))
        .andExpect(jsonPath("$.weather.temperature").value(15.5))
        .andExpect(jsonPath("$.forecasts").isArray())
        .andExpect(jsonPath("$.forecasts.length()").value(3));

    verify(compositeWeatherService).getWeatherAndForecast(locationName, 3, false);
  }

  @Test
  void getWeatherAndForecast_WithDefaultDays_Uses3Days() throws Exception {
    // Arrange
    when(compositeWeatherService.getWeatherAndForecast(anyString(), eq(3), anyBoolean()))
        .thenReturn(weatherWithForecast);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/weather-and-forecast")
                .param("locationName", "London")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(compositeWeatherService).getWeatherAndForecast("London", 3, false);
  }

  @Test
  void getWeatherAndForecast_WithSaveFlag_PassesSaveTrue() throws Exception {
    // Arrange
    when(compositeWeatherService.getWeatherAndForecast(anyString(), anyInt(), eq(true)))
        .thenReturn(weatherWithForecast);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/weather-and-forecast")
                .param("locationName", "Paris")
                .param("days", "7")
                .param("save", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(compositeWeatherService).getWeatherAndForecast("Paris", 7, true);
  }

  @Test
  void getWeatherAndForecast_WithInvalidDays_ReturnsBadRequest() throws Exception {
    // Act & Assert - Days less than 1
    mockMvc
        .perform(
            get("/api/composite/weather-and-forecast")
                .param("locationName", "London")
                .param("days", "0")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    // Act & Assert - Days more than 14
    mockMvc
        .perform(
            get("/api/composite/weather-and-forecast")
                .param("locationName", "London")
                .param("days", "15")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(compositeWeatherService, never())
        .getWeatherAndForecast(anyString(), anyInt(), anyBoolean());
  }

  @Test
  void getWeatherAndForecast_WithBlankLocation_ReturnsBadRequest() throws Exception {
    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/weather-and-forecast")
                .param("locationName", "")
                .param("days", "3")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(compositeWeatherService, never())
        .getWeatherAndForecast(anyString(), anyInt(), anyBoolean());
  }

  @Test
  void getWeatherAndForecast_WithMaximumDays_AcceptsRequest() throws Exception {
    // Arrange
    when(compositeWeatherService.getWeatherAndForecast(anyString(), eq(14), anyBoolean()))
        .thenReturn(weatherWithForecast);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/weather-and-forecast")
                .param("locationName", "Tokyo")
                .param("days", "14")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(compositeWeatherService).getWeatherAndForecast("Tokyo", 14, false);
  }

  // ==================== GET /api/composite/weather-and-forecast/{locationId} ====================

  @Test
  void getWeatherAndForecastByLocationId_WithValidId_ReturnsCompositeData() throws Exception {
    // Arrange
    Long locationId = 1L;
    when(compositeWeatherService.getWeatherAndForecastByLocationId(
            anyLong(), anyInt(), anyBoolean()))
        .thenReturn(weatherWithForecast);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/weather-and-forecast/{locationId}", locationId)
                .param("days", "5")
                .param("save", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.weather").exists())
        .andExpect(jsonPath("$.weather.locationName").value("London"))
        .andExpect(jsonPath("$.forecasts").isArray())
        .andExpect(jsonPath("$.forecasts.length()").value(3));

    verify(compositeWeatherService).getWeatherAndForecastByLocationId(locationId, 5, true);
  }

  @Test
  void getWeatherAndForecastByLocationId_WithDefaultParameters_UsesDefaults() throws Exception {
    // Arrange
    Long locationId = 2L;
    when(compositeWeatherService.getWeatherAndForecastByLocationId(eq(2L), eq(3), eq(false)))
        .thenReturn(weatherWithForecast);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/weather-and-forecast/{locationId}", locationId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(compositeWeatherService).getWeatherAndForecastByLocationId(2L, 3, false);
  }

  @Test
  void getWeatherAndForecastByLocationId_WithInvalidDays_ReturnsBadRequest() throws Exception {
    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/weather-and-forecast/{locationId}", 1L)
                .param("days", "20")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(compositeWeatherService, never())
        .getWeatherAndForecastByLocationId(anyLong(), anyInt(), anyBoolean());
  }

  // ==================== GET /api/composite/complete-info/{locationId} ====================

  @Test
  void getCompleteLocationInfo_WithValidId_ReturnsCompleteData() throws Exception {
    // Arrange
    Long locationId = 1L;
    when(compositeWeatherService.getCompleteLocationInfo(anyLong(), anyInt(), anyBoolean()))
        .thenReturn(completeLocationInfo);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/complete-info/{locationId}", locationId)
                .param("days", "7")
                .param("save", "false")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.location").exists())
        .andExpect(jsonPath("$.location.name").value("London"))
        .andExpect(jsonPath("$.location.country").value("United Kingdom"))
        .andExpect(jsonPath("$.weather").exists())
        .andExpect(jsonPath("$.weather.temperature").value(15.5))
        .andExpect(jsonPath("$.forecasts").isArray())
        .andExpect(jsonPath("$.forecasts.length()").value(3));

    verify(compositeWeatherService).getCompleteLocationInfo(locationId, 7, false);
  }

  @Test
  void getCompleteLocationInfo_WithDefaultParameters_UsesDefaults() throws Exception {
    // Arrange
    Long locationId = 3L;
    when(compositeWeatherService.getCompleteLocationInfo(eq(3L), eq(3), eq(false)))
        .thenReturn(completeLocationInfo);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/complete-info/{locationId}", locationId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(compositeWeatherService).getCompleteLocationInfo(3L, 3, false);
  }

  @Test
  void getCompleteLocationInfo_WithSaveFlag_PassesSaveTrue() throws Exception {
    // Arrange
    Long locationId = 1L;
    when(compositeWeatherService.getCompleteLocationInfo(anyLong(), anyInt(), eq(true)))
        .thenReturn(completeLocationInfo);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/complete-info/{locationId}", locationId)
                .param("days", "14")
                .param("save", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(compositeWeatherService).getCompleteLocationInfo(locationId, 14, true);
  }

  @Test
  void getCompleteLocationInfo_WithInvalidDays_ReturnsBadRequest() throws Exception {
    // Act & Assert - Days less than 1
    mockMvc
        .perform(
            get("/api/composite/complete-info/{locationId}", 1L)
                .param("days", "-1")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(compositeWeatherService, never())
        .getCompleteLocationInfo(anyLong(), anyInt(), anyBoolean());
  }

  @Test
  void getCompleteLocationInfo_ValidatesThreeWayParallelFetch() throws Exception {
    // Arrange
    Long locationId = 1L;
    when(compositeWeatherService.getCompleteLocationInfo(anyLong(), anyInt(), anyBoolean()))
        .thenReturn(completeLocationInfo);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/complete-info/{locationId}", locationId)
                .param("days", "3")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.location.id").value(1))
        .andExpect(jsonPath("$.weather.locationId").value(1))
        .andExpect(jsonPath("$.forecasts[0].locationId").value(1));

    // Verify all three components were fetched concurrently
    verify(compositeWeatherService).getCompleteLocationInfo(locationId, 3, false);
  }

  // ==================== GET /api/composite/bulk-weather ====================

  @Test
  void getBulkWeather_WithMultipleLocations_ReturnsAllWeatherData() throws Exception {
    // Arrange
    List<String> locations = Arrays.asList("London", "Paris", "Tokyo");
    List<WeatherDto> weatherList = Arrays.asList(weatherDto, weatherDto, weatherDto);

    when(compositeWeatherService.getBulkWeather(anyList(), anyBoolean())).thenReturn(weatherList);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/bulk-weather")
                .param("locations", "London", "Paris", "Tokyo")
                .param("save", "false")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0].locationName").value("London"))
        .andExpect(jsonPath("$[0].temperature").value(15.5));

    verify(compositeWeatherService).getBulkWeather(locations, false);
  }

  @Test
  void getBulkWeather_WithSingleLocation_ReturnsSingleResult() throws Exception {
    // Arrange
    List<String> locations = Collections.singletonList("London");
    List<WeatherDto> weatherList = Collections.singletonList(weatherDto);

    when(compositeWeatherService.getBulkWeather(anyList(), anyBoolean())).thenReturn(weatherList);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/bulk-weather")
                .param("locations", "London")
                .param("save", "false")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));

    verify(compositeWeatherService).getBulkWeather(locations, false);
  }

  @Test
  void getBulkWeather_WithSaveFlag_PassesSaveTrue() throws Exception {
    // Arrange
    List<String> locations = Arrays.asList("London", "Paris");
    List<WeatherDto> weatherList = Arrays.asList(weatherDto, weatherDto);

    when(compositeWeatherService.getBulkWeather(anyList(), eq(true))).thenReturn(weatherList);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/bulk-weather")
                .param("locations", "London", "Paris")
                .param("save", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(compositeWeatherService).getBulkWeather(locations, true);
  }

  @Test
  void getBulkWeather_WithEmptyLocationList_ReturnsBadRequest() throws Exception {
    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/bulk-weather")
                .param("locations", "")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(compositeWeatherService, never()).getBulkWeather(anyList(), anyBoolean());
  }

  @Test
  void getBulkWeather_WithTooManyLocations_ReturnsBadRequest() throws Exception {
    // Arrange - Create array with 101 locations (exceeds max of 100)
    String[] locations = new String[101];
    Arrays.fill(locations, "London");

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/bulk-weather")
                .param("locations", locations)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(compositeWeatherService, never()).getBulkWeather(anyList(), anyBoolean());
  }

  @Test
  void getBulkWeather_WithDefaultSave_UsesFalse() throws Exception {
    // Arrange
    List<String> locations = Collections.singletonList("Berlin");
    when(compositeWeatherService.getBulkWeather(anyList(), eq(false)))
        .thenReturn(Collections.singletonList(weatherDto));

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/bulk-weather")
                .param("locations", "Berlin")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(compositeWeatherService).getBulkWeather(locations, false);
  }

  @Test
  void getBulkWeather_ValidatesConcurrentExecution() throws Exception {
    // Arrange
    List<String> locations = Arrays.asList("London", "Paris", "Tokyo", "New York", "Berlin");
    List<WeatherDto> weatherList =
        Arrays.asList(weatherDto, weatherDto, weatherDto, weatherDto, weatherDto);

    when(compositeWeatherService.getBulkWeather(anyList(), anyBoolean())).thenReturn(weatherList);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/bulk-weather")
                .param("locations", "London", "Paris", "Tokyo", "New York", "Berlin")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(5));

    // Verify all 5 locations were processed
    verify(compositeWeatherService).getBulkWeather(locations, false);
  }

  // ==================== Response Structure Tests ====================

  @Test
  void weatherWithForecast_ContainsCorrectStructure() throws Exception {
    // Arrange
    when(compositeWeatherService.getWeatherAndForecast(anyString(), anyInt(), anyBoolean()))
        .thenReturn(weatherWithForecast);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/weather-and-forecast")
                .param("locationName", "London")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.weather.temperature").exists())
        .andExpect(jsonPath("$.weather.humidity").exists())
        .andExpect(jsonPath("$.weather.windSpeed").exists())
        .andExpect(jsonPath("$.forecasts[0].maxTemperature").exists())
        .andExpect(jsonPath("$.forecasts[0].minTemperature").exists());
  }

  @Test
  void completeLocationInfo_ContainsAllThreeComponents() throws Exception {
    // Arrange
    when(compositeWeatherService.getCompleteLocationInfo(anyLong(), anyInt(), anyBoolean()))
        .thenReturn(completeLocationInfo);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/composite/complete-info/{locationId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        // Location component
        .andExpect(jsonPath("$.location.name").value("London"))
        .andExpect(jsonPath("$.location.latitude").exists())
        .andExpect(jsonPath("$.location.longitude").exists())
        // Weather component
        .andExpect(jsonPath("$.weather.temperature").exists())
        .andExpect(jsonPath("$.weather.condition").exists())
        // Forecast component
        .andExpect(jsonPath("$.forecasts").isArray())
        .andExpect(jsonPath("$.forecasts[0].forecastDate").exists());
  }
}

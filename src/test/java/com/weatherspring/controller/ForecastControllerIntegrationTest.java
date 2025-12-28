package com.weatherspring.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.weatherspring.config.TestSecurityConfig;
import com.weatherspring.dto.ForecastDto;
import com.weatherspring.service.ForecastService;

/** Integration tests for ForecastController. */
@WebMvcTest(ForecastController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@WithMockUser(roles = "USER")
class ForecastControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ForecastService forecastService;

  @Test
  void getForecast_WithValidParameters_ReturnsForecastList() throws Exception {
    // Arrange
    ForecastDto forecastDto =
        new ForecastDto(
            1L, // id
            1L, // locationId
            "London", // locationName
            LocalDate.now().plusDays(1), // forecastDate
            25.0, // maxTemperature
            15.0, // minTemperature
            20.0, // avgTemperature
            22.0, // maxWindSpeed
            60, // avgHumidity
            "Partly cloudy", // condition
            "Partly cloudy with occasional sunshine", // description
            5.0, // precipitationMm
            30, // precipitationProbability
            4.0, // uvIndex
            "06:30", // sunriseTime
            "20:00" // sunsetTime
            );
    List<ForecastDto> forecasts = Collections.singletonList(forecastDto);
    when(forecastService.getForecast(anyString(), anyInt(), anyBoolean())).thenReturn(forecasts);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/forecast")
                .param("location", "London")
                .param("days", "3")
                .param("save", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].maxTemperature").value(25.0))
        .andExpect(jsonPath("$[0].minTemperature").value(15.0))
        .andExpect(jsonPath("$[0].condition").value("Partly cloudy"));
  }

  @Test
  void getForecastById_WithValidId_ReturnsForecastList() throws Exception {
    // Arrange
    ForecastDto forecastDto =
        new ForecastDto(
            1L, // id
            1L, // locationId
            "Paris", // locationName
            LocalDate.now().plusDays(1), // forecastDate
            24.0, // maxTemperature
            14.0, // minTemperature
            19.0, // avgTemperature
            20.0, // maxWindSpeed
            55, // avgHumidity
            "Sunny", // condition
            "Clear sunny weather expected", // description
            0.0, // precipitationMm
            0, // precipitationProbability
            6.0, // uvIndex
            "06:15", // sunriseTime
            "20:15" // sunsetTime
            );
    List<ForecastDto> forecasts = Collections.singletonList(forecastDto);
    when(forecastService.getForecastByLocationId(anyLong(), anyInt(), anyBoolean()))
        .thenReturn(forecasts);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/forecast/location/1")
                .param("days", "5")
                .param("save", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].maxTemperature").value(24.0))
        .andExpect(jsonPath("$[0].condition").value("Sunny"));
  }

  @Test
  void getFutureForecasts_WithValidId_ReturnsForecastList() throws Exception {
    // Arrange
    ForecastDto forecastDto =
        new ForecastDto(
            1L, // id
            1L, // locationId
            "Berlin", // locationName
            LocalDate.now().plusDays(2), // forecastDate
            23.0, // maxTemperature
            13.0, // minTemperature
            18.0, // avgTemperature
            19.0, // maxWindSpeed
            65, // avgHumidity
            "Cloudy", // condition
            "Overcast with possible light rain", // description
            10.0, // precipitationMm
            60, // precipitationProbability
            3.0, // uvIndex
            "06:45", // sunriseTime
            "19:45" // sunsetTime
            );
    List<ForecastDto> forecasts = Collections.singletonList(forecastDto);
    when(forecastService.getFutureForecasts(anyLong())).thenReturn(forecasts);

    // Act & Assert
    mockMvc
        .perform(get("/api/forecast/future/location/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].maxTemperature").value(23.0))
        .andExpect(jsonPath("$[0].condition").value("Cloudy"));
  }

  @Test
  void getForecastsByDateRange_WithValidParameters_ReturnsForecastList() throws Exception {
    // Arrange
    ForecastDto forecastDto =
        new ForecastDto(
            1L, // id
            1L, // locationId
            "Madrid", // locationName
            LocalDate.now().plusDays(3), // forecastDate
            26.0, // maxTemperature
            16.0, // minTemperature
            21.0, // avgTemperature
            15.0, // maxWindSpeed
            50, // avgHumidity
            "Clear", // condition
            "Clear skies throughout the day", // description
            0.0, // precipitationMm
            0, // precipitationProbability
            7.0, // uvIndex
            "07:00", // sunriseTime
            "20:30" // sunsetTime
            );
    List<ForecastDto> forecasts = Collections.singletonList(forecastDto);
    when(forecastService.getForecastsByDateRange(anyLong(), any(), any())).thenReturn(forecasts);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/forecast/range/location/1")
                .param("startDate", "2025-11-20")
                .param("endDate", "2025-11-27")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].maxTemperature").value(26.0))
        .andExpect(jsonPath("$[0].condition").value("Clear"));
  }

  @Test
  void getForecast_WithBlankLocation_ReturnsBadRequest() throws Exception {
    // Act & Assert
    mockMvc
        .perform(
            get("/api/forecast")
                .param("location", "")
                .param("days", "3")
                .param("save", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(forecastService, never()).getForecast(anyString(), anyInt(), anyBoolean());
  }

  @Test
  void getForecast_WithDaysZero_ReturnsBadRequest() throws Exception {
    // Act & Assert
    mockMvc
        .perform(
            get("/api/forecast")
                .param("location", "London")
                .param("days", "0")
                .param("save", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(forecastService, never()).getForecast(anyString(), anyInt(), anyBoolean());
  }

  @Test
  void getForecast_WithNegativeDays_ReturnsBadRequest() throws Exception {
    // Act & Assert
    mockMvc
        .perform(
            get("/api/forecast")
                .param("location", "London")
                .param("days", "-1")
                .param("save", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(forecastService, never()).getForecast(anyString(), anyInt(), anyBoolean());
  }

  @Test
  void getForecast_WithDaysAboveMaximum_ReturnsBadRequest() throws Exception {
    // Act & Assert
    mockMvc
        .perform(
            get("/api/forecast")
                .param("location", "London")
                .param("days", "15")
                .param("save", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(forecastService, never()).getForecast(anyString(), anyInt(), anyBoolean());
  }

  @Test
  void getForecastByLocationId_WithDaysAboveMaximum_ReturnsBadRequest() throws Exception {
    // Act & Assert
    mockMvc
        .perform(
            get("/api/forecast/location/1")
                .param("days", "20")
                .param("save", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(forecastService, never()).getForecastByLocationId(anyLong(), anyInt(), anyBoolean());
  }
}

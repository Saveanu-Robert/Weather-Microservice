package com.weatherspring.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Collections;

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
import com.weatherspring.dto.WeatherDto;
import com.weatherspring.service.WeatherService;

/** Integration tests for WeatherController. */
@WebMvcTest(WeatherController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@WithMockUser(roles = "USER")
class WeatherControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private WeatherService weatherService;

  @Test
  void getCurrentWeather_WithValidLocation_ReturnsWeatherDto() throws Exception {
    // Arrange
    WeatherDto weatherDto =
        new WeatherDto(
            1L, // id
            1L, // locationId
            "London", // locationName
            20.5, // temperature
            18.0, // feelsLike
            65, // humidity
            10.0, // windSpeed
            "NW", // windDirection
            "Partly cloudy", // condition
            "Partly cloudy with light winds", // description
            1013.0, // pressureMb
            0.0, // precipitationMm
            40, // cloudCoverage
            3.0, // uvIndex
            LocalDateTime.now() // timestamp
            );
    when(weatherService.getCurrentWeather(anyString(), anyBoolean())).thenReturn(weatherDto);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/weather/current")
                .param("location", "London")
                .param("save", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.temperature").value(20.5))
        .andExpect(jsonPath("$.feelsLike").value(18.0))
        .andExpect(jsonPath("$.humidity").value(65))
        .andExpect(jsonPath("$.condition").value("Partly cloudy"));
  }

  @Test
  void getCurrentWeatherById_WithValidId_ReturnsWeatherDto() throws Exception {
    // Arrange
    WeatherDto weatherDto =
        new WeatherDto(
            1L, // id
            1L, // locationId
            "London", // locationName
            22.0, // temperature
            20.0, // feelsLike
            60, // humidity
            8.0, // windSpeed
            "N", // windDirection
            "Sunny", // condition
            "Clear sunny day", // description
            1015.0, // pressureMb
            0.0, // precipitationMm
            10, // cloudCoverage
            5.0, // uvIndex
            LocalDateTime.now() // timestamp
            );
    when(weatherService.getCurrentWeatherByLocationId(anyLong(), anyBoolean()))
        .thenReturn(weatherDto);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/weather/current/location/1")
                .param("save", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.temperature").value(22.0))
        .andExpect(jsonPath("$.condition").value("Sunny"));
  }

  @Test
  void getWeatherHistory_WithValidParameters_ReturnsWeatherPage() throws Exception {
    // Arrange
    WeatherDto weatherDto =
        new WeatherDto(
            1L, // id
            1L, // locationId
            "London", // locationName
            19.0, // temperature
            17.0, // feelsLike
            70, // humidity
            12.0, // windSpeed
            "W", // windDirection
            "Cloudy", // condition
            "Overcast conditions", // description
            1010.0, // pressureMb
            0.5, // precipitationMm
            80, // cloudCoverage
            2.0, // uvIndex
            LocalDateTime.now().minusDays(1) // timestamp
            );

    org.springframework.data.domain.Page<WeatherDto> page =
        new org.springframework.data.domain.PageImpl<>(Collections.singletonList(weatherDto));
    when(weatherService.getWeatherHistory(anyLong(), any())).thenReturn(page);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/weather/history/location/1")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].temperature").value(19.0))
        .andExpect(jsonPath("$.content[0].condition").value("Cloudy"));
  }

  @Test
  void getCurrentWeather_WithBlankLocation_ReturnsBadRequest() throws Exception {
    // Act & Assert
    mockMvc
        .perform(
            get("/api/weather/current")
                .param("location", "")
                .param("save", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(weatherService, never()).getCurrentWeather(anyString(), anyBoolean());
  }
}

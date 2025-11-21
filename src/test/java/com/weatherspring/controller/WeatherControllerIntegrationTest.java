package com.weatherspring.controller;

import com.weatherspring.dto.WeatherDto;
import com.weatherspring.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for WeatherController.
 */
@WebMvcTest(WeatherController.class)
@ActiveProfiles("test")
class WeatherControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Test
    void getCurrentWeather_WithValidLocation_ReturnsWeatherDto() throws Exception {
        // Arrange
        WeatherDto weatherDto = WeatherDto.builder()
                .id(1L)
                .locationId(1L)
                .locationName("London")
                .temperature(20.5)
                .feelsLike(18.0)
                .humidity(65)
                .windSpeed(10.0)
                .windDirection("NW")
                .condition("Partly cloudy")
                .description("Partly cloudy with light winds")
                .pressureMb(1013.0)
                .precipitationMm(0.0)
                .cloudCoverage(40)
                .uvIndex(3.0)
                .timestamp(LocalDateTime.now())
                .build();
        when(weatherService.getCurrentWeather(anyString(), anyBoolean())).thenReturn(weatherDto);

        // Act & Assert
        mockMvc.perform(get("/api/weather/current")
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
        WeatherDto weatherDto = WeatherDto.builder()
                .id(1L)
                .locationId(1L)
                .locationName("London")
                .temperature(22.0)
                .feelsLike(20.0)
                .humidity(60)
                .windSpeed(8.0)
                .windDirection("N")
                .condition("Sunny")
                .description("Clear sunny day")
                .pressureMb(1015.0)
                .precipitationMm(0.0)
                .cloudCoverage(10)
                .uvIndex(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        when(weatherService.getCurrentWeatherByLocationId(anyLong(), anyBoolean())).thenReturn(weatherDto);

        // Act & Assert
        mockMvc.perform(get("/api/weather/current/location/1")
                        .param("save", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(22.0))
                .andExpect(jsonPath("$.condition").value("Sunny"));
    }

    @Test
    void getWeatherHistory_WithValidParameters_ReturnsWeatherPage() throws Exception {
        // Arrange
        WeatherDto weatherDto = WeatherDto.builder()
                .id(1L)
                .locationId(1L)
                .locationName("London")
                .temperature(19.0)
                .feelsLike(17.0)
                .humidity(70)
                .windSpeed(12.0)
                .windDirection("W")
                .condition("Cloudy")
                .description("Overcast conditions")
                .pressureMb(1010.0)
                .precipitationMm(0.5)
                .cloudCoverage(80)
                .uvIndex(2.0)
                .timestamp(LocalDateTime.now().minusDays(1))
                .build();

        org.springframework.data.domain.Page<WeatherDto> page =
                new org.springframework.data.domain.PageImpl<>(Collections.singletonList(weatherDto));
        when(weatherService.getWeatherHistory(anyLong(), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/weather/history/location/1")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].temperature").value(19.0))
                .andExpect(jsonPath("$.content[0].condition").value("Cloudy"));
    }
}

package com.weatherspring.controller;

import com.weatherspring.dto.ForecastDto;
import com.weatherspring.service.ForecastService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ForecastController.
 */
@WebMvcTest(ForecastController.class)
@ActiveProfiles("test")
class ForecastControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ForecastService forecastService;

    @Test
    void getForecast_WithValidParameters_ReturnsForecastList() throws Exception {
        // Arrange
        ForecastDto forecastDto = ForecastDto.builder()
                .id(1L)
                .locationId(1L)
                .locationName("London")
                .forecastDate(LocalDate.now().plusDays(1))
                .maxTemperature(25.0)
                .minTemperature(15.0)
                .avgTemperature(20.0)
                .maxWindSpeed(22.0)
                .avgHumidity(60)
                .condition("Partly cloudy")
                .description("Partly cloudy with occasional sunshine")
                .precipitationMm(5.0)
                .precipitationProbability(30)
                .uvIndex(4.0)
                .sunriseTime("06:30")
                .sunsetTime("20:00")
                .build();
        List<ForecastDto> forecasts = Collections.singletonList(forecastDto);
        when(forecastService.getForecast(anyString(), anyInt(), anyBoolean())).thenReturn(forecasts);

        // Act & Assert
        mockMvc.perform(get("/api/forecast")
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
        ForecastDto forecastDto = ForecastDto.builder()
                .id(1L)
                .locationId(1L)
                .locationName("Paris")
                .forecastDate(LocalDate.now().plusDays(1))
                .maxTemperature(24.0)
                .minTemperature(14.0)
                .avgTemperature(19.0)
                .maxWindSpeed(20.0)
                .avgHumidity(55)
                .condition("Sunny")
                .description("Clear sunny weather expected")
                .precipitationMm(0.0)
                .precipitationProbability(0)
                .uvIndex(6.0)
                .sunriseTime("06:15")
                .sunsetTime("20:15")
                .build();
        List<ForecastDto> forecasts = Collections.singletonList(forecastDto);
        when(forecastService.getForecastByLocationId(anyLong(), anyInt(), anyBoolean())).thenReturn(forecasts);

        // Act & Assert
        mockMvc.perform(get("/api/forecast/location/1")
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
        ForecastDto forecastDto = ForecastDto.builder()
                .id(1L)
                .locationId(1L)
                .locationName("Berlin")
                .forecastDate(LocalDate.now().plusDays(2))
                .maxTemperature(23.0)
                .minTemperature(13.0)
                .avgTemperature(18.0)
                .maxWindSpeed(19.0)
                .avgHumidity(65)
                .condition("Cloudy")
                .description("Overcast with possible light rain")
                .precipitationMm(10.0)
                .precipitationProbability(60)
                .uvIndex(3.0)
                .sunriseTime("06:45")
                .sunsetTime("19:45")
                .build();
        List<ForecastDto> forecasts = Collections.singletonList(forecastDto);
        when(forecastService.getFutureForecasts(anyLong())).thenReturn(forecasts);

        // Act & Assert
        mockMvc.perform(get("/api/forecast/future/location/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].maxTemperature").value(23.0))
                .andExpect(jsonPath("$[0].condition").value("Cloudy"));
    }

    @Test
    void getForecastsByDateRange_WithValidParameters_ReturnsForecastList() throws Exception {
        // Arrange
        ForecastDto forecastDto = ForecastDto.builder()
                .id(1L)
                .locationId(1L)
                .locationName("Madrid")
                .forecastDate(LocalDate.now().plusDays(3))
                .maxTemperature(26.0)
                .minTemperature(16.0)
                .avgTemperature(21.0)
                .maxWindSpeed(15.0)
                .avgHumidity(50)
                .condition("Clear")
                .description("Clear skies throughout the day")
                .precipitationMm(0.0)
                .precipitationProbability(0)
                .uvIndex(7.0)
                .sunriseTime("07:00")
                .sunsetTime("20:30")
                .build();
        List<ForecastDto> forecasts = Collections.singletonList(forecastDto);
        when(forecastService.getForecastsByDateRange(anyLong(), any(), any())).thenReturn(forecasts);

        // Act & Assert
        mockMvc.perform(get("/api/forecast/range/location/1")
                        .param("startDate", "2025-11-20")
                        .param("endDate", "2025-11-27")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].maxTemperature").value(26.0))
                .andExpect(jsonPath("$[0].condition").value("Clear"));
    }
}

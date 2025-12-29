package com.weatherspring.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.weatherspring.TestDataFactory;
import com.weatherspring.client.WeatherApiClient;
import com.weatherspring.dto.ForecastDto;
import com.weatherspring.dto.external.ForecastApiResponse;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.mapper.ForecastMapper;
import com.weatherspring.model.ForecastRecord;
import com.weatherspring.model.Location;
import com.weatherspring.repository.ForecastRecordRepository;

/** Unit tests for ForecastService. */
@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {

  @Mock private WeatherApiClient weatherApiClient;

  @Mock private ForecastRecordRepository forecastRecordRepository;

  @Mock private LocationService locationService;

  @Mock private ForecastMapper forecastMapper;

  @InjectMocks private ForecastService forecastService;

  private Location testLocation;
  private ForecastApiResponse apiResponse;
  private ForecastDto forecastDto;
  private ForecastRecord forecastRecord;

  @BeforeEach
  void setUp() {
    testLocation = TestDataFactory.createTestLocation();

    forecastDto =
        TestDataFactory.forecastDtoBuilder()
            .id(null)
            .locationId(null)
            .maxTemperature(18.5)
            .minTemperature(10.2)
            .condition("Moderate rain")
            .build();

    forecastRecord =
        ForecastRecord.builder()
            .id(1L)
            .location(testLocation)
            .forecastDate(LocalDate.now().plusDays(1))
            .maxTemperature(18.5)
            .minTemperature(10.2)
            .condition("Moderate rain")
            .build();

    apiResponse = createMockApiResponse();
  }

  @Test
  void getForecast_WithValidDays_ReturnsForecastList() {
    // Arrange
    List<ForecastDto> forecasts = Arrays.asList(forecastDto);
    when(weatherApiClient.getForecast("London", 3)).thenReturn(apiResponse);
    when(forecastMapper.toDtoFromApi(apiResponse, "London")).thenReturn(forecasts);

    // Act
    List<ForecastDto> result = forecastService.getForecast("London", 3, false);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    verify(weatherApiClient).getForecast("London", 3);
    verify(forecastRecordRepository, never()).saveAll(anyList());
  }

  @Test
  void getForecast_WithSaving_SavesForecastRecords() {
    // Arrange
    List<ForecastDto> forecasts = Arrays.asList(forecastDto);
    List<ForecastRecord> records = Arrays.asList(forecastRecord);

    when(weatherApiClient.getForecast("London", 3)).thenReturn(apiResponse);
    when(forecastMapper.toDtoFromApi(apiResponse, "London")).thenReturn(forecasts);
    when(locationService.findOrCreateLocation(
            anyString(), anyString(), anyDouble(), anyDouble(), any()))
        .thenReturn(testLocation);
    when(forecastMapper.fromWeatherApi(apiResponse, testLocation)).thenReturn(records);

    // Act
    List<ForecastDto> result = forecastService.getForecast("London", 3, true);

    // Assert
    assertThat(result).isNotNull();
    verify(forecastRecordRepository).saveAll(anyList());
  }

  @Test
  void getForecastByLocationId_WithValidData_ReturnsForecastList() {
    // Arrange
    List<ForecastDto> forecasts = Arrays.asList(forecastDto);
    when(locationService.getLocationEntityById(1L)).thenReturn(testLocation);
    when(weatherApiClient.getForecast("London,United Kingdom", 3)).thenReturn(apiResponse);
    when(forecastMapper.toDtoFromApi(apiResponse, "London")).thenReturn(forecasts);

    // Act
    List<ForecastDto> result = forecastService.getForecastByLocationId(1L, 3, false);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    verify(weatherApiClient).getForecast("London,United Kingdom", 3);
  }

  @Test
  void getStoredForecasts_ReturnsListOfForecasts() {
    // Arrange
    List<ForecastRecord> records = Arrays.asList(forecastRecord);
    when(locationService.getLocationEntityById(1L)).thenReturn(testLocation);
    when(forecastRecordRepository.findByLocationId(1L)).thenReturn(records);
    when(forecastMapper.toDto(forecastRecord)).thenReturn(forecastDto);

    // Act
    List<ForecastDto> result = forecastService.getStoredForecasts(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    verify(forecastRecordRepository).findByLocationId(1L);
  }

  @Test
  void getFutureForecasts_ReturnsOnlyFutureForecasts() {
    // Arrange
    List<ForecastRecord> records = Arrays.asList(forecastRecord);
    when(locationService.getLocationEntityById(1L)).thenReturn(testLocation);
    when(forecastRecordRepository.findFutureForecastsByLocationId(eq(1L), any(LocalDate.class)))
        .thenReturn(records);
    when(forecastMapper.toDto(forecastRecord)).thenReturn(forecastDto);

    // Act
    List<ForecastDto> result = forecastService.getFutureForecasts(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).forecastDate()).isAfter(LocalDate.now());
  }

  @Test
  void getForecastsByDateRange_ReturnsListOfForecasts() {
    // Arrange
    LocalDate startDate = LocalDate.now();
    LocalDate endDate = LocalDate.now().plusDays(7);
    List<ForecastRecord> records = Arrays.asList(forecastRecord);

    when(locationService.getLocationEntityById(1L)).thenReturn(testLocation);
    when(forecastRecordRepository.findByLocationIdAndForecastDateBetween(1L, startDate, endDate))
        .thenReturn(records);
    when(forecastMapper.toDto(forecastRecord)).thenReturn(forecastDto);

    // Act
    List<ForecastDto> result = forecastService.getForecastsByDateRange(1L, startDate, endDate);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    verify(forecastRecordRepository).findByLocationIdAndForecastDateBetween(1L, startDate, endDate);
  }

  private ForecastApiResponse createMockApiResponse() {
    ForecastApiResponse response = new ForecastApiResponse();

    WeatherApiResponse.LocationInfo locationInfo = new WeatherApiResponse.LocationInfo();
    locationInfo.setName("London");
    locationInfo.setCountry("United Kingdom");
    locationInfo.setLat(51.5074);
    locationInfo.setLon(-0.1278);

    ForecastApiResponse.Forecast forecast = new ForecastApiResponse.Forecast();
    ForecastApiResponse.ForecastDay forecastDay = new ForecastApiResponse.ForecastDay();
    forecastDay.setDate(LocalDate.now().plusDays(1).toString());

    ForecastApiResponse.Day day = new ForecastApiResponse.Day();
    day.setMaxtempC(18.5);
    day.setMintempC(10.2);

    WeatherApiResponse.Condition condition = new WeatherApiResponse.Condition();
    condition.setText("Moderate rain");
    day.setCondition(condition);

    forecastDay.setDay(day);
    forecast.setForecastday(Arrays.asList(forecastDay));

    response.setLocation(locationInfo);
    response.setForecast(forecast);

    return response;
  }
}

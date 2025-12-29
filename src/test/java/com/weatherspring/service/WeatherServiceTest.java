package com.weatherspring.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.weatherspring.TestDataFactory;
import com.weatherspring.client.WeatherApiClient;
import com.weatherspring.dto.WeatherDto;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.exception.LocationNotFoundException;
import com.weatherspring.mapper.WeatherMapper;
import com.weatherspring.model.Location;
import com.weatherspring.model.WeatherRecord;
import com.weatherspring.repository.WeatherRecordRepository;

/** Unit tests for WeatherService. */
@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

  @Mock private WeatherApiClient weatherApiClient;

  @Mock private WeatherRecordRepository weatherRecordRepository;

  @Mock private LocationService locationService;

  @Mock private WeatherMapper weatherMapper;

  @InjectMocks private WeatherService weatherService;

  private WeatherApiResponse apiResponse;
  private WeatherDto weatherDto;
  private Location testLocation;
  private WeatherRecord weatherRecord;

  @BeforeEach
  void setUp() {
    testLocation = TestDataFactory.createTestLocation();

    weatherDto =
        TestDataFactory.weatherDtoBuilder()
            .id(null)
            .locationId(null)
            .temperature(15.5)
            .humidity(65)
            .windSpeed(12.5)
            .condition("Partly cloudy")
            .build();

    weatherRecord =
        WeatherRecord.builder()
            .id(1L)
            .location(testLocation)
            .temperature(15.5)
            .humidity(65)
            .windSpeed(12.5)
            .condition("Partly cloudy")
            .timestamp(LocalDateTime.now())
            .build();

    apiResponse = createMockApiResponse();
  }

  @Test
  void getCurrentWeather_WithoutSaving_ReturnsWeatherDto() {
    // Arrange
    when(weatherApiClient.getCurrentWeather("London")).thenReturn(apiResponse);
    when(weatherMapper.toDtoFromApi(apiResponse)).thenReturn(weatherDto);

    // Act
    WeatherDto result = weatherService.getCurrentWeather("London", false);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.locationName()).isEqualTo("London");
    assertThat(result.temperature()).isEqualTo(15.5);
    verify(weatherApiClient).getCurrentWeather("London");
    verify(weatherRecordRepository, never()).save(any(WeatherRecord.class));
  }

  @Test
  void getCurrentWeatherByLocationId_WithSaving_SavesWeatherRecord() {
    // Arrange
    when(locationService.getLocationEntityById(1L)).thenReturn(testLocation);
    when(weatherApiClient.getCurrentWeather("London,United Kingdom")).thenReturn(apiResponse);
    when(weatherMapper.fromWeatherApi(apiResponse, testLocation)).thenReturn(weatherRecord);
    when(weatherMapper.toDtoFromApi(apiResponse)).thenReturn(weatherDto);
    when(weatherRecordRepository.save(any(WeatherRecord.class))).thenReturn(weatherRecord);

    // Act
    WeatherDto result = weatherService.getCurrentWeatherByLocationId(1L, true);

    // Assert
    assertThat(result).isNotNull();
    verify(weatherRecordRepository).save(any(WeatherRecord.class));
  }

  @Test
  void getCurrentWeatherByLocationId_WhenLocationNotFound_ThrowsException() {
    // Arrange
    when(locationService.getLocationEntityById(999L))
        .thenThrow(new LocationNotFoundException(999L));

    // Act & Assert
    assertThatThrownBy(() -> weatherService.getCurrentWeatherByLocationId(999L, false))
        .isInstanceOf(LocationNotFoundException.class);
  }

  @Test
  void getWeatherHistory_ReturnsPageOfWeatherDtos() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    List<WeatherRecord> records = Arrays.asList(weatherRecord);
    Page<WeatherRecord> page = new PageImpl<>(records, pageable, 1);

    when(locationService.getLocationEntityById(1L)).thenReturn(testLocation);
    when(weatherRecordRepository.findByLocationId(1L, pageable)).thenReturn(page);
    when(weatherMapper.toDto(any(WeatherRecord.class))).thenReturn(weatherDto);

    // Act
    Page<WeatherDto> result = weatherService.getWeatherHistory(1L, pageable);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).locationName()).isEqualTo("London");
  }

  @Test
  void getWeatherHistoryByDateRange_ReturnsListOfWeatherDtos() {
    // Arrange
    LocalDateTime startDate = LocalDateTime.now().minusDays(7);
    LocalDateTime endDate = LocalDateTime.now();
    List<WeatherRecord> records = Arrays.asList(weatherRecord);

    when(locationService.getLocationEntityById(1L)).thenReturn(testLocation);
    when(weatherRecordRepository.findByLocationIdAndTimestampBetween(1L, startDate, endDate))
        .thenReturn(records);
    when(weatherMapper.toDto(weatherRecord)).thenReturn(weatherDto);

    // Act
    List<WeatherDto> result = weatherService.getWeatherHistoryByDateRange(1L, startDate, endDate);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    verify(weatherRecordRepository).findByLocationIdAndTimestampBetween(1L, startDate, endDate);
  }

  private WeatherApiResponse createMockApiResponse() {
    WeatherApiResponse response = new WeatherApiResponse();
    WeatherApiResponse.LocationInfo locationInfo = new WeatherApiResponse.LocationInfo();
    locationInfo.setName("London");
    locationInfo.setCountry("United Kingdom");
    locationInfo.setLat(51.5074);
    locationInfo.setLon(-0.1278);
    locationInfo.setLocaltime("2024-01-15 14:30");

    WeatherApiResponse.CurrentWeather current = new WeatherApiResponse.CurrentWeather();
    current.setTempC(15.5);
    current.setHumidity(65);
    current.setWindKph(12.5);

    WeatherApiResponse.Condition condition = new WeatherApiResponse.Condition();
    condition.setText("Partly cloudy");
    current.setCondition(condition);

    response.setLocation(locationInfo);
    response.setCurrent(current);

    return response;
  }
}

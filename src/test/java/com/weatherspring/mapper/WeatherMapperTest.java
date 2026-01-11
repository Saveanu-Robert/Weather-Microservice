package com.weatherspring.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.weatherspring.dto.WeatherDto;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.model.Location;
import com.weatherspring.model.WeatherRecord;

/** Unit tests for WeatherMapper. */
class WeatherMapperTest {

  private WeatherMapper weatherMapper;
  private Location testLocation;

  @BeforeEach
  void setUp() {
    weatherMapper = new WeatherMapper();
    testLocation = Location.builder().id(1L).name("London").country("United Kingdom").build();
  }

  @Test
  void toDto_WithValidWeatherRecord_ReturnsWeatherDto() {
    // Arrange
    WeatherRecord weatherRecord =
        WeatherRecord.builder()
            .id(1L)
            .location(testLocation)
            .temperature(15.5)
            .feelsLike(13.2)
            .humidity(65)
            .windSpeed(12.5)
            .windDirection("NW")
            .condition("Partly cloudy")
            .description("Partly cloudy with light winds")
            .pressureMb(1013.2)
            .precipitationMm(0.5)
            .cloudCoverage(40)
            .uvIndex(3.5)
            .timestamp(LocalDateTime.now())
            .build();

    // Act
    WeatherDto result = weatherMapper.toDto(weatherRecord);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.locationId()).isEqualTo(1L);
    assertThat(result.locationName()).isEqualTo("London");
    assertThat(result.temperature()).isEqualTo(15.5);
    assertThat(result.feelsLike()).isEqualTo(13.2);
    assertThat(result.humidity()).isEqualTo(65);
    assertThat(result.windSpeed()).isEqualTo(12.5);
    assertThat(result.condition()).isEqualTo("Partly cloudy");
  }

  @Test
  void toDto_WithNull_ReturnsNull() {
    // Act
    WeatherDto result = weatherMapper.toDto(null);

    // Assert
    assertThat(result).isNull();
  }

  @Test
  void fromWeatherApi_WithValidResponse_ReturnsWeatherRecord() {
    // Arrange
    WeatherApiResponse apiResponse = createMockApiResponse();

    // Act
    WeatherRecord result = weatherMapper.fromWeatherApi(apiResponse, testLocation);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getLocation()).isEqualTo(testLocation);
    assertThat(result.getTemperature()).isEqualTo(15.5);
    assertThat(result.getHumidity()).isEqualTo(65);
    assertThat(result.getWindSpeed()).isEqualTo(12.5);
    assertThat(result.getCondition()).isEqualTo("Partly cloudy");
  }

  @Test
  void fromWeatherApi_WithNull_ReturnsNull() {
    // Act
    WeatherRecord result = weatherMapper.fromWeatherApi(null, testLocation);

    // Assert
    assertThat(result).isNull();
  }

  @Test
  void toDtoFromApi_WithValidResponse_ReturnsWeatherDto() {
    // Arrange
    WeatherApiResponse apiResponse = createMockApiResponse();

    // Act
    WeatherDto result = weatherMapper.toDtoFromApi(apiResponse);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.locationName()).isEqualTo("London");
    assertThat(result.temperature()).isEqualTo(15.5);
    assertThat(result.humidity()).isEqualTo(65);
    assertThat(result.windSpeed()).isEqualTo(12.5);
  }

  @Test
  void toDtoFromApi_WithNull_ReturnsNull() {
    // Act
    WeatherDto result = weatherMapper.toDtoFromApi(null);

    // Assert
    assertThat(result).isNull();
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
    current.setFeelslikeC(13.2);
    current.setHumidity(65);
    current.setWindKph(12.5);
    current.setWindDir("NW");
    current.setPressureMb(1013.2);
    current.setPrecipMm(0.5);
    current.setCloud(40);
    current.setUv(3.5);

    WeatherApiResponse.Condition condition = new WeatherApiResponse.Condition();
    condition.setText("Partly cloudy");
    current.setCondition(condition);

    response.setLocation(locationInfo);
    response.setCurrent(current);

    return response;
  }
}

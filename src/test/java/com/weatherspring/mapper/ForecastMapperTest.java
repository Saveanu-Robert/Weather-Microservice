package com.weatherspring.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.weatherspring.dto.ForecastDto;
import com.weatherspring.dto.external.ForecastApiResponse;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.model.ForecastRecord;
import com.weatherspring.model.Location;

/** Unit tests for ForecastMapper. */
class ForecastMapperTest {

  private ForecastMapper forecastMapper;
  private Location testLocation;

  @BeforeEach
  void setUp() {
    forecastMapper = new ForecastMapper();
    testLocation = Location.builder().id(1L).name("London").country("United Kingdom").build();
  }

  @Test
  void toDto_WithValidForecastRecord_ReturnsForecastDto() {
    // Arrange
    ForecastRecord forecastRecord =
        ForecastRecord.builder()
            .id(1L)
            .location(testLocation)
            .forecastDate(LocalDate.now().plusDays(1))
            .maxTemperature(18.5)
            .minTemperature(10.2)
            .avgTemperature(14.3)
            .maxWindSpeed(25.0)
            .avgHumidity(70)
            .condition("Moderate rain")
            .description("Expect moderate rain")
            .precipitationMm(12.5)
            .precipitationProbability(80)
            .uvIndex(4.0)
            .sunriseTime("06:45")
            .sunsetTime("18:30")
            .build();

    // Act
    ForecastDto result = forecastMapper.toDto(forecastRecord);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.locationId()).isEqualTo(1L);
    assertThat(result.locationName()).isEqualTo("London");
    assertThat(result.maxTemperature()).isEqualTo(18.5);
    assertThat(result.minTemperature()).isEqualTo(10.2);
    assertThat(result.condition()).isEqualTo("Moderate rain");
    assertThat(result.sunriseTime()).isEqualTo("06:45");
    assertThat(result.sunsetTime()).isEqualTo("18:30");
  }

  @Test
  void toDto_WithNull_ReturnsNull() {
    // Act
    ForecastDto result = forecastMapper.toDto(null);

    // Assert
    assertThat(result).isNull();
  }

  @Test
  void fromWeatherApi_WithValidResponse_ReturnsListOfForecastRecords() {
    // Arrange
    ForecastApiResponse apiResponse = createMockForecastApiResponse();

    // Act
    List<ForecastRecord> result = forecastMapper.fromWeatherApi(apiResponse, testLocation);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    ForecastRecord record = result.get(0);
    assertThat(record.getLocation()).isEqualTo(testLocation);
    assertThat(record.getMaxTemperature()).isEqualTo(18.5);
    assertThat(record.getMinTemperature()).isEqualTo(10.2);
  }

  @Test
  void fromWeatherApi_WithNull_ReturnsEmptyList() {
    // Act
    List<ForecastRecord> result = forecastMapper.fromWeatherApi(null, testLocation);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void toDtoFromApi_WithValidResponse_ReturnsListOfForecastDtos() {
    // Arrange
    ForecastApiResponse apiResponse = createMockForecastApiResponse();

    // Act
    List<ForecastDto> result = forecastMapper.toDtoFromApi(apiResponse, "London");

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    ForecastDto dto = result.get(0);
    assertThat(dto.locationName()).isEqualTo("London");
    assertThat(dto.maxTemperature()).isEqualTo(18.5);
    assertThat(dto.minTemperature()).isEqualTo(10.2);
  }

  @Test
  void toDtoFromApi_WithNull_ReturnsEmptyList() {
    // Act
    List<ForecastDto> result = forecastMapper.toDtoFromApi(null, "London");

    // Assert
    assertThat(result).isEmpty();
  }

  private ForecastApiResponse createMockForecastApiResponse() {
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
    day.setAvgtempC(14.3);
    day.setMaxwindKph(25.0);
    day.setAvghumidity(70);
    day.setTotalprecipMm(12.5);
    day.setDailyChanceOfRain(80);
    day.setUv(4.0);

    WeatherApiResponse.Condition condition = new WeatherApiResponse.Condition();
    condition.setText("Moderate rain");
    day.setCondition(condition);

    ForecastApiResponse.Astro astro = new ForecastApiResponse.Astro();
    astro.setSunrise("06:45");
    astro.setSunset("18:30");

    forecastDay.setDay(day);
    forecastDay.setAstro(astro);
    forecast.setForecastday(Arrays.asList(forecastDay));

    response.setLocation(locationInfo);
    response.setForecast(forecast);

    return response;
  }
}

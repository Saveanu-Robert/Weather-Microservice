package com.weatherspring;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.weatherspring.dto.CreateLocationRequest;
import com.weatherspring.dto.ForecastDto;
import com.weatherspring.dto.WeatherDto;
import com.weatherspring.model.Location;

/**
 * Factory for creating test data objects using the Test Data Builder pattern.
 *
 * <p>Provides centralized test data to reduce duplication and improve test maintainability. All
 * test data uses realistic values for well-known locations to make tests readable and
 * understandable.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * // Use defaults
 * WeatherDto weather = TestDataFactory.createDefaultWeatherDto();
 *
 * // Customize specific fields
 * WeatherDto customWeather = TestDataFactory.weatherDtoBuilder()
 *     .temperature(25.0)
 *     .condition("Sunny")
 *     .build();
 * }</pre>
 */
public final class TestDataFactory {

  private TestDataFactory() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  // Test IDs
  public static final Long TEST_ID = 1L;
  public static final Long TEST_ID_2 = 2L;
  public static final Long NON_EXISTENT_ID = 999L;

  // London test data
  public static final String LONDON_NAME = "London";
  public static final String LONDON_COUNTRY = "United Kingdom";
  public static final Double LONDON_LATITUDE = 51.5074;
  public static final Double LONDON_LONGITUDE = -0.1278;
  public static final String LONDON_REGION = "Greater London";

  // Paris test data
  public static final String PARIS_NAME = "Paris";
  public static final String PARIS_COUNTRY = "France";
  public static final Double PARIS_LATITUDE = 48.8566;
  public static final Double PARIS_LONGITUDE = 2.3522;
  public static final String PARIS_REGION = "Île-de-France";

  // Berlin test data
  public static final String BERLIN_NAME = "Berlin";
  public static final String BERLIN_COUNTRY = "Germany";
  public static final Double BERLIN_LATITUDE = 52.5200;
  public static final Double BERLIN_LONGITUDE = 13.4050;
  public static final String BERLIN_REGION = "Berlin";

  // Weather test data constants
  public static final Double DEFAULT_TEMPERATURE = 15.5;
  public static final Double DEFAULT_FEELS_LIKE = 13.2;
  public static final Integer DEFAULT_HUMIDITY = 65;
  public static final Double DEFAULT_WIND_SPEED = 12.5;
  public static final String DEFAULT_WIND_DIRECTION = "NW";
  public static final String DEFAULT_WEATHER_CONDITION = "Partly cloudy";
  public static final String DEFAULT_WEATHER_DESCRIPTION = "Partly cloudy with light winds";
  public static final Double DEFAULT_PRESSURE = 1013.2;
  public static final Double DEFAULT_PRECIPITATION = 0.5;
  public static final Integer DEFAULT_CLOUD_COVERAGE = 40;
  public static final Double DEFAULT_UV_INDEX = 3.5;

  // Forecast test data constants
  public static final Double DEFAULT_MAX_TEMP = 18.5;
  public static final Double DEFAULT_MIN_TEMP = 10.2;
  public static final Double DEFAULT_AVG_TEMP = 14.3;
  public static final Double DEFAULT_MAX_WIND = 25.0;
  public static final Integer DEFAULT_AVG_HUMIDITY = 70;
  public static final String DEFAULT_FORECAST_CONDITION = "Moderate rain";
  public static final String DEFAULT_FORECAST_DESCRIPTION =
      "Expect moderate rain throughout the day";
  public static final Double DEFAULT_FORECAST_PRECIPITATION = 12.5;
  public static final Integer DEFAULT_PRECIPITATION_PROBABILITY = 80;
  public static final String DEFAULT_SUNRISE = "06:45";
  public static final String DEFAULT_SUNSET = "18:30";

  /**
   * Creates a default test Location entity (London).
   *
   * @return Location entity with London data
   */
  public static Location createTestLocation() {
    Location location =
        Location.builder()
            .id(TEST_ID)
            .name(LONDON_NAME)
            .country(LONDON_COUNTRY)
            .latitude(LONDON_LATITUDE)
            .longitude(LONDON_LONGITUDE)
            .region(LONDON_REGION)
            .build();

    // Set audit fields (inherited from BaseAuditableEntity)
    location.setCreatedAt(LocalDateTime.now());
    location.setUpdatedAt(LocalDateTime.now());

    return location;
  }

  /**
   * Creates a test Location entity with custom ID (London data).
   *
   * @param id the location ID
   * @return Location entity with specified ID and London data
   */
  public static Location createTestLocationWithId(Long id) {
    Location location =
        Location.builder()
            .id(id)
            .name(LONDON_NAME)
            .country(LONDON_COUNTRY)
            .latitude(LONDON_LATITUDE)
            .longitude(LONDON_LONGITUDE)
            .region(LONDON_REGION)
            .build();

    // Set audit fields (inherited from BaseAuditableEntity)
    location.setCreatedAt(LocalDateTime.now());
    location.setUpdatedAt(LocalDateTime.now());

    return location;
  }

  /**
   * Creates a test Location entity for Paris.
   *
   * @return Location entity with Paris data
   */
  public static Location createParisLocation() {
    return Location.builder()
        .name(PARIS_NAME)
        .country(PARIS_COUNTRY)
        .latitude(PARIS_LATITUDE)
        .longitude(PARIS_LONGITUDE)
        .region(PARIS_REGION)
        .build();
  }

  /**
   * Creates a CreateLocationRequest for London.
   *
   * @return CreateLocationRequest with London data
   */
  public static CreateLocationRequest createLondonRequest() {
    return new CreateLocationRequest(
        LONDON_NAME, LONDON_COUNTRY, LONDON_LATITUDE, LONDON_LONGITUDE, LONDON_REGION);
  }

  /**
   * Creates a CreateLocationRequest for Paris.
   *
   * @return CreateLocationRequest with Paris data
   */
  public static CreateLocationRequest createParisRequest() {
    return new CreateLocationRequest(
        PARIS_NAME, PARIS_COUNTRY, PARIS_LATITUDE, PARIS_LONGITUDE, PARIS_REGION);
  }

  // ==================== WeatherDto Factory Methods ====================

  /**
   * Creates a default WeatherDto for testing. Uses London location with typical weather values.
   *
   * @return WeatherDto with default test values
   */
  public static WeatherDto createDefaultWeatherDto() {
    return new WeatherDto(
        TEST_ID,
        TEST_ID,
        LONDON_NAME,
        DEFAULT_TEMPERATURE,
        DEFAULT_FEELS_LIKE,
        DEFAULT_HUMIDITY,
        DEFAULT_WIND_SPEED,
        DEFAULT_WIND_DIRECTION,
        DEFAULT_WEATHER_CONDITION,
        DEFAULT_WEATHER_DESCRIPTION,
        DEFAULT_PRESSURE,
        DEFAULT_PRECIPITATION,
        DEFAULT_CLOUD_COVERAGE,
        DEFAULT_UV_INDEX,
        LocalDateTime.now());
  }

  /**
   * Creates a minimal WeatherDto with only essential fields. Useful for testing basic weather data
   * without full details.
   *
   * @return WeatherDto with minimal fields populated
   */
  public static WeatherDto createMinimalWeatherDto() {
    return new WeatherDto(
        null,
        null,
        LONDON_NAME,
        DEFAULT_TEMPERATURE,
        null,
        DEFAULT_HUMIDITY,
        DEFAULT_WIND_SPEED,
        null,
        DEFAULT_WEATHER_CONDITION,
        null,
        null,
        null,
        null,
        null,
        LocalDateTime.now());
  }

  /**
   * Creates a WeatherDto builder for customization.
   *
   * @return WeatherDtoBuilder instance
   */
  public static WeatherDtoBuilder weatherDtoBuilder() {
    return new WeatherDtoBuilder();
  }

  /** Builder for WeatherDto with fluent API. */
  public static class WeatherDtoBuilder {
    private Long id = TEST_ID;
    private Long locationId = TEST_ID;
    private String locationName = LONDON_NAME;
    private Double temperature = DEFAULT_TEMPERATURE;
    private Double feelsLike = DEFAULT_FEELS_LIKE;
    private Integer humidity = DEFAULT_HUMIDITY;
    private Double windSpeed = DEFAULT_WIND_SPEED;
    private String windDirection = DEFAULT_WIND_DIRECTION;
    private String condition = DEFAULT_WEATHER_CONDITION;
    private String description = DEFAULT_WEATHER_DESCRIPTION;
    private Double pressureMb = DEFAULT_PRESSURE;
    private Double precipitationMm = DEFAULT_PRECIPITATION;
    private Integer cloudCoverage = DEFAULT_CLOUD_COVERAGE;
    private Double uvIndex = DEFAULT_UV_INDEX;
    private LocalDateTime timestamp = LocalDateTime.now();

    public WeatherDtoBuilder id(Long id) {
      this.id = id;
      return this;
    }

    public WeatherDtoBuilder locationId(Long locationId) {
      this.locationId = locationId;
      return this;
    }

    public WeatherDtoBuilder locationName(String locationName) {
      this.locationName = locationName;
      return this;
    }

    public WeatherDtoBuilder temperature(Double temperature) {
      this.temperature = temperature;
      return this;
    }

    public WeatherDtoBuilder feelsLike(Double feelsLike) {
      this.feelsLike = feelsLike;
      return this;
    }

    public WeatherDtoBuilder humidity(Integer humidity) {
      this.humidity = humidity;
      return this;
    }

    public WeatherDtoBuilder windSpeed(Double windSpeed) {
      this.windSpeed = windSpeed;
      return this;
    }

    public WeatherDtoBuilder windDirection(String windDirection) {
      this.windDirection = windDirection;
      return this;
    }

    public WeatherDtoBuilder condition(String condition) {
      this.condition = condition;
      return this;
    }

    public WeatherDtoBuilder description(String description) {
      this.description = description;
      return this;
    }

    public WeatherDtoBuilder pressureMb(Double pressureMb) {
      this.pressureMb = pressureMb;
      return this;
    }

    public WeatherDtoBuilder precipitationMm(Double precipitationMm) {
      this.precipitationMm = precipitationMm;
      return this;
    }

    public WeatherDtoBuilder cloudCoverage(Integer cloudCoverage) {
      this.cloudCoverage = cloudCoverage;
      return this;
    }

    public WeatherDtoBuilder uvIndex(Double uvIndex) {
      this.uvIndex = uvIndex;
      return this;
    }

    public WeatherDtoBuilder timestamp(LocalDateTime timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public WeatherDto build() {
      return new WeatherDto(
          id,
          locationId,
          locationName,
          temperature,
          feelsLike,
          humidity,
          windSpeed,
          windDirection,
          condition,
          description,
          pressureMb,
          precipitationMm,
          cloudCoverage,
          uvIndex,
          timestamp);
    }
  }

  // ==================== ForecastDto Factory Methods ====================

  /**
   * Creates a default ForecastDto for testing. Uses London location with typical forecast values
   * for tomorrow.
   *
   * @return ForecastDto with default test values
   */
  public static ForecastDto createDefaultForecastDto() {
    return new ForecastDto(
        TEST_ID,
        TEST_ID,
        LONDON_NAME,
        LocalDate.now().plusDays(1),
        DEFAULT_MAX_TEMP,
        DEFAULT_MIN_TEMP,
        DEFAULT_AVG_TEMP,
        DEFAULT_MAX_WIND,
        DEFAULT_AVG_HUMIDITY,
        DEFAULT_FORECAST_CONDITION,
        DEFAULT_FORECAST_DESCRIPTION,
        DEFAULT_FORECAST_PRECIPITATION,
        DEFAULT_PRECIPITATION_PROBABILITY,
        DEFAULT_UV_INDEX,
        DEFAULT_SUNRISE,
        DEFAULT_SUNSET);
  }

  /**
   * Creates a minimal ForecastDto with only essential fields. Useful for testing basic forecast
   * data without full details.
   *
   * @return ForecastDto with minimal fields populated
   */
  public static ForecastDto createMinimalForecastDto() {
    return new ForecastDto(
        null,
        null,
        LONDON_NAME,
        LocalDate.now().plusDays(1),
        DEFAULT_MAX_TEMP,
        DEFAULT_MIN_TEMP,
        null,
        null,
        null,
        DEFAULT_FORECAST_CONDITION,
        null,
        null,
        null,
        null,
        null,
        null);
  }

  /**
   * Creates a ForecastDto for a specific date.
   *
   * @param forecastDate the date for the forecast
   * @return ForecastDto for the specified date
   */
  public static ForecastDto createForecastDtoForDate(LocalDate forecastDate) {
    return forecastDtoBuilder().forecastDate(forecastDate).build();
  }

  /**
   * Creates a ForecastDto builder for customization.
   *
   * @return ForecastDtoBuilder instance
   */
  public static ForecastDtoBuilder forecastDtoBuilder() {
    return new ForecastDtoBuilder();
  }

  /** Builder for ForecastDto with fluent API. */
  public static class ForecastDtoBuilder {
    private Long id = TEST_ID;
    private Long locationId = TEST_ID;
    private String locationName = LONDON_NAME;
    private LocalDate forecastDate = LocalDate.now().plusDays(1);
    private Double maxTemperature = DEFAULT_MAX_TEMP;
    private Double minTemperature = DEFAULT_MIN_TEMP;
    private Double avgTemperature = DEFAULT_AVG_TEMP;
    private Double maxWindSpeed = DEFAULT_MAX_WIND;
    private Integer avgHumidity = DEFAULT_AVG_HUMIDITY;
    private String condition = DEFAULT_FORECAST_CONDITION;
    private String description = DEFAULT_FORECAST_DESCRIPTION;
    private Double precipitationMm = DEFAULT_FORECAST_PRECIPITATION;
    private Integer precipitationProbability = DEFAULT_PRECIPITATION_PROBABILITY;
    private Double uvIndex = DEFAULT_UV_INDEX;
    private String sunriseTime = DEFAULT_SUNRISE;
    private String sunsetTime = DEFAULT_SUNSET;

    public ForecastDtoBuilder id(Long id) {
      this.id = id;
      return this;
    }

    public ForecastDtoBuilder locationId(Long locationId) {
      this.locationId = locationId;
      return this;
    }

    public ForecastDtoBuilder locationName(String locationName) {
      this.locationName = locationName;
      return this;
    }

    public ForecastDtoBuilder forecastDate(LocalDate forecastDate) {
      this.forecastDate = forecastDate;
      return this;
    }

    public ForecastDtoBuilder maxTemperature(Double maxTemperature) {
      this.maxTemperature = maxTemperature;
      return this;
    }

    public ForecastDtoBuilder minTemperature(Double minTemperature) {
      this.minTemperature = minTemperature;
      return this;
    }

    public ForecastDtoBuilder avgTemperature(Double avgTemperature) {
      this.avgTemperature = avgTemperature;
      return this;
    }

    public ForecastDtoBuilder maxWindSpeed(Double maxWindSpeed) {
      this.maxWindSpeed = maxWindSpeed;
      return this;
    }

    public ForecastDtoBuilder avgHumidity(Integer avgHumidity) {
      this.avgHumidity = avgHumidity;
      return this;
    }

    public ForecastDtoBuilder condition(String condition) {
      this.condition = condition;
      return this;
    }

    public ForecastDtoBuilder description(String description) {
      this.description = description;
      return this;
    }

    public ForecastDtoBuilder precipitationMm(Double precipitationMm) {
      this.precipitationMm = precipitationMm;
      return this;
    }

    public ForecastDtoBuilder precipitationProbability(Integer precipitationProbability) {
      this.precipitationProbability = precipitationProbability;
      return this;
    }

    public ForecastDtoBuilder uvIndex(Double uvIndex) {
      this.uvIndex = uvIndex;
      return this;
    }

    public ForecastDtoBuilder sunriseTime(String sunriseTime) {
      this.sunriseTime = sunriseTime;
      return this;
    }

    public ForecastDtoBuilder sunsetTime(String sunsetTime) {
      this.sunsetTime = sunsetTime;
      return this;
    }

    public ForecastDto build() {
      return new ForecastDto(
          id,
          locationId,
          locationName,
          forecastDate,
          maxTemperature,
          minTemperature,
          avgTemperature,
          maxWindSpeed,
          avgHumidity,
          condition,
          description,
          precipitationMm,
          precipitationProbability,
          uvIndex,
          sunriseTime,
          sunsetTime);
    }
  }

  // ==================== LocationDto Factory Methods ====================

  /**
   * Creates a default test LocationDto for testing. Uses London location data.
   *
   * @return LocationDto with default test values
   */
  public static com.weatherspring.dto.LocationDto createTestLocationDto() {
    return new com.weatherspring.dto.LocationDto(
        TEST_ID,
        LONDON_NAME,
        LONDON_COUNTRY,
        LONDON_LATITUDE,
        LONDON_LONGITUDE,
        LONDON_REGION,
        LocalDateTime.now(),
        LocalDateTime.now());
  }

  /**
   * Creates a LocationDto with custom ID.
   *
   * @param id the location ID
   * @return LocationDto with specified ID and London data
   */
  public static com.weatherspring.dto.LocationDto createTestLocationDtoWithId(Long id) {
    return new com.weatherspring.dto.LocationDto(
        id,
        LONDON_NAME,
        LONDON_COUNTRY,
        LONDON_LATITUDE,
        LONDON_LONGITUDE,
        LONDON_REGION,
        LocalDateTime.now(),
        LocalDateTime.now());
  }

  /**
   * Creates a LocationDto for Paris.
   *
   * @return LocationDto with Paris data
   */
  public static com.weatherspring.dto.LocationDto createParisLocationDto() {
    return new com.weatherspring.dto.LocationDto(
        TEST_ID_2,
        PARIS_NAME,
        PARIS_COUNTRY,
        PARIS_LATITUDE,
        PARIS_LONGITUDE,
        PARIS_REGION,
        LocalDateTime.now(),
        LocalDateTime.now());
  }
}

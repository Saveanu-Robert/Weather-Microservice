package com.weatherspring.mapper;

import static com.weatherspring.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.weatherspring.TestDataFactory;
import com.weatherspring.dto.CreateLocationRequest;
import com.weatherspring.dto.LocationDto;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.model.Location;

/** Unit tests for LocationMapper. */
class LocationMapperTest {

  private LocationMapper locationMapper;

  @BeforeEach
  void setUp() {
    locationMapper = new LocationMapper();
  }

  @Test
  void toDto_WithValidLocation_ReturnsLocationDto() {
    // Arrange
    Location location = TestDataFactory.createTestLocation();

    // Act
    LocationDto result = locationMapper.toDto(location);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(TEST_ID);
    assertThat(result.name()).isEqualTo(LONDON_NAME);
    assertThat(result.country()).isEqualTo(LONDON_COUNTRY);
    assertThat(result.latitude()).isEqualTo(LONDON_LATITUDE);
    assertThat(result.longitude()).isEqualTo(LONDON_LONGITUDE);
    assertThat(result.region()).isEqualTo(LONDON_REGION);
  }

  @Test
  void toDto_WithNull_ReturnsNull() {
    // Act
    LocationDto result = locationMapper.toDto(null);

    // Assert
    assertThat(result).isNull();
  }

  @Test
  void toEntity_WithValidRequest_ReturnsLocation() {
    // Arrange
    CreateLocationRequest request = TestDataFactory.createParisRequest();

    // Act
    Location result = locationMapper.toEntity(request);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(PARIS_NAME);
    assertThat(result.getCountry()).isEqualTo(PARIS_COUNTRY);
    assertThat(result.getLatitude()).isEqualTo(PARIS_LATITUDE);
    assertThat(result.getLongitude()).isEqualTo(PARIS_LONGITUDE);
    assertThat(result.getRegion()).isEqualTo(PARIS_REGION);
  }

  @Test
  void toEntity_WithNull_ReturnsNull() {
    // Act
    Location result = locationMapper.toEntity(null);

    // Assert
    assertThat(result).isNull();
  }

  @Test
  void fromWeatherApi_WithValidLocationInfo_ReturnsLocation() {
    // Arrange
    WeatherApiResponse.LocationInfo locationInfo = new WeatherApiResponse.LocationInfo();
    locationInfo.setName(BERLIN_NAME);
    locationInfo.setCountry(BERLIN_COUNTRY);
    locationInfo.setLat(BERLIN_LATITUDE);
    locationInfo.setLon(BERLIN_LONGITUDE);
    locationInfo.setRegion(BERLIN_REGION);

    // Act
    Location result = locationMapper.fromWeatherApi(locationInfo);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(BERLIN_NAME);
    assertThat(result.getCountry()).isEqualTo(BERLIN_COUNTRY);
    assertThat(result.getLatitude()).isEqualTo(BERLIN_LATITUDE);
    assertThat(result.getLongitude()).isEqualTo(BERLIN_LONGITUDE);
    assertThat(result.getRegion()).isEqualTo(BERLIN_REGION);
  }

  @Test
  void fromWeatherApi_WithNull_ReturnsNull() {
    // Act
    Location result = locationMapper.fromWeatherApi(null);

    // Assert
    assertThat(result).isNull();
  }

  @Test
  void updateEntityFromRequest_WithValidData_UpdatesLocation() {
    // Arrange
    Location location =
        Location.builder()
            .id(TEST_ID)
            .name("Old Name")
            .country("Old Country")
            .latitude(0.0)
            .longitude(0.0)
            .region("Old Region")
            .build();

    CreateLocationRequest request = TestDataFactory.createParisRequest();

    // Act
    Location updatedLocation = locationMapper.updateEntityFromRequest(location, request);

    // Assert
    assertThat(updatedLocation.getName()).isEqualTo(PARIS_NAME);
    assertThat(updatedLocation.getCountry()).isEqualTo(PARIS_COUNTRY);
    assertThat(updatedLocation.getLatitude()).isEqualTo(PARIS_LATITUDE);
    assertThat(updatedLocation.getLongitude()).isEqualTo(PARIS_LONGITUDE);
    assertThat(updatedLocation.getRegion()).isEqualTo(PARIS_REGION);
    assertThat(updatedLocation.getId()).isEqualTo(TEST_ID); // ID should not change
  }

  @Test
  void updateEntityFromRequest_WithNullLocation_DoesNothing() {
    // Arrange
    CreateLocationRequest request = new CreateLocationRequest("Test", "Test", null, null, null);

    // Act & Assert - should not throw exception
    locationMapper.updateEntityFromRequest(null, request);
  }

  @Test
  void updateEntityFromRequest_WithNullRequest_DoesNothing() {
    // Arrange
    Location location = Location.builder().id(TEST_ID).name("Original").build();

    // Act
    locationMapper.updateEntityFromRequest(location, null);

    // Assert - location should be unchanged
    assertThat(location.getName()).isEqualTo("Original");
  }
}

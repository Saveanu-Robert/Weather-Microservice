package com.weatherspring.mapper;

import com.weatherspring.dto.CreateLocationRequest;
import com.weatherspring.dto.LocationDto;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LocationMapper.
 */
class LocationMapperTest {

    private LocationMapper locationMapper;

    @BeforeEach
    void setUp() {
        locationMapper = new LocationMapper();
    }

    @Test
    void toDto_WithValidLocation_ReturnsLocationDto() {
        // Arrange
        Location location = Location.builder()
                .id(1L)
                .name("London")
                .country("United Kingdom")
                .latitude(51.5074)
                .longitude(-0.1278)
                .region("Greater London")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act
        LocationDto result = locationMapper.toDto(location);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("London");
        assertThat(result.getCountry()).isEqualTo("United Kingdom");
        assertThat(result.getLatitude()).isEqualTo(51.5074);
        assertThat(result.getLongitude()).isEqualTo(-0.1278);
        assertThat(result.getRegion()).isEqualTo("Greater London");
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
        CreateLocationRequest request = CreateLocationRequest.builder()
                .name("Paris")
                .country("France")
                .latitude(48.8566)
                .longitude(2.3522)
                .region("Île-de-France")
                .build();

        // Act
        Location result = locationMapper.toEntity(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Paris");
        assertThat(result.getCountry()).isEqualTo("France");
        assertThat(result.getLatitude()).isEqualTo(48.8566);
        assertThat(result.getLongitude()).isEqualTo(2.3522);
        assertThat(result.getRegion()).isEqualTo("Île-de-France");
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
        locationInfo.setName("Berlin");
        locationInfo.setCountry("Germany");
        locationInfo.setLat(52.5200);
        locationInfo.setLon(13.4050);
        locationInfo.setRegion("Berlin");

        // Act
        Location result = locationMapper.fromWeatherApi(locationInfo);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Berlin");
        assertThat(result.getCountry()).isEqualTo("Germany");
        assertThat(result.getLatitude()).isEqualTo(52.5200);
        assertThat(result.getLongitude()).isEqualTo(13.4050);
        assertThat(result.getRegion()).isEqualTo("Berlin");
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
        Location location = Location.builder()
                .id(1L)
                .name("Old Name")
                .country("Old Country")
                .latitude(0.0)
                .longitude(0.0)
                .region("Old Region")
                .build();

        CreateLocationRequest request = CreateLocationRequest.builder()
                .name("New Name")
                .country("New Country")
                .latitude(10.0)
                .longitude(20.0)
                .region("New Region")
                .build();

        // Act
        Location updatedLocation = locationMapper.updateEntityFromRequest(location, request);

        // Assert
        assertThat(updatedLocation.getName()).isEqualTo("New Name");
        assertThat(updatedLocation.getCountry()).isEqualTo("New Country");
        assertThat(updatedLocation.getLatitude()).isEqualTo(10.0);
        assertThat(updatedLocation.getLongitude()).isEqualTo(20.0);
        assertThat(updatedLocation.getRegion()).isEqualTo("New Region");
        assertThat(updatedLocation.getId()).isEqualTo(1L); // ID should not change
    }

    @Test
    void updateEntityFromRequest_WithNullLocation_DoesNothing() {
        // Arrange
        CreateLocationRequest request = CreateLocationRequest.builder()
                .name("Test")
                .country("Test")
                .build();

        // Act & Assert - should not throw exception
        locationMapper.updateEntityFromRequest(null, request);
    }

    @Test
    void updateEntityFromRequest_WithNullRequest_DoesNothing() {
        // Arrange
        Location location = Location.builder()
                .id(1L)
                .name("Original")
                .build();

        // Act
        locationMapper.updateEntityFromRequest(location, null);

        // Assert - location should be unchanged
        assertThat(location.getName()).isEqualTo("Original");
    }
}

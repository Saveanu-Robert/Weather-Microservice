package com.weatherspring.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.weatherspring.dto.CreateLocationRequest;
import com.weatherspring.dto.LocationDto;
import com.weatherspring.exception.LocationNotFoundException;
import com.weatherspring.mapper.LocationMapper;
import com.weatherspring.model.Location;
import com.weatherspring.repository.LocationRepository;

/** Unit tests for LocationService. */
@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

  @Mock private LocationRepository locationRepository;

  @Mock private LocationMapper locationMapper;

  @Mock private MetricsService metricsService;

  @InjectMocks private LocationService locationService;

  private Location testLocation;
  private LocationDto testLocationDto;
  private CreateLocationRequest createRequest;

  @BeforeEach
  void setUp() {
    testLocation =
        Location.builder()
            .id(1L)
            .name("London")
            .country("United Kingdom")
            .latitude(51.5074)
            .longitude(-0.1278)
            .region("Greater London")
            .build();

    testLocationDto =
        new LocationDto(
            1L, // id
            "London", // name
            "United Kingdom", // country
            51.5074, // latitude
            -0.1278, // longitude
            "Greater London", // region
            null, // createdAt
            null // updatedAt
            );

    createRequest =
        new CreateLocationRequest("London", "United Kingdom", 51.5074, -0.1278, "Greater London");
  }

  @Test
  void createLocation_WithValidData_ReturnsLocationDto() {
    // Arrange
    when(locationRepository.existsByNameAndCountry("London", "United Kingdom")).thenReturn(false);
    when(locationMapper.toEntity(createRequest)).thenReturn(testLocation);
    when(locationRepository.save(any(Location.class))).thenReturn(testLocation);
    when(locationMapper.toDto(testLocation)).thenReturn(testLocationDto);

    // Act
    LocationDto result = locationService.createLocation(createRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("London");
    assertThat(result.country()).isEqualTo("United Kingdom");
    verify(locationRepository).save(any(Location.class));
  }

  @Test
  void createLocation_WhenLocationExists_ThrowsException() {
    // Arrange
    when(locationRepository.existsByNameAndCountry("London", "United Kingdom")).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> locationService.createLocation(createRequest))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Location already exists");

    verify(locationRepository, never()).save(any(Location.class));
  }

  @Test
  void getLocationById_WhenLocationExists_ReturnsLocationDto() {
    // Arrange
    when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
    when(locationMapper.toDto(testLocation)).thenReturn(testLocationDto);

    // Act
    LocationDto result = locationService.getLocationById(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.name()).isEqualTo("London");
  }

  @Test
  void getLocationById_WhenLocationNotFound_ThrowsException() {
    // Arrange
    when(locationRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> locationService.getLocationById(999L))
        .isInstanceOf(LocationNotFoundException.class)
        .hasMessageContaining("999");
  }

  @Test
  void getAllLocations_ReturnsListOfLocations() {
    // Arrange
    Location location2 =
        Location.builder()
            .id(2L)
            .name("Paris")
            .country("France")
            .latitude(48.8566)
            .longitude(2.3522)
            .build();

    List<Location> locations = Arrays.asList(testLocation, location2);
    when(locationRepository.findAll()).thenReturn(locations);
    when(locationMapper.toDto(any(Location.class)))
        .thenReturn(testLocationDto)
        .thenReturn(new LocationDto(2L, "Paris", null, null, null, null, null, null));

    // Act
    List<LocationDto> result = locationService.getAllLocations();

    // Assert
    assertThat(result).hasSize(2);
    verify(locationRepository).findAll();
  }

  @Test
  void searchLocationsByName_ReturnsMatchingLocations() {
    // Arrange
    List<Location> locations = Arrays.asList(testLocation);
    when(locationRepository.findByNameContaining("Lon")).thenReturn(locations);
    when(locationMapper.toDto(testLocation)).thenReturn(testLocationDto);

    // Act
    List<LocationDto> result = locationService.searchLocationsByName("Lon");

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).name()).isEqualTo("London");
    verify(locationRepository).findByNameContaining("Lon");
  }

  @Test
  void updateLocation_WhenLocationExists_ReturnsUpdatedDto() {
    // Arrange
    CreateLocationRequest updateRequest =
        new CreateLocationRequest("London Updated", "United Kingdom", 51.5074, -0.1278, null);

    Location updatedLocationEntity =
        Location.builder()
            .id(1L)
            .name("London Updated")
            .country("United Kingdom")
            .latitude(51.5074)
            .longitude(-0.1278)
            .build();

    Location savedLocation =
        Location.builder()
            .id(1L)
            .name("London Updated")
            .country("United Kingdom")
            .latitude(51.5074)
            .longitude(-0.1278)
            .build();

    when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
    when(locationMapper.updateEntityFromRequest(eq(testLocation), eq(updateRequest)))
        .thenReturn(updatedLocationEntity);
    when(locationRepository.save(updatedLocationEntity)).thenReturn(savedLocation);
    when(locationMapper.toDto(savedLocation))
        .thenReturn(new LocationDto(1L, "London Updated", null, null, null, null, null, null));

    // Act
    LocationDto result = locationService.updateLocation(1L, updateRequest);

    // Assert
    assertThat(result).isNotNull();
    verify(locationMapper).updateEntityFromRequest(eq(testLocation), eq(updateRequest));
    verify(locationRepository).save(eq(updatedLocationEntity));
  }

  @Test
  void deleteLocation_WhenLocationExists_DeletesSuccessfully() {
    // Arrange
    when(locationRepository.existsById(1L)).thenReturn(true);

    // Act
    locationService.deleteLocation(1L);

    // Assert
    verify(locationRepository).deleteById(1L);
  }

  @Test
  void deleteLocation_WhenLocationNotFound_ThrowsException() {
    // Arrange
    when(locationRepository.existsById(999L)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> locationService.deleteLocation(999L))
        .isInstanceOf(LocationNotFoundException.class);

    verify(locationRepository, never()).deleteById(anyLong());
  }

  @Test
  void findOrCreateLocation_WhenLocationExists_ReturnsExistingLocation() {
    // Arrange
    when(locationRepository.findByNameAndCountry("London", "United Kingdom"))
        .thenReturn(Optional.of(testLocation));

    // Act
    Location result =
        locationService.findOrCreateLocation(
            "London", "United Kingdom", 51.5074, -0.1278, "Greater London");

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    verify(locationRepository, never()).save(any(Location.class));
  }

  @Test
  void findOrCreateLocation_WhenLocationDoesNotExist_CreatesNewLocation() {
    // Arrange
    when(locationRepository.findByNameAndCountry("Paris", "France")).thenReturn(Optional.empty());
    when(locationRepository.save(any(Location.class))).thenReturn(testLocation);

    // Act
    Location result =
        locationService.findOrCreateLocation("Paris", "France", 48.8566, 2.3522, "Île-de-France");

    // Assert
    assertThat(result).isNotNull();
    verify(locationRepository).save(any(Location.class));
  }
}

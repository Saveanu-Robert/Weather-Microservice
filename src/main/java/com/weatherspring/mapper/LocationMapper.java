package com.weatherspring.mapper;

import jakarta.annotation.Nullable;

import org.springframework.stereotype.Component;

import com.weatherspring.dto.CreateLocationRequest;
import com.weatherspring.dto.LocationDto;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.model.Location;

import lombok.extern.slf4j.Slf4j;

/**
 * Converts between Location entities, DTOs, and external API responses.
 *
 * <p>Handles creation from user requests and extraction from WeatherAPI.com responses.
 */
@Slf4j
@Component
public class LocationMapper {

  /**
   * Converts a Location entity to a LocationDto.
   *
   * @param location the location entity to convert
   * @return the location DTO, or null if input is null
   */
  @Nullable
  public LocationDto toDto(@Nullable Location location) {
    if (location == null) {
      return null;
    }

    return new LocationDto(
        location.getId(),
        location.getName(),
        location.getCountry(),
        location.getLatitude(),
        location.getLongitude(),
        location.getRegion(),
        location.getCreatedAt(),
        location.getUpdatedAt());
  }

  /**
   * Converts a CreateLocationRequest to a Location entity.
   *
   * @param request the location creation request
   * @return the location entity, or null if input is null
   */
  @Nullable
  public Location toEntity(@Nullable CreateLocationRequest request) {
    if (request == null) {
      return null;
    }

    return Location.builder()
        .name(request.name())
        .country(request.country())
        .latitude(request.latitude())
        .longitude(request.longitude())
        .region(request.region())
        .build();
  }

  /**
   * Converts location information from WeatherAPI.com response to a Location entity.
   *
   * @param locationInfo the location information from the external API
   * @return the location entity, or null if input is null
   */
  @Nullable
  public Location fromWeatherApi(@Nullable WeatherApiResponse.LocationInfo locationInfo) {
    if (locationInfo == null) {
      return null;
    }

    return Location.builder()
        .name(locationInfo.getName())
        .country(locationInfo.getCountry())
        .latitude(locationInfo.getLat())
        .longitude(locationInfo.getLon())
        .region(locationInfo.getRegion())
        .build();
  }

  /**
   * Updates a location entity with new values from a request.
   *
   * <p>Creates a new Location instance (due to Lombok @Builder immutability) but preserves
   * the original createdAt/updatedAt timestamps since they're in the parent class.
   *
   * @param location the existing location to update
   * @param request the new values
   * @return new Location instance with updated values and preserved timestamps
   */
  public Location updateEntityFromRequest(Location location, CreateLocationRequest request) {
    if (location == null || request == null) {
      return location;
    }

    Location updated =
        Location.builder()
            .id(location.getId())
            .name(request.name())
            .country(request.country())
            .latitude(request.latitude())
            .longitude(request.longitude())
            .region(request.region())
            .build();

    // Preserve audit timestamps (Lombok builder doesn't include inherited fields)
    updated.setCreatedAt(location.getCreatedAt());
    updated.setUpdatedAt(location.getUpdatedAt());

    return updated;
  }
}

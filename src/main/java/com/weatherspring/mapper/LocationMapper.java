package com.weatherspring.mapper;

import com.weatherspring.dto.CreateLocationRequest;
import com.weatherspring.dto.LocationDto;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.model.Location;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Location entities and DTOs.
 */
@Component
public class LocationMapper {

    /**
     * Converts a Location entity to LocationDto.
     *
     * @param location the location entity
     * @return the location DTO
     */
    public LocationDto toDto(Location location) {
        if (location == null) {
            return null;
        }

        return LocationDto.builder()
                .id(location.getId())
                .name(location.getName())
                .country(location.getCountry())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .region(location.getRegion())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .build();
    }

    /**
     * Converts a CreateLocationRequest to Location entity.
     *
     * @param request the create location request
     * @return the location entity
     */
    public Location toEntity(CreateLocationRequest request) {
        if (request == null) {
            return null;
        }

        return Location.builder()
                .name(request.getName())
                .country(request.getCountry())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .region(request.getRegion())
                .build();
    }

    /**
     * Converts WeatherAPI location info to Location entity.
     *
     * @param locationInfo the external API location info
     * @return the location entity
     */
    public Location fromWeatherApi(WeatherApiResponse.LocationInfo locationInfo) {
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
     * Updates an existing Location entity with data from CreateLocationRequest.
     * Returns a new Location instance with updated values.
     *
     * @param location the existing location entity
     * @param request the update request
     * @return updated location entity with new values
     */
    public Location updateEntityFromRequest(Location location, CreateLocationRequest request) {
        if (location == null || request == null) {
            return location;
        }

        return Location.builder()
                .id(location.getId())
                .name(request.getName())
                .country(request.getCountry())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .region(request.getRegion())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .build();
    }
}

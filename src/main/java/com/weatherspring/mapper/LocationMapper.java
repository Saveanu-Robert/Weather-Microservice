package com.weatherspring.mapper;

import com.weatherspring.dto.CreateLocationRequest;
import com.weatherspring.dto.LocationDto;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.model.Location;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Location entities and DTOs.
 */
@Slf4j
@Component
public class LocationMapper {

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
                location.getUpdatedAt()
        );
    }

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
     * Updates a location by creating a new instance with values from the request.
     * Preserves audit timestamps from the original.
     */
    public Location updateEntityFromRequest(Location location, CreateLocationRequest request) {
        if (location == null || request == null) {
            return location;
        }

        Location updated = Location.builder()
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

package com.weatherspring.mapper;

import com.weatherspring.dto.WeatherDto;
import com.weatherspring.dto.external.WeatherApiResponse;
import com.weatherspring.model.Location;
import com.weatherspring.model.WeatherRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mapper for converting between WeatherRecord entities and DTOs.
 */
@Component
public class WeatherMapper {

    private static final Logger logger = LoggerFactory.getLogger(WeatherMapper.class);
    private static final DateTimeFormatter DATETIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Converts a WeatherRecord entity to WeatherDto.
     *
     * @param weatherRecord the weather record entity
     * @return the weather DTO
     */
    public WeatherDto toDto(WeatherRecord weatherRecord) {
        if (weatherRecord == null) {
            return null;
        }

        return WeatherDto.builder()
                .id(weatherRecord.getId())
                .locationId(weatherRecord.getLocation().getId())
                .locationName(weatherRecord.getLocation().getName())
                .temperature(weatherRecord.getTemperature())
                .feelsLike(weatherRecord.getFeelsLike())
                .humidity(weatherRecord.getHumidity())
                .windSpeed(weatherRecord.getWindSpeed())
                .windDirection(weatherRecord.getWindDirection())
                .condition(weatherRecord.getCondition())
                .description(weatherRecord.getDescription())
                .pressureMb(weatherRecord.getPressureMb())
                .precipitationMm(weatherRecord.getPrecipitationMm())
                .cloudCoverage(weatherRecord.getCloudCoverage())
                .uvIndex(weatherRecord.getUvIndex())
                .timestamp(weatherRecord.getTimestamp())
                .build();
    }

    /**
     * Converts WeatherAPI response to WeatherRecord entity.
     *
     * @param apiResponse the external API response
     * @param location the associated location entity
     * @return the weather record entity
     */
    public WeatherRecord fromWeatherApi(WeatherApiResponse apiResponse, Location location) {
        if (apiResponse == null || apiResponse.getCurrent() == null) {
            return null;
        }

        WeatherApiResponse.CurrentWeather current = apiResponse.getCurrent();
        LocalDateTime timestamp = parseTimestamp(apiResponse.getLocation().getLocaltime());

        return WeatherRecord.builder()
                .location(location)
                .temperature(current.getTempC())
                .feelsLike(current.getFeelslikeC())
                .humidity(current.getHumidity())
                .windSpeed(current.getWindKph())
                .windDirection(current.getWindDir())
                .condition(current.getCondition().getText())
                .description(current.getCondition().getText())
                .pressureMb(current.getPressureMb())
                .precipitationMm(current.getPrecipMm())
                .cloudCoverage(current.getCloud())
                .uvIndex(current.getUv())
                .timestamp(timestamp != null ? timestamp : LocalDateTime.now())
                .build();
    }

    /**
     * Creates a WeatherDto from WeatherAPI response (without persisting).
     *
     * @param apiResponse the external API response
     * @return the weather DTO
     */
    public WeatherDto toDtoFromApi(WeatherApiResponse apiResponse) {
        if (apiResponse == null || apiResponse.getCurrent() == null) {
            return null;
        }

        WeatherApiResponse.CurrentWeather current = apiResponse.getCurrent();
        WeatherApiResponse.LocationInfo location = apiResponse.getLocation();
        LocalDateTime timestamp = parseTimestamp(location.getLocaltime());

        return WeatherDto.builder()
                .locationName(location.getName())
                .temperature(current.getTempC())
                .feelsLike(current.getFeelslikeC())
                .humidity(current.getHumidity())
                .windSpeed(current.getWindKph())
                .windDirection(current.getWindDir())
                .condition(current.getCondition().getText())
                .description(current.getCondition().getText())
                .pressureMb(current.getPressureMb())
                .precipitationMm(current.getPrecipMm())
                .cloudCoverage(current.getCloud())
                .uvIndex(current.getUv())
                .timestamp(timestamp != null ? timestamp : LocalDateTime.now())
                .build();
    }

    /**
     * Parses timestamp from WeatherAPI format.
     *
     * @param localtimeStr the localtime string from API
     * @return parsed LocalDateTime or null if parsing fails
     */
    private LocalDateTime parseTimestamp(String localtimeStr) {
        if (localtimeStr == null || localtimeStr.isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(localtimeStr, DATETIME_FORMATTER);
        } catch (Exception e) {
            logger.warn("Failed to parse timestamp '{}', using current time", localtimeStr, e);
            return LocalDateTime.now();
        }
    }
}

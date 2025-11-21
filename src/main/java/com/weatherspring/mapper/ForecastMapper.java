package com.weatherspring.mapper;

import com.weatherspring.dto.ForecastDto;
import com.weatherspring.dto.external.ForecastApiResponse;
import com.weatherspring.model.ForecastRecord;
import com.weatherspring.model.Location;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between ForecastRecord entities and DTOs.
 */
@Component
public class ForecastMapper {

    /**
     * Converts a ForecastRecord entity to ForecastDto.
     *
     * @param forecastRecord the forecast record entity
     * @return the forecast DTO
     */
    public ForecastDto toDto(ForecastRecord forecastRecord) {
        if (forecastRecord == null) {
            return null;
        }

        return ForecastDto.builder()
                .id(forecastRecord.getId())
                .locationId(forecastRecord.getLocation().getId())
                .locationName(forecastRecord.getLocation().getName())
                .forecastDate(forecastRecord.getForecastDate())
                .maxTemperature(forecastRecord.getMaxTemperature())
                .minTemperature(forecastRecord.getMinTemperature())
                .avgTemperature(forecastRecord.getAvgTemperature())
                .maxWindSpeed(forecastRecord.getMaxWindSpeed())
                .avgHumidity(forecastRecord.getAvgHumidity())
                .condition(forecastRecord.getCondition())
                .description(forecastRecord.getDescription())
                .precipitationMm(forecastRecord.getPrecipitationMm())
                .precipitationProbability(forecastRecord.getPrecipitationProbability())
                .uvIndex(forecastRecord.getUvIndex())
                .sunriseTime(forecastRecord.getSunriseTime())
                .sunsetTime(forecastRecord.getSunsetTime())
                .build();
    }

    /**
     * Converts WeatherAPI forecast response to list of ForecastRecord entities.
     *
     * @param apiResponse the external API response
     * @param location the associated location entity
     * @return list of forecast record entities
     */
    public List<ForecastRecord> fromWeatherApi(ForecastApiResponse apiResponse, Location location) {
        if (apiResponse == null || apiResponse.getForecast() == null ||
            apiResponse.getForecast().getForecastday() == null) {
            return List.of();
        }

        return apiResponse.getForecast().getForecastday().stream()
                .map(day -> fromForecastDay(day, location))
                .collect(Collectors.toList());
    }

    /**
     * Converts a single ForecastDay to ForecastRecord entity.
     *
     * @param forecastDay the forecast day from API
     * @param location the associated location entity
     * @return the forecast record entity
     */
    private ForecastRecord fromForecastDay(ForecastApiResponse.ForecastDay forecastDay, Location location) {
        if (forecastDay == null || forecastDay.getDay() == null) {
            return null;
        }

        ForecastApiResponse.Day day = forecastDay.getDay();
        ForecastApiResponse.Astro astro = forecastDay.getAstro();

        return ForecastRecord.builder()
                .location(location)
                .forecastDate(LocalDate.parse(forecastDay.getDate()))
                .maxTemperature(day.getMaxtempC())
                .minTemperature(day.getMintempC())
                .avgTemperature(day.getAvgtempC())
                .maxWindSpeed(day.getMaxwindKph())
                .avgHumidity(day.getAvghumidity())
                .condition(day.getCondition().getText())
                .description(day.getCondition().getText())
                .precipitationMm(day.getTotalprecipMm())
                .precipitationProbability(day.getDailyChanceOfRain())
                .uvIndex(day.getUv())
                .sunriseTime(astro != null ? astro.getSunrise() : null)
                .sunsetTime(astro != null ? astro.getSunset() : null)
                .build();
    }

    /**
     * Converts WeatherAPI forecast response to list of ForecastDto (without persisting).
     *
     * @param apiResponse the external API response
     * @param locationName the location name
     * @return list of forecast DTOs
     */
    public List<ForecastDto> toDtoFromApi(ForecastApiResponse apiResponse, String locationName) {
        if (apiResponse == null || apiResponse.getForecast() == null ||
            apiResponse.getForecast().getForecastday() == null) {
            return List.of();
        }

        return apiResponse.getForecast().getForecastday().stream()
                .map(day -> toDtoFromForecastDay(day, locationName))
                .collect(Collectors.toList());
    }

    /**
     * Converts a single ForecastDay to ForecastDto.
     *
     * @param forecastDay the forecast day from API
     * @param locationName the location name
     * @return the forecast DTO
     */
    private ForecastDto toDtoFromForecastDay(ForecastApiResponse.ForecastDay forecastDay, String locationName) {
        if (forecastDay == null || forecastDay.getDay() == null) {
            return null;
        }

        ForecastApiResponse.Day day = forecastDay.getDay();
        ForecastApiResponse.Astro astro = forecastDay.getAstro();

        return ForecastDto.builder()
                .locationName(locationName)
                .forecastDate(LocalDate.parse(forecastDay.getDate()))
                .maxTemperature(day.getMaxtempC())
                .minTemperature(day.getMintempC())
                .avgTemperature(day.getAvgtempC())
                .maxWindSpeed(day.getMaxwindKph())
                .avgHumidity(day.getAvghumidity())
                .condition(day.getCondition().getText())
                .description(day.getCondition().getText())
                .precipitationMm(day.getTotalprecipMm())
                .precipitationProbability(day.getDailyChanceOfRain())
                .uvIndex(day.getUv())
                .sunriseTime(astro != null ? astro.getSunrise() : null)
                .sunsetTime(astro != null ? astro.getSunset() : null)
                .build();
    }
}

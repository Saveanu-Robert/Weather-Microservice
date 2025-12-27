package com.weatherspring.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for WeatherAPI.com forecast endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastApiResponse {

    @JsonProperty("location")
    private WeatherApiResponse.LocationInfo location;

    @JsonProperty("forecast")
    private Forecast forecast;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Forecast {
        @JsonProperty("forecastday")
        private List<ForecastDay> forecastday;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ForecastDay {
        @JsonProperty("date")
        private String date;

        @JsonProperty("day")
        private Day day;

        @JsonProperty("astro")
        private Astro astro;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Day {
        @JsonProperty("maxtemp_c")
        private Double maxtempC;

        @JsonProperty("mintemp_c")
        private Double mintempC;

        @JsonProperty("avgtemp_c")
        private Double avgtempC;

        @JsonProperty("maxwind_kph")
        private Double maxwindKph;

        @JsonProperty("totalprecip_mm")
        private Double totalprecipMm;

        @JsonProperty("avghumidity")
        private Integer avghumidity;

        @JsonProperty("daily_chance_of_rain")
        private Integer dailyChanceOfRain;

        @JsonProperty("condition")
        private WeatherApiResponse.Condition condition;

        @JsonProperty("uv")
        private Double uv;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Astro {
        @JsonProperty("sunrise")
        private String sunrise;

        @JsonProperty("sunset")
        private String sunset;
    }
}

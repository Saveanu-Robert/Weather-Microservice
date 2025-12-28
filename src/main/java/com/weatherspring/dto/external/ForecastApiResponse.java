package com.weatherspring.dto.external;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for WeatherAPI.com forecast endpoint. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastApiResponse {

  @JsonProperty("location")
  private WeatherApiResponse.LocationInfo location;

  @JsonProperty("forecast")
  private Forecast forecast;

  /** Container for forecast day data from WeatherAPI.com. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Forecast {
    @JsonProperty("forecastday")
    private List<ForecastDay> forecastday;
  }

  /** Forecast data for a single day from WeatherAPI.com. */
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

  /** Daily weather statistics from WeatherAPI.com forecast. */
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

  /** Astronomical data including sunrise and sunset times from WeatherAPI.com. */
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

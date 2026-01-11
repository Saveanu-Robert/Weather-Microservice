package com.weatherspring.dto.external;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response structure from WeatherAPI.com's forecast endpoint.
 *
 * <p>This matches the JSON format returned by the external API. Fields are populated via Jackson
 * during deserialization. Unknown fields from the API are ignored to prevent failures when the
 * API adds new fields.
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

  /**
   * Wraps the list of daily forecast entries.
   *
   * <p>The API returns forecasts nested inside this "forecast" object.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Forecast {
    @JsonProperty("forecastday")
    private List<ForecastDay> forecastday;
  }

  /**
   * Forecast information for one calendar day.
   *
   * <p>Contains the date, weather statistics for the day, and astronomical data like sunrise/sunset.
   */
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

  /**
   * Aggregated weather statistics for the entire day.
   *
   * <p>Includes max/min/avg temperatures, wind, precipitation totals, and humidity.
   */
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

  /**
   * Sunrise and sunset times for the day.
   *
   * <p>Times are in the local timezone of the location being queried.
   */
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

package com.weatherspring.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response structure from WeatherAPI.com's current weather endpoint.
 *
 * <p>This matches the JSON format returned by the external API. Fields are populated via Jackson
 * during deserialization. Unknown fields from the API are ignored to prevent failures when the
 * API adds new fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherApiResponse {

  @JsonProperty("location")
  private LocationInfo location;

  @JsonProperty("current")
  private CurrentWeather current;

  /**
   * Geographic and timezone information for the queried location.
   *
   * <p>Includes the resolved location name (which may differ from the query), coordinates,
   * timezone ID, and local time at the location.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class LocationInfo {
    @JsonProperty("name")
    private String name;

    @JsonProperty("region")
    private String region;

    @JsonProperty("country")
    private String country;

    @JsonProperty("lat")
    private Double lat;

    @JsonProperty("lon")
    private Double lon;

    @JsonProperty("tz_id")
    private String tzId;

    @JsonProperty("localtime")
    private String localtime;
  }

  /**
   * Current weather measurements and conditions.
   *
   * <p>Contains real-time readings including temperature, wind, precipitation, humidity,
   * and cloud cover.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class CurrentWeather {
    @JsonProperty("temp_c")
    private Double tempC;

    @JsonProperty("feelslike_c")
    private Double feelslikeC;

    @JsonProperty("condition")
    private Condition condition;

    @JsonProperty("wind_kph")
    private Double windKph;

    @JsonProperty("wind_dir")
    private String windDir;

    @JsonProperty("pressure_mb")
    private Double pressureMb;

    @JsonProperty("precip_mm")
    private Double precipMm;

    @JsonProperty("humidity")
    private Integer humidity;

    @JsonProperty("cloud")
    private Integer cloud;

    @JsonProperty("uv")
    private Double uv;
  }

  /**
   * Weather condition description and icon information.
   *
   * <p>The text field contains a human-readable description like "Partly cloudy". The icon
   * is a URL to a weather icon image. The code is an internal WeatherAPI identifier.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Condition {
    @JsonProperty("text")
    private String text;

    @JsonProperty("icon")
    private String icon;

    @JsonProperty("code")
    private Integer code;
  }
}

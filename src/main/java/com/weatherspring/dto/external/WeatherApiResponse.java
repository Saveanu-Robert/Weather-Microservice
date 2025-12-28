package com.weatherspring.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for WeatherAPI.com current weather endpoint. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherApiResponse {

  @JsonProperty("location")
  private LocationInfo location;

  @JsonProperty("current")
  private CurrentWeather current;

  /** Location information from WeatherAPI.com response. */
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

  /** Current weather data from WeatherAPI.com response. */
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

  /** Weather condition information from WeatherAPI.com response. */
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

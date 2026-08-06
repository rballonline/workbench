package com.tiltedev.spring_reactive.dto.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastResult {

    private double latitude;
    private double longitude;

    @JsonProperty("current")
    private CurrentWeather current;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentWeather {
        private String time;

        @JsonProperty("temperature_2m")
        private double temperature;

        @JsonProperty("wind_speed_10m")
        private double windSpeed;

        @JsonProperty("relative_humidity_2m")
        private int humidity;

        @JsonProperty("weather_code")
        private int weatherCode;
    }
}

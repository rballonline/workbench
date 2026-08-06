package com.tiltedev.spring_reactive.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeatherResponse {

    private String cityName;
    private String countryName;
    private Double latitude;
    private Double longitude;
    private Double temperatureCelsius;
    private Double windSpeedKmh;
    private Integer humidity;
    private Integer weatherCode;
}

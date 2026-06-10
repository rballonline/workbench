package com.tiltedev.spring_reactive.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CitySearchResponse {

    private String cityName;
    private Double latitude;
    private Double longitude;
    private String countryCode;
    private String countryName;
    private String capital;
    private String region;
    private String flagUrl;
}

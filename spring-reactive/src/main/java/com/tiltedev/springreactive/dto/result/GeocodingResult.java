package com.tiltedev.spring_reactive.dto.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodingResult {

    private List<GeocodingEntry> results;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeocodingEntry {
        private String name;
        private double latitude;
        private double longitude;
        private String country;
        private String country_code;
    }
}

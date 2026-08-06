package com.tiltedev.spring_reactive.dto.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssApiResult {

    private String message;
    private long timestamp;

    @JsonProperty("iss_position")
    private IssPosition issPosition;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssPosition {
        private String latitude;
        private String longitude;
    }
}

package com.tiltedev.spring_reactive.dto.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryApiResult {

    private Map<String, String> name;
    private List<String> capital;
    private String region;
    private Long population;
    private List<String> cca2;
    private Flags flags;

    public String getCommonName() {
        return name != null ? name.get("common") : null;
    }

    public String getCapitalCity() {
        return capital != null && !capital.isEmpty() ? capital.get(0) : null;
    }

    public String getAlpha2Code() {
        return cca2 != null && !cca2.isEmpty() ? cca2.get(0) : null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Flags {
        @JsonProperty("png")
        private String png;
    }
}

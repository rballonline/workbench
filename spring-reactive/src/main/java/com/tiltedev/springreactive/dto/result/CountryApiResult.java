package com.tiltedev.springreactive.dto.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryApiResult {

    private Names names;
    private List<Capital> capitals;
    private String region;
    private Long population;
    private Codes codes;
    private Flags flag;

    public String getCommonName() {
        return names != null ? names.getCommon() : null;
    }

    public String getCapitalCity() {
        return capitals != null && !capitals.isEmpty() ? capitals.get(0).getName() : null;
    }

    public String getAlpha2Code() {
        return codes != null ? codes.getAlpha2() : null;
    }

    public Flags getFlags() {
        return flag;
    }

    public String getFlagUrl() {
        return flag != null ? flag.getPng() : null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Names {
        private String common;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Capital {
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Codes {
        @JsonProperty("alpha_2")
        private String alpha2;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Flags {
        @JsonProperty("url_png")
        private String png;
    }
}

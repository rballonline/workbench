package com.tiltedev.springreactive.dto.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryApiV5Response {

    private Payload data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {
        private List<CountryApiResult> objects;
    }
}

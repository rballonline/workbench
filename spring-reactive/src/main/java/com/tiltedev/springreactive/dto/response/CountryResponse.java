package com.tiltedev.spring_reactive.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CountryResponse {

    private String code;
    private String name;
    private String capital;
    private String region;
    private Long population;
    private String flagUrl;
}

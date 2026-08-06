package com.tiltedev.spring_reactive.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DestinationWithCountry {

    private Long id;
    private String cityName;
    private Double latitude;
    private Double longitude;
    private String addedBy;
    private LocalDateTime createdAt;
    private String countryCode;
    private String countryName;
    private String capital;
    private String region;
    private Long population;
    private String flagUrl;
}

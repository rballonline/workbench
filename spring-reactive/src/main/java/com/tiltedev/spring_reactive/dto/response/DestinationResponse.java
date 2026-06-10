package com.tiltedev.spring_reactive.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DestinationResponse {

    private Long id;
    private String cityName;
    private Double latitude;
    private Double longitude;
    private String addedBy;
    private LocalDateTime createdAt;
    private CountryResponse country;
}

package com.tiltedev.spring_reactive.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IssResponse {

    private Double latitude;
    private Double longitude;
    private Long timestamp;
}

package com.tiltedev.spring_reactive.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddDestinationRequest {

    @NotBlank
    private String cityName;

    @NotBlank
    @Size(min = 2, max = 10)
    private String countryCode;

    @NotNull
    @DecimalMin("-90.0") @DecimalMax("90.0")
    private Double latitude;

    @NotNull
    @DecimalMin("-180.0") @DecimalMax("180.0")
    private Double longitude;

    @Size(max = 100)
    private String addedBy;
}

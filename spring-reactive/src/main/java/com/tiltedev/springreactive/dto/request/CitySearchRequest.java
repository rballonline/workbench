package com.tiltedev.spring_reactive.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CitySearchRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    private String q;
}

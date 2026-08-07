package com.tiltedev.springreactive.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

@Data
public class ValidateKeyRequest {

  @NotBlank @ToString.Exclude private String apiKey;
}

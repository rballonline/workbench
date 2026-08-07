package com.tiltedev.springreactive.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
public class ChatRequest {

  @NotBlank private String conversationId;

  @NotBlank
  @Size(max = 2000)
  private String message;

  /** Bring-your-own-key - there is no server-side default, so every request must carry one. */
  @NotBlank @ToString.Exclude private String apiKey;
}

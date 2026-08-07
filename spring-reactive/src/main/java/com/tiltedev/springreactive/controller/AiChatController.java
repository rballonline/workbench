package com.tiltedev.springreactive.controller;

import com.tiltedev.springreactive.dto.request.ChatRequest;
import com.tiltedev.springreactive.dto.request.ValidateKeyRequest;
import com.tiltedev.springreactive.dto.response.ApiKeyValidationResponse;
import com.tiltedev.springreactive.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AiChatController {

  private final AiChatService aiChatService;

  @PostMapping(path = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<String>> chat(@RequestBody @Valid ChatRequest request) {
    return aiChatService.chat(request);
  }

  @PostMapping("/validate-key")
  public Mono<ApiKeyValidationResponse> validateKey(
      @RequestBody @Valid ValidateKeyRequest request) {
    return aiChatService.validateKey(request.getApiKey());
  }
}

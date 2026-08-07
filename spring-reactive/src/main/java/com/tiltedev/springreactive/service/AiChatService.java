package com.tiltedev.springreactive.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiltedev.springreactive.ai.AssistantChatClientFactory;
import com.tiltedev.springreactive.ai.AssistantTools;
import com.tiltedev.springreactive.dto.event.PendingDeleteConfirmation;
import com.tiltedev.springreactive.dto.request.ChatRequest;
import com.tiltedev.springreactive.dto.response.ApiKeyValidationResponse;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

  private final AssistantChatClientFactory assistantChatClientFactory;
  private final ObjectMapper objectMapper;
  private final ReactiveHttpClient httpClient;
  private final WebClient anthropicWebClient;

  /**
   * Confirms a key actually works against Anthropic before the frontend locks it in. Hits {@code
   * GET /v1/models} rather than sending a chat message - it is authenticated the same way but costs
   * no tokens. A bad key surfaces as {@link
   * com.tiltedev.springreactive.exception.ApiUnauthorizedException} via {@link
   * ReactiveHttpClient}'s error-handling filter, which {@code GlobalExceptionHandler} already turns
   * into a 401 ProblemDetail - no special-casing needed here.
   */
  public Mono<ApiKeyValidationResponse> validateKey(String apiKey) {
    WebClient client = anthropicWebClient.mutate().defaultHeader("x-api-key", apiKey).build();
    return httpClient
        .get(client, "/v1/models", String.class, uri -> uri.queryParam("limit", 1))
        .map(body -> ApiKeyValidationResponse.builder().validatedAt(Instant.now()).build());
  }

  public Flux<ServerSentEvent<String>> chat(ChatRequest request) {
    var pendingDeletes = new CopyOnWriteArrayList<PendingDeleteConfirmation>();

    Flux<ServerSentEvent<String>> tokens =
        assistantChatClientFactory
            .forApiKey(request.getApiKey())
            .prompt()
            .user(request.getMessage())
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, request.getConversationId()))
            .toolContext(Map.of(AssistantTools.PENDING_DELETE_CONFIRMATIONS_KEY, pendingDeletes))
            .stream()
            .content()
            .map(chunk -> ServerSentEvent.builder(chunk).event("token").build());

    Flux<ServerSentEvent<String>> confirmations =
        Flux.defer(
            () ->
                Flux.fromIterable(pendingDeletes)
                    .map(
                        pending ->
                            ServerSentEvent.builder(toJson(pending))
                                .event("confirm-delete")
                                .build()));

    return tokens
        .concatWith(confirmations)
        .onErrorResume(
            ex -> {
              log.error(
                  "Assistant chat failed for conversation {}", request.getConversationId(), ex);
              return Flux.just(
                  ServerSentEvent.builder("Something went wrong talking to the assistant.")
                      .event("error")
                      .build());
            });
  }

  private String toJson(PendingDeleteConfirmation pending) {
    try {
      return objectMapper.writeValueAsString(pending);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize " + pending, e);
    }
  }
}

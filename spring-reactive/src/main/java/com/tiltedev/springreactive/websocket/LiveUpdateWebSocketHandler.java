package com.tiltedev.spring_reactive.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiltedev.spring_reactive.dto.event.DestinationEvent;
import com.tiltedev.spring_reactive.service.IssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveUpdateWebSocketHandler implements WebSocketHandler {

    private final Sinks.Many<DestinationEvent> eventSink;
    private final IssService issService;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.debug("WebSocket client connected: {}", session.getId());

        Flux<String> crudStream = eventSink.asFlux()
                .map(this::toJson);

        Flux<String> issStream = issService.liveStream()
                .map(this::toJson);

        return session.send(
                Flux.merge(crudStream, issStream)
                        .map(session::textMessage)
        ).doFinally(signal ->
                log.debug("WebSocket client disconnected: {} ({})", session.getId(), signal)
        );
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize WebSocket message: {}", e.getMessage());
            return "{}";
        }
    }
}

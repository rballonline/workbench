package com.tiltedev.spring_reactive.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tiltedev.spring_reactive.dto.event.DestinationEvent;
import com.tiltedev.spring_reactive.websocket.LiveUpdateWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import reactor.core.publisher.Sinks;

import java.util.Map;

@Configuration
public class WebSocketConfig {

    @Bean
    public Sinks.Many<DestinationEvent> destinationEventSink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public HandlerMapping webSocketHandlerMapping(LiveUpdateWebSocketHandler handler) {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(Map.of("/ws/destinations", handler));
        mapping.setOrder(1);
        return mapping;
    }

    @Bean
    public WebSocketService webSocketService() {
        return new HandshakeWebSocketService(new ReactorNettyRequestUpgradeStrategy());
    }

    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter(WebSocketService webSocketService) {
        return new WebSocketHandlerAdapter(webSocketService);
    }
}

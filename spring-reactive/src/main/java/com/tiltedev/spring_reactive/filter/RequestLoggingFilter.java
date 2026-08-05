package com.tiltedev.spring_reactive.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter implements WebFilter {

    public static final String REQUEST_ID_ATTRIBUTE = "requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = UUID.randomUUID().toString();
        exchange.getAttributes().put(REQUEST_ID_ATTRIBUTE, requestId);

        Instant start = Instant.now();
        log.info("Request Id: {}, Method Type: {}, Request URI: {}, Start Time: {}",
                requestId, request.getMethod(), request.getURI(), start);

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    Instant end = Instant.now();
                    log.info("Request Id: {}, Method Type: {}, Request URI: {}, End Time: {}, Elapsed: {}ms, Status: {}",
                            requestId, request.getMethod(), request.getURI(), end,
                            Duration.between(start, end).toMillis(),
                            exchange.getResponse().getStatusCode());
                });
    }
}

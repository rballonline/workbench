package com.tiltedev.springreactive.controller;

import com.tiltedev.springreactive.service.ReactiveHttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter implements WebFilter {

  public static final String REQUEST_ID_ATTRIBUTE = "requestId";
  private static final String REQUEST_ID_HEADER = "X-Request-Id";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String incomingRequestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
    String requestId =
        StringUtils.hasText(incomingRequestId) ? incomingRequestId : generateRequestId();
    exchange.getAttributes().put(REQUEST_ID_ATTRIBUTE, requestId);

    Instant start = Instant.now();
    log.info(
        "[{}], Method Type: {}, Request URI: {}, Start Time: {}",
        requestId,
        request.getMethod(),
        request.getURI(),
        start);

    return chain
        .filter(exchange)
        .doFinally(
            signalType -> {
              Instant end = Instant.now();
              log.info(
                  "[{}] Method Type: {}, Request URI: {}, End Time: {}, Elapsed: {}ms, Status: {}",
                  requestId,
                  request.getMethod(),
                  request.getURI(),
                  end,
                  Duration.between(start, end).toMillis(),
                  exchange.getResponse().getStatusCode());
            })
        .contextWrite(Context.of(ReactiveHttpClient.REQUEST_ID_CONTEXT_KEY, requestId));
  }

  private static String generateRequestId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
  }
}

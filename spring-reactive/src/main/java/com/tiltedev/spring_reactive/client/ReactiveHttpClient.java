package com.tiltedev.spring_reactive.client;

import com.tiltedev.spring_reactive.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Component
public class ReactiveHttpClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    public <T> Mono<T> get(WebClient client, String path, Class<T> responseType,
            Consumer<UriBuilder> uriQueryParams) {
        String requestId = generateRequestId();
        return execute("GET", path, requestId,
                build(client, requestId),
                c -> c.get().uri(uriBuilder -> {
                    uriQueryParams.accept(uriBuilder.path(path));
                    return uriBuilder.build();
                }).retrieve(),
                responseType);
    }

    public <T> Flux<T> getFlux(WebClient client, String path, Class<T> responseType,
            Consumer<UriBuilder> uriQueryParams) {
        String requestId = generateRequestId();
        return executeFlux("GET", path, requestId,
                build(client, requestId),
                c -> c.get().uri(uriBuilder -> {
                    uriQueryParams.accept(uriBuilder.path(path));
                    return uriBuilder.build();
                }).retrieve(),
                responseType);
    }

    public <B, T> Mono<T> post(WebClient client, String path, B body, Class<T> responseType) {
        String requestId = generateRequestId();
        return execute("POST", path, requestId,
                build(client, requestId),
                c -> c.post().uri(path).bodyValue(body).retrieve(),
                responseType);
    }

    public <B, T> Flux<T> postFlux(WebClient client, String path, B body, Class<T> responseType) {
        String requestId = generateRequestId();
        return executeFlux("POST", path, requestId,
                build(client, requestId),
                c -> c.post().uri(path).bodyValue(body).retrieve(),
                responseType);
    }

    public <B, T> Mono<T> put(WebClient client, String path, B body, Class<T> responseType) {
        String requestId = generateRequestId();
        return execute("PUT", path, requestId,
                build(client, requestId),
                c -> c.put().uri(path).bodyValue(body).retrieve(),
                responseType);
    }

    public <B, T> Flux<T> putFlux(WebClient client, String path, B body, Class<T> responseType) {
        String requestId = generateRequestId();
        return executeFlux("PUT", path, requestId,
                build(client, requestId),
                c -> c.put().uri(path).bodyValue(body).retrieve(),
                responseType);
    }

    public Mono<Void> delete(WebClient client, String path) {
        String requestId = generateRequestId();
        return execute("DELETE", path, requestId,
                build(client, requestId),
                c -> c.delete().uri(path).retrieve(),
                Void.class);
    }

    private WebClient build(WebClient client, String requestId) {
        return client.mutate()
                .defaultHeader("X-Request-Id", requestId)
                .filter(errorHandlingFilter())
                .build();
    }

    private <T> Mono<T> execute(String method, String path, String requestId,
            WebClient client,
            Function<WebClient, WebClient.ResponseSpec> toResponseSpec,
            Class<T> responseType) {
        String label = method + " " + path;
        return toResponseSpec.apply(client).bodyToMono(responseType)
                .timeout(TIMEOUT,
                        Mono.error(new ApiTimeoutException(label,
                                "Request timed out after " + TIMEOUT.getSeconds() + "s")))
                .doOnSubscribe(s -> log.debug("[{}] → {} {}", requestId, method, path))
                .doOnSuccess(r -> log.debug("[{}] ← {} {} success", requestId, method, path))
                .onErrorMap(WebClientRequestException.class, e -> {
                    log.error("Connection failure {} {}: {}", method, path, e.getMessage());
                    return new ApiConnectionException(label, e.getMessage(), e);
                })
                .onErrorMap(org.springframework.core.codec.DecodingException.class, e -> {
                    log.error("Incomplete/malformed response {} {}: {}", method, path, e.getMessage());
                    return new ApiIncompleteResponseException(label, e.getMessage(), e);
                })
                .doOnError(ApiException.class, e -> {
                    if (e.getStatus() >= 500) {
                        log.error("[{}] ← {} {} [{}]: {}", requestId, method, path, e.getStatus(), e.getMessage());
                    } else {
                        log.warn("[{}] ← {} {} [{}]: {}", requestId, method, path, e.getStatus(), e.getMessage());
                    }
                });
    }

    private <T> Flux<T> executeFlux(String method, String path, String requestId,
            WebClient client,
            Function<WebClient, WebClient.ResponseSpec> toResponseSpec,
            Class<T> responseType) {
        String label = method + " " + path;
        return toResponseSpec.apply(client).bodyToFlux(responseType)
                .timeout(TIMEOUT)
                .doOnSubscribe(s -> log.debug("[{}] → {} (flux) {}", requestId, method, path))
                .doOnComplete(() -> log.debug("[{}] ← {} (flux) {} complete", requestId, method, path))
                .onErrorMap(WebClientRequestException.class, e -> {
                    log.error("Connection failure {} {}: {}", method, path, e.getMessage());
                    return new ApiConnectionException(label, e.getMessage(), e);
                })
                .onErrorMap(org.springframework.core.codec.DecodingException.class, e -> {
                    log.error("Incomplete/malformed response {} {}: {}", method, path, e.getMessage());
                    return new ApiIncompleteResponseException(label, e.getMessage(), e);
                })
                .doOnError(ApiException.class, e -> {
                    if (e.getStatus() >= 500) {
                        log.error("[{}] ← {} {} [{}]: {}", requestId, method, path, e.getStatus(), e.getMessage());
                    } else {
                        log.warn("[{}] ← {} {} [{}]: {}", requestId, method, path, e.getStatus(), e.getMessage());
                    }
                });
    }

    private static ExchangeFilterFunction errorHandlingFilter() {
        return (request, next) -> next.exchange(request).flatMap(response -> {
            String label = request.method().name() + " " + request.url();
            int code = response.statusCode().value();
            switch (code) {
                case 400:
                    return response.bodyToMono(String.class)
                            .flatMap(b -> Mono.error(new ApiBadRequestException(label, b)));
                case 401:
                    return response.bodyToMono(String.class)
                            .flatMap(b -> Mono.error(new ApiUnauthorizedException(label, b)));
                case 403:
                    return response.bodyToMono(String.class)
                            .flatMap(b -> Mono.error(new ApiForbiddenException(label, b)));
                case 404:
                    return response.bodyToMono(String.class)
                            .flatMap(b -> Mono.error(new ApiNotFoundException(label, b)));
                case 408, 504:
                    return response.bodyToMono(String.class)
                            .flatMap(b -> Mono.error(new ApiTimeoutException(label, b)));
                case 503:
                    return response.bodyToMono(String.class)
                            .flatMap(b -> Mono.error(new ApiUnavailableException(label, b)));
            }
            if (response.statusCode().is5xxServerError())
                return response.bodyToMono(String.class)
                        .flatMap(b -> Mono.error(new ApiServerException(code, label, b)));
            return Mono.just(response);
        });
    }

    private static String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}

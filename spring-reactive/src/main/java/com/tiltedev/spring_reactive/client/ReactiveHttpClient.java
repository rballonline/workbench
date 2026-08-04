package com.tiltedev.spring_reactive.client;

import com.tiltedev.spring_reactive.config.HttpClientProperties;
import com.tiltedev.spring_reactive.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveHttpClient {

    private static final Set<String> IDEMPOTENT_METHODS = Set.of("GET", "PUT", "DELETE");

    private final HttpClientProperties properties;

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
        Duration timeout = properties.getTimeout();
        return toResponseSpec.apply(client).bodyToMono(responseType)
                .timeout(timeout,
                        Mono.error(new ApiTimeoutException(label,
                                "Request timed out after " + timeout.toMillis() + "ms")))
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
                })
                .retryWhen(retrySpec(method, path, requestId, () -> true));
    }

    private <T> Flux<T> executeFlux(String method, String path, String requestId,
            WebClient client,
            Function<WebClient, WebClient.ResponseSpec> toResponseSpec,
            Class<T> responseType) {
        String label = method + " " + path;
        Duration timeout = properties.getTimeout();
        return Flux.defer(() -> {
            // A retry resubscribes and replays the stream from the start, so once an
            // element
            // has reached the caller another attempt would deliver duplicates. Stop there.
            AtomicBoolean emitted = new AtomicBoolean();
            return toResponseSpec.apply(client).bodyToFlux(responseType)
                    .timeout(timeout,
                            Flux.error(new ApiTimeoutException(label,
                                    "Request timed out after " + timeout.toMillis() + "ms")))
                    .doOnSubscribe(s -> log.debug("[{}] → {} (flux) {}", requestId, method, path))
                    .doOnNext(v -> emitted.set(true))
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
                    })
                    .retryWhen(retrySpec(method, path, requestId, () -> !emitted.get()));
        });
    }

    /**
     * Exponential backoff over transient failures only. The retried source still
     * carries its
     * own timeout, so every attempt gets the full per-attempt budget. Once the
     * retries run out
     * the original exception is thrown rather than Reactor's wrapper, so callers
     * and
     * {@code GlobalExceptionHandler} keep seeing the typed {@code ApiException}.
     */
    private Retry retrySpec(String method, String path, String requestId, BooleanSupplier stillRetryable) {
        HttpClientProperties.RetryProperties cfg = properties.getRetry();
        int maxRetries = Math.max(0, cfg.getMaxRetries());
        return Retry.backoff(maxRetries, cfg.getInitialBackoff())
                .maxBackoff(cfg.getMaxBackoff())
                .jitter(cfg.getJitter())
                .filter(t -> cfg.isEnabled()
                        && isMethodRetryable(method, cfg)
                        && stillRetryable.getAsBoolean()
                        && isTransient(t))
                .doBeforeRetry(signal -> log.warn("[{}] ↻ {} {} retry {}/{}: {}",
                        requestId, method, path, signal.totalRetries() + 1, maxRetries,
                        signal.failure().getMessage()))
                .onRetryExhaustedThrow((spec, signal) -> signal.failure());
    }

    private static boolean isMethodRetryable(String method, HttpClientProperties.RetryProperties cfg) {
        return !cfg.isIdempotentOnly() || IDEMPOTENT_METHODS.contains(method);
    }

    /**
     * Only failures a later attempt could plausibly survive: the connection never
     * landed, the
     * call timed out, or the far side reported a server-side problem. 4xx responses
     * and
     * malformed bodies are deterministic — retrying them just burns the budget.
     */
    private static boolean isTransient(Throwable t) {
        if (t instanceof ApiConnectionException) {
            return true;
        }
        return t instanceof ApiException api && (api.getStatus() >= 500 || api.getStatus() == 408);
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

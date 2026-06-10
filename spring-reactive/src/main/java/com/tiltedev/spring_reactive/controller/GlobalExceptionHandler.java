package com.tiltedev.spring_reactive.controller;

import com.tiltedev.spring_reactive.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ProblemDetail> handleValidation(WebExchangeBindException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        f -> f.getField(),
                        f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "invalid",
                        (a, b) -> a
                ));
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        detail.setProperty("errors", fieldErrors);
        return Mono.just(detail);
    }

    @ExceptionHandler(ApiNotFoundException.class)
    public Mono<ProblemDetail> handleNotFound(ApiNotFoundException ex) {
        return Mono.just(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(ApiUnauthorizedException.class)
    public Mono<ProblemDetail> handleUnauthorized(ApiUnauthorizedException ex) {
        return Mono.just(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(ApiForbiddenException.class)
    public Mono<ProblemDetail> handleForbidden(ApiForbiddenException ex) {
        return Mono.just(ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage()));
    }

    @ExceptionHandler(ApiTimeoutException.class)
    public Mono<ProblemDetail> handleTimeout(ApiTimeoutException ex) {
        log.error("Upstream timeout: {}", ex.getMessage());
        return Mono.just(ProblemDetail.forStatusAndDetail(HttpStatus.GATEWAY_TIMEOUT, "Upstream request timed out"));
    }

    @ExceptionHandler({ApiConnectionException.class, ApiUnavailableException.class, ApiIncompleteResponseException.class})
    public Mono<ProblemDetail> handleUpstreamFailure(RuntimeException ex) {
        log.error("Upstream failure: {}", ex.getMessage());
        return Mono.just(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "Upstream service unavailable"));
    }

    @ExceptionHandler(ApiServerException.class)
    public Mono<ProblemDetail> handleApiServer(ApiServerException ex) {
        log.error("Upstream server error [{}]: {}", ex.getStatus(), ex.getMessage());
        return Mono.just(ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Upstream server error"));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ProblemDetail> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return Mono.just(ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
    }
}

package com.tiltedev.spring_reactive.service;

import com.tiltedev.spring_reactive.client.ReactiveHttpClient;
import org.springframework.web.reactive.function.client.WebClient;
import com.tiltedev.spring_reactive.dto.response.IssResponse;
import com.tiltedev.spring_reactive.dto.result.IssApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssService {

    private final ReactiveHttpClient httpClient;
    private final WebClient issWebClient;

    public Mono<IssResponse> getCurrentPosition() {
        return fetchPosition();
    }

    public Flux<IssResponse> liveStream() {
        return Flux.interval(Duration.ofSeconds(5))
                .flatMap(tick -> fetchPosition()
                        .onErrorResume(e -> {
                            log.warn("ISS position fetch failed on tick {}: {}", tick, e.getMessage());
                            return Mono.empty();
                        })
                );
    }

    private Mono<IssResponse> fetchPosition() {
        return httpClient.get(issWebClient, "/iss-now.json", IssApiResult.class, uri -> {})
                .map(result -> IssResponse.builder()
                        .latitude(Double.parseDouble(result.getIssPosition().getLatitude()))
                        .longitude(Double.parseDouble(result.getIssPosition().getLongitude()))
                        .timestamp(result.getTimestamp())
                        .build()
                );
    }
}

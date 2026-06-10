package com.tiltedev.spring_reactive.service;

import com.tiltedev.spring_reactive.client.ReactiveHttpClient;
import org.springframework.web.reactive.function.client.WebClient;
import com.tiltedev.spring_reactive.dto.result.CountryApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryApiService {

    private final ReactiveHttpClient httpClient;
    private final WebClient countriesWebClient;

    public Mono<CountryApiResult> fetchByCode(String alpha2Code) {
        log.debug("Fetching country by code: {}", alpha2Code);
        return httpClient.getFlux(countriesWebClient, "/v3.1/alpha/" + alpha2Code, CountryApiResult.class, uri -> {})
                .next();
    }

    public Mono<CountryApiResult> fetchByName(String name) {
        log.debug("Fetching country by name: {}", name);
        return httpClient.getFlux(countriesWebClient, "/v3.1/name/" + name, CountryApiResult.class, uri -> {})
                .next();
    }
}

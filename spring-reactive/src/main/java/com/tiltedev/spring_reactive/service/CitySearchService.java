package com.tiltedev.spring_reactive.service;

import com.tiltedev.spring_reactive.client.ReactiveHttpClient;
import org.springframework.web.reactive.function.client.WebClient;
import com.tiltedev.spring_reactive.dto.response.CitySearchResponse;
import com.tiltedev.spring_reactive.dto.result.GeocodingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitySearchService {

    private final ReactiveHttpClient httpClient;
    private final WebClient geocodingWebClient;
    private final CountryApiService countryApiService;

    public Flux<CitySearchResponse> search(String query) {
        log.debug("Searching cities for: {}", query);
        return httpClient.get(geocodingWebClient, "/v1/search", GeocodingResult.class,
                        uri -> uri.queryParam("name", query)
                                  .queryParam("count", 5)
                                  .queryParam("language", "en")
                                  .queryParam("format", "json"))
                .flatMapMany(result -> {
                    if (result.getResults() == null || result.getResults().isEmpty()) {
                        return Flux.empty();
                    }
                    return Flux.fromIterable(result.getResults());
                })
                .flatMap(entry ->
                        countryApiService.fetchByCode(entry.getCountry_code())
                                .map(country -> CitySearchResponse.builder()
                                        .cityName(entry.getName())
                                        .latitude(entry.getLatitude())
                                        .longitude(entry.getLongitude())
                                        .countryCode(entry.getCountry_code().toUpperCase())
                                        .countryName(country.getCommonName())
                                        .capital(country.getCapitalCity())
                                        .region(country.getRegion())
                                        .flagUrl(country.getFlags() != null ? country.getFlags().getPng() : null)
                                        .build())
                                .onErrorResume(e -> {
                                    log.warn("Could not enrich country {} for {}: {}", entry.getCountry_code(), entry.getName(), e.getMessage());
                                    return Mono.just(CitySearchResponse.builder()
                                            .cityName(entry.getName())
                                            .latitude(entry.getLatitude())
                                            .longitude(entry.getLongitude())
                                            .countryCode(entry.getCountry_code() != null ? entry.getCountry_code().toUpperCase() : null)
                                            .countryName(entry.getCountry())
                                            .build());
                                })
                );
    }
}

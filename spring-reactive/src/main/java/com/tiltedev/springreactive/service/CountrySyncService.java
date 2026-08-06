package com.tiltedev.spring_reactive.service;

import com.tiltedev.spring_reactive.model.Country;
import com.tiltedev.spring_reactive.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountrySyncService {

    private final CountryRepository countryRepository;
    private final CountryApiService countryApiService;

    public Mono<Country> ensureExists(String countryCode) {
        return countryRepository.findById(countryCode)
                .switchIfEmpty(
                        countryApiService.fetchByCode(countryCode)
                                .map(result -> Country.builder()
                                        .code(countryCode.toUpperCase())
                                        .name(result.getCommonName())
                                        .capital(result.getCapitalCity())
                                        .region(result.getRegion())
                                        .population(result.getPopulation())
                                        .flagUrl(result.getFlags() != null ? result.getFlags().getPng() : null)
                                        .build())
                                .flatMap(country -> {
                                    log.info("Synced country to DB: {}", country.getCode());
                                    return countryRepository.save(country);
                                })
                );
    }
}

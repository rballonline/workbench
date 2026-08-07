package com.tiltedev.springreactive.service;

import com.tiltedev.springreactive.model.Country;
import com.tiltedev.springreactive.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountrySyncService {

  private final CountryRepository countryRepository;
  private final CountryApiService countryApiService;
  private final R2dbcEntityTemplate template;

  public Mono<Country> ensureExists(String countryCode) {
    return countryRepository
        .findById(countryCode)
        .switchIfEmpty(
            countryApiService
                .fetchByCode(countryCode)
                .map(
                    result ->
                        Country.builder()
                            .code(countryCode.toUpperCase())
                            .name(result.getCommonName())
                            .capital(result.getCapitalCity())
                            .region(result.getRegion())
                            .population(result.getPopulation())
                            .flagUrl(result.getFlags() != null ? result.getFlags().getPng() : null)
                            .build())
                .flatMap(
                    country -> {
                      log.info("Synced country to DB: {}", country.getCode());
                      return template.insert(Country.class).using(country);
                    }));
  }
}

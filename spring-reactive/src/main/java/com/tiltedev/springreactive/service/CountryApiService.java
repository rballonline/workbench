package com.tiltedev.springreactive.service;

import com.tiltedev.springreactive.dto.result.CountryApiResult;
import com.tiltedev.springreactive.dto.result.CountryApiV5Response;
import com.tiltedev.springreactive.exception.ApiNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryApiService {

  private static final String PATH = "/countries/v5";

  private final ReactiveHttpClient httpClient;
  private final WebClient countriesWebClient;

  public Mono<CountryApiResult> fetchByCode(String alpha2Code) {
    log.debug("Fetching country by code: {}", alpha2Code);
    return httpClient
        .get(
            countriesWebClient,
            PATH,
            CountryApiV5Response.class,
            uri -> uri.queryParam("codes.alpha_2", alpha2Code))
        .flatMap(response -> firstResult(PATH, response));
  }

  public Mono<CountryApiResult> fetchByName(String name) {
    log.debug("Fetching country by name: {}", name);
    return httpClient
        .get(countriesWebClient, PATH, CountryApiV5Response.class, uri -> uri.queryParam("q", name))
        .flatMap(response -> firstResult(PATH, response));
  }

  private static Mono<CountryApiResult> firstResult(String path, CountryApiV5Response response) {
    var objects = response.getData() != null ? response.getData().getObjects() : null;
    if (objects == null || objects.isEmpty()) {
      return Mono.error(new ApiNotFoundException(path, "No country found"));
    }
    return Mono.just(objects.get(0));
  }
}

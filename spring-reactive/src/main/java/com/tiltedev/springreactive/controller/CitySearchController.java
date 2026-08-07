package com.tiltedev.springreactive.controller;

import com.tiltedev.springreactive.dto.request.CitySearchRequest;
import com.tiltedev.springreactive.dto.response.CitySearchResponse;
import com.tiltedev.springreactive.service.CitySearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CitySearchController {

  private final CitySearchService service;

  @GetMapping("/search")
  public Flux<CitySearchResponse> search(@ModelAttribute @Valid CitySearchRequest request) {
    return service.search(request.getQ());
  }
}

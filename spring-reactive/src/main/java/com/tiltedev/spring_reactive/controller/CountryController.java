package com.tiltedev.spring_reactive.controller;

import com.tiltedev.spring_reactive.dto.result.CountryApiResult;
import com.tiltedev.spring_reactive.service.CountryApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryApiService service;

    @GetMapping("/{name}")
    public Mono<CountryApiResult> getByName(@PathVariable String name) {
        return service.fetchByName(name);
    }
}

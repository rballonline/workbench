package com.tiltedev.spring_reactive.controller;

import com.tiltedev.spring_reactive.dto.request.AddDestinationRequest;
import com.tiltedev.spring_reactive.dto.response.DestinationResponse;
import com.tiltedev.spring_reactive.service.DestinationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService service;

    @GetMapping
    public Flux<DestinationResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Mono<DestinationResponse> findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DestinationResponse> create(@RequestBody @Valid AddDestinationRequest request) {
        return service.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}

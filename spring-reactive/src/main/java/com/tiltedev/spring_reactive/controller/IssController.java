package com.tiltedev.spring_reactive.controller;

import com.tiltedev.spring_reactive.dto.response.IssResponse;
import com.tiltedev.spring_reactive.service.IssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/iss")
@RequiredArgsConstructor
public class IssController {

    private final IssService service;

    @GetMapping
    public Mono<IssResponse> getCurrentPosition() {
        return service.getCurrentPosition();
    }
}

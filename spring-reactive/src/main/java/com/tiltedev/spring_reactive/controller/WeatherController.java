package com.tiltedev.spring_reactive.controller;

import com.tiltedev.spring_reactive.dto.response.WeatherResponse;
import com.tiltedev.spring_reactive.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService service;

    @GetMapping("/city/{cityName}")
    public Mono<WeatherResponse> getWeatherByCity(@PathVariable String cityName) {
        return service.getWeatherByCity(cityName);
    }
}

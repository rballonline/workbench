package com.tiltedev.springreactive.controller;

import com.tiltedev.springreactive.dto.response.WeatherResponse;
import com.tiltedev.springreactive.service.WeatherService;
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

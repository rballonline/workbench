package com.tiltedev.springreactive.service;

import com.tiltedev.springreactive.dto.response.WeatherResponse;
import com.tiltedev.springreactive.dto.result.ForecastResult;
import com.tiltedev.springreactive.dto.result.GeocodingResult;
import com.tiltedev.springreactive.exception.ApiNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

  private final ReactiveHttpClient httpClient;
  private final WebClient geocodingWebClient;
  private final WebClient forecastWebClient;

  public Mono<WeatherResponse> getWeatherByCity(String cityName) {
    return geocodeCity(cityName)
        .flatMap(
            entry ->
                fetchForecast(entry.getLatitude(), entry.getLongitude())
                    .map(
                        forecast ->
                            WeatherResponse.builder()
                                .cityName(entry.getName())
                                .countryName(entry.getCountry())
                                .latitude(entry.getLatitude())
                                .longitude(entry.getLongitude())
                                .temperatureCelsius(forecast.getCurrent().getTemperature())
                                .windSpeedKmh(forecast.getCurrent().getWindSpeed())
                                .humidity(forecast.getCurrent().getHumidity())
                                .weatherCode(forecast.getCurrent().getWeatherCode())
                                .build()));
  }

  private Mono<GeocodingResult.GeocodingEntry> geocodeCity(String cityName) {
    return httpClient
        .get(
            geocodingWebClient,
            "/v1/search",
            GeocodingResult.class,
            uri ->
                uri.queryParam("name", cityName)
                    .queryParam("count", 1)
                    .queryParam("language", "en")
                    .queryParam("format", "json"))
        .flatMap(
            result -> {
              if (result.getResults() == null || result.getResults().isEmpty()) {
                return Mono.error(
                    new ApiNotFoundException("/v1/search", "City not found: " + cityName));
              }
              return Mono.just(result.getResults().get(0));
            });
  }

  private Mono<ForecastResult> fetchForecast(double latitude, double longitude) {
    return httpClient.get(
        forecastWebClient,
        "/v1/forecast",
        ForecastResult.class,
        uri ->
            uri.queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam(
                    "current", "temperature_2m,wind_speed_10m,relative_humidity_2m,weather_code"));
  }
}

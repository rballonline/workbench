package com.tiltedev.springreactive.mcp;

import com.tiltedev.springreactive.dto.request.AddDestinationRequest;
import com.tiltedev.springreactive.dto.response.CitySearchResponse;
import com.tiltedev.springreactive.dto.response.DestinationResponse;
import com.tiltedev.springreactive.dto.response.IssResponse;
import com.tiltedev.springreactive.dto.response.WeatherResponse;
import com.tiltedev.springreactive.dto.result.CountryApiResult;
import com.tiltedev.springreactive.service.CitySearchService;
import com.tiltedev.springreactive.service.CountryApiService;
import com.tiltedev.springreactive.service.DestinationService;
import com.tiltedev.springreactive.service.IssService;
import com.tiltedev.springreactive.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TravelMcpTools {

  private final CitySearchService citySearchService;
  private final WeatherService weatherService;
  private final CountryApiService countryApiService;
  private final IssService issService;
  private final DestinationService destinationService;

  @McpTool(
      name = "search_cities",
      description = "Search for cities by name, enriched with country info")
  public Flux<CitySearchResponse> searchCities(
      @McpToolParam(description = "City name to search for", required = true) String query) {
    return citySearchService.search(query);
  }

  @McpTool(name = "get_weather", description = "Get current weather for a city by name")
  public Mono<WeatherResponse> getWeather(
      @McpToolParam(description = "City name", required = true) String cityName) {
    return weatherService.getWeatherByCity(cityName);
  }

  @McpTool(name = "get_country", description = "Get country details by name")
  public Mono<CountryApiResult> getCountry(
      @McpToolParam(description = "Country name", required = true) String name) {
    return countryApiService.fetchByName(name);
  }

  @McpTool(
      name = "get_iss_position",
      description = "Get the current position of the International Space Station")
  public Mono<IssResponse> getIssPosition() {
    return issService.getCurrentPosition();
  }

  @McpTool(
      name = "list_destinations",
      description = "List every destination on the shared travel wishlist")
  public Flux<DestinationResponse> listDestinations() {
    return destinationService.findAll();
  }

  @McpTool(name = "add_destination", description = "Add a city to the shared travel wishlist")
  public Mono<DestinationResponse> addDestination(
      @McpToolParam(description = "City name", required = true) String cityName,
      @McpToolParam(description = "ISO country code", required = true) String countryCode,
      @McpToolParam(description = "Latitude", required = true) Double latitude,
      @McpToolParam(description = "Longitude", required = true) Double longitude,
      @McpToolParam(description = "Name of the person adding the destination", required = false)
          String addedBy) {
    var request = new AddDestinationRequest();
    request.setCityName(cityName);
    request.setCountryCode(countryCode);
    request.setLatitude(latitude);
    request.setLongitude(longitude);
    request.setAddedBy(addedBy);
    return destinationService.create(request);
  }

  @McpTool(
      name = "remove_destination",
      description = "Remove a destination from the shared travel wishlist")
  public Mono<Void> removeDestination(
      @McpToolParam(description = "Destination id", required = true) Long id) {
    return destinationService.delete(id);
  }
}

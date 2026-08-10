package com.tiltedev.springreactive.ai;

import com.tiltedev.springreactive.dto.event.PendingDeleteConfirmation;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tool-calling entry points for the in-app AI assistant, invoked synchronously by Spring AI's
 * {@code ChatClient} (via reflection, dispatched on {@code Schedulers.boundedElastic()} - see
 * {@code ToolCallingAdvisor}). {@code @Tool} methods must return resolved values, not Mono/Flux, so
 * these block on the same service calls {@code mcp.TravelMcpTools} exposes reactively to the
 * external MCP server.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssistantTools {

  public static final String PENDING_DELETE_CONFIRMATIONS_KEY = "pendingDeleteConfirmations";

  /**
   * Anthropic resumes generation with a fresh API turn after a tool call, and the resumed text
   * sometimes drops the space that would normally join it to the sentence fragment streamed
   * before the call. {@link com.tiltedev.springreactive.service.AiChatService} watches this flag
   * to know when a tool ran since the last streamed chunk, so it can repair the seam.
   */
  public static final String TOOL_INVOKED_KEY = "toolInvoked";

  private final CitySearchService citySearchService;
  private final WeatherService weatherService;
  private final CountryApiService countryApiService;
  private final IssService issService;
  private final DestinationService destinationService;

  @Tool(
      name = "search_cities",
      description = "Search for cities by name, enriched with country info")
  public List<CitySearchResponse> searchCities(
      @ToolParam(description = "City name to search for", required = true) String query,
      ToolContext toolContext) {
    markInvoked(toolContext);
    return citySearchService.search(query).collectList().block();
  }

  @Tool(name = "get_weather", description = "Get current weather for a city by name")
  public WeatherResponse getWeather(
      @ToolParam(description = "City name", required = true) String cityName,
      ToolContext toolContext) {
    markInvoked(toolContext);
    return weatherService.getWeatherByCity(cityName).block();
  }

  @Tool(name = "get_country", description = "Get country details by name")
  public CountryApiResult getCountry(
      @ToolParam(description = "Country name", required = true) String name,
      ToolContext toolContext) {
    markInvoked(toolContext);
    return countryApiService.fetchByName(name).block();
  }

  @Tool(
      name = "get_iss_position",
      description = "Get the current position of the International Space Station")
  public IssResponse getIssPosition(ToolContext toolContext) {
    markInvoked(toolContext);
    return issService.getCurrentPosition().block();
  }

  @Tool(
      name = "list_destinations",
      description = "List every destination on the shared travel wishlist")
  public List<DestinationResponse> listDestinations(ToolContext toolContext) {
    markInvoked(toolContext);
    return destinationService.findAll().collectList().block();
  }

  @Tool(name = "add_destination", description = "Add a city to the shared travel wishlist")
  public DestinationResponse addDestination(
      @ToolParam(description = "City name", required = true) String cityName,
      @ToolParam(description = "ISO country code", required = true) String countryCode,
      @ToolParam(description = "Latitude", required = true) Double latitude,
      @ToolParam(description = "Longitude", required = true) Double longitude,
      @ToolParam(description = "Name of the person adding the destination", required = false)
          String addedBy,
      ToolContext toolContext) {
    markInvoked(toolContext);
    var request = new AddDestinationRequest();
    request.setCityName(cityName);
    request.setCountryCode(countryCode);
    request.setLatitude(latitude);
    request.setLongitude(longitude);
    request.setAddedBy(addedBy);
    return destinationService.create(request).block();
  }

  @Tool(
      name = "propose_remove_destination",
      description =
          "Flag a destination for removal from the shared wishlist. This does NOT delete it -"
              + " the user must confirm the removal in the UI before it actually happens."
              + " Always use this instead of assuming a destination was removed.")
  public String proposeRemoveDestination(
      @ToolParam(description = "Destination id", required = true) Long id,
      ToolContext toolContext) {
    markInvoked(toolContext);
    DestinationResponse destination;
    try {
      destination = destinationService.findById(id).block();
    } catch (RuntimeException e) {
      log.warn("propose_remove_destination: no destination with id {}", id);
      return "No destination found with id " + id + ". Check the id or list destinations first.";
    }
    pendingConfirmations(toolContext)
        .add(new PendingDeleteConfirmation(id, destination.getCityName()));
    return "Flagged '"
        + destination.getCityName()
        + "' (id="
        + id
        + ") for removal, pending user confirmation in the UI. Do not tell the user it has been"
        + " removed yet - only their confirmation click actually deletes it.";
  }

  @SuppressWarnings("unchecked")
  private List<PendingDeleteConfirmation> pendingConfirmations(ToolContext toolContext) {
    return (List<PendingDeleteConfirmation>)
        toolContext.getContext().get(PENDING_DELETE_CONFIRMATIONS_KEY);
  }

  private void markInvoked(ToolContext toolContext) {
    ((AtomicBoolean) toolContext.getContext().get(TOOL_INVOKED_KEY)).set(true);
  }
}

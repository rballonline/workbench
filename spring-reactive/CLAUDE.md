# spring-reactive - "Places I'd Like to Visit"

A collaborative travel wishlist. Users search for cities, pick one, and add it to a shared list. All connected users see new destinations appear in real time via WebSocket.

## Tech Stack

- **Java 21**, **Spring Boot 4.0.6**, **Gradle 9.4.1**
- **Spring WebFlux** - reactive HTTP (no blocking Servlet API)
- **R2DBC + MySQL** - reactive database; Flyway handles schema migrations via JDBC at startup
- **Spring WebSocket** - real-time push to all clients via `Sinks.Many`
- **Spring for GraphQL** - query/mutation API alongside REST; GraphiQL at `/graphiql`
- **Spring Security OAuth2 Client** - M2M client credentials for outbound authenticated calls
- **Lombok** - `@Data`, `@Builder`, `@Slf4j` on all components
- **SpringDoc OpenAPI** - Swagger UI at `/swagger-ui.html`, spec at `/v3/api-docs`
- **Spring AI MCP Server (WebFlux, ASYNC)** - exposes app features as MCP tools at `/mcp`
- **Spring AI Anthropic + ChatClient** - powers the in-app AI assistant at `POST /api/assistant/chat`

## Code Standards

- Don't add comments unless there is something that needs further explanation as to why something was done. Stay as terse as possible.
- Use var when allowed
- Don't use special characters in the code. For example: ← should just be <-, - would just be -.

## Package Structure

```
com.tiltedev.springreactive/
├── config/         WebSocketConfig, WebClientConfig, SecurityConfig, OpenApiConfig
├── controller/     REST controllers + GlobalExceptionHandler + RequestLoggingFilter
├── graphql/        GraphQL controller (@QueryMapping, @MutationMapping)
├── mcp/            TravelMcpTools - wraps services as MCP tools (@McpTool)
├── websocket/      LiveUpdateWebSocketHandler
├── service/        Business logic + external API services + ReactiveHttpClient (only place that calls WebClient)
├── repository/     DestinationRepository, CountryRepository (R2DBC)
├── model/          Destination, Country (R2DBC entities)
├── dto/
│   ├── request/    *Request - validated inbound objects from clients
│   ├── response/   *Response - outbound objects to clients
│   ├── projection/ DestinationWithCountry - internal DB join result
│   ├── result/     *Result - raw external API response shapes
│   └── event/      DestinationEvent - WebSocket broadcast payload
└── exception/      ApiException hierarchy (9 typed exceptions)
```

## Database

Flyway migrations in `src/main/resources/db/migration/`:

## External APIs (all free, no key required)

| API                  | Base URL                               | Used by                                                        |
| -------------------- | -------------------------------------- | -------------------------------------------------------------- |
| Open-Meteo Geocoding | `https://geocoding-api.open-meteo.com` | `CitySearchService`, `WeatherService`                          |
| Open-Meteo Forecast  | `https://api.open-meteo.com`           | `WeatherService`                                               |
| REST Countries       | `https://restcountries.com`            | `CitySearchService`, `CountryApiService`, `CountrySyncService` |
| Open Notify (ISS)    | `http://api.open-notify.org`           | `IssService`                                                   |

### Sequential API call patterns

**City search** (`CitySearchService.search`) - two different external APIs chained:

```
geocode(query)              → Open-Meteo: city name → {lat, lon, countryCode}
  .flatMap(entry ->
    fetchCountry(countryCode))  → REST Countries: countryCode → {flag, capital, region}
```

**Weather** (`WeatherService.getWeatherByCity`) - same external API called twice:

```
geocodeCity(name)           → Open-Meteo geocoding: city name → {lat, lon}
  .flatMap(coords ->
    fetchForecast(coords))  → Open-Meteo forecast: {lat, lon} → current weather
```

## WebSocket

**Endpoint:** `ws://localhost:8080/ws/destinations`

Each connected session receives a merged stream of:

1. **CRUD events** - emitted by `DestinationService` via `Sinks.Many<DestinationEvent>` on every create/delete
2. **ISS position** - polled from Open Notify on an interval configured via `app.iss.poll-interval` (`application.yml`, default 30s)

Event JSON shape:

```json
{ "action": "CREATED", "destination": { "id": 1, "cityName": "Tokyo", ... } }
```

The `Sinks.Many` bean is declared in `WebSocketConfig` and injected into both `DestinationService` (publisher) and `LiveUpdateWebSocketHandler` (subscriber). Controllers have no knowledge of WebSocket.

## GraphQL

GraphiQL: http://localhost:8080/graphiql

Queries: `destinations`, `destination(id)`
Mutations: `addDestination(cityName, countryCode, latitude, longitude, addedBy)`, `removeDestination(id)`

## MCP Server

`mcp/TravelMcpTools` wraps existing services (not controllers) as `@McpTool` methods, so an MCP client (e.g. Claude) can drive the app directly. Server type is `ASYNC`, so tools return `Mono`/`Flux` - configured via `spring.ai.mcp.server.*` in `application.yml`.

Tools: `search_cities`, `get_weather`, `get_country`, `get_iss_position`, `list_destinations`, `add_destination`, `remove_destination`.

Endpoint: `http://localhost:8080/mcp` (streamable HTTP transport).

## AI Assistant

`POST /api/assistant/chat` - `{ conversationId, message, apiKey }` in, `text/event-stream` out.
Backs the chat widget in vue-app. **Bring-your-own-key**: there is no server-side default
Anthropic key. `apiKey` comes from the caller's browser (`useUserStore.aiApiKey` on the frontend)
and is required on every request (`@NotBlank`) - the app has no auth/sessions to hang a
per-user server-stored key off of, so this follows the same pattern as `addedBy`: a
client-remembered value, not an account. `AnthropicChatAutoConfiguration` is excluded via
`spring.autoconfigure.exclude` in `application.yml` so no dangling keyless `AnthropicChatModel`
bean gets created at startup.

- `ai/AssistantTools.java` - `@Tool`-annotated methods Spring AI's `ChatClient` calls directly.
  These are **separate, blocking wrappers** around the same services `mcp/TravelMcpTools` wraps
  reactively - `ChatClient` tool calling invokes methods synchronously via reflection (dispatched
  on `Schedulers.boundedElastic()`, confirmed in `ToolCallingAdvisor`), so a method returning
  `Mono`/`Flux` would have the publisher itself serialized as the tool result instead of its
  resolved value. Each tool here therefore ends in `.block()`.
- `remove_destination` is deliberately **not** a directly-callable tool here (unlike the MCP
  server, which does expose a real one for trusted external clients). `propose_remove_destination`
  only looks up the destination and stashes a `PendingDeleteConfirmation` onto a `ToolContext`
  map keyed by `AssistantTools.PENDING_DELETE_CONFIRMATIONS_KEY` - `AiChatService` reads that
  list back after the model's turn and emits it as a `confirm-delete` SSE event. The frontend
  renders a confirm button that calls `DELETE /api/destinations/{id}` directly; the AI never
  performs the delete itself.
- `ai/AssistantChatClientFactory.java` - builds a fresh `ChatClient` **per request**, not a
  singleton bean: `forApiKey(String)` constructs `AnthropicChatOptions` + `AnthropicChatModel`
  straight from the caller's key (`ChatClient.builder(chatModel)` - no dependency on any
  autoconfigured `ChatClient.Builder`), then wires in the system prompt, `AssistantTools`, and a
  `MessageChatMemoryAdvisor` (using the `ChatMemory` bean auto-configured by
  `spring-ai-autoconfigure-model-chat-memory`, in-process/non-persistent, unrelated to the
  per-request model).
- `service/AiChatService.java` - streams `chatClient.prompt()...stream().content()` as `event:
  token` SSE frames, appends any pending delete confirmations as `event: confirm-delete` frames
  (JSON `PendingDeleteConfirmation`), and wraps the whole thing in `onErrorResume` to emit an
  `event: error` frame instead of letting an exception hit the stream after headers are sent
  (`GlobalExceptionHandler`'s `ProblemDetail` responses don't fit mid-stream). A bad/expired key
  surfaces this way too - Anthropic's 401 becomes the same generic error frame, not a distinct
  message; that's a reasonable place to improve later.
- `ChatRequest.apiKey` is `@ToString.Exclude` so it can never leak into a stray `log.debug("{}",
  request)`-style call; `RequestLoggingFilter` only ever logs method/URI/status, never bodies.

## OpenAPI / Swagger

Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI spec: http://localhost:8080/v3/api-docs

Metadata (title/description) is set in `config/OpenApiConfig`. Controllers need no extra annotations to show up - springdoc scans `@RestController` beans automatically.

## HTTP Client - ReactiveHttpClient

`service/ReactiveHttpClient` is the **only** place that calls `WebClient.get/post/put/delete`. All services inject and use it. It provides:

- Per-call `DEBUG` request/response logging with timing
- `WARN` on 4xx, `ERROR` on 5xx and connection failures
- Typed exception mapping per status code (see exception hierarchy below)
- 10-second timeout per request

```bash
./gradlew bootRun
```

Requires a local MySQL instance with a `spring_reactive` database. Flyway creates the schema on first start.

## Logging

When run with the `local` Spring profile (VS Code launch configs default to this), logs are also written to `logs/spring-reactive.log` alongside the console. If asked to check "spring logs" or investigate a backend error/request-id, read that file directly instead of asking for pasted log output. This only applies locally - the `docker`/hosted profiles don't write a log file.

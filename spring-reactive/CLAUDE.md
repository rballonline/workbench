# spring-reactive — "Places I'd Like to Visit"

A collaborative travel wishlist. Users search for cities, pick one, and add it to a shared list. All connected users see new destinations appear in real time via WebSocket.

## Tech Stack

- **Java 21**, **Spring Boot 4.0.6**, **Gradle 9.4.1**
- **Spring WebFlux** — reactive HTTP (no blocking Servlet API)
- **R2DBC + MySQL** — reactive database; Flyway handles schema migrations via JDBC at startup
- **Spring WebSocket** — real-time push to all clients via `Sinks.Many`
- **Spring for GraphQL** — query/mutation API alongside REST; GraphiQL at `/graphiql`
- **Spring Security OAuth2 Client** — M2M client credentials for outbound authenticated calls
- **Lombok** — `@Data`, `@Builder`, `@Slf4j` on all components
- **SpringDoc OpenAPI** — Swagger UI at `/swagger-ui.html`

## Package Structure

```
com.tiltedev.spring_reactive/
├── config/         WebSocketConfig, WebClientConfig, SecurityConfig
├── controller/     REST controllers + GlobalExceptionHandler
├── graphql/        GraphQL controller (@QueryMapping, @MutationMapping)
├── websocket/      LiveUpdateWebSocketHandler
├── service/        Business logic + external API services
├── client/         ReactiveHttpClient — only place that calls WebClient
├── repository/     DestinationRepository, CountryRepository (R2DBC)
├── model/          Destination, Country (R2DBC entities)
├── dto/
│   ├── request/    *Request — validated inbound objects from clients
│   ├── response/   *Response — outbound objects to clients
│   ├── projection/ DestinationWithCountry — internal DB join result
│   ├── result/     *Result — raw external API response shapes
│   └── event/      DestinationEvent — WebSocket broadcast payload
└── exception/      ApiException hierarchy (9 typed exceptions)
```

## Database

Flyway migrations in `src/main/resources/db/migration/`:
- `V1__create_countries.sql` — `countries` (code PK, name, capital, region, population, flag_url)
- `V2__create_destinations.sql` — `destinations` (FK → countries.code, city_name, lat, lon, added_by)

Multiple destinations share the same country row. Country data is fetched from REST Countries API and upserted on first use.

## External APIs (all free, no key required)

| API | Base URL | Used by |
|-----|----------|---------|
| Open-Meteo Geocoding | `https://geocoding-api.open-meteo.com` | `CitySearchService`, `WeatherService` |
| Open-Meteo Forecast | `https://api.open-meteo.com` | `WeatherService` |
| REST Countries | `https://restcountries.com` | `CitySearchService`, `CountryApiService`, `CountrySyncService` |
| Open Notify (ISS) | `http://api.open-notify.org` | `IssService` |

### Sequential API call patterns

**City search** (`CitySearchService.search`) — two different external APIs chained:
```
geocode(query)              → Open-Meteo: city name → {lat, lon, countryCode}
  .flatMap(entry ->
    fetchCountry(countryCode))  → REST Countries: countryCode → {flag, capital, region}
```

**Weather** (`WeatherService.getWeatherByCity`) — same external API called twice:
```
geocodeCity(name)           → Open-Meteo geocoding: city name → {lat, lon}
  .flatMap(coords ->
    fetchForecast(coords))  → Open-Meteo forecast: {lat, lon} → current weather
```

## WebSocket

**Endpoint:** `ws://localhost:8080/ws/destinations`

Each connected session receives a merged stream of:
1. **CRUD events** — emitted by `DestinationService` via `Sinks.Many<DestinationEvent>` on every create/delete
2. **ISS position** — polled every 5 seconds from Open Notify

Event JSON shape:
```json
{ "action": "CREATED", "destination": { "id": 1, "cityName": "Tokyo", ... } }
```

The `Sinks.Many` bean is declared in `WebSocketConfig` and injected into both `DestinationService` (publisher) and `LiveUpdateWebSocketHandler` (subscriber). Controllers have no knowledge of WebSocket.

## GraphQL

GraphiQL: http://localhost:8080/graphiql

Queries: `destinations`, `destination(id)`
Mutations: `addDestination(cityName, countryCode, latitude, longitude, addedBy)`, `removeDestination(id)`

## HTTP Client — ReactiveHttpClient

`client/ReactiveHttpClient` is the **only** place that calls `WebClient.get/post/put/delete`. All services inject and use it. It provides:
- Per-call `DEBUG` request/response logging with timing
- `WARN` on 4xx, `ERROR` on 5xx and connection failures
- Typed exception mapping per status code (see exception hierarchy below)
- 10-second timeout per request

## Exception Hierarchy

| Exception | HTTP Status | Trigger |
|-----------|-------------|---------|
| `ApiUnauthorizedException` | 401 | Bad/missing credentials on outbound call |
| `ApiForbiddenException` | 403 | Not allowed |
| `ApiNotFoundException` | 404 | Resource not found |
| `ApiBadRequestException` | 400 | Invalid data sent to external API |
| `ApiTimeoutException` | 408/504 | Timeout |
| `ApiUnavailableException` | 503 | Service down |
| `ApiServerException` | 5xx | Other server error |
| `ApiConnectionException` | — | Network failure (refused, DNS, SSL) |
| `ApiIncompleteResponseException` | — | Partial/malformed response body |

`GlobalExceptionHandler` maps each to the appropriate HTTP response. Clients never receive a stack trace.

## OAuth2 M2M

Configure `spring.security.oauth2.client.registration.my-api.*` in `application.properties` with your provider's `client-id`, `client-secret`, and `token-uri`. The `oAuth2WebClient` bean in `WebClientConfig` auto-fetches and caches tokens via client credentials grant.

## Running

```bash
./gradlew bootRun
```

Requires a local MySQL instance with a `spring_reactive` database. Flyway creates the schema on first start.

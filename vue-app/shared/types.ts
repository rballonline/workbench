/**
 * Mirrors the DTOs in `spring-reactive/src/main/java/com/tiltedev/spring_reactive/dto/`.
 * Keep these in sync by hand - there is no codegen step.
 */

/** `dto/response/CountryResponse` */
export interface Country {
  code: string
  name: string
  capital: string | null
  region: string | null
  population: number | null
  flagUrl: string | null
}

/** `dto/response/DestinationResponse` - GET /api/destinations */
export interface Destination {
  id: number
  cityName: string
  latitude: number
  longitude: number
  addedBy: string | null
  createdAt: string
  country: Country
}

/** `dto/request/AddDestinationRequest` - POST /api/destinations */
export interface AddDestinationRequest {
  cityName: string
  countryCode: string
  latitude: number
  longitude: number
  addedBy?: string
}

/** `dto/response/CitySearchResponse` - GET /api/cities/search?q= */
export interface CitySearchResult {
  cityName: string
  latitude: number
  longitude: number
  countryCode: string
  countryName: string | null
  capital: string | null
  region: string | null
  flagUrl: string | null
}

/** `dto/response/WeatherResponse` - GET /api/weather/city/{cityName} */
export interface Weather {
  cityName: string
  countryName: string | null
  latitude: number
  longitude: number
  temperatureCelsius: number | null
  windSpeedKmh: number | null
  humidity: number | null
  weatherCode: number | null
}

/** `dto/response/IssResponse` - GET /api/iss, and pushed over the WebSocket */
export interface IssPosition {
  latitude: number
  longitude: number
  /** Unix seconds, as reported by Open Notify. */
  timestamp: number
}

/**
 * `dto/result/CountryApiResult` - GET /api/countries/{name}.
 * This one is the raw REST Countries shape passed straight through by
 * `CountryController`, so it is shaped differently from `CountryResponse`.
 */
export interface CountryApiResult {
  name: Record<string, string> | null
  capital: string[] | null
  region: string | null
  population: number | null
  cca2: string[] | null
  flags: { png?: string } | null
}

/**
 * `dto/event/DestinationEvent` - WebSocket payload.
 * Note the nested object is the raw `model/Destination` entity, not the enriched
 * `DestinationResponse`: it carries `countryCode` but no nested `country`.
 */
export interface DestinationEvent {
  action: 'CREATED' | 'UPDATED' | 'DELETED'
  destination: {
    id: number
    cityName: string
    countryCode: string
    latitude: number
    longitude: number
    addedBy: string | null
    createdAt: string | null
    updatedAt: string | null
  }
}

/**
 * `/ws/destinations` merges two unrelated streams onto one socket, with no
 * envelope to tell them apart. Discriminate on the presence of `action`.
 */
export type LiveMessage = DestinationEvent | IssPosition

export function isDestinationEvent (msg: LiveMessage): msg is DestinationEvent {
  return typeof (msg as DestinationEvent).action === 'string'
}

/** RFC 7807 body produced by `GlobalExceptionHandler`. */
export interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
  errors?: Record<string, string>
}

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
 * The upstream provider's own nested fields (`names`, `capitals`, `codes`,
 * `flag`) reshape whenever they change API versions - bind to these flat
 * convenience fields instead, which `CountryApiResult` keeps stable across
 * that churn.
 */
export interface CountryApiResult {
  commonName: string | null
  capitalCity: string | null
  region: string | null
  population: number | null
  alpha2Code: string | null
  flagUrl: string | null
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

/**
 * `dto/request/ChatRequest` - POST /api/assistant/chat.
 * Bring-your-own-key: there is no server-side default, so `apiKey` is required on
 * every request. It comes from `useUserStore.aiApiKey` and never gets logged - see
 * `@ToString.Exclude` on the backend field.
 */
export interface ChatRequest {
  conversationId: string
  message: string
  apiKey: string
}

/**
 * `dto/response/ApiKeyValidationResponse` - POST /api/assistant/validate-key.
 * A bad key throws server-side (Anthropic's 401 flows through as an `ApiError` with
 * `status: 401`, same as any other upstream rejection) rather than coming back as a
 * `valid: false` field on this shape.
 */
export interface ApiKeyValidationResponse {
  validatedAt: string
}

/**
 * `dto/event/PendingDeleteConfirmation` - the `confirm-delete` SSE event emitted by
 * POST /api/assistant/chat. The assistant only ever proposes a removal; deleting for
 * real means calling `DELETE /api/destinations/{id}` directly, same as a manual edit.
 */
export interface PendingDeleteConfirmation {
  id: number
  cityName: string
}

/** One turn in the assistant chat widget. Not a backend DTO - assembled client-side from SSE frames. */
export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  pendingConfirms: PendingDeleteConfirmation[]
}

/** RFC 7807 body produced by `GlobalExceptionHandler`. */
export interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
  errors?: Record<string, string>
}

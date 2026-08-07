import type {
  AddDestinationRequest,
  ApiKeyValidationResponse,
  ChatRequest,
  CitySearchResult,
  CountryApiResult,
  Destination,
  IssPosition,
  ProblemDetail,
  Weather,
} from '@shared/types'
import { getAppContext } from './useAppContext'

/**
 * Thrown for any non-2xx response. `GlobalExceptionHandler` on the backend always
 * answers with an RFC 7807 body, so `detail` usually carries a usable message and
 * `fieldErrors` is populated for 400s from bean validation.
 */
export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors?: Record<string, string>

  constructor (
    status: number,
    message: string,
    fieldErrors?: Record<string, string>,
  ) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

/**
 * A failure reduced to something a page can render. Stores live outside any component
 * and so have no `t()`; they classify, and the page translates `kind` before falling
 * back to `detail`, which is always technical English straight from the backend.
 */
export interface ApiFailure {
  kind: 'unreachable' | 'notFound' | 'validation' | 'other'
  detail: string
}

export function classifyError (error: unknown): ApiFailure {
  if (error instanceof ApiError) {
    if (error.status === 0) {
      return { kind: 'unreachable', detail: error.message }
    }
    if (error.status === 404) {
      return { kind: 'notFound', detail: error.message }
    }
    if (error.fieldErrors) {
      return {
        kind: 'validation',
        detail: Object.entries(error.fieldErrors)
          .map(([field, message]) => `${field}: ${message}`)
          .join(', '),
      }
    }
    return { kind: 'other', detail: error.message }
  }
  return {
    kind: 'other',
    detail: error instanceof Error ? error.message : String(error),
  }
}

export function useApi () {
  const { apiBase } = getAppContext()

  async function apiFetch (path: string, opts?: RequestInit): Promise<Response> {
    let res: Response
    try {
      res = await fetch(`${apiBase}${path}`, {
        ...opts,
        headers: {
          'Content-Type': 'application/json',
          ...opts?.headers,
        },
      })
    } catch (error) {
      // fetch only rejects on network-level failure; surface it as a 0 so callers
      // can treat "backend not running" the same as any other API error.
      throw new ApiError(
        0,
        error instanceof Error ? error.message : 'Network request failed',
      )
    }

    if (!res.ok) {
      const problem = (await res
        .json()
        .catch(() => null)) as ProblemDetail | null

      // `GlobalExceptionHandler` answers every error with a ProblemDetail body, so a
      // 5xx carrying no JSON did not come from the app - it is the dev proxy (or a
      // gateway) reporting that it could not reach it. Report that as unreachable,
      // which is the actionable message.
      if (!problem && res.status >= 500) {
        throw new ApiError(
          0,
          `HTTP ${res.status} with no problem detail - backend unreachable`,
        )
      }

      throw new ApiError(
        res.status,
        problem?.detail ?? problem?.title ?? `HTTP ${res.status}`,
        problem?.errors,
      )
    }
    return res
  }

  async function getJson<T> (path: string): Promise<T> {
    const res = await apiFetch(path)
    return res.json() as Promise<T>
  }

  return {
    apiFetch,

    // GET /api/destinations
    listDestinations: () => getJson<Destination[]>('/api/destinations'),

    // GET /api/destinations/{id}
    getDestination: (id: number) =>
      getJson<Destination>(`/api/destinations/${id}`),

    // POST /api/destinations
    addDestination: async (
      body: AddDestinationRequest,
    ): Promise<Destination> => {
      const res = await apiFetch('/api/destinations', {
        method: 'POST',
        body: JSON.stringify(body),
      })
      return res.json() as Promise<Destination>
    },

    // DELETE /api/destinations/{id} - 204, no body
    deleteDestination: async (id: number): Promise<void> => {
      await apiFetch(`/api/destinations/${id}`, { method: 'DELETE' })
    },

    // GET /api/cities/search?q= - backend requires 2..100 chars
    searchCities: (q: string) =>
      getJson<CitySearchResult[]>(
        `/api/cities/search?q=${encodeURIComponent(q)}`,
      ),

    // GET /api/weather/city/{cityName}
    getWeather: (cityName: string) =>
      getJson<Weather>(`/api/weather/city/${encodeURIComponent(cityName)}`),

    // GET /api/countries/{name}
    getCountry: (name: string) =>
      getJson<CountryApiResult>(`/api/countries/${encodeURIComponent(name)}`),

    // GET /api/iss
    getIssPosition: () => getJson<IssPosition>('/api/iss'),

    // POST /api/assistant/chat - text/event-stream; caller reads the raw body itself
    streamChat: (body: ChatRequest): Promise<Response> =>
      apiFetch('/api/assistant/chat', {
        method: 'POST',
        body: JSON.stringify(body),
      }),

    // POST /api/assistant/validate-key - throws ApiError(401) for a bad key
    validateApiKey: async (apiKey: string): Promise<ApiKeyValidationResponse> => {
      const res = await apiFetch('/api/assistant/validate-key', {
        method: 'POST',
        body: JSON.stringify({ apiKey }),
      })
      return res.json() as Promise<ApiKeyValidationResponse>
    },
  }
}

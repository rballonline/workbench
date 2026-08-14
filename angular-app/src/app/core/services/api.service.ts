import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import type {
  Destination,
  AddDestinationRequest,
  CitySearchResult,
  Weather,
  Country,
  CountryApiResult,
  IssPosition,
  ChatRequest,
  ApiKeyValidationResponse,
  ProblemDetail
} from '@shared/types';
import { AppContextService } from './app-context.service';

export interface ApiFailure {
  kind: 'unreachable' | 'notFound' | 'validation' | 'other';
  detail: string;
}

export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
    readonly fieldErrors?: Record<string, string>
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly appContext = inject(AppContextService);

  async listDestinations(): Promise<Destination[]> {
    try {
      return await firstValueFrom(
        this.http.get<Destination[]>(`${this.appContext.apiBase}/api/destinations`)
      );
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async getDestination(id: number): Promise<Destination> {
    try {
      return await firstValueFrom(
        this.http.get<Destination>(`${this.appContext.apiBase}/api/destinations/${id}`)
      );
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async addDestination(body: AddDestinationRequest): Promise<Destination> {
    try {
      return await firstValueFrom(
        this.http.post<Destination>(`${this.appContext.apiBase}/api/destinations`, body)
      );
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async deleteDestination(id: number): Promise<void> {
    try {
      await firstValueFrom(
        this.http.delete<void>(`${this.appContext.apiBase}/api/destinations/${id}`)
      );
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async searchCities(q: string): Promise<CitySearchResult[]> {
    try {
      return await firstValueFrom(
        this.http.get<CitySearchResult[]>(
          `${this.appContext.apiBase}/api/cities/search?q=${encodeURIComponent(q)}`
        )
      );
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async getWeather(cityName: string): Promise<Weather> {
    try {
      return await firstValueFrom(
        this.http.get<Weather>(
          `${this.appContext.apiBase}/api/weather/city/${encodeURIComponent(cityName)}`
        )
      );
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async getCountry(name: string): Promise<CountryApiResult> {
    try {
      return await firstValueFrom(
        this.http.get<CountryApiResult>(
          `${this.appContext.apiBase}/api/countries/${encodeURIComponent(name)}`
        )
      );
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async getIssPosition(): Promise<IssPosition> {
    try {
      return await firstValueFrom(
        this.http.get<IssPosition>(`${this.appContext.apiBase}/api/iss`)
      );
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async validateApiKey(apiKey: string): Promise<ApiKeyValidationResponse> {
    try {
      return await firstValueFrom(
        this.http.post<ApiKeyValidationResponse>(
          `${this.appContext.apiBase}/api/assistant/validate-key`,
          { apiKey }
        )
      );
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * POST /api/assistant/chat - text/event-stream; caller reads the raw body itself.
   * Not EventSource: it only supports GET, and this endpoint takes the conversation
   * id/message/key as a JSON body (a GET would put the key in the URL - logs, history).
   */
  async streamChat(body: ChatRequest): Promise<Response> {
    let response: Response;
    try {
      response = await fetch(`${this.appContext.apiBase}/api/assistant/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
    } catch (error) {
      throw new ApiError(0, error instanceof Error ? error.message : 'Network request failed');
    }

    if (!response.ok) {
      const problem = (await response.json().catch(() => null)) as ProblemDetail | null;
      if (!problem && response.status >= 500) {
        throw new ApiError(0, `HTTP ${response.status} with no problem detail - backend unreachable`);
      }
      throw new ApiError(
        response.status,
        problem?.detail ?? problem?.title ?? `HTTP ${response.status}`,
        problem?.errors
      );
    }

    return response;
  }

  classifyError(error: unknown): ApiFailure {
    if (error instanceof ApiError) {
      if (error.status === 0) {
        return { kind: 'unreachable', detail: error.message };
      }
      if (error.status === 404) {
        return { kind: 'notFound', detail: error.message };
      }
      if (error.fieldErrors) {
        return {
          kind: 'validation',
          detail: Object.entries(error.fieldErrors)
            .map(([field, message]) => `${field}: ${message}`)
            .join(', ')
        };
      }
      return { kind: 'other', detail: error.message };
    }
    return {
      kind: 'other',
      detail: error instanceof Error ? error.message : String(error)
    };
  }

  private handleError(error: unknown): ApiError {
    if (error instanceof ApiError) {
      return error;
    }
    if (!(error instanceof HttpErrorResponse)) {
      return new ApiError(0, error instanceof Error ? error.message : 'Network request failed');
    }

    if (error.status === 0) {
      return new ApiError(
        0,
        error.error instanceof Error ? error.error.message : 'Network request failed'
      );
    }

    const problem = error.error as ProblemDetail | null;

    if (!problem && error.status >= 500) {
      return new ApiError(
        0,
        `HTTP ${error.status} with no problem detail - backend unreachable`
      );
    }

    return new ApiError(
      error.status,
      problem?.detail ?? problem?.title ?? `HTTP ${error.status}`,
      problem?.errors
    );
  }
}

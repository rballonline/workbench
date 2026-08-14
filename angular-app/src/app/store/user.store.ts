import { Injectable, signal, computed, inject } from '@angular/core';
import { ApiService, type ApiFailure } from '../core/services/api.service';

@Injectable({ providedIn: 'root' })
export class UserStore {
  private readonly apiService = inject(ApiService);

  private readonly _displayName = signal<string>('');
  private readonly _locale = signal<string>('en');
  private readonly _theme = signal<'light' | 'dark'>('light');
  private readonly _aiApiKey = signal<string>('');
  private readonly _aiApiKeyValidatedAt = signal<string | null>(null);
  private readonly _validatingApiKey = signal(false);
  private readonly _error = signal<ApiFailure | null>(null);

  readonly displayName = this._displayName.asReadonly();
  readonly locale = this._locale.asReadonly();
  readonly theme = this._theme.asReadonly();
  readonly aiApiKey = this._aiApiKey.asReadonly();
  readonly aiApiKeyValidatedAt = this._aiApiKeyValidatedAt.asReadonly();
  readonly validatingApiKey = this._validatingApiKey.asReadonly();
  readonly error = this._error.asReadonly();

  // Computed getters
  readonly hasName = computed(() => this._displayName().length > 0);
  readonly hasAiApiKey = computed(() => this._aiApiKey().length > 0);

  setDisplayName(name: string): void {
    this._displayName.set(name);
  }

  setLocale(locale: string): void {
    this._locale.set(locale);
  }

  setTheme(theme: 'light' | 'dark'): void {
    this._theme.set(theme);
  }

  setAiApiKey(key: string): void {
    this._aiApiKey.set(key);
    // Reset validation timestamp when key changes
    this._aiApiKeyValidatedAt.set(null);
  }

  async validateApiKey(apiKey: string): Promise<boolean> {
    this._validatingApiKey.set(true);
    this._error.set(null);
    try {
      const response = await this.apiService.validateApiKey(apiKey);
      this._aiApiKey.set(apiKey);
      this._aiApiKeyValidatedAt.set(response.validatedAt);
      return true;
    } catch (error) {
      this._error.set(this.classifyError(error));
      return false;
    } finally {
      this._validatingApiKey.set(false);
    }
  }

  clearError(): void {
    this._error.set(null);
  }

  private classifyError(error: unknown): ApiFailure {
    return this.apiService.classifyError(error);
  }
}

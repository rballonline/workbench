import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

export interface AppContext {
  apiBase: string;
  wsUrl: string;
  overridden: boolean;
}

@Injectable({ providedIn: 'root' })
export class AppContextService implements AppContext {
  private static readonly instance = AppContextService.resolve();

  readonly apiBase = AppContextService.instance.apiBase;
  readonly wsUrl = AppContextService.instance.wsUrl;
  readonly overridden = AppContextService.instance.overridden;

  private static resolve(): AppContext {
    const params = new URLSearchParams(window.location.search);
    const override = params.get('api');

    const stripTrailingSlash = (value: string): string => value.replace(/\/$/, '');

    const apiBase = stripTrailingSlash(
      override ?? environment.apiBaseUrl ?? ''
    );

    const wsOrigin = stripTrailingSlash(
      override ?? environment.wsBaseUrl ?? window.location.origin
    );

    return {
      apiBase,
      wsUrl: `${wsOrigin.replace(/^http/, 'ws')}/ws/destinations`,
      overridden: override != null
    };
  }
}

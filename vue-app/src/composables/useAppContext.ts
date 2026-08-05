import { inject, provide } from 'vue'

export interface AppContext {
  /** Origin the REST API lives on. `''` means same-origin (Vite proxies /api in dev). */
  apiBase: string
  /** Fully-qualified WebSocket URL for the live update stream. */
  wsUrl: string
  /** True when the backend origin came from `?api=`, not from the build config. */
  overridden: boolean
}

const APP_CONTEXT_KEY = Symbol('appContext')

function stripTrailingSlash (value: string): string {
  return value.replace(/\/$/, '')
}

function resolve (): AppContext {
  // `?api=http://some-host:8080` points the whole app at another backend without a
  // rebuild - handy when the Spring app runs somewhere other than localhost.
  const override = new URLSearchParams(window.location.search).get('api')
  const apiBase = stripTrailingSlash(
    override ?? import.meta.env.VITE_API_BASE_URL ?? '',
  )

  const wsOrigin = stripTrailingSlash(
    override ?? import.meta.env.VITE_WS_BASE_URL ?? window.location.origin,
  )

  return {
    apiBase,
    wsUrl: `${wsOrigin.replace(/^http/, 'ws')}/ws/destinations`,
    overridden: override != null,
  }
}

// Memoized at module scope rather than resolved per-call, because Pinia stores and
// the WebSocket singleton need the same context outside of any component setup(),
// where `inject` is unavailable.
let context: AppContext | null = null

export function getAppContext (): AppContext {
  context ??= resolve()
  return context
}

export function provideAppContext (): AppContext {
  const ctx = getAppContext()
  provide(APP_CONTEXT_KEY, ctx)
  return ctx
}

export function useAppContext (): AppContext {
  return inject<AppContext>(APP_CONTEXT_KEY) ?? getAppContext()
}

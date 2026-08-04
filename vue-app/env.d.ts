/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Origin the REST API lives on. Empty in dev — Vite proxies /api to the backend. */
  readonly VITE_API_BASE_URL?: string
  /** Origin the WebSocket lives on. Empty in dev — Vite proxies /ws to the backend. */
  readonly VITE_WS_BASE_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

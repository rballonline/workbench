/**
 * Build-time backend origin overrides. Both left unset by default - the app
 * relies on `?api=` for pointing at another backend, and same-origin (via the
 * dev proxy or a reverse proxy in front of the built bundle) otherwise.
 */
export const environment = {
  apiBaseUrl: undefined as string | undefined,
  wsBaseUrl: undefined as string | undefined
};

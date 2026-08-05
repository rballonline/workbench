# vue-app - client for spring-reactive

A Vue 3 front end for the **"Places I'd Like to Visit"** backend in [`../spring-reactive`](../spring-reactive/). It is a read/write client for that app's REST endpoints plus a live consumer of its WebSocket.

The structure is deliberately modelled on `C:\Code\gangsheet-builder` - same stack, same shell/sidebar/page-map layout, same query-param routing, same `@`/`@shared` aliases - so patterns learned in one repo carry to the other. It shares no code with it and builds nothing gang-sheet related.

## Stack

- **Vue 3** (Composition API, `<script setup>`) + **TypeScript**
- **Vuetify 3** for UI components
- **Pinia** (with persisted state) for stores
- **vue-i18n** for localization (en, es)
- **Vite** for build tooling
- No canvas library, no Shopify, no vue-router

## Key paths

- `shared/types.ts` - hand-maintained mirror of the backend's DTOs
- `src/components/` - `AppShell.vue` is the root; `pages/` holds one component per page
- `src/composables/` - `useApi`, `useAppContext`, `useAppNav`, `useLiveUpdates`
- `src/stores/` - `useDestinationsStore`, `useIssStore`, `useUserStore`
- `src/utils/weatherCodes.ts` - WMO code → icon/label mapping
- `src/locales/` - `en.json`, `es.json`

## App structure

`App.vue` renders `AppShell.vue` and nothing else. The shell owns the `v-app` root, the app bar, the sidebar, the WebSocket wiring, and the async page map.

### Page navigation

No Vue Router. `useAppNav` reads `?page=home` (default) from the URL and updates it via `history.pushState`, which preserves the other query params - `?api=` in particular must survive navigation.

`AppShell` maps the `page` key to a `defineAsyncComponent` import, so each page is a separate JS chunk. Pages emit `navigate` with an optional params object: `$emit('navigate', 'weather', { city: 'Tokyo' })` deep-links into the weather page, which reads it back with `readPageParam('city')`. Page-scoped params are listed in `PAGE_PARAMS` and cleared on every navigation.

| Page        | Backend endpoints                   |
| ----------- | ----------------------------------- |
| `home`      | (aggregates the destinations store) |
| `wishlist`  | `GET/POST/DELETE /api/destinations` |
| `explore`   | `GET /api/cities/search?q=`         |
| `weather`   | `GET /api/weather/city/{city}`      |
| `countries` | `GET /api/countries/{name}`         |
| `iss`       | `GET /api/iss` + WebSocket          |
| `live`      | WebSocket only                      |
| `settings`  | (client-side prefs)                 |

### Backend targeting: `useAppContext`

Resolves the REST base and WebSocket URL from, in order: `?api=<origin>`, `VITE_API_BASE_URL` / `VITE_WS_BASE_URL`, then same-origin. `?api=` lets you point a built bundle at another backend with no rebuild - the Settings page shows what resolved.

The resolved context is memoized at **module scope**, not just provided via `provide`/`inject`, because Pinia stores and the WebSocket singleton need it outside any component `setup()`.

### API layer: `useApi`

Every endpoint gets a typed method. All failures throw `ApiError { status, message, fieldErrors }`.

`GlobalExceptionHandler` on the backend answers _every_ error with an RFC 7807 body, so a 5xx **without** a JSON body did not come from the app - it is the Vite dev proxy reporting that the backend is down. `apiFetch` remaps that case to `status: 0`, same as a fetch-level network failure.

`classifyError()` reduces any thrown value to an `ApiFailure { kind, detail }`. Stores have no `t()`, so they classify and store the kind; `ApiErrorAlert.vue` translates it at render time. That is why store `error` fields hold objects, not strings.

### WebSocket: `useLiveUpdates`

A **module-scope singleton** - `/ws/destinations` broadcasts the same merged stream to every session, so a second socket would only duplicate work. It survives page switches and is never closed on unmount; `AppShell` subscribes once in `onMounted` and fans frames out to the stores.

Reconnects with exponential backoff capped at 15s, and keeps a 200-entry ring buffer for the Live Feed page.

**The socket merges two unrelated streams with no envelope.** `isDestinationEvent()` in `shared/types.ts` discriminates on the presence of an `action` field. Anything else is an `IssPosition`.

**The event payload is not the response DTO.** `DestinationEvent.destination` is the raw `model/Destination` entity - it has `countryCode` but no nested `country` object. `useDestinationsStore.applyEvent` therefore refetches `GET /api/destinations/{id}` on a create to get the joined country row the cards render. Creates originating from this browser are skipped, since the POST response already carried the enriched shape.

## `shared/types.ts`

Hand-maintained; there is no codegen. Two shapes to watch:

- `Destination` mirrors `DestinationResponse` (nested `country`), **not** the `Destination` entity.
- `CountryApiResult` is the raw REST Countries payload that `CountryController` passes straight through - `name.common`, `capital[0]`, `flags.png`. It is shaped differently from `CountryResponse`, which is what hangs off a destination.

## Components worth knowing

- `WorldMap.vue` - dependency-free equirectangular SVG. The viewBox is literally `0 0 360 180` degrees, so projecting is `x = lon + 180`, `y = 90 - lat`. Drawn as a graticule; there is no bundled coastline geometry and no map tiles. Trails must be pre-split at the antimeridian (`useIssStore.trailSegments` does this) or a wrap draws a line back across the map.
- `CitySearchField.vue` - debounced autocomplete with a monotonic request token so a slow earlier response cannot overwrite a newer one. Enforces the backend's `@Size(min = 2)` client-side rather than earning a 400.
- `ApiErrorAlert.vue` - renders an `ApiFailure`; takes an optional `not-found` prop for page-specific 404 wording.

## Dev workflow

```bash
npm install
npm run dev          # Vite on :3002, proxies /api and /ws to localhost:8080
npm run build        # type-check + build to dist/
npm run lint         # ESLint with auto-fix
```

The backend must be running separately (`./gradlew bootRun` in `../spring-reactive`, which needs a local MySQL). With it down, every page still renders and shows "Cannot reach the API" rather than breaking.

Both `/api` and `/ws` are proxied in dev (`ws: true` for the latter), so the browser sees a same-origin app - `SecurityConfig` on the backend permits all requests but declares no CORS mappings, so a cross-origin front end would be blocked.

### Notes on the toolchain

- `tsconfig.app.json` sets `lib: ES2023` for `Array#at` and `Array#toSorted`, which the lint config prefers over index arithmetic and in-place `sort`.
- `eslint.config.js` awaits `vuetify()` (it resolves to the flat-config array) and disables `unicorn/no-this-outside-of-class` for `src/stores/**` - Pinia's options API is built on `this` inside plain object literals.

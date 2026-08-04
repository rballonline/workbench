# vue-app

A Vue 3 + Vuetify client for the **"Places I'd Like to Visit"** service in [`../spring-reactive`](../spring-reactive/).

Search the world's cities, add the good ones to a shared wishlist, and watch other people's additions appear in real time. Along the way it exercises every endpoint the backend exposes, including the WebSocket that carries both destination events and the ISS's current position.

## Running it

The backend first — it needs a local MySQL with a `spring_reactive` database:

```bash
cd ../spring-reactive
./gradlew bootRun          # serves on :8080
```

Then this app:

```bash
npm install
npm run dev                # http://localhost:3002
```

Vite proxies `/api` and `/ws` to `localhost:8080`, so the browser sees a single origin. Override the backend with `VITE_BACKEND_ORIGIN` at dev-server start, or at runtime by appending `?api=http://some-host:8080` to the URL.

If the backend is down the app still runs — every page renders and reports that the API is unreachable.

## Pages

| Page | What it does |
|---|---|
| **Welcome** | Counts, a region breakdown, and links into everything else |
| **Wishlist** | The shared list, as cards or plotted on a world map. Add and remove destinations |
| **Explore** | City search — geocodes the query and resolves the country in one backend call |
| **Weather** | Current conditions for any city |
| **Countries** | Flag, capital, region, and population for any country |
| **ISS Tracker** | The station's live position and recent trail, plus the nearest wishlist city |
| **Live Feed** | Raw WebSocket frames as they arrive, filterable by kind |
| **Settings** | Display name, theme, language, and which backend is in use |

## Scripts

| Command | Does |
|---|---|
| `npm run dev` | Vite dev server with hot reload |
| `npm run build` | Type-check and build to `dist/` |
| `npm run preview` | Serve the built bundle |
| `npm run lint` | ESLint with auto-fix |

## Stack

Vue 3 · TypeScript · Vuetify 3 · Pinia · vue-i18n · Vite

Structure and conventions follow `C:\Code\gangsheet-builder`. See [CLAUDE.md](CLAUDE.md) for the architecture.

# vue-app

A Vue 3 + Vuetify client for the **"Places I'd Like to Visit"** service in [`../spring-reactive`](../spring-reactive/).

It isn't meant to be a usable product - it's a showcase of how I architect a full-stack app, pairing this Vue 3 client with a reactive Spring backend. Each page demonstrates a different technique: API chaining, WebSocket streaming, optimistic client state, and so on.

## Running it

The backend first - it needs a local MySQL with a `spring_reactive` database:

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

If the backend is down the app still runs - every page renders and reports that the API is unreachable.

## Pages

| Page            | What it demonstrates                                                                |
| --------------- | ------------------------------------------------------------------------------------ |
| **Home**        | An overview and jumping-off point into every other page                              |
| **Wishlist**    | Full CRUD over a reactive REST API, kept in sync via Pinia and live WebSocket events  |
| **Explore**     | Debounced search that chains two upstream APIs into one backend response             |
| **Weather**     | Two sequential external API calls: geocode a city, then fetch its forecast           |
| **Countries**   | A pass-through proxy - a third-party API's response shape, forwarded as-is           |
| **ISS Tracker** | Server-side polling pushed to clients over a shared WebSocket                        |
| **Live Feed**   | The raw, merged WebSocket stream, frame by frame, filterable by kind                 |
| **Settings**    | Client-side prefs, and pointing the app at a different backend without a rebuild     |

## Scripts

| Command           | Does                            |
| ----------------- | ------------------------------- |
| `npm run dev`     | Vite dev server with hot reload |
| `npm run build`   | Type-check and build to `dist/` |
| `npm run preview` | Serve the built bundle          |
| `npm run lint`    | ESLint with auto-fix            |

## Stack

Vue 3 · TypeScript · Vuetify 3 · Pinia · vue-i18n · Vite

Structure and conventions follow `C:\Code\gangsheet-builder`. See [CLAUDE.md](CLAUDE.md) for the architecture.

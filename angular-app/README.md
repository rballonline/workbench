# Angular App - Places I'd Like to Visit

A modern Angular 19+ port of the Vue 3 application featuring:

- **Signal-based state management** (no NgRx) — injectable services with `signal()`, `computed()`, and actions
- **Tailwind CSS + Angular CDK** — headless UI primitives, no Material Design
- **Standalone components** — no NgModules, modern Angular APIs
- **Lazy-loaded routes** — 8 pages with query param preservation

## Architecture

### State Management

Four injectable singleton stores (signal-based):
- `DestinationsStore` — CRUD operations + WebSocket upsert
- `UserStore` — User preferences + API key validation
- `IssStore` — ISS position tracking + trail rendering
- `AssistantStore` — AI chat with SSE streaming

### Services

Three core injectable singletons:
- `ApiService` — HTTP client + error classification (RFC 7807)
- `AppContextService` — Backend URL resolution (`?api=`, env, same-origin)
- `LiveUpdatesService` — WebSocket singleton + listener registration + auto-reconnect

### Components

Standalone components using `input()` / `output()` signals:
- Pages (lazy-loaded): `home`, `wishlist`, `explore`, `weather`, `countries`, `iss`, `live`, `settings`
- Reusable: `destination-card`, `city-search-field`, `api-error-alert`, `ai-assistant-panel`, `world-map`, etc.

### Routing

8 lazy-loaded routes with query param (`?api=`) preservation across navigation.

## Quick Start

### Installation

```bash
npm install
```

### Development

```bash
npm start
# Runs on http://localhost:4200 (Tailwind watch enabled)
# Proxies /api and /ws to http://localhost:8080 (Spring backend)
```

### Build

```bash
npm run build
# Outputs to dist/angular-app/
```

### Type Check

```bash
npm run type-check
```

## Key Patterns

### Signal-Based Store

```typescript
@Injectable({ providedIn: 'root' })
export class DestinationsStore {
  private readonly _items = signal<Destination[]>([]);
  readonly items = this._items.asReadonly();

  readonly sorted = computed(() =>
    this._items().toSorted((a, b) => a.createdAt < b.createdAt ? 1 : -1)
  );

  async load(): Promise<void> {
    try {
      this._items.set(await this.apiService.listDestinations());
    } catch (error) {
      this._error.set(this.classifyError(error));
    }
  }
}
```

### Standalone Component with Signals

```typescript
@Component({
  selector: 'app-destination-card',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div>{{ destination().cityName }}</div>
    <button (click)="onRemove()">Remove</button>
  `
})
export class DestinationCardComponent {
  readonly destination = input.required<Destination>();
  readonly removeClicked = output<number>();

  onRemove(): void {
    this.removeClicked.emit(this.destination().id);
  }
}
```

### Control Flow Syntax

```html
@if (store.loading()) {
  <p>Loading...</p>
} @else if (store.error()) {
  <app-api-error-alert [error]="store.error()!" />
} @else {
  @for (item of store.sorted(); track item.id) {
    <app-destination-card [destination]="item" />
  }
}
```

## Design Tokens

See `DESIGN_TOKENS.md` for theme configuration and portability approach. Tokens are defined in:
- `src/styles.css` — CSS custom properties
- `tailwind.config.js` — Tailwind extensions

## Environment

Backend URL resolution (in priority order):
1. `?api=http://localhost:9000` (query param override)
2. `VITE_API_BASE_URL` environment variable
3. Same-origin (default)

Dev proxy (see `proxy.conf.json`):
- `/api/*` → `http://localhost:8080/api/*`
- `/ws` → `ws://localhost:8080/ws`

## Next Steps (Phase 2)

- [ ] Add CDK-based dialog for add/remove destinations
- [ ] Implement city search with debounce + request deduplication
- [ ] Add i18n with `@ngx-translate`
- [ ] Build WorldMap component (SVG equirectangular, antimeridian-aware)
- [ ] Implement AiAssistantPanel with SSE streaming
- [ ] Add unit tests (Jasmine) + e2e tests (Cypress)
- [ ] Accessibility audit (axe DevTools)
- [ ] Consider zoneless mode if change detection becomes a bottleneck

## Shared Types

`src/shared/types.ts` is copied from `vue-app/shared/types.ts` and mirrors the Spring DTOs.

To keep in sync, update both locations or consider a monorepo setup with symlinks.

## File Structure

```
angular-app/
├── src/
│   ├── app/
│   │   ├── core/services/          # API, context, WebSocket
│   │   ├── store/                  # Signal-based stores
│   │   ├── shell/                  # Layout components
│   │   ├── components/             # Reusable UI components
│   │   ├── pages/                  # Lazy-loaded route pages
│   │   └── shared/                 # Shared types, pipes
│   ├── styles.css                  # Tailwind + design tokens
│   └── main.ts
├── tailwind.config.js
├── postcss.config.js
├── proxy.conf.json
└── DESIGN_TOKENS.md
```

## References

- Angular: https://angular.io
- Tailwind CSS: https://tailwindcss.com
- Angular CDK: https://material.angular.io/cdk
- Vue app (source): ../vue-app

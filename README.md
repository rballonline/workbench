# workbench

Examples and showcase of various back and front end code

## Getting started

There are three ways to run this, depending on whether you're actively developing or just want the whole thing running.

### VSCode Day-to-day development (hot reload)

- Bring up MySQL (`docker compose up`), :
- (Ctrl+Shift+D) to bring up the Run and Debug panel.
- Choose **"Full stack: spring-reactive + vue-app"**

### Day-to-day development (hot reload)

- Bring up MySQL (`docker compose up`)

Backend:

```bash
# spring-reactive/
./gradlew bootRun          # spring-boot-devtools auto-restarts on change
```

Backend API (Spring): http://localhost:8080

Frontend:

```
# vue-app/
npm run dev                # Vite HMR on :3002, proxies /api and /ws to :8080
```

Open the site at http://localhost:3002.

### Full stack in Docker

```
docker compose -f docker-compose.spring-reactive-vue.yml up --build
```

- Frontend (Vue app): http://localhost:3002
- Backend API (Spring): http://localhost:8080

Note: changes to source code aren't picked up automatically here - rerun with `--build` after editing backend or frontend code.

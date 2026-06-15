# Frontend — 2Hands Web

React 19 + Vite 5 + Tailwind 4 + React Router 7. Gọi trực tiếp các microservice qua `VITE_*_SERVICE_BASE_URL` (dev không qua API Gateway).

## Chạy trên host (dev hàng ngày)

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

- URL: http://localhost:5173
- Mock API: `VITE_USE_MOCK=true` (MSW) — mặc định `false` gọi backend thật

## Chạy trong Docker

Template env: `.env.docker.example` (commit được). Setup:

```bash
cd Infrastructure
./scripts/setup-docker-env.ps1
```

| Profile | Lệnh | FE |
|---------|------|-----|
| `apps` | `docker compose -f docker-compose.yml -f docker-compose.apps.yml --profile apps up -d --build` | nginx :5173 |
| `dev` | `docker compose -f docker-compose.yml -f docker-compose.dev.yml --profile dev up` | Vite HMR :5173 |

`VITE_*` URL vẫn trỏ `http://localhost:3001`–`3005` vì browser chạy trên host.

## Biến môi trường

| Biến | Mặc định local |
|------|----------------|
| `VITE_AUTH_SERVICE_BASE_URL` | http://localhost:3001 |
| `VITE_SOCIAL_SERVICE_BASE_URL` | http://localhost:3002 |
| `VITE_COMMERCE_SERVICE_BASE_URL` | http://localhost:3003 |
| `VITE_ADMIN_SERVICE_BASE_URL` | http://localhost:3004 |
| `VITE_NOTIFICATION_SERVICE_BASE_URL` | http://localhost:3005 |
| `VITE_USE_MOCK` | false |
| `VITE_COMMERCE_CHECKOUT_COD_ONLY_ENABLED` | false |

## Build production

```bash
npm run build
npm run preview
```

Docker multi-stage: `Dockerfile` target `production` (dùng bởi compose `apps`).

## Tài liệu

- Monorepo: [README.md](../README.md)
- API behavior: [docs/api_fe_behavior/](../docs/api_fe_behavior/)
- Frontend convention: [docs/engineering_rules/frontend-convention.md](../docs/engineering_rules/frontend-convention.md)
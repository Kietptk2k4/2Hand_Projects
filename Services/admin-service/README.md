# Admin Service

Operations, moderation, system configuration, and audit for 2Hands.

## Local setup

1. Start PostgreSQL + Redis from [`Infrastructure/docker-compose.yml`](../../Infrastructure/docker-compose.yml):

```bash
cd Infrastructure
docker compose up -d postgres-admin redis
```

`postgres-admin` exposes **localhost:5436** → database `admin_db` (user/password: `postgres` / `123456`).

2. Copy `.env.example` to `.env` and align `JWT_ACCESS_SECRET` with `auth-service` for local JWT tests.
3. Run:

```bash
./gradlew bootRun
```

- Health: `GET http://localhost:3004/actuator/health`
- Protected smoke: `GET http://localhost:3004/admin/api/v1/health` (Bearer JWT)

## Local ports (docker-compose)

| Service | App port | PostgreSQL port | Database |
|---|---|---|---|
| auth-service | 3001 | 5432 | auth_db |
| social-service | 3002 | 5433 | social_db |
| commerce-service | 3003 | 5434 | commerce_db |
| notification-service | — | 5435 | notification_db |
| **admin-service** | **3004** | **5436** | **admin_db** |

## Docs

- [admin-service-spec.md](../../docs/business-spec/admin-service-spec.md)
- [admin-schema.md](../../docs/database/admin-schema.md)

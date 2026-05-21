# notification-service

Notification microservice for 2Hands (MVP scaffold).

## Local setup

1. Start infrastructure:

```bash
cd Infrastructure
docker compose up -d postgres-notification redis
```

2. Copy env file and set JWT secrets (same as auth-service for token validation):

```bash
cd Services/notification-service
cp .env.example .env
```

3. Run service:

```bash
./gradlew bootRun
```

- HTTP port: `3004`
- PostgreSQL: `localhost:5435/notification_db`
- Health: `GET http://localhost:3004/actuator/health`
- API health: `GET http://localhost:3004/api/v1/notification/health`

## Internal event ingest (dev)

```bash
curl -X POST http://localhost:3004/api/v1/notification/internal/events \
  -H "Content-Type: application/json" \
  -H "X-Internal-Api-Key: dev-internal-key" \
  -d "{\"sourceEventId\":\"11111111-1111-1111-1111-111111111111\",\"eventType\":\"POST_LIKED\",\"sourceService\":\"SOCIAL\",\"payload\":\"{}\"}"
```

## Docs

- Business spec: `docs/business-spec/notification-service-spec.md`
- Database schema: `docs/database/notification-schema.md`

## Package layout

`application` → `delivery` → `domain` → `infrastructure` (Clean Architecture)

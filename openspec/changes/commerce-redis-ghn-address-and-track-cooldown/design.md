## Context

Commerce-service already declares `spring.data.redis` and the Redis starter, but has no Redis application code. GHN address master-data is loaded via `GhnAddressMasterDataGatewayAdapter` on every provinces/districts/wards request. Track sync cooldown uses `GhnTrackSyncCooldownRegistry` with a `ConcurrentHashMap`, so each commerce replica has its own cooldown window. GHN webhooks are not registered yet; shipment status updates rely on pull sync (`SyncGhnShipmentStatusUseCase`), which makes a shared cooldown more important.

## Goals / Non-Goals

**Goals:**

- Cache GHN province/district/ward lists in Redis to cut repeated external calls.
- Store track-sync cooldown in Redis so cooldown works across instances.
- Fail open when Redis is unavailable (still call GHN / still allow sync).
- Keep HTTP contracts unchanged.

**Non-Goals:**

- Caching GHN fee or leadtime quotes.
- GHN webhook ingestion or idempotency.
- Notification unread Redis / product-detail cache.
- Changing `trackSyncCooldownSeconds` default semantics (still from config).

## Decisions

### D1 — Cache-aside at gateway adapter (address)

Wrap or decorate `GhnAddressMasterDataGateway` / adapter with Redis GET→miss→GHN→SET. Use cases and controllers stay unaware of Redis.

**Alternatives:** HTTP-layer response cache (harder to share keys); Caffeine-only (not multi-instance). Rejected for shared Redis + existing dependency.

### D2 — Key layout and TTL (address)

| Key | Value | TTL |
|-----|--------|-----|
| `commerce:ghn:provinces` | JSON list | 7 days |
| `commerce:ghn:districts:{provinceId}` | JSON list | 7 days |
| `commerce:ghn:wards:{districtId}` | JSON list | 7 days |

Serialize with Jackson (same domain DTOs or compact JSON string via `StringRedisTemplate`). No mandatory invalidate API in this change; ops can `DEL` keys if GHN master data changes.

### D3 — Cooldown as Redis key with TTL

Replace in-memory map with:

- `shouldSync`: if `force` or cooldown seconds ≤ 0 → true; else if key `commerce:ghn:track-cooldown:{shipmentId}` exists → false; else true.
- `markSynced`: `SET` key with TTL = `trackSyncCooldownSeconds` (presence = “in cooldown”).

**Alternatives:** keep in-memory (broken under scale); store last-sync timestamp in Postgres (heavier). Redis TTL matches existing config cleanly.

### D4 — Fail-open on Redis errors

On Redis connection/command failure: treat address as cache miss (call GHN); treat cooldown as “allow sync”. Log WARN. Prefer availability over strict rate limiting of GHN.

### D5 — Minimal Redis wiring in commerce

Add `RedisConfig` (or Boot auto-config `StringRedisTemplate`) for string keys/values. Prefer `StringRedisTemplate` for these two use cases to avoid complex Object serializers. Tests: `@Profile("test")` noop/in-memory doubles or mock Redis if patterns exist in auth/social.

## Risks / Trade-offs

- [Stale address master for up to 7d] → Acceptable for GHN admin codes; document manual `DEL`; shorten TTL via config if needed later.
- [Fail-open floods GHN when Redis down] → Same as today’s behavior without cache; monitor WARN logs.
- [Two pods race markSynced] → Worst case one extra GHN track call; acceptable.
- [JSON shape drift if GHN fields change] → Parser already tolerant; cache stores post-parse domain lists so GHN response quirks are normalized before SET.

## Migration Plan

1. Deploy commerce with Redis cache + cooldown; no FE deploy.
2. Verify Redis keys appear after opening address form and after track sync.
3. Rollback: revert commerce build; in-memory cooldown and uncached GHN return (no DB migration).

## Open Questions

- None blocking. Optional follow-up: configurable address TTL via `application.yml`.

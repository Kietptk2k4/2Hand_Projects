## Why

Commerce already depends on Redis but never uses it. GHN province/district/ward master-data is fetched from GHN on every address-form request, and shipment track sync cooldown lives in an in-process `ConcurrentHashMap` that does not work across multiple commerce instances. Without GHN webhooks, track status depends on pull sync, so a shared cooldown matters now.

## What Changes

- Cache GHN address master-data (provinces, districts-by-province, wards-by-district) in Redis with long TTL and fail-open to live GHN calls.
- Move GHN track sync cooldown from in-memory map to Redis keys with TTL matching `trackSyncCooldownSeconds`, fail-open if Redis is unavailable.
- Add minimal commerce Redis wiring (`StringRedisTemplate` / config) as needed for these two uses.
- No API contract changes for FE; no webhook work in this change.

## Capabilities

### New Capabilities

- `ghn-address-master-cache`: Redis cache-aside for GHN province/district/ward master-data used by commerce address shipping endpoints.
- `ghn-track-sync-cooldown`: Distributed cooldown for GHN shipment status pull-sync across commerce instances.

### Modified Capabilities

- (none)

## Impact

- **Service:** `commerce-service` only (`GhnAddressMasterDataGatewayAdapter`, `GhnTrackSyncCooldownRegistry`, Redis config).
- **Infra:** Shared Redis instance already in docker-compose; key prefix `commerce:ghn:…`.
- **APIs:** Existing `GET …/shipping/ghn/provinces|districts|wards` and track/sync paths — behavior unchanged except fewer GHN calls and cross-instance cooldown.
- **Out of scope:** Fee/leadtime cache, unread notification Redis, product-detail cache, GHN webhooks.

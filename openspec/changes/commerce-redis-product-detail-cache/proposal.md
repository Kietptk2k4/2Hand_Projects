## Why

Buyer product detail is a hot multi-join read path with no cache today. Adding Redis cache-aside with short TTL plus after-commit invalidation (L1) cuts DB load while keeping stock/price/vacation reasonably fresh for a C2C marketplace—without waiting for a separate outbox/Kafka cache consumer (L2).

## What Changes

- Cache assembled buyer-visible product detail in Redis (`commerce:product:detail:{productId}`) with short TTL (~45–60s).
- On product/shop writes that affect PDP fields, delete the corresponding cache key(s) after successful commit (same-process L1).
- Invalidate on inventory reserve/release paths that change displayed stock.
- Fail-open if Redis is down (serve from DB).
- No FE/API contract changes; add-to-cart / checkout remain DB source of truth.

## Capabilities

### New Capabilities

- `product-detail-read-cache`: Redis cache-aside for buyer `ViewProductDetail` with TTL and after-commit invalidation.

### Modified Capabilities

- (none)

## Impact

- **Service:** `commerce-service` (`ViewProductDetailUseCase` / repository path, product & shop write use cases, inventory reserve/release callers).
- **Depends on:** commerce Redis wiring (`StringRedisTemplate`) — share with `commerce-redis-ghn-address-and-track-cooldown` if applied first; otherwise add minimal Redis config here.
- **Out of scope:** L2 outbox/Kafka cache consumer; list/search/card caches; caching checkout quotes; notification Redis.

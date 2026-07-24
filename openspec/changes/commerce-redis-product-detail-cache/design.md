## Context

`ViewProductDetailUseCase` loads a rich buyer-visible snapshot (header, shop, price, inventory summary, media, attributes, rating aggregate, vacation flags) via JDBC joins. Commerce already (or will soon) have Redis for GHN caching; product detail is the next high-ROI read cache. Explore agreed on **L1**: short TTL + after-commit `DEL` in the same service—not L2 outbox listeners yet.

## Goals / Non-Goals

**Goals:**

- Cache-aside GET/SET for successful product detail reads.
- Invalidate on writes that change PDP-visible fields (product, price, inventory, media, attributes, lifecycle, shop vacation affecting embedded flags).
- Invalidate when checkout inventory reserve/release changes stock shown on PDP.
- Fail-open to DB when Redis fails.
- Keep purchase paths (`ProductPurchaseContext`, checkout) uncached / authoritative from DB.

**Non-Goals:**

- L2 Kafka/outbox cache invalidation consumers.
- Caching product list/search cards.
- Caching fee quotes or order totals.
- Perfect real-time review averages (TTL is enough for rating_avg/count).

## Decisions

### D1 — Cache the assembled detail result, not raw SQL rows

Store a serialized `ViewProductDetailResult` (or delivery-equivalent JSON) under `commerce:product:detail:{productId}` after a successful DB load. Use case: GET → hit return; miss → repository → SET with TTL.

**Alternatives:** Cache only fragments (price/stock keys) — more round-trips; reject for MVP L1.

### D2 — TTL 45–60 seconds + explicit DEL

TTL bounds worst-case staleness if a write path forgets invalidate. DEL after commit keeps happy-path freshness for stock/price.

Configurable via `commerce.cache.product-detail-ttl-seconds` (default 60).

### D3 — After-commit invalidation (TransactionSynchronization)

Call `DEL` in `afterCommit` (or equivalent) so failed transactions do not wipe cache. Prefer a small `ProductDetailCache` port in domain/application with Redis adapter in infrastructure.

### D4 — Invalidation matrix (product-scoped)

| Write / effect | Action |
|----------------|--------|
| Update product / media / attributes / price / inventory (seller) | `DEL detail:{productId}` |
| Publish / pause / archive / remove / restore | `DEL detail:{productId}` |
| Admin remove / restore | `DEL detail:{productId}` |
| Inventory reserved / released (product ids in command) | `DEL` each affected `productId` |
| Shop vacation updated | `DEL` all cached details for products of that `shopId` (query id list or batch delete known actives) |
| Shop profile update (name/avatar on PDP) | Same as vacation if shop fields are embedded — `DEL` shop’s product detail keys |
| Review create/update (rating aggregate) | Optional: rely on TTL only **or** `DEL` productId — prefer **TTL-only** for L1 to reduce write churn |

### D5 — Shop vacation / shop profile strategy

After `UpdateShopVacationUseCase` / shop profile update that changes PDP shop block: load product ids for shop (buyer-visible or all) and `DEL` each key. If list is large, accept TTL-only for shop-wide changes in a follow-up; L1 MVP still attempts id-list DEL for vacation (critical for buy CTA).

**Alternative considered:** sidecar key `commerce:shop:vacation:{shopId}` merged at read — cleaner for shop-wide fields but changes assemble path; defer unless id-list DEL proves too heavy.

### D6 — Fail-open

Redis GET/SET/DEL errors → log WARN, continue with DB result; never fail ViewProductDetail solely due to cache.

### D7 — Ordering vs GHN Redis change

If `commerce-redis-ghn-address-and-track-cooldown` lands first, reuse its `StringRedisTemplate` / `RedisConfig`. Otherwise this change adds the same minimal wiring.

## Risks / Trade-offs

- [Missed invalidate path → stale stock/price up to TTL] → Keep TTL ≤ 60s; checklist tests for main seller write use cases; purchase still revalidates at cart/checkout.
- [Shop vacation DEL many keys] → Bound query to active listings; monitor; fall back to TTL if needed.
- [Reserve path forgets DEL] → Explicit task on `ReserveInventory` / release callers.
- [Duplicate Redis config if both changes parallel] → Coordinate or extract shared config once.

## Migration Plan

1. Deploy commerce with cache + invalidation; no FE change.
2. Verify Redis keys on PDP view; DEL on price/inventory/vacation updates.
3. Rollback: revert build; reads return to DB-only.

## Open Questions

- None blocking. Optional later: L2 outbox consumer; review aggregate explicit invalidate.

## Why

Online `/feed/for-you` recall currently requires `created_at` within the last **7 days**. In sparse or sim-heavy catalogs most ACTIVE PUBLIC posts fall outside that window, so new and returning users get an empty (or near-empty) recommend feed even though hundreds of eligible posts exist and the ONNX ranker is healthy. We need progressive recall widening without weakening ranking freshness (recency half-life stays 7d).

## What Changes

- Candidate pool for Recommend Feed SHALL try recall windows in order **7d → 30d → 90d**, advancing when the unseen candidate count after filters is **&lt; N** (default **N = 20**, configurable).
- Stop at the first window that yields `>= N` candidates, or use the widest window’s result (may still be &lt; N or empty).
- Keep existing filters: ACTIVE, PUBLIC, moderation-safe, exclude `user_seen_posts`, prefer followee posts then fill from public.
- **Do not** change LightGBM/rule-based feature formulas; **`recency_score` half-life remains 7 days** so older recalled posts score lower on recency but can still appear.
- **Out of scope:** seen-post TTL / re-surface; sim `created_at` seed fixes; FE empty-state copy; changing `/feed/global`.

## Capabilities

### New Capabilities

- `recsys-candidate-recall`: Online Recommend Feed candidate recall with progressive time-window fallback (7d → 30d → 90d) when pool size is below threshold N, while ranking freshness features stay unchanged.

### Modified Capabilities

- (none — no existing main spec currently defines the online 7-day candidate pool contract)

## Impact

- **Social Service:** [`CandidatePoolServiceImpl`](Services/social-service/src/main/java/com/twohands/social_service/infrastructure/persistence/adapter/CandidatePoolServiceImpl.java) (+ interface/config if needed), unit tests; optional debug log of chosen window.
- **Config:** e.g. `social.recommendation.recall.min-pool-size` (N), window days list (7,30,90).
- **API contract:** `/feed/for-you` response shape unchanged; more non-empty feeds when older unseen posts exist.
- **Ops/docs:** note that empty feed after 90d means no unseen inventory (not model failure).

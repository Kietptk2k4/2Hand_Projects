## Context

`CandidatePoolServiceImpl` builds Recommend Feed candidates with a hard `created_at >= now - 7 days` filter, then excludes `user_seen_posts`, merges followee-priority + public fill up to `maxSize` (500). Measured locally: ~611 ACTIVE PUBLIC posts but only ~1 inside 7d → `/feed/for-you` empty after that post is seen. Ranking already applies `recency_score` with a 7-day half-life in `PostFeatureBuilder` / LightGBM features; that must stay so older posts are recallable but not treated as “fresh”.

## Goals / Non-Goals

**Goals:**

- Progressive recall windows **7 → 30 → 90** days when post-filter pool size &lt; **N** (default 20).
- Preserve followee-first + public fill + seen exclusion + status/visibility/moderation filters.
- Leave recency half-life and other feature formulas unchanged.
- Configurable N and window list via Spring config.
- Unit-testable window selection logic.

**Non-Goals:**

- Seen TTL / re-impression after cooldown.
- Fixing simulation seed `created_at` (separate change).
- Changing `/feed/global` or FE empty-state copy.
- Object-store / ANN retrieval; increasing maxSize beyond current 500 unless needed for tests.
- Returning chosen window in the public API response (log-only for Phase 1).

## Decisions

### D1 — Fallback trigger = count after filters, not raw Mongo hits
- **Choice:** Advance window when the merged unseen candidate list size is `&lt; N`.
- **Why:** Seen + followee merge defines what ranking actually receives; raw query count would over-count.
- **Alternatives:** Trigger on Mongo count before seen filter (wrong for heavy-seen users).

### D2 — Default N = 20
- **Choice:** Match default page size / one full first page (`FEED_PAGE_SIZE`).
- **Why:** Enough to avoid empty first paint without always expanding to 90d when 7d already has 50+.
- **Alternatives:** N = 50 (more aggressive widen); N = 1 (almost never widen).

### D3 — Windows fixed ladder 7, 30, 90
- **Choice:** Ordered list; first window with `size >= N` wins; else last window’s result.
- **Why:** Simple, predictable, matches product exploration.
- **Alternatives:** Single configurable max days; exponential backoff.

### D4 — Re-query per window (not expand in place)
- **Choice:** For each window day value, run the same query shape with `created_at >= now - W`, rebuild merge.
- **Why:** Clearest correctness; 3 queries worst-case only when 7d and 30d both fail threshold.
- **Alternatives:** One 90d query then filter by window in memory (one scan, more memory) — acceptable later optimization if latency shows pain.

### D5 — Recency half-life untouched
- **Choice:** No changes to `PostFeatureBuilder` recency formula (7d half-life).
- **Why:** Separates recall inventory from freshness scoring; older posts can appear with lower recency contribution.
- **Alternatives:** Soften half-life when wide window used (couples layers; reject for Phase 1).

### D6 — Lifetime seen remains
- **Choice:** Keep excluding all `user_seen_posts` IDs regardless of window.
- **Why:** Out of scope for this change; documents that exhaust inventory still yields empty.
- **Alternatives:** Seen TTL (future change).

### D7 — Config keys
- **Choice:**  
  - `social.recommendation.recall.min-pool-size` ← `SOCIAL_RECOMMENDATION_RECALL_MIN_POOL_SIZE` (default 20)  
  - `social.recommendation.recall.window-days` ← e.g. `7,30,90` (list)
- **Why:** Ops can tune without code change.

## Risks / Trade-offs

- **[Risk] Extra Mongo queries on sparse catalogs** → Mitigation: only widen when needed; log window used; index on `created_at` + status assumed.
- **[Risk] Older low-quality posts surface** → Mitigation: ranker + 7d recency half-life down-weight age; engagement features still apply.
- **[Risk] User who saw entire catalog still empty** → Mitigation: document; empty is correct until new content or seen-TTL change.
- **[Trade-off] Worst-case 3× query latency** → Accept for MVP; optimize to single wide fetch if metrics require.

## Migration Plan

1. Deploy Social with new recall logic + defaults (7,30,90 / N=20).
2. No DB migration.
3. Verify `/feed/for-you` returns items when older unseen posts exist.
4. Rollback: revert Social jar / compose image; behavior returns to hard 7d.

## Open Questions

- None blocking (N=20 and 7/30/90 locked for Phase 1).

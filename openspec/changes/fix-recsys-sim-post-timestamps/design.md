## Context

Social online recall (`CandidatePoolServiceImpl`) filters ACTIVE/PUBLIC posts with `created_at >= Instant.now() − windowDays` where `created_at` is mapped as `Instant` / BSON Date. Progressive recall (7 → 30 → 90) only helps when documents are **Date-typed and inside the window**.

Current sim writer (`write_social_posts_mongo`) sets:

```python
"created_at": "2025-12-20T10:00:00Z"  # str → BSON string
```

Mongo `$gte` against Date does not match strings. Offline `to_utc_iso` still parses strings for train. Interaction engine uses a sim clock (`start_at` default ~2026-01-01 + `sim_days`), so even a Date at Dec 2025 is stale vs wall-clock serve in mid/late 2026.

Approach **C**: fix writers for future seeds **and** ship a one-shot migrate for the existing ~600 posts.

## Goals / Non-Goals

**Goals:**

- Future sim seeds write BSON Date `created_at` / `updated_at` on a shared clock ending near wall-clock `now` (or explicit `start_at` + `sim_days`).
- Existing bad docs can be repaired in place (dry-run + apply) into a recent window so `/for-you` sees hundreds of candidates.
- Preserve Social Java serving code and recall ladder unchanged.
- Document ops: migrate → optional clear `user_seen_posts` for test users → verify recall logs / for-you.

**Non-Goals:**

- Seen TTL, soft re-surface, or empty-pool content fallback.
- Changing half-life / ladder windows in Social.
- Mandatory full retrain as part of this change (recommended after re-sim for train/serve clock consistency, not required for serve-only demo).
- Production migrate of real user posts (dev/sim inventory only; repair MUST be guarded like other sim writers).

## Decisions

### D1 — Writer stores `datetime` (UTC), not ISO string

- **Choice:** Pass `datetime` with `tzinfo=timezone.utc` into pymongo so BSON type is Date.
- **Why:** Matches `PostDocument.createdAt: Instant` and online queries.
- **Alternatives:** Keep string and change Social to `$or` string/Date — rejected (fragile, trains bad habit, hurts indexing).

### D2 — Spread posts across `[window_end − sim_days, window_end]`

- **Choice:** Default `window_end = utcnow()` (or injectable `start_at + sim_days` when engine passes shared end). Assign each post a deterministic offset by index (and optional niche jitter) so inventory is not a single timestamp spike.
- **Why:** Recency features and recall windows need diversity; hardcoding one day collapses inventory into one bucket.
- **Alternatives:** All posts = `utcnow` — rejected (unrealistic recency feature mass). Keep engine `start_at=2026-01-01` without shifting end to now — rejected for serve-first demos against wall clock.

### D3 — Migrate script: convert + re-time, guarded

- **Choice:** Dev CLI under `recsys-offline` (e.g. `python -m simulation.repair_post_timestamps` or `scripts/repair_mongo_post_timestamps.py`) gated by `RECSYS_SIM_ALLOW=1` (same family as other sim writers).
- **Behavior:**
  - Select posts where `created_at` is string **or** Date older than cutoff (optional flag).
  - Parse string → datetime; assign new Date in `[now − window_days, now]` (default `window_days=21` or `90`, configurable).
  - Set `updated_at = created_at` (or now) consistently.
  - `--dry-run` prints counts by current BSON type / in-window vs out-of-window; apply prints updated count.
- **Why:** Unblocks current Docker Mongo without full re-seed; window default chosen to sit inside the 90d ladder (prefer 21d so 7d/30d also work for many posts).
- **Alternatives:** Only re-seed — slower for local demo. Only convert type without re-time — still outside 90d.

### D4 — Engine clock optional follow-up

- **Choice:** Writer accepts optional `start_at` / `sim_days` / `end_at` from the same CLI/config as simulate. If not passed, writer defaults to `end_at=utcnow`, `sim_days` from skeleton volumes.
- **Why:** Approach C serve-first migrate does not require changing engine defaults in the same PR; aligning engine `start_at` to `utcnow − sim_days` is recommended in docs for next full re-sim.
- **Alternatives:** Force engine rewrite in this change — higher scope; defer unless easy.

### D5 — Do not auto-clear `user_seen_posts`

- **Choice:** Document manual SQL/delete for test users after migrate; do not wipe seen tables in the migrate script.
- **Why:** Seen state is product behavior; wiping silently hides recommendation bugs.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Migrate shifts post times while impressions stay on old sim clock → train/serve temporal skew if rebuild without re-sim | Document: serve-only OK; for full consistency re-run simulate after writer fix (or re-seed) |
| Repairing non-sim real posts if run against shared DB | Gate with `RECSYS_SIM_ALLOW`; optional filter by caption prefix `Sim ` / known UUID set from skeleton |
| Deterministic spread still clusters if N large and window small | Use uniform index-based offsets across full window; log min/max created_at |
| UTF-16 file corruption on Windows edits | Verify UTF-8 after writes; prefer StrReplace |

## Migration Plan

1. Land writer fix + unit/smoke tests.
2. Land repair CLI; run `--dry-run` against local Mongo; then apply with default window.
3. Optionally `DELETE FROM user_seen_posts WHERE user_id = '…'` for the demo user.
4. Hit `/feed/for-you`; confirm Social logs show larger `poolSize` (ideally within 7d/30d).
5. Rollback: restore Mongo dump if taken; or re-run seed with old writer only if needed (not expected). New seeds after writer fix do not need migrate.

## Open Questions

- Exact default migrate window: **21d** (stronger for 7d step) vs **90d** (max ladder). Propose **21d** default, flag `--window-days`.
- Whether to filter migrate to `caption` starting with `Sim` only — propose **yes** as default safety; `--all-string-dates` escape hatch for known-dev DBs.

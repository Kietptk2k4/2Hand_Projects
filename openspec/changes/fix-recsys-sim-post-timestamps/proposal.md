## Why

Online `/feed/for-you` candidate recall queries Mongo with BSON `Date` cutoffs (`created_at >= now − N days`). Sim seed currently writes `created_at` / `updated_at` as **ISO strings** hard-coded to `2025-12-20`, so ~600 fashion sim posts are invisible to Social recall even after the 7d→30d→90d ladder. Offline clean still parses strings, so train works while serve does not — a train/serve skew that blocks demo and empty for-you inventory. Fix now as a **separate** change from recall fallback.

## What Changes

- **Writer fix (future seeds):** `write_social_posts_mongo` stores `created_at` / `updated_at` as Python `datetime` (UTC) so pymongo persists BSON `Date`, not string.
- **Clock alignment:** Post timestamps are spread across the shared sim window (e.g. `sim_days` ending at `utcnow` or the same `start_at` used by the interaction engine), not a fixed Dec 2025 literal.
- **One-shot migrate (existing DB):** Dev script converts existing string / stale post timestamps to BSON `Date` inside a configurable recent window (default aligned to online recall, e.g. last 21–90 days) so current inventory becomes eligible without a full re-seed.
- **Docs + ops notes:** Document type + window requirement; optional clear of `user_seen_posts` for test users after migrate.
- **Out of scope:** Changing progressive recall ladder; seen-TTL / soft re-surface; mandatory retrain (migrate-only is enough for serve; full train/serve clock consistency prefers re-sim after writer fix).

## Capabilities

### New Capabilities
- `recsys-sim-post-timestamp-repair`: Dev-only one-shot Mongo repair that converts string/stale post `created_at`/`updated_at` to BSON Date values inside a recent serving window, with dry-run and summary counts.

### Modified Capabilities
- `recsys-simulation-seed`: Social Mongo post writes MUST use BSON Date timestamps on the shared sim clock so seeded posts are eligible for online candidate recall windows without type mismatch.

## Impact

- **Code:** `Services/recsys-offline/simulation/writers.py`; new migrate/repair script under `recsys-offline` (CLI or module); unit/smoke tests for writer datetime type and migrate dry-run logic.
- **Data (dev):** Mongo `posts` collection — in-place update of sim (and matching) documents; no Social Java API contract change.
- **Runtime:** Social `CandidatePoolService` unchanged; after repair, recall pool size should rise from ~tens to hundreds within 90d.
- **Docs:** `Services/recsys-offline/README.md` (or sim ops section) — writer contract + migrate how-to.
- **Related but separate:** `recsys-candidate-recall-fallback` (ladder) remains as-is; this change supplies eligible inventory those windows can see.

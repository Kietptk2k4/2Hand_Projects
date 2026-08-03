## Context

Social For You (`feed_ranker`) online path is already live. Offline train still uses shared-DB `seed-db --simulate`, unlike Commerce Home which uses file/RAM sim + Admin train modes. `user_seen_posts` has no TTL, so sparse catalogs exhaust recall.

## Goals / Non-Goals

**Goals:**
- File-only Post feed sim for cold-start train
- Admin-selectable `SEED_ONLY` | `HYBRID` | `REAL_ONLY` for feed LTR
- Daily TTL purge of `user_seen_posts` by `seen_at` with Admin-configured retention days

**Non-Goals:**
- Auto-delete historical bot users/posts/orders (manual checklist only)
- Mobile `/for-you` wiring
- Diversity / live purchase affinity online
- Dedicated Admin UI beyond system-configs tab

## Decisions

| ID | Decision |
|----|----------|
| D1 | Mirror Home keys: `social.feed.ltr.train_data_mode`, `seed_row_weight`, `real_only_min_impressions` via Admin Flyway seed |
| D2 | Feed sim writes only under `RECSYS_FEED_SIM_DIR` (default `data/feed_sim/`); reuse `run_simulation` + `result_to_cleaned_sources` → CSV files; never call `simulation.writers` |
| D3 | Resolver `feed_train_mode.py`: env when `RECSYS_FEED_CONFIG_FROM_ENV=1`, else Admin list+exact key; fail closed |
| D4 | HYBRID: union cleaned real CSVs ∪ seed CSVs; tag `data_source`; LightGBM `sample_weight` for SEED rows |
| D5 | REAL_ONLY: skip feed-sim; fail if impressions &lt; min; no silent seed fallback |
| D6 | Seen retention: delete `WHERE seen_at < now - N days`; cron daily `0 15 3 * * *`; config `social.feed.seen_posts_retention_days` default 7; property fallback if Admin unreachable |
| D7 | `seed-db --simulate` remains for legacy demos but README marks it deprecated for train |

## Risks / Trade-offs

- HYBRID needs careful dedupe of impression keys across seed and real.
- Admin HTTP from social-service adds dependency; mitigated by env fallback so cron still runs.
- Existing polluted DBs require operator cleanup before REAL_ONLY is meaningful.

## Migration Plan

1. Ship Admin config seeds + offline file sim + mode-aware build.
2. Operators delete prior sim rows (checklist in README).
3. Enable seen TTL cron; set retention via system-configs.
4. Prefer `/jobs/feed-retrain` over `seed-db` for new models.

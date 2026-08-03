## Why

Post `feed_ranker` training still relies on `seed-db --simulate` writing fake users/posts/impressions into shared Auth/Social/Commerce databases, which pollutes production-like DBs and fills `user_seen_posts` so For You recall starves. Operators need Commerce-Home-style file seed + Admin-selectable train modes, and a TTL purge so seen posts can reappear after N days.

## What Changes

- Add Admin `system_configs` for Social feed LTR train mode (`SEED_ONLY` | `HYBRID` | `REAL_ONLY`) plus seed weight and REAL_ONLY min impressions.
- Add file-only feed sim under `RECSYS_FEED_SIM_DIR` (`POST /jobs/feed-sim`) that must not INSERT into shared DBs; deprecate `seed-db` for train.
- Mode-aware feed build-dataset / retrain orchestrator reading Admin (or env when `RECSYS_FEED_CONFIG_FROM_ENV=1`).
- Add Admin config `social.feed.seen_posts_retention_days` and a daily social-service cron that deletes `user_seen_posts` where `seen_at` is older than N days.

## Capabilities

### New Capabilities

- `social-feed-train-data-mode`: Admin-configurable SEED_ONLY/HYBRID/REAL_ONLY train corpus for `feed_ranker` using file seed (no shared-DB pollution).
- `social-feed-seen-posts-retention`: TTL retention cron for `user_seen_posts` driven by Admin system-configs.

### Modified Capabilities

- `recsys-offline-ops`: Add feed-sim / feed-retrain jobs and document that Post train must not use shared-DB seed-db.
- `recsys-simulation-seed`: Mark shared-DB `seed-db` as deprecated for feed_ranker train; file sim is the cold-start path.

## Impact

- `Services/recsys-offline` (sim, mode resolver, build-dataset, orchestrator, FastAPI jobs, README)
- `Services/admin-service` Flyway seed for system_configs
- `Services/social-service` (`UserSeenPostsRepository`, retention scheduler, optional Admin HTTP client)
- Admin FE system-configs tab (reuse existing CRUD; no new UI)
- Ops checklist to manually delete prior DB seed pollution

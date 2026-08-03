## 1. Admin configs

- [x] 1.1 Add Admin Flyway seed for `social.feed.ltr.train_data_mode` (SEED_ONLY), `seed_row_weight` (0.5), `real_only_min_impressions` (5000), `social.feed.seen_posts_retention_days` (7)
- [x] 1.2 Document keys are editable on system-configs tab (no dedicated UI)

## 2. Offline feed train mode

- [x] 2.1 Implement `pipelines/feed_train_mode.py` (Admin or `RECSYS_FEED_CONFIG_FROM_ENV`)
- [x] 2.2 Implement `pipelines/feed_sim.py` writing cleaned CSVs under `RECSYS_FEED_SIM_DIR` without DB writers
- [x] 2.3 Implement mode-aware feed build-dataset (SEED_ONLY / HYBRID / REAL_ONLY + sample_weight + meta)
- [x] 2.4 Wire sample_weight into feed LightGBM train when present
- [x] 2.5 Implement `feed_orchestrator` + `POST /jobs/feed-sim` and `POST /jobs/feed-retrain`
- [x] 2.6 Update config/settings and `.env.example` (`RECSYS_FEED_SIM_DIR`, feed env overrides)

## 3. Seen posts TTL

- [x] 3.1 Add `UserSeenPostsRepository.deleteSeenBefore(Instant)`
- [x] 3.2 Add Admin system-config client (optional) + retention resolver with property fallback
- [x] 3.3 Add `SeenPostsRetentionScheduler` daily cron
- [x] 3.4 Add application.yml properties for retention days and cleanup cron

## 4. Tests and docs

- [x] 4.1 Unit tests: mode resolver, feed_sim no writers, HYBRID weights, REAL_ONLY fail-closed
- [x] 4.2 Unit tests: deleteSeenBefore + scheduler
- [x] 4.3 README: deprecate seed-db for train; feed-retrain flow; DB cleanup checklist

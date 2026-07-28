## 1. Writer fix (future seeds)

- [x] 1.1 Update `write_social_posts_mongo` to accept optional `end_at` / `sim_days` (default `end_at=utcnow`, `sim_days` from skeleton volumes)
- [x] 1.2 Assign each post `created_at`/`updated_at` as timezone-aware UTC `datetime` spread across `[end_at − sim_days, end_at]` (no ISO strings, no hard-coded `2025-12-20`)
- [x] 1.3 Wire CLI/seed path so shared clock params can be passed when available; keep backward-compatible defaults

## 2. Timestamp repair CLI (existing DB)

- [x] 2.1 Add repair module/entrypoint gated by `RECSYS_SIM_ALLOW` (refuse when unset)
- [x] 2.2 Implement dry-run: count posts by `created_at` BSON type and in-window vs would-update (default filter caption prefix `Sim `)
- [x] 2.3 Implement apply: convert string/stale → BSON Date distributed in `[now − window_days, now]` (default `window_days=21`, flag override); set `updated_at` consistently; print update summary
- [x] 2.4 Add escape hatch to widen scope beyond default sim filter for known-dev DBs

## 3. Tests

- [x] 3.1 Unit/smoke: writer documents use `datetime` (not `str`) and timestamps span more than one distinct value when N>1
- [x] 3.2 Unit: repair dry-run does not write; apply updates string docs into window; guard rejects without `RECSYS_SIM_ALLOW`

## 4. Docs and verify

- [x] 4.1 Document writer Date+window contract and repair dry-run/apply steps in `Services/recsys-offline/README.md` (optional clear `user_seen_posts` for test users)
- [x] 4.2 Run repair dry-run then apply against local Mongo; confirm ACTIVE PUBLIC Date-in-window count rises materially
- [x] 4.3 Call `/feed/for-you` (after optional seen clear) and confirm Social recall `poolSize` is no longer stuck near zero from missing Date inventory

## 1. Social Postgres training tables

- [x] 1.1 Add Flyway migration creating `model_artifacts` with `(model_name, version)` UNIQUE, `format`, `artifact_path`, `metrics` JSONB, `is_active`, `trained_at`
- [x] 1.2 Add partial unique index `uk_model_artifacts_one_active` on `(model_name) WHERE is_active = TRUE`
- [x] 1.3 Add Flyway DDL for `post_impression_log` with `user_id`, `post_id`, `shown_at`, `rank_position`, `model_version INT NULL`, `request_id`, and indexes `(user_id, shown_at DESC)`, `(post_id, shown_at DESC)`, `(user_id, post_id, shown_at)`
- [x] 1.4 Add Flyway DDL for `user_seen_posts` with PK `(user_id, post_id)`, `seen_at`, and index `(user_id, seen_at DESC)`
- [x] 1.5 Update `docs/database/social-schema.md` to document the three tables and version/`NULL` rule-based convention

## 2. Offline FastAPI package (Task 1)

- [x] 2.1 Scaffold `Services/recsys-offline` with `app/main.py`, `pipelines/`, `requirements.txt`, and README stating offline-only (no recommend serving)
- [x] 2.2 Implement `/health` endpoint
- [x] 2.3 Add env-based config for Social Postgres URL, Mongo URL, dataset output dir, and artifact dir
- [x] 2.4 Implement `POST /jobs/clean` (or equivalent) that invokes the clean pipeline and returns kept/dropped summary
- [x] 2.5 Add stub routes or clearly documented placeholders for `/jobs/train`, `/jobs/evaluate`, `/jobs/export-activate` without online predict APIs
- [x] 2.6 Add minimal automated tests or smoke script for health + clean job failure on missing config

## 3. Dataset clean pipeline (Task 2)

- [x] 3.1 Implement read-only extractors for Mongo `posts` and `comments`
- [x] 3.2 Implement read-only extractors for Postgres `post_likes`, `post_saves`, `follows`, `search_history`, and `post_impression_log` (graceful if empty)
- [x] 3.3 Implement null dropping, duplicate removal by natural keys, and validation (UUID, self-follow, empty keyword, DRAFT/DELETED posts)
- [x] 3.4 Implement UTC timestamp normalization and hashtag normalize (strip `#`, trim, lowercase, dedupe within post)
- [x] 3.5 Write cleaned outputs to configured filesystem paths (CSV or Parquet) without mutating source DB
- [x] 3.6 Emit structured drop logs / summary file with per-source kept, dropped, and reason breakdown
- [x] 3.7 Unit-test clean rules (hashtag, self-follow, null author, duplicate likes) with fixtures

## 4. Java hooks alignment (optional in this change)

- [x] 4.1 Persist recommend impressions into `post_impression_log` with `model_version INT NULL` when rule-based
- [x] 4.2 Upsert `user_seen_posts` from recommend path for candidate exclusion
- [x] 4.3 (Optional follow) Resolve active `model_artifacts.artifact_path` for `ModelLoader` without calling Python at request time

## 5. Verification

- [x] 5.1 Apply migration on local Social DB and verify one-active constraint rejects dual active rows
- [x] 5.2 Run clean job against local/dev data (or fixtures) and confirm outputs + drop summary
- [x] 5.3 Confirm Social recommend still works with rule-based/ONNX fallback and does not call FastAPI

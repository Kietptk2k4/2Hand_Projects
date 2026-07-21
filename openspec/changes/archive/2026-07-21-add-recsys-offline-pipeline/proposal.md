## Why

Recommended Feed ranking in `social-service` already has Candidate Pool, feature vectors, ONNX `ModelLoader`, and rule-based fallback, but there is no offline Python pipeline to clean training data, train LightGBM, or register model versions. Without impression/seen/artifact tables and a read-only dataset clean step, labels and retrain-on-schedule cannot be completed while keeping serving inside Java (no HTTP ML service).

## What Changes

- Add a new **offline** Python FastAPI package (`recsys-offline`) that only triggers clean / train / evaluate / export jobs — Social Service never calls it at recommend time.
- Add Flyway migration for three Social Postgres tables: `model_artifacts`, `post_impression_log`, `user_seen_posts`.
- Enforce **one active artifact per `model_name`** and unify **`model_version` as INT** (aligned with `model_artifacts.version`; rule-based fallback uses `NULL`).
- Add a **read-only** clean-dataset script: extract from Mongo/Postgres → drop null/dupes → normalize timestamps/hashtags → validate → write clean dataset files + drop-count logs (no UPDATE/DELETE on production DB).
- Wire Java recommend path later to persist impressions / seen posts and resolve active artifact (scope may start with schema + stubs if existing TODOs already exist).

## Capabilities

### New Capabilities

- `recsys-offline-ops`: Offline FastAPI job API to run clean/train/evaluate/export and register/activate model artifacts without participating in recommend serving.
- `recsys-training-tables`: Social Postgres schema for `model_artifacts`, `post_impression_log`, and `user_seen_posts` with versioning and impression/seen semantics for labels and candidate filtering.
- `recsys-dataset-clean`: Read-only extract-and-clean pipeline producing a labeled/feature-ready dataset from posts, engagements, follows, search history, and impressions, with structured drop logging.

### Modified Capabilities

- (none — no existing OpenSpec capability specs for recommend feed yet)

## Impact

- **New**: Python package under `Services/recsys-offline` (or equivalent), dependencies (FastAPI, DB drivers, pandas/lightgbm later), Docker/cron entrypoints.
- **Social Postgres**: new Flyway migration; docs/schema updates.
- **Mongo/Postgres read access** for offline jobs (social posts/comments + likes/saves/follows/search/impressions); commerce optional/deferred for `cross_domain_product_score`.
- **Existing Java**: `PostImpressionLoggerImpl`, `CandidatePoolServiceImpl`, `ModelLoader` will eventually consume the new tables/paths; Phase 1 of this change prioritizes schema + offline package + clean script.
- **Non-goals**: online Python inference, Kafka for serving, mutating production rows during clean, TensorFlow Serving.

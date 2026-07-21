## Context

2Hands Recommended Feed ranks `(user, post)` with six features and LightGBM (ONNX) inside `social-service`. Java already has Candidate Pool, `PostFeatureBuilder`, `RuleBasedRankingModel`, `LightGBMRankingModel`, and `ModelLoader` (file path + cron reload). Gaps: no offline Python package, no training tables (`model_artifacts`, `post_impression_log`, `user_seen_posts`), and no clean-dataset step for labels.

Constraints from architecture:
- No online ML microservice on the recommend path.
- Python offline only: clean → train → evaluate → export artifact.
- Java loads ONNX; rule-based is baseline/fallback.
- Clean MUST NOT UPDATE/DELETE production DB rows.

Stakeholders: Backend (serve + migration wiring), ML (offline pipeline), Data (seed/bot later).

## Goals / Non-Goals

**Goals:**
- Ship offline FastAPI job host for clean (and stubs/hooks for train/evaluate/export).
- Add Social Postgres tables for artifacts, impressions, and seen posts with INT version + one-active invariant.
- Implement read-only extract → clean → dataset files + drop metrics for Phase 1 sources.
- Keep serving contract: Social never HTTP-calls Python at recommend time.

**Non-Goals:**
- Online Python inference / TensorFlow Serving / Kafka for ranking.
- Full LightGBM train+ONNX export productionization beyond job stubs (may land as follow-up tasks in same change if scoped in tasks.md).
- Bot simulation / persona seed (separate data workstream).
- Commerce-backed `cross_domain_product_score` enrichment (column may be 0.0).
- Purging/expiring `user_seen_posts`.
- Changing recommend API response shape for clients.

## Decisions

### D1 — Offline ML Ops (Option A), not inference service

- **Choice:** `Services/recsys-offline` FastAPI exposes internal job endpoints (`/health`, `/jobs/clean`, later `/jobs/train`, `/jobs/evaluate`, `/jobs/export-activate`). Cron/admin/CLI invoke them.
- **Why:** Matches “no ML service on recommend path”; Java already serves ONNX.
- **Alternatives:** (B) HTTP predict from Social — rejected; (C) scripts-only without FastAPI — acceptable subset, but FastAPI helps schedule/health in Docker.

### D2 — Tables live in Social Postgres

- **Choice:** Flyway under `social-service` for `model_artifacts`, `post_impression_log`, `user_seen_posts`.
- **Why:** Recommend, impressions, and seen filtering are owned by Social; no cross-service DB writes.
- **Alternatives:** Separate ML database — extra ops for đồ án, delayed value.

### D3 — Version INT + one active + rule-based NULL

```
model_artifacts.version INT
post_impression_log.model_version INT NULL  -- NULL = rule-based / unknown
UNIQUE (model_name, version)
UNIQUE (model_name) WHERE is_active = TRUE
```

- **Activate:** single transaction deactivate previous + activate new.
- **Why:** Join metrics/debug without string parsing; partial unique prevents dual-active races.
- **Alternatives:** `model_version VARCHAR` — rejected after explore; sentinel `0` for rule-based — rejected in favor of `NULL` clarity.

### D4 — Clean = dataset files, not in-place DB mutation

- **Choice:** Extract → transform → Parquet/CSV under a configurable output dir; log drop counts by reason.
- **Sources (Phase 1):** Mongo `posts`, `comments`; Postgres `post_likes`, `post_saves`, `follows`, `search_history`, `post_impression_log` (when present).
- **Excluded from samples:** `outbox_events`, `processed_domain_events`, `comment_reaction`, `user_seen_posts` (runtime filter only).
- **Hashtag normalize:** strip `#`, trim, lowercase (mirror Java `PostFeatureBuilder`).
- **Timestamps:** UTC-normalized ISO/UTC timestamps.
- **Label:** deferred to build-dataset step after clean; clean validates raw entities used for labels. If impressions empty, clean still produces cleaned entity extracts and logs warning.

### D5 — Package layout

```
Services/recsys-offline/
  app/main.py              # FastAPI
  pipelines/clean_data.py
  pipelines/...            # train/eval/export stubs as tasks allow
  requirements.txt
  README.md
```

Config via env: Social PG URL, Mongo URL, output path, model artifact dir.

### D6 — Java wiring scope in this change

- Migration lands first.
- Persist impressions/seen and resolve active `artifact_path` may be partial: implement if tasks include them; otherwise leave existing TODOs with schema ready.
- `ModelLoader` today uses file config path; activation may copy/symlink active artifact to that path or later read DB — prefer keep file reload first, write `artifact_path` consistently.

## Risks / Trade-offs

- [Empty impressions early] → Clean/extract still works; label quality waits for logger + bot — document dependency.
- [Feature drift Java vs Python] → Document shared formulas; clean only normalizes raw fields; feature build should mirror `PostFeatureBuilder` in a later task.
- [Dual-active race] → Partial unique index + transactional activate.
- [High impression volume] → Indexes on `(user_id, shown_at)`, `(user_id, post_id, shown_at)`; partitioning later if needed.
- [FastAPI mistaken for serving] → Name package `recsys-offline`, README states non-serving; no public gateway route by default.
- [Cross-DB extract complexity] → Start with Social only; commerce optional flag off.

## Migration Plan

1. Deploy Flyway `V2__create_recsys_training_tables.sql` (or next free version) on Social PG.
2. Deploy `recsys-offline` container (internal network only).
3. Run `/jobs/clean` against non-prod; verify output + drop logs.
4. Wire Java impression/seen writers when ready; backfill not required for empty tables.
5. Rollback: drop three tables only if unused; keep artifact files on disk; FastAPI undeploy independent of Social serve (fallback rule-based unchanged).

## Open Questions

- Exact next Flyway version number in social-service (inspect at apply time).
- Whether Phase 1 tasks include full train+ONNX export or stop at clean + schema + FastAPI scaffold with stub job routes.
- Output format preference: Parquet vs CSV for đồ án tooling.
- Whether `model_name` column is stored on `post_impression_log` (recommended optional default `feed_ranker`) — include if low cost.

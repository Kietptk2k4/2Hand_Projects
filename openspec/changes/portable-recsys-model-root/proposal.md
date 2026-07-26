## Why

`export-activate` currently persists absolute host paths (e.g. `D:\...\feed_ranker_v1.onnx`) into Social `model_artifacts.artifact_path`. Social `ModelLoader` opens that string as-is, so a Linux Docker `social-service` cannot load an activated ONNX even when the file exists on the host. Operators need portable artifact references so the same DB row works on Windows host, Docker, and later Kubernetes by changing only a model-root env (plus a volume mount).

## What Changes

- **BREAKING (contract):** `model_artifacts.artifact_path` MUST store a portable relative name (Phase 1: basename such as `feed_ranker_v1.onnx`), not an environment-specific absolute path.
- Offline `export-activate` writes only the portable relative name to Postgres (still writes ONNX bytes under its local artifact directory).
- Social resolves the on-disk file as `MODEL_ROOT` + relative name (config/env), with path-safety checks (no `..`, no absolute override from DB).
- Docker Compose (`dev`, and `apps` if applicable) bind-mounts the shared artifacts directory to the configured `MODEL_ROOT` inside `social-service`.
- One-time migration/ops update for any existing absolute `artifact_path` rows (e.g. active `feed_ranker` v1) to basename.
- Docs/env examples document `MODEL_ROOT` / Social equivalent and the mount convention.
- Admin registry continues to display the stored relative value; runtime status remains the source of truth for whether ONNX actually loaded.

## Capabilities

### New Capabilities

- `recsys-model-artifact-path`: Contract for portable artifact references — DB stores relative basename; runtime joins configurable model root; Docker/K8s mount the shared artifacts volume at that root.

### Modified Capabilities

- `recsys-export-activate`: Persist portable relative `artifact_path` instead of absolute resolved filesystem path.
- `recsys-ranking-model-status`: Runtime load path resolution uses model root + relative artifact; fallback reasons remain accurate when the resolved file is missing.
- `recsys-training-tables`: Clarify that `artifact_path` is a portable relative reference, not a host-absolute path.

## Impact

- **Offline:** `Services/recsys-offline/pipelines/export_activate.py` (+ tests), README / `.env.example`.
- **Social:** `ModelLoader`, `application.yml` / env (`SOCIAL_RECOMMENDATION_MODEL_ROOT` or equivalent), unit tests; optional status fields if we expose resolved path in logs only.
- **Infra:** `Infrastructure/docker-compose.dev.yml` (and `docker-compose.apps.yml` if Social runs as image) volume mounts for artifacts → `/models/recsys`.
- **Data:** Existing `model_artifacts` rows with absolute paths need a one-time update.
- **Admin FE:** No activation UX change; list may show relative paths (acceptable).
- **Out of scope:** Changing metrics JSON absolute paths for `train_meta` / `evaluate_report`; MinIO/object-storage for models; renaming the DB column.

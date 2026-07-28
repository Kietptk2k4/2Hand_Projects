## 1. Offline export portable path

- [x] 1.1 Change `export_activate` to persist `artifact_path` as ONNX basename only (e.g. `feed_ranker_v{N}.onnx`), not `Path.resolve()` absolute
- [x] 1.2 Update export-activate unit tests to assert portable relative `artifact_path` (and that summary may still report local absolute for operator logs if useful)
- [x] 1.3 Document in `Services/recsys-offline/README.md` that DB stores basename; Social joins `MODEL_ROOT`

## 2. Social model root resolution

- [x] 2.1 Add `social.recommendation.model-root` config wired from `SOCIAL_RECOMMENDATION_MODEL_ROOT` (document as MODEL_ROOT in ops notes)
- [x] 2.2 Update `ModelLoader` to resolve active artifact as `modelRoot` + safe basename; reject `..`, separators, absolutes
- [x] 2.3 Keep existing `model-path` fallback when no usable active relative artifact
- [x] 2.4 Extend `ModelLoader` unit tests for join success, missing file → `file_not_found`, unsafe path rejected
- [x] 2.5 Update `.env.example` / `.env.docker` examples with `SOCIAL_RECOMMENDATION_MODEL_ROOT`

## 3. Docker mount

- [x] 3.1 Bind-mount `../Services/recsys-offline/data/artifacts` → `/models/recsys:ro` on `social-service` in `docker-compose.dev.yml`
- [x] 3.2 Set default/example `SOCIAL_RECOMMENDATION_MODEL_ROOT=/models/recsys` for Docker Social
- [x] 3.3 Mirror the same volume (+ env) on `docker-compose.apps.yml` social-service if that profile is used for local verify
- [x] 3.4 Note mount + recreate steps in Infrastructure or Social README

## 4. Data migration and verify

- [x] 4.1 Provide/run one-time SQL (or scripted) update to basename-ify existing absolute `model_artifacts.artifact_path` rows
- [x] 4.2 Recreate/restart `social-service` with mount + env; confirm log loads ONNX for active version
- [x] 4.3 Verify Admin recommendation-model-status reports `lightgbm` + version; artifacts list shows relative path
- [x] 4.4 Smoke recommend-feed still succeeds (LightGBM path when session present)

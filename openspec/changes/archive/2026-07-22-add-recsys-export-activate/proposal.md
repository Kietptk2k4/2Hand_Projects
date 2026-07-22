## Why

LightGBM training and evaluate already produce `model.txt` and `evaluate_report.json`, but `/jobs/export-activate` is still a stub. Without ONNX export, `model_artifacts` versioning/publish, metric gates vs the locked rule-based baseline, and an admin-visible registry, Social cannot safely promote a new ranker for the graduation Recommended Feed milestone.

## What Changes

- Implement `POST /jobs/export-activate`: convert LightGBM `model.txt` → ONNX, smoke-verify vs native scores, insert a new `model_artifacts` version (`MAX(version)+1`), apply metric gate, activate or leave inactive with `rejected_by_metrics`.
- Persist metrics metadata into `model_artifacts.metrics` (JSONB): AUC, Precision/Recall/HitRate@10, `feature_order`, `feature_version=1`, baseline weights, paths to train/evaluate artifacts, ONNX verify + gate results.
- Metric gate (activate only if pass): AUC ≥ baseline **and** Precision@10 ≥ baseline; null/undefined required metrics → fail closed (no activate).
- ONNX smoke: 32–64 test samples; `max_abs_diff ≤ 1e-4` or **job fails** (no DB insert).
- Gate reject: still insert row with `is_active=false` and `rejected_by_metrics`; keep prior active version (natural rollback). Job returns success with status such as `exported_not_activated` vs `activated`.
- Social admin: list `model_artifacts` (+ optional status enrichment) for admin UI registry (active / rejected / inactive, metrics drill-down).
- Admin FE: minimal model registry panel reading Social admin APIs (no trigger of offline jobs from UI).
- Does **not** re-run evaluate inside export; requires existing `evaluate_report.json`. Java `ModelLoader` cron reload remains the activation path (no Social restart).

## Capabilities

### New Capabilities
- `recsys-export-activate`: Offline export LightGBM→ONNX, verify, versioned `model_artifacts` insert, metric gate, activate vs reject semantics.
- `recsys-model-registry`: Admin-facing list/view of model artifact versions and metrics for registry UI.

### Modified Capabilities
- `recsys-offline-ops`: Replace stub `export-activate` with real job contract (success statuses, fail-closed inputs/verify).

## Impact

- `Services/recsys-offline`: new export pipeline, deps (ONNX converter + runtime for verify), wire `POST /jobs/export-activate`, README/tests.
- Social Postgres `model_artifacts` (existing table): Python writer via `SOCIAL_POSTGRES_URL`; transactional activate.
- `Services/social-service`: admin list artifacts API; reuse ADMIN/MODERATOR auth pattern.
- Admin frontend: registry table/panel.
- Runtime: Social continues loading ONNX via `ModelLoader` scheduled reload after activate.

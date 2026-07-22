## 1. Dependencies and data load

- [x] 1.1 Add `lightgbm` to `Services/recsys-offline/requirements.txt`
- [x] 1.2 Implement parquet → `X`/`y` loader with fixed six-feature order; fail closed on missing/empty train; NaN → 0.0 with warning count

## 2. Train pipeline

- [x] 2.1 Implement binary LightGBM train (`objective=binary`, eval `binary_logloss` + `auc`) with Phase 1 default hyperparams
- [x] 2.2 Early stopping when val non-empty; if val missing/empty, warn `no_early_stopping` and train without early stop
- [x] 2.3 Save `model.txt` + `train_meta.json` under `RECSYS_ARTIFACT_DIR` (feature_order, params, metrics, warnings, best_iteration)

## 3. API and docs

- [x] 3.1 Wire `POST /jobs/train` to the pipeline; map ValueError → 400 with structured detail
- [x] 3.2 Update README with train inputs/outputs and binary/early-stop policy

## 4. Tests and smoke

- [x] 4.1 Unit tests: missing train fails; train+val early stop path; empty val warns and succeeds; feature order in meta
- [x] 4.2 Smoke: train on fixture split parquet; confirm `model.txt` + `train_meta.json`

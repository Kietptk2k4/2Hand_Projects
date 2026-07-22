## Why

Recommended Feed offline pipeline already produces time-split parquet datasets, but `/jobs/train` is still a stub. Without a real LightGBM binary trainer that uses the same six features Java serves, the project cannot produce a native model artifact or training metrics needed before ONNX export and activation.

## What Changes

- Implement LightGBM **binary** training from `dataset_train.parquet` (required) with optional `dataset_val.parquet` early stopping.
- Objective `binary`; track `binary_logloss` (primary early-stop) and report `auc`.
- Persist native booster (`model.txt`) + `train_meta.json` under `RECSYS_ARTIFACT_DIR` (feature order locked to Java ONNX input order).
- If validation is missing or empty: **warn and train without early stopping** (do not fail).
- Fail closed only when train parquet is missing or unusable.
- Wire real `POST /jobs/train`; update README and deps (`lightgbm`).
- **Out of scope:** ONNX export, `model_artifacts` DB activate, LambdaRank, full evaluate@K vs rule-based (follow-up changes).

## Capabilities

### New Capabilities

- `recsys-lightgbm-train`: Offline LightGBM binary training job that loads split parquet, trains with optional early stopping, logs metrics, and saves native model + meta.

### Modified Capabilities

- `recsys-offline-ops`: Replace train stub contract with a real train job endpoint (still no online predict/recommend).

## Impact

- **Code:** `Services/recsys-offline` (`pipelines/train.py` or equivalent, `app/main.py`, `requirements.txt`, README, tests).
- **Inputs:** `dataset_train.parquet` (+ optional `dataset_val.parquet`) under `RECSYS_DATASET_OUTPUT_DIR`.
- **Outputs:** `RECSYS_ARTIFACT_DIR/model.txt`, `train_meta.json`.
- **Serving:** No Java/online path change in this change; feature column order must match `LightGBMRankingModel` for later export.

## Context

`recsys-offline` can clean, build `dataset.parquet`, and split 80/10/10. `/jobs/train` returns `not_implemented`. Social `LightGBMRankingModel` expects six float features in fixed order and loads ONNX later via `ModelLoader` / `model_artifacts`. Phase 1 needs a real binary LightGBM trainer that saves a native booster first; ONNX export is a follow-up.

Locked explore decisions: objective `binary`; early-stop on `binary_logloss` + report `auc`; native save only; val missing/empty → warn and train without early stopping.

## Goals / Non-Goals

**Goals:**
- Load train parquet (required) and optional val parquet.
- Train LightGBM binary with documented defaults and early stopping when val usable.
- Write `model.txt` + `train_meta.json` (params, metrics, feature_order, warnings, best_iteration).
- Expose working `POST /jobs/train` with fail-closed on missing train data.
- Unit tests on synthetic data (with/without val).

**Non-Goals:**
- ONNX conversion / DB activate / Java reload.
- LambdaRank / Optuna.
- Full evaluate job (Precision@K vs rule-based).
- Changing build-dataset or split ratios.

## Decisions

### D1 — Binary objective (not LambdaRank)
- **Choice:** `objective: binary`, labels 0/1 from 24h interaction.
- **Why:** Matches current labels; simpler for Phase 1; scores still rank candidates at serve time.
- **Alt:** lambdarank by `request_id` — deferred until groups are dense enough.

### D2 — Metrics
- **Choice:** Early stop / primary eval = `binary_logloss`; also evaluate `auc` for meta/report.
- **Why:** Aligns with binary objective; AUC is thesis-friendly.

### D3 — Feature order (serve parity)
- **Choice:** Fixed column list matching Java:
  `recency_score`, `engagement_score`, `hashtag_match_score`, `author_affinity_score`, `mutual_follow_score`, `cross_domain_product_score`.
- **Why:** Prevents silent train/serve skew before ONNX.

### D4 — Validation policy
- **Choice:** Non-empty `dataset_val.parquet` → early stopping; missing/empty → warning `no_early_stopping`, train for full `num_boost_round`.
- **Why:** Fixtures and early data often lack val rows; fail-closed would block smoke runs.

### D5 — Artifacts
- **Choice:** `RECSYS_ARTIFACT_DIR/model.txt` (LightGBM text) + `train_meta.json`.
- **Why:** Native format is enough for Task 5; export-activate owns ONNX + PG registry.

### D6 — Hyperparams (Phase 1 defaults)
- **Choice:** Conservative defaults e.g. `learning_rate=0.05`, `num_leaves=31`, `feature_fraction=0.8`, `bagging_fraction=0.8`, `num_boost_round=200`, `early_stopping_rounds=30` (when val present). Document in meta; allow future env overrides without blocking this change.
- **Why:** Avoid overfit on small datasets; good enough for graduation demo.

## Risks / Trade-offs

- [Class imbalance] Low positive_rate → weak AUC → Mitigate: log positive rates in meta; later class_weight / scale_pos_weight if needed.
- [Empty train] → Fail closed.
- [Feature NaN] → Fill 0.0 and warn count in meta.
- [Windows LightGBM install] → Pin known-good wheel in requirements; document Visual C++ runtime if needed.

## Migration Plan

1. `pip install -r requirements.txt` (adds lightgbm).
2. Ensure split outputs exist; call `/jobs/train`.
3. Rollback: revert package; stub behavior returns (no DB migration).

## Open Questions

- None blocking. Env-tunable hyperparams can be added during apply if cheap.

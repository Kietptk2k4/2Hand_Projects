# Recsys Offline (2Hands)

Offline ML ops package for Recommended Feed training data and model jobs.

**This is NOT an online recommendation / inference service.**
Social Service ranks posts in-process (ONNX + rule-based fallback) and never calls this API during recommend requests.

## What it does

- `GET /health` — liveness
- `POST /jobs/clean` — read-only extract + clean → CSV files + drop summary
- `POST /jobs/build-dataset` — join impressions + features + 24h labels → `dataset.parquet`
- `POST /jobs/split-dataset` — time-ordered **80/10/10** train/val/test by `shown_at` (no shuffle); fail-closed temporal leak checks; writes overlap + positive-rate report in `split_meta.json`
- `POST /jobs/train` — LightGBM **binary** train from `dataset_train.parquet` → `model.txt` + `train_meta.json`
- `POST /jobs/evaluate` — pointwise ROC-AUC + Precision/Recall/HitRate@10 (by `request_id`) vs rule-based baseline → `evaluate_report.json`
- Stub: `POST /jobs/export-activate`

## Config (env)

| Variable | Purpose |
|----------|---------|
| `SOCIAL_POSTGRES_URL` | Postgres URL for clean extract |
| `SOCIAL_MONGO_URL` | Mongo URI for clean extract |
| `SOCIAL_MONGO_DB` | Mongo database name (default `social_db`) |
| `RECSYS_DATASET_OUTPUT_DIR` | Cleaned CSV + `dataset.parquet` directory (default `data/cleaned`) |
| `RECSYS_ARTIFACT_DIR` | Model artifact directory (`model.txt`, `train_meta.json`) |

## Typical offline flow

```bash
# 1) Clean entities from DB → CSV under RECSYS_DATASET_OUTPUT_DIR
curl -X POST http://localhost:8095/jobs/clean

# 2) Build labeled training table (needs post_impression_log.csv + posts.csv)
curl -X POST http://localhost:8095/jobs/build-dataset

# 3) Time split (80/10/10 by shown_at; temporal leak fails the job)
curl -X POST http://localhost:8095/jobs/split-dataset

# 4) Train LightGBM binary (requires dataset_train.parquet)
curl -X POST http://localhost:8095/jobs/train

# 5) Evaluate on test split vs rule-based baseline
curl -X POST http://localhost:8095/jobs/evaluate
```

Outputs (under `RECSYS_DATASET_OUTPUT_DIR`):

- `dataset.parquet`
- `dataset_meta.json` (row count, positive rate, warnings)
- `dataset_train.parquet` / `dataset_val.parquet` / `dataset_test.parquet` after split
- `split_meta.json` — counts, time ranges, positive rates, Jaccard user/post overlap % (informational; entity overlap does not fail the job)

Train outputs (under `RECSYS_ARTIFACT_DIR`):

- `model.txt` — LightGBM native text model
- `train_meta.json` — feature_order, params, metrics, warnings, best_iteration
- `evaluate_report.json` — AUC + @10 metrics for model and baseline (after evaluate)

Train policy:

- Objective: `binary` · metrics: `binary_logloss` (early-stop) + `auc` (reported)
- Feature order matches Java `LightGBMRankingModel` (6 scores)
- If `dataset_val.parquet` exists and is non-empty → early stopping
- If val missing/empty → warning `no_early_stopping`, still trains (does not fail)
- Missing `dataset_train.parquet` → fail closed (HTTP 400)
- ONNX export / DB activate are separate follow-up jobs

Evaluate policy:

- Inputs: `dataset_test.parquet` + `model.txt` (fail closed if missing)
- Pointwise: ROC-AUC (undefined if single class → `null` + warning)
- Ranking @10: group by `request_id` (fallback `user_id` + warning `no_request_id`)
- Precision@10 denominator: `min(10, n)` for group size `n`
- Baseline weights (must match Java `RuleBasedRankingModel`):  
  `0.12 / 0.28 / 0.22 / 0.13 / 0.13 / 0.12`  
  (recency, engagement, hashtag, author_affinity, mutual_follow, cross_domain)
- Report: JSON only (`evaluate_report.json`) — no markdown

Online status (Social Service, not this package):

- `GET /api/v1/social/admin/recommendation-model-status` — ADMIN/MODERATOR  
  Returns active model id/version, runtime mode (`onnx` | `rule_based`), load ok, fallback reason.

Split notes:

- Sort: `shown_at` ASC, tie-break `user_id`, `post_id`
- Cuts: `int(n*0.80)` / `int(n*0.90)` (remainder → test)
- Tiny `n` may yield empty val/test with warnings (`small_n`, `empty_val`, …)

Optional `user_purchase_profile.csv` columns: `user_id`, `category_ids` (JSON list), `shop_ids` (JSON list) for cross_domain.

## Run

```bash
cd Services/recsys-offline
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8095
```

## Tests

```bash
pytest -q
```

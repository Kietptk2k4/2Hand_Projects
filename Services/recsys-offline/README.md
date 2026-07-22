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
- `POST /jobs/export-activate` — LightGBM→ONNX, parity smoke, versioned `model_artifacts` insert, metric gate, activate or soft-reject

## Config (env)

| Variable | Purpose |
|----------|---------|
| `SOCIAL_POSTGRES_URL` | Postgres URL for clean extract **and** `model_artifacts` write |
| `SOCIAL_MONGO_URL` | Mongo URI for clean extract |
| `SOCIAL_MONGO_DB` | Mongo database name (default `social_db`) |
| `RECSYS_DATASET_OUTPUT_DIR` | Cleaned CSV + `dataset.parquet` directory (default `data/cleaned`) |
| `RECSYS_ARTIFACT_DIR` | Model artifact directory (`model.txt`, `train_meta.json`, ONNX, reports) |

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

# 6) Export ONNX + gate + activate (needs SOCIAL_POSTGRES_URL)
curl -X POST http://localhost:8095/jobs/export-activate
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
- `feed_ranker_v{N}.onnx` — exported ONNX (after export-activate)
- Social DB `model_artifacts` row for version N

Train policy:

- Objective: `binary` · metrics: `binary_logloss` (early-stop) + `auc` (reported)
- Feature order matches Java `LightGBMRankingModel` (6 scores)
- If `dataset_val.parquet` exists and is non-empty → early stopping
- If val missing/empty → warning `no_early_stopping`, still trains (does not fail)
- Missing `dataset_train.parquet` → fail closed (HTTP 400)

Evaluate policy:

- Inputs: `dataset_test.parquet` + `model.txt` (fail closed if missing)
- Pointwise: ROC-AUC (undefined if single class → `null` + warning)
- Ranking @10: group by `request_id` (fallback `user_id` + warning `no_request_id`)
- Precision@10 denominator: `min(10, n)` for group size `n`
- Baseline weights (must match Java `RuleBasedRankingModel`):  
  `0.12 / 0.28 / 0.22 / 0.13 / 0.13 / 0.12`  
  (recency, engagement, hashtag, author_affinity, mutual_follow, cross_domain)
- Report: JSON only (`evaluate_report.json`) — no markdown

Export-activate policy:

- Inputs: `model.txt` + `evaluate_report.json` + `dataset_test.parquet` (≥32 rows) + `SOCIAL_POSTGRES_URL`
- Convert LightGBM → ONNX; smoke 32–64 samples; fail job (no DB write) if `max_abs_diff > 1e-4`
- Insert `model_artifacts` with `version = MAX+1`, `feature_version=1`, metrics JSONB
- Gate activate when AUC ≥ baseline **and** Precision@10 ≥ baseline (null → fail closed)
- Gate fail → insert inactive + `rejected_by_metrics`; keep prior active (`status: exported_not_activated`)
- Gate pass → transactional activate (`status: activated`); Social `ModelLoader` cron reloads ONNX
- Does **not** re-run evaluate

Online admin (Social Service, not this package):

- `GET /api/v1/social/admin/recommendation-model-status` — mode / version / fallback reason
- `GET /api/v1/social/admin/recommendation-model-artifacts` — registry list for Admin FE
- Admin UI: System Operations → **Model registry** (read-only; no export button)

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

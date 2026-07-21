# Recsys Offline (2Hands)

Offline ML ops package for Recommended Feed training data and model jobs.

**This is NOT an online recommendation / inference service.**
Social Service ranks posts in-process (ONNX + rule-based fallback) and never calls this API during recommend requests.

## What it does

- `GET /health` — liveness
- `POST /jobs/clean` — read-only extract + clean → CSV files + drop summary
- `POST /jobs/build-dataset` — join impressions + features + 24h labels → `dataset.parquet`
- `POST /jobs/split-dataset` — time-ordered 70/15/15 split → `dataset_train/val/test.parquet`
- Stub hooks: `POST /jobs/train`, `POST /jobs/evaluate`, `POST /jobs/export-activate`

## Config (env)

| Variable | Purpose |
|----------|---------|
| `SOCIAL_POSTGRES_URL` | Postgres URL for clean extract |
| `SOCIAL_MONGO_URL` | Mongo URI for clean extract |
| `SOCIAL_MONGO_DB` | Mongo database name (default `social_db`) |
| `RECSYS_DATASET_OUTPUT_DIR` | Cleaned CSV + `dataset.parquet` directory (default `data/cleaned`) |
| `RECSYS_ARTIFACT_DIR` | Model artifact directory (for future export) |

## Typical offline flow

```bash
# 1) Clean entities from DB → CSV under RECSYS_DATASET_OUTPUT_DIR
curl -X POST http://localhost:8095/jobs/clean

# 2) Build labeled training table (needs post_impression_log.csv + posts.csv)
curl -X POST http://localhost:8095/jobs/build-dataset

# 3) Optional time split
curl -X POST http://localhost:8095/jobs/split-dataset
```

Outputs (under `RECSYS_DATASET_OUTPUT_DIR`):

- `dataset.parquet`
- `dataset_meta.json` (row count, positive rate, warnings)
- `dataset_train.parquet` / `dataset_val.parquet` / `dataset_test.parquet` after split

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

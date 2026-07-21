# Recsys Offline (2Hands)

Offline ML ops package for Recommended Feed training data and model jobs.

**This is NOT an online recommendation / inference service.**
Social Service ranks posts in-process (ONNX + rule-based fallback) and never calls this API during recommend requests.

## What it does

- `GET /health` — liveness
- `POST /jobs/clean` — read-only extract + clean → dataset files + drop summary
- Stub hooks: `POST /jobs/train`, `POST /jobs/evaluate`, `POST /jobs/export-activate`

## Config (env)

| Variable | Purpose |
|----------|---------|
| `SOCIAL_POSTGRES_URL` | Postgres SQLAlchemy URL (e.g. `postgresql+psycopg://user:pass@host:5432/social_db`) |
| `SOCIAL_MONGO_URL` | Mongo URI |
| `SOCIAL_MONGO_DB` | Mongo database name (default `social_db`) |
| `RECSYS_DATASET_OUTPUT_DIR` | Cleaned CSV output directory |
| `RECSYS_ARTIFACT_DIR` | Model artifact directory (for future export) |

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

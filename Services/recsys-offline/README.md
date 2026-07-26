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
- `POST /jobs/export-purchase-profile` — read-only Commerce export → `user_purchase_profile.csv` (optional `as_of` / `T_cut`)

## Config (env)

| Variable | Purpose |
|----------|---------|
| `SOCIAL_POSTGRES_URL` | Postgres URL for clean extract **and** `model_artifacts` write |
| `SOCIAL_MONGO_URL` | Mongo URI for clean extract |
| `SOCIAL_MONGO_DB` | Mongo database name (default `social_db`) |
| `COMMERCE_POSTGRES_URL` | Commerce Postgres for purchase-profile export / sim writers |
| `AUTH_POSTGRES_URL` | Auth Postgres for sim user seed (optional) |
| `RECSYS_DATASET_OUTPUT_DIR` | Cleaned CSV + `dataset.parquet` directory (default `data/cleaned`) |
| `RECSYS_ARTIFACT_DIR` | Model artifact directory (`model.txt`, `train_meta.json`, ONNX, reports). Offline **writes** files here. Social reads via `SOCIAL_RECOMMENDATION_MODEL_ROOT` (same folder when Docker-mounted). |
| `RECSYS_SIM_ALLOW` | Must be `1`/`true` to run DB seed/simulate writers (dev-only) |

**Portable model path:** `model_artifacts.artifact_path` stores only the ONNX **basename** (e.g. `feed_ranker_v1.onnx`). Social joins `SOCIAL_RECOMMENDATION_MODEL_ROOT` (ops alias: `MODEL_ROOT`) + that basename. Do not store host-absolute paths in the DB.

## Fashion simulation seed (dev-only)

Generates Fashion Social + Fashion Commerce training corpus (no laptop/gaming). Personas: `config/personas.yaml`.

**Default full volumes:** 120 users, 40 sellers/shops, 600 posts (~60% product tags), 400 products, 12 sessions × feed 20 → ~28.8k raw impressions (target ≥20k cleaned rows).

**Search keywords MUST equal normalized hashtag tokens** (lowercase, no `#`) so `hashtag_match_score` can fire.

```bash
# Validate planned counts (no DB)
python -m simulation.cli dry-run --scale full

# In-memory sim + build_rows + KPI (smoke thresholds)
python -m simulation.cli simulate-memory --scale smoke

# Write to DBs (requires RECSYS_SIM_ALLOW=1 and URLs)
set RECSYS_SIM_ALLOW=1
python -m simulation.cli seed-db --scale full --simulate

# Optional: align post + interaction clock to wall-clock end
python -m simulation.cli seed-db --scale full --simulate --sim-days 21
```

### Mongo post timestamps (online recall)

Social `/feed/for-you` candidate recall filters `created_at` with BSON **Date** cutoffs (`now − 7/30/90d`). Sim writers MUST store `created_at` / `updated_at` as UTC `datetime` (BSON Date), spread across `[end − sim_days, end]` (default `end=utcnow`).

**Do not** write ISO strings — Mongo Date `$gte` will not match strings, so seeded posts become invisible to online recall.

`seed-db` also upserts Social Mongo `user_projections` for every sim user (Auth `users` alone is not enough — feed author cards read Social projections).

#### Repair existing string / stale posts (dev-only)

```bash
set RECSYS_SIM_ALLOW=1
# Dry-run: type counts + would_update (default caption prefix "Sim ")
python -m simulation.cli repair-post-timestamps --window-days 21

# Apply: rewrite into [now − 21d, now] as BSON Date
python -m simulation.cli repair-post-timestamps --window-days 21 --apply

# Escape hatch (known-dev DBs only): all posts, no caption filter
python -m simulation.cli repair-post-timestamps --all-posts --apply
```

After apply, optionally clear seen for a test user so inventory is not already exhausted:

```sql
DELETE FROM user_seen_posts WHERE user_id = '<uuid>';
```

Then call `/feed/for-you` and check Social logs for a larger `poolSize`.

### Operator workflow (final train with as-of `T_cut`)

1. `seed-db --simulate` (or use existing DBs)
2. `POST /jobs/clean` — cleaned posts **retain `product_tags`**
3. `POST /jobs/build-dataset` (provisional profile optional)
4. `POST /jobs/split-dataset` → read train/val boundary `shown_at` as `T_cut` from `split_meta.json`
5. `POST /jobs/export-purchase-profile` with body `{"as_of":"<T_cut>"}` → `user_purchase_profile.csv`
6. `POST /jobs/build-dataset` again (final features with cutoff profile)
7. `POST /jobs/split-dataset` again → `train` → `evaluate`
8. Run KPI gate mentally/via `simulate-memory` metrics: rows≥20k, positive_rate 12–25%, cross_domain share≥15%, hashtag share≥40%, buyer_rate≥60%

DoD: final dataset uses purchase profile with **no purchases after `T_cut`**; Cart/Purchase in sim so `cross_domain_product_score` is non-trivial.

**Recorded in-memory full pass** (`python -m simulation.cli simulate-memory --scale full`, seed 42): rows=28800, positive_rate≈0.20, cross_domain_share≈0.16, hashtag_share≈0.73, buyer_rate≈0.73 — KPI gate `ok=true`. Smoke scale also passes smoke thresholds. Live DB seed requires `RECSYS_SIM_ALLOW=1` plus Auth/Social/Commerce URLs and fashion category rows already migrated.

## Typical offline flow

```bash
# 1) Clean entities from DB → CSV under RECSYS_DATASET_OUTPUT_DIR
curl -X POST http://localhost:8095/jobs/clean

# 2) Build labeled training table (needs post_impression_log.csv + posts.csv)
curl -X POST http://localhost:8095/jobs/build-dataset

# 3) Time split (80/10/10 by shown_at; temporal leak fails the job)
curl -X POST http://localhost:8095/jobs/split-dataset

# 3b) Export purchase profile as-of train cutoff (COMMERCE_POSTGRES_URL)
curl -X POST http://localhost:8095/jobs/export-purchase-profile \
  -H "Content-Type: application/json" \
  -d "{\"as_of\":\"2026-01-15T00:00:00Z\"}"

# 3c) Rebuild dataset with cutoff profile, then split again
curl -X POST http://localhost:8095/jobs/build-dataset
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
- Insert `model_artifacts` with `version = MAX+1`, `feature_version=1`, metrics JSONB; **`artifact_path` = ONNX basename only** (portable; Social joins `MODEL_ROOT`)
- Gate activate when AUC ≥ baseline **and** Precision@10 ≥ baseline (null → fail closed)
- Gate fail → insert inactive + `rejected_by_metrics`; keep prior active (`status: exported_not_activated`)
- Gate pass → transactional activate (`status: activated`); Social `ModelLoader` cron reloads ONNX
- Job summary includes `artifact_path` (basename) and `artifact_local_path` (absolute on the offline host, for operators)
- Does **not** re-run evaluate

If older rows still store absolute paths, run:

`scripts/migrate_model_artifact_paths_to_basename.sql` against Social Postgres.

Online admin (Social Service, not this package):

- `GET /api/v1/social/admin/recommendation-model-status` — mode / version / fallback reason
- `GET /api/v1/social/admin/recommendation-model-artifacts` — registry list for Admin FE
- Admin UI: System Operations → **Model registry** (read-only; no export button)

Split notes:

- Sort: `shown_at` ASC, tie-break `user_id`, `post_id`
- Cuts: `int(n*0.80)` / `int(n*0.90)` (remainder → test)
- Tiny `n` may yield empty val/test with warnings (`small_n`, `empty_val`, …)

Optional `user_purchase_profile.csv` columns: `user_id`, `category_ids` (JSON list), `shop_ids` (JSON list) for cross_domain.
Prefer exporting via `/jobs/export-purchase-profile` with `as_of=T_cut` after the first split for the final build.

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

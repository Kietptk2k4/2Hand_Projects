## Why

LightGBM training already produces `model.txt`, but `/jobs/evaluate` is still a stub. Without offline ROC-AUC and ranking@10 metrics versus a documented rule-based baseline, the team cannot justify model quality for the graduation Recommended Feed milestone. Admins also cannot see when online ranking silently falls back to rule-based after ONNX load failure.

## What Changes

- Implement `POST /jobs/evaluate`: score `dataset_test.parquet` with LightGBM and a rule-based baseline; compute pointwise **ROC-AUC** and group **Precision@10 / Recall@10 / HitRate@10**; write `evaluate_report.json` only (no Markdown).
- Lock baseline weights (Java + Python): **0.12 / 0.28 / 0.22 / 0.13 / 0.13 / 0.12** for recency, engagement, hashtag, author_affinity, mutual_follow, cross_domain (**BREAKING** vs current Java 5-weight rule-based that ignored cross_domain).
- Ranking@K grouping: prefer `request_id`, fallback `user_id` + warning; Precision denominator = `min(10, n_candidates)`.
- Add Social **internal recommendation model status** endpoint so admins can see `lightgbm` vs `rule_based` (and reason) when ONNX/session is unavailable.
- Fail closed when test parquet or model artifact is missing.
- **Out of scope:** ONNX export/activate, Markdown reports, NDCG, public FE exposure of fallback details.

## Capabilities

### New Capabilities

- `recsys-evaluate`: Offline evaluation of LightGBM vs rule-based baseline (AUC + P/R/Hit@10) and JSON report generation.
- `recsys-ranking-model-status`: Internal/admin-visible status of the active ranking mode (LightGBM vs rule-based fallback) for Social recommend serving.

### Modified Capabilities

- `recsys-offline-ops`: Replace evaluate stub with a real evaluate job endpoint (still no online predict for feeds).

## Impact

- **Python:** `Services/recsys-offline` evaluate pipeline, `/jobs/evaluate`, README, tests; reads `dataset_test.parquet` + `model.txt`.
- **Java:** `RuleBasedRankingModel` weight update (+ tests); new internal HTTP status endpoint near Social admin/ops conventions; impression logging already stores `model_version NULL` on fallback.
- **Deps:** may add `scikit-learn` (or equivalent) for ROC-AUC if not already present.

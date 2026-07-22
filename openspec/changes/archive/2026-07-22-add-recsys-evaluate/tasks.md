## 1. Java baseline + model status

- [x] 1.1 Update `RuleBasedRankingModel` to six locked weights (0.12/0.28/0.22/0.13/0.13/0.12) including cross_domain; fix unit tests
- [x] 1.2 Extend `ModelLoader` (or thin facade) to expose mode/version/reason for fallback
- [x] 1.3 Add internal/admin `GET` recommendation-model-status endpoint with auth; unit/integration test for lightgbm vs rule_based responses

## 2. Offline evaluate pipeline

- [x] 2.1 Implement rule-based scorer in Python with the same six weights
- [x] 2.2 Load test parquet + LightGBM `model.txt`; fail closed if missing/empty
- [x] 2.3 Compute pointwise ROC-AUC for lightgbm and baseline (null + warning if undefined)
- [x] 2.4 Compute Precision@10 / Recall@10 / HitRate@10 with request_id grouping (user_id fallback), Precision denom `min(10,n)`
- [x] 2.5 Write `evaluate_report.json` (metrics, deltas, group_by, warnings); wire `POST /jobs/evaluate`

## 3. Docs and verification

- [x] 3.1 Update `Services/recsys-offline/README.md` for evaluate inputs/outputs and metric definitions
- [x] 3.2 Unit tests for metrics (n&lt;10 precision, hit/recall, AUC); smoke evaluate on fixture test parquet + model

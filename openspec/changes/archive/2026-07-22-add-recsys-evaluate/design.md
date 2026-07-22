## Context

Offline pipeline can clean → build → split → train (`model.txt`). Evaluate is stubbed. Online recommend uses LightGBM ONNX when `ModelLoader.getSession() != null`, else `RuleBasedRankingModel` (currently five weights, no cross_domain). Impressions already store `model_version`/`model_name` as null on fallback. Explore locked metrics, weights, JSON-only report, and an internal status endpoint in this change.

## Goals / Non-Goals

**Goals:**
- Offline evaluate job: ROC-AUC (pointwise) + Precision/Recall/HitRate@10 vs rule-based baseline.
- Shared 6-feature baseline weights in Java serve and Python evaluate.
- `evaluate_report.json` under artifact dir.
- Internal Social endpoint exposing ranking mode + fallback reason.
- Unit tests for metrics edge cases (n&lt;10, missing groups, AUC undefined).

**Non-Goals:**
- ONNX export / `model_artifacts` activate.
- Markdown report, NDCG, Optuna.
- Public FE banner for fallback.
- Changing train/split contracts.

## Decisions

### D1 — Baseline weights (locked)
```
recency=0.12, engagement=0.28, hashtag=0.22,
author_affinity=0.13, mutual_follow=0.13, cross_domain=0.12
```
Sum = 1.0. Apply identically in `RuleBasedRankingModel` and Python baseline scorer.

### D2 — Metrics
- **ROC-AUC:** sklearn-style on all test rows `(y_true, y_score)`; if only one class → null + warning.
- **@10 ranking:** group by `request_id` if present/non-blank for majority of rows; else fallback `user_id` + warning `group_by_user_id`.
- **Precision@10:** `|rel ∩ topk| / min(10, n)` where `topk = min(10, n)`.
- **Recall@10:** `|rel ∩ topk| / max(1, |rel in group|)`; skip groups with zero positives for recall average (document in report `recall_groups_used`).
- **HitRate@10:** 1 if any relevant in topk else 0; mean over groups with ≥1 candidate.
- Macro-average over eligible groups.

### D3 — Report
- Single file `evaluate_report.json`: lightgbm metrics, baseline metrics, delta, k, group_by, warnings, paths, counts.
- No Markdown.

### D4 — Internal model status
- Path suggestion: `GET /api/v1/social/internal/recommendation-model-status` (or under existing admin base if security filter already allows admin JWT).
- Payload: `{ "mode": "lightgbm"|"rule_based", "modelVersion": int|null, "modelName": string|null, "reason": string|null, "configuredRankingModel": string }`.
- `reason` examples: `onnx_session_missing`, `file_not_found`, `load_error`, `config_rule_based`.
- Auth: protect as internal/admin (reuse Social admin auth pattern); not a public user API.
- ModelLoader should expose last fallback reason if available (extend lightly).

### D5 — Fail closed
- Missing `dataset_test.parquet` or `model.txt` → evaluate job 400.
- Empty test → fail or warn+empty metrics (prefer fail closed if zero usable rows).

## Risks / Trade-offs

- [Sparse request_id] Many rows share null request_id → fallback user grouping dilutes ranking semantics → Warn in report; improve impression logging separately if needed.
- [Weight CHANGE] Online ranking scores shift for rule-based path → Document BREAKING; update unit tests expecting old weights.
- [Status endpoint auth] Wrong placement leaks ops info → Bind to admin/internal security config only.

## Migration Plan

1. Deploy Social with new weights + status endpoint.
2. Deploy recsys-offline evaluate job; run after train.
3. Rollback: revert weights/endpoint/package independently.

## Open Questions

- None blocking; exact security annotation follows existing Social admin controllers at apply time.

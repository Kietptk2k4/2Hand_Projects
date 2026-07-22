## Context

Recommended Feed offline pipeline already supports clean → build → split → train → evaluate. Social serves ONNX via `ModelLoader` reading active `model_artifacts` (or configured path) with cron reload. `/jobs/export-activate` remains a stub. Evaluate writes `evaluate_report.json` with LightGBM vs locked baseline metrics (weights 0.12/0.28/0.22/0.13/0.13/0.12). Java `LightGBMRankingModel` expects ONNX input `float[batch][6]`.

## Goals / Non-Goals

**Goals:**
- Single `POST /jobs/export-activate` that exports ONNX, verifies parity with `model.txt`, versions + persists metrics, gates on AUC and Precision@10 vs baseline, activates or rejects.
- Admin-visible model registry (Social API + FE) for versions/metrics/onnx_verify/gate — same change.
- Fail-closed on missing inputs, ONNX verify failure, or undefined required gate metrics.

**Non-Goals:**
- Re-running evaluate inside export-activate.
- Triggering export from Admin FE (curl/ops only).
- Manual rollback button (rejected gate keeps prior active).
- Object storage / canary / A-B traffic.
- Changing baseline weights or feature set (`feature_version` stays 1).

## Decisions

1. **One job endpoint** — Keep `POST /jobs/export-activate` as the complete export→gate→activate workflow for the thesis scope (no separate export/activate APIs).

2. **Consume evaluate artifacts; do not re-evaluate** — Require `evaluate_report.json` (+ `model.txt`, test parquet for smoke samples). Missing → HTTP 400.

3. **ONNX conversion + parity smoke** — Convert LightGBM native → ONNX (converter chosen at implement time: prefer maintained LightGBM→ONNX path compatible with ORT). Smoke 32–64 rows from `dataset_test.parquet`; fail job if `max_abs_diff > 1e-4` (no DB insert).

4. **Versioning** — `version = COALESCE(MAX(version),0)+1` for `model_name=feed_ranker`. Never overwrite an existing version row.

5. **Metrics JSONB payload** — Store at least: auc, precision_at_10, recall_at_10, hit_rate_at_10 (lightgbm + baseline and/or deltas as available from report), feature_order, feature_version=1, baseline_weights, train_meta_path, evaluate_report_path, onnx_verify{passed,n_samples,max_abs_diff,threshold}, gate{passed,reason,failed[]}.

6. **Gate** — Activate only if `auc_lgbm >= auc_baseline` AND `precision_at_10_lgbm >= precision_at_10_baseline`. If either side is null/undefined → gate fail closed (no activate).

7. **Two failure classes**  
   - Hard fail (no insert): missing inputs, convert error, onnx verify fail.  
   - Soft reject (insert inactive): gate fail → `rejected_by_metrics`, prior active unchanged. Job HTTP 200 with `status: exported_not_activated` (vs `activated`).

8. **Transactional activate** — In one Postgres transaction: set prior `is_active=false` for model_name, insert/update new row `is_active=true` (or insert inactive first then flip only on gate pass). Respect `uk_model_artifacts_one_active`.

9. **Serving reload** — Rely on existing `ModelLoader` `@Scheduled` reload; no Social restart; no force-reload HTTP required for Phase 1.

10. **Registry UI (same change)** — Social admin `GET` list artifacts (newest first); FE table with badges active / rejected_by_metrics / inactive and expand metrics. Auth: ADMIN/MODERATOR. No FE call to offline port 8095.

11. **DB access from offline** — Python uses `SOCIAL_POSTGRES_URL` (same pattern as clean job) to write `model_artifacts`.

## Risks / Trade-offs

- [ONNX converter / ORT shape mismatch] → Mitigation: parity smoke against `model.txt`; fail closed before insert; document expected input shape `[N,6]`.
- [Shared filesystem path for `artifact_path`] → Mitigation: write ONNX under configured artifact dir; document that Social must resolve the same path (dev volume / absolute path).
- [Gate too strict on small test sets] → Mitigation: null metrics fail closed; operators keep rejected rows for audit; can re-export after better data.
- [UI + backend in one change] → Mitigation: keep FE minimal (list/detail only); defer trigger/rollback UX.

## Migration Plan

1. Deploy Social list API + FE (read-only) anytime.
2. Deploy offline export job + deps; run train→evaluate→export-activate in staging.
3. Confirm cron picks up new active ONNX; status endpoint shows lightgbm + version.
4. Rollback serving: activate previous version row manually in DB (or re-run export of older artifact) — automated UI rollback out of scope.

## Open Questions

- Exact Python ONNX conversion library pin (resolve during apply; must produce ORT-loadable model matching Java float batch input).
- Sample count default inside 32–64 (suggest 64 if test rows allow, else `min(64, n)` with floor 32 or fail if `n < 32`).

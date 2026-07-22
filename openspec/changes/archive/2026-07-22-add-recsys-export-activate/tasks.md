## 1. Offline export + verify

- [x] 1.1 Add ONNX conversion dependency and implement LightGBM `model.txt` → ONNX writer under artifact dir
- [x] 1.2 Implement parity smoke (32–64 test samples, `max_abs_diff <= 1e-4`); fail closed with no DB write on exceed/convert error
- [x] 1.3 Unit tests for verify pass/fail and missing `model.txt` / `evaluate_report.json`

## 2. Registry write + gate + activate

- [x] 2.1 Implement `model_artifacts` insert with `MAX(version)+1`, metrics JSONB (`feature_version=1`, scores, baseline_weights, paths, onnx_verify, gate)
- [x] 2.2 Implement metric gate (AUC ≥ baseline AND P@10 ≥ baseline; null → fail closed); soft reject inserts inactive + `rejected_by_metrics`
- [x] 2.3 Implement transactional activate on gate pass; wire `POST /jobs/export-activate` statuses `activated` / `exported_not_activated`
- [x] 2.4 Integration/smoke test: fixture model + report → ONNX + DB row (activated or rejected)

## 3. Social admin registry API

- [x] 3.1 Extend domain/repository to list artifacts by model name (newest first)
- [x] 3.2 Add admin GET list endpoint (ADMIN/MODERATOR) returning version, is_active, metrics projection, trained_at
- [x] 3.3 Unit tests for auth rejection and list mapping (active vs rejected)

## 4. Admin FE + docs

- [x] 4.1 Add minimal model registry UI (table + badges + expand metrics); no offline job trigger
- [x] 4.2 Update `Services/recsys-offline/README.md` for export-activate inputs/outputs/gate/verify
- [x] 4.3 End-to-end smoke notes or script: train→evaluate→export-activate → Social status/list shows version

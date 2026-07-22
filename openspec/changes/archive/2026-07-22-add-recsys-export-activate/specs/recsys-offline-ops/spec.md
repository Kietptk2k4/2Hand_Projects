## ADDED Requirements

### Requirement: Export-activate job endpoint
The offline FastAPI service SHALL expose `POST /jobs/export-activate` that runs the export-activate pipeline (ONNX export, parity verify, versioned `model_artifacts` insert, metric gate, optional activate), returns success with status `activated` or `exported_not_activated`, or structured failure when inputs are missing or verify/convert fails, and MUST NOT expose online recommend or predict APIs for end-user feeds.

#### Scenario: Trigger export-activate successfully activated
- **WHEN** an operator calls export-activate with valid `model.txt`, `evaluate_report.json`, and test samples, and the metric gate passes
- **THEN** the service returns success with status `activated` and artifact/version summary fields

#### Scenario: Trigger export-activate soft reject
- **WHEN** export and verify succeed but the metric gate fails
- **THEN** the service returns success with status `exported_not_activated` and does not activate the new version

#### Scenario: Export-activate fails closed without inputs
- **WHEN** required `model.txt` or `evaluate_report.json` is missing
- **THEN** the job fails with an explicit error and does not report success

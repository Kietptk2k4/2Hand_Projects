## ADDED Requirements

### Requirement: Evaluate job endpoint
The offline FastAPI service SHALL expose a job endpoint that runs the evaluate pipeline (LightGBM vs rule-based metrics + JSON report), returns success with a summary or structured failure when inputs are missing, and MUST NOT expose online recommend or predict APIs for end-user feeds.

#### Scenario: Trigger evaluate successfully
- **WHEN** an operator calls the evaluate job with valid `dataset_test.parquet` and `model.txt`
- **THEN** the service runs evaluation and returns success including report path/summary fields

#### Scenario: Evaluate fails closed without inputs
- **WHEN** required test parquet or model artifact is missing
- **THEN** the job fails with an explicit error and does not report success

# recsys-offline-ops

## Purpose

Offline FastAPI job host for Recommended Feed ML ops (clean / train / evaluate / export). Does not participate in online recommend serving.

## Requirements

### Requirement: Offline job API does not serve recommendations
The system SHALL provide a FastAPI application dedicated to offline recsys jobs (health and job triggers) that MUST NOT be invoked by Social Service during recommend-feed request handling.

#### Scenario: Health check available
- **WHEN** an operator calls the offline service health endpoint
- **THEN** the service returns a successful health response without reading or writing recommend ranking results for end users

#### Scenario: Recommend path independence
- **WHEN** Social Service handles a recommend-feed request
- **THEN** it MUST complete ranking using in-process Java models (ONNX and/or rule-based) without HTTP calls to the offline FastAPI service

### Requirement: Clean job is triggerable
The offline service SHALL expose a job endpoint or CLI-equivalent entry that runs the dataset clean pipeline and reports completion status including drop counts summary.

#### Scenario: Clean job completes with summary
- **WHEN** an authorized operator triggers the clean job with valid DB connection configuration
- **THEN** the job finishes with a success status and a summary of records kept and dropped by reason

#### Scenario: Clean job fails closed on missing config
- **WHEN** required database connection settings are missing
- **THEN** the job MUST fail with an explicit error and MUST NOT write a partial dataset silently as success

### Requirement: Train and export jobs are offline-only hooks
The offline service SHALL provide job hooks (implemented or stubbed with clear contract) for train, evaluate, and export/activate that write artifacts for Java to load, without exposing online predict APIs for feed ranking.

#### Scenario: No online predict API for feed
- **WHEN** a client inspects the offline FastAPI routes intended for Phase 1
- **THEN** there is no public recommend/predict endpoint that returns ranked posts for a user feed request

### Requirement: Build dataset job endpoint
The offline FastAPI service SHALL expose a job endpoint to run the build-dataset pipeline without exposing online recommend or predict APIs for end-user feeds.

#### Scenario: Trigger build dataset
- **WHEN** an operator calls the build-dataset job endpoint with valid configuration
- **THEN** the service runs the pipeline and returns success or a structured failure detail

#### Scenario: Still no feed predict route
- **WHEN** a client lists Phase 1 offline routes
- **THEN** there is still no public endpoint that returns ranked posts for a user feed request

### Requirement: Split dataset job endpoint
The offline FastAPI service SHALL expose a job endpoint that runs the time-ordered 80/10/10 split pipeline on `dataset.parquet`, returns success with split summary (counts, leak status, overlap report) or a structured failure when inputs are missing or temporal leak checks fail, and MUST NOT expose online recommend or predict APIs for end-user feeds.

#### Scenario: Trigger split dataset
- **WHEN** an operator calls the split-dataset job endpoint with a valid `dataset.parquet` present
- **THEN** the service runs the split pipeline and returns success or structured failure detail

#### Scenario: Missing dataset fails closed
- **WHEN** `dataset.parquet` is missing under the configured output directory
- **THEN** the job fails with an explicit error and does not report success

### Requirement: Train LightGBM job endpoint
The offline FastAPI service SHALL expose a job endpoint that runs the LightGBM binary training pipeline, returns success with a summary (paths, metrics, warnings) or a structured failure when train inputs are missing/unusable, and MUST NOT expose online recommend or predict APIs for end-user feeds.

#### Scenario: Trigger train successfully
- **WHEN** an operator calls the train job endpoint with a valid `dataset_train.parquet` present
- **THEN** the service runs training and returns success with artifact/summary fields

#### Scenario: Train fails closed without train parquet
- **WHEN** `dataset_train.parquet` is missing
- **THEN** the job fails with an explicit error and does not report success

### Requirement: Evaluate job endpoint
The offline FastAPI service SHALL expose a job endpoint that runs the evaluate pipeline (LightGBM vs rule-based metrics + JSON report), returns success with a summary or structured failure when inputs are missing, and MUST NOT expose online recommend or predict APIs for end-user feeds.

#### Scenario: Trigger evaluate successfully
- **WHEN** an operator calls the evaluate job with valid `dataset_test.parquet` and `model.txt`
- **THEN** the service runs evaluation and returns success including report path/summary fields

#### Scenario: Evaluate fails closed without inputs
- **WHEN** required test parquet or model artifact is missing
- **THEN** the job fails with an explicit error and does not report success

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


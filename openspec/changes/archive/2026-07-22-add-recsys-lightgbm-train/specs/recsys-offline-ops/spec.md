## ADDED Requirements

### Requirement: Train LightGBM job endpoint
The offline FastAPI service SHALL expose a job endpoint that runs the LightGBM binary training pipeline, returns success with a summary (paths, metrics, warnings) or a structured failure when train inputs are missing/unusable, and MUST NOT expose online recommend or predict APIs for end-user feeds.

#### Scenario: Trigger train successfully
- **WHEN** an operator calls the train job endpoint with a valid `dataset_train.parquet` present
- **THEN** the service runs training and returns success with artifact/summary fields

#### Scenario: Train fails closed without train parquet
- **WHEN** `dataset_train.parquet` is missing
- **THEN** the job fails with an explicit error and does not report success

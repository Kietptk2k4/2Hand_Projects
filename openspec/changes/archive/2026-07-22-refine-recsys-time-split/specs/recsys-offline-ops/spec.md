## ADDED Requirements

### Requirement: Split dataset job endpoint
The offline FastAPI service SHALL expose a job endpoint that runs the time-ordered 80/10/10 split pipeline on `dataset.parquet`, returns success with split summary (counts, leak status, overlap report) or a structured failure when inputs are missing or temporal leak checks fail, and MUST NOT expose online recommend or predict APIs for end-user feeds.

#### Scenario: Trigger split dataset
- **WHEN** an operator calls the split-dataset job endpoint with a valid `dataset.parquet` present
- **THEN** the service runs the split pipeline and returns success or structured failure detail

#### Scenario: Missing dataset fails closed
- **WHEN** `dataset.parquet` is missing under the configured output directory
- **THEN** the job fails with an explicit error and does not report success

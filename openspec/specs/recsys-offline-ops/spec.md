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

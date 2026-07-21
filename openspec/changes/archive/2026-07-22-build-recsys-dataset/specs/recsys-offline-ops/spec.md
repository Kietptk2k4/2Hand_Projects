## ADDED Requirements

### Requirement: Build dataset job endpoint
The offline FastAPI service SHALL expose a job endpoint to run the build-dataset pipeline without exposing online recommend or predict APIs for end-user feeds.

#### Scenario: Trigger build dataset
- **WHEN** an operator calls the build-dataset job endpoint with valid configuration
- **THEN** the service runs the pipeline and returns success or a structured failure detail

#### Scenario: Still no feed predict route
- **WHEN** a client lists Phase 1 offline routes
- **THEN** there is still no public endpoint that returns ranked posts for a user feed request

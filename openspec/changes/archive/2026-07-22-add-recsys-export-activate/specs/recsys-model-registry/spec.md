## ADDED Requirements

### Requirement: Admin list model artifacts
Social Service SHALL expose an admin-protected HTTP endpoint that lists `model_artifacts` for the recommendation model (default `feed_ranker`), newest version first, including version, format, artifact path, is_active, trained_at, and metrics JSON (or a projection sufficient for registry UI: gate reason, onnx_verify summary, key scores).

#### Scenario: List includes rejected and active
- **WHEN** an ADMIN or MODERATOR calls the list endpoint and both an active version and a `rejected_by_metrics` inactive version exist
- **THEN** both rows appear with distinguishable active vs rejected/inactive indicators derived from `is_active` and metrics

#### Scenario: Unauthenticated rejected
- **WHEN** an unauthenticated client calls the list endpoint
- **THEN** the request is rejected according to Social admin auth rules

### Requirement: Admin model registry UI
The admin frontend SHALL provide a minimal registry view that loads the Social model-artifacts list API and displays versions with badges for active, rejected_by_metrics, and inactive states, and SHALL allow expanding a row to inspect metrics including onnx_verify and gate fields when present.

#### Scenario: Operator views verify and gate
- **WHEN** an operator opens the registry and expands a version that has metrics with onnx_verify and gate
- **THEN** the UI shows those fields without requiring a call to the offline FastAPI service

#### Scenario: No offline job trigger from UI
- **WHEN** an operator uses the Phase-1 registry UI
- **THEN** there is no control that invokes `POST /jobs/export-activate` on the offline service

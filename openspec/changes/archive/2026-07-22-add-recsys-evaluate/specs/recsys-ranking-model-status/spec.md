## ADDED Requirements

### Requirement: Internal recommendation model status
Social Service SHALL expose an internal/admin-protected HTTP endpoint that reports whether recommend ranking is currently using LightGBM or rule-based fallback, including optional model version/name and a machine-readable reason when in rule-based mode.

#### Scenario: LightGBM active
- **WHEN** the ONNX session is loaded and ranking is configured for lightgbm
- **THEN** the status response reports mode `lightgbm` with the active model version when known

#### Scenario: Rule-based fallback visible
- **WHEN** the ONNX session is unavailable (missing file, load failure, or null session) while lightgbm is configured
- **THEN** the status response reports mode `rule_based` and a non-empty reason such as `onnx_session_missing` or `load_error`

#### Scenario: Not a public feed API
- **WHEN** an unauthenticated client calls the status endpoint
- **THEN** the request is rejected according to Social admin/internal auth rules

## MODIFIED Requirements

### Requirement: Model artifacts registry
The Social Postgres schema SHALL store model artifact metadata including `model_name`, integer `version`, `format`, `artifact_path` as a portable relative reference to the ONNX file (Phase 1: basename such as `feed_ranker_v1.onnx`, not a host-absolute path), optional `metrics` JSON, `is_active`, and `trained_at`, with uniqueness on `(model_name, version)`.

#### Scenario: Unique model version
- **WHEN** a second artifact is inserted with the same `model_name` and `version` as an existing row
- **THEN** the database MUST reject the insert

#### Scenario: Artifact path is environment-independent
- **WHEN** a valid artifact row is stored for serving
- **THEN** `artifact_path` does not encode a machine-specific absolute filesystem location
- **AND** the serving process combines a configured model root with `artifact_path` to locate the file

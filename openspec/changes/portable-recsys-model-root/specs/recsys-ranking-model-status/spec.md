## ADDED Requirements

### Requirement: Active artifact resolved under model root
When an active `model_artifacts` row exists for the configured model name, Social SHALL resolve its ONNX file under the configured recommendation model root joined with the stored relative `artifact_path`, and SHALL use that resolved file for the OrtSession when the file exists and the relative value is safe.

#### Scenario: Relative active path loads under model root
- **WHEN** lightgbm ranking is configured, an active row has `artifact_path=feed_ranker_v1.onnx`, model root is configured, and the resolved file exists
- **THEN** the recommendation model status reports mode `lightgbm` with version 1 when known

#### Scenario: Missing file under model root
- **WHEN** an active row has a safe relative `artifact_path` but the resolved file under model root does not exist
- **THEN** the status response reports mode `rule_based` and a non-empty reason such as `file_not_found`

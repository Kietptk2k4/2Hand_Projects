## MODIFIED Requirements

### Requirement: Versioned artifact insert with metrics JSON
On successful ONNX verify, the pipeline SHALL insert a new `model_artifacts` row for `feed_ranker` with `version = MAX(version)+1` (starting at 1 if none exist), `format` indicating ONNX, `artifact_path` set to the portable relative ONNX filename only (for example `feed_ranker_v3.onnx`, not an absolute host path), and `metrics` JSONB including at least AUC, Precision@10, Recall@10, HitRate@10, `feature_order`, `feature_version` equal to 1, `baseline_weights`, paths to `train_meta.json` and `evaluate_report.json`, plus onnx verify and gate result fields. The pipeline MUST NOT overwrite an existing `(model_name, version)` row.

#### Scenario: New version allocated
- **WHEN** versions 1 and 2 already exist for `feed_ranker`
- **THEN** the next successful export inserts version 3

#### Scenario: Artifact path is portable relative
- **WHEN** export-activate inserts a new version after writing ONNX under the offline artifact directory
- **THEN** `artifact_path` equals the basename of the ONNX file (e.g. `feed_ranker_v3.onnx`)
- **AND** MUST NOT be an absolute filesystem path from the offline host

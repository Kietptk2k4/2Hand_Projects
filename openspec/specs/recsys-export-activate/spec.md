# recsys-export-activate

## Purpose

Offline export of LightGBM to ONNX, parity verification, versioned `model_artifacts` registry writes, and metric-gated activate vs soft reject for Recommended Feed serving.

## Requirements

### Requirement: Export LightGBM to ONNX with parity smoke
The export-activate pipeline SHALL convert the trained LightGBM `model.txt` into an ONNX artifact consumable by Social `LightGBMRankingModel` / ORT (six float features per row). After conversion it SHALL score 32–64 samples from `dataset_test.parquet` with both native LightGBM and ONNX and SHALL fail the job without inserting `model_artifacts` when `max_abs_diff` exceeds `1e-4`.

#### Scenario: Parity within threshold
- **WHEN** ONNX and native scores on the smoke sample set have `max_abs_diff <= 1e-4`
- **THEN** export verification passes and the job may proceed to registry insert

#### Scenario: Parity exceeds threshold
- **WHEN** `max_abs_diff > 1e-4`
- **THEN** the job fails with an explicit error and MUST NOT insert a `model_artifacts` row or change the active model

### Requirement: Versioned artifact insert with metrics JSON
On successful ONNX verify, the pipeline SHALL insert a new `model_artifacts` row for `feed_ranker` with `version = MAX(version)+1` (starting at 1 if none exist), `format` indicating ONNX, `artifact_path` pointing at the exported file, and `metrics` JSONB including at least AUC, Precision@10, Recall@10, HitRate@10, `feature_order`, `feature_version` equal to 1, `baseline_weights`, paths to `train_meta.json` and `evaluate_report.json`, plus onnx verify and gate result fields. The pipeline MUST NOT overwrite an existing `(model_name, version)` row.

#### Scenario: New version allocated
- **WHEN** versions 1 and 2 already exist for `feed_ranker`
- **THEN** the next successful export inserts version 3

### Requirement: Metric gate before activate
The pipeline SHALL activate the new artifact only when LightGBM AUC is greater than or equal to baseline AUC **and** LightGBM Precision@10 is greater than or equal to baseline Precision@10, using values from `evaluate_report.json`. If a required gate metric is null or undefined, the gate MUST fail closed (no activate).

#### Scenario: Gate passes
- **WHEN** AUC and Precision@10 for lightgbm are both defined and each is >= the corresponding baseline value
- **THEN** the new version is set active in a transaction that deactivates the previous active row for the same model name

#### Scenario: Gate rejects but keeps audit row
- **WHEN** the gate fails (metric below baseline or undefined)
- **THEN** the new version is inserted with `is_active=false` and marked `rejected_by_metrics` in metrics, and the previously active version remains active

### Requirement: Job response distinguishes activate vs reject
The export-activate job SHALL return a structured success when ONNX verify and DB insert complete, with a status distinguishing `activated` from `exported_not_activated` (gate reject). Hard failures (missing inputs, convert/verify errors) MUST NOT report success.

#### Scenario: Activated response
- **WHEN** gate passes and activate commits
- **THEN** the HTTP job response indicates success with status `activated` and the new version number

#### Scenario: Soft reject response
- **WHEN** gate fails after a successful insert of an inactive row
- **THEN** the HTTP job response indicates success with status `exported_not_activated` and does not claim the model is active

#### Scenario: Missing evaluate report fails closed
- **WHEN** `evaluate_report.json` or `model.txt` is missing
- **THEN** the job fails with an explicit error and does not insert or activate

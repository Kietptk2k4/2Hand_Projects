# recsys-lightgbm-train

## Purpose

Offline LightGBM binary training from split parquet for Recommended Feed, native model artifacts only.

## Requirements

### Requirement: Train data preparation
The train pipeline SHALL load `dataset_train.parquet` from the configured dataset output directory, build feature matrix `X` from exactly these columns in order — `recency_score`, `engagement_score`, `hashtag_match_score`, `author_affinity_score`, `mutual_follow_score`, `cross_domain_product_score` — and label vector `y` from `label` (0/1). Missing train file or zero usable rows MUST fail the job.

#### Scenario: Successful load
- **WHEN** `dataset_train.parquet` exists with the six feature columns and labels
- **THEN** the trainer builds `X` and `y` with that feature order

#### Scenario: Missing train parquet fails closed
- **WHEN** `dataset_train.parquet` is missing or has zero usable rows
- **THEN** the job fails with an explicit error and does not write a success model artifact

### Requirement: Binary LightGBM training
The pipeline SHALL train a LightGBM model with `objective` binary, using `binary_logloss` as the primary evaluation metric for early stopping when validation is used, and SHALL also record `auc` in training metadata when computable.

#### Scenario: Train with validation early stopping
- **WHEN** `dataset_val.parquet` exists and contains at least one usable row
- **THEN** training uses validation for early stopping on `binary_logloss`
- **AND** metadata records validation metrics including auc when available

#### Scenario: Train without validation
- **WHEN** `dataset_val.parquet` is missing or empty
- **THEN** training still runs without early stopping
- **AND** the summary includes an explicit warning such as `no_early_stopping`
- **AND** the job does not fail solely because validation is absent

### Requirement: Persist native model and train meta
On successful training, the pipeline SHALL write a LightGBM native text model (e.g. `model.txt`) and `train_meta.json` under the configured artifact directory, including at least: feature_order, params, train row counts, warnings, and best_iteration when early stopping applied.

#### Scenario: Artifacts written
- **WHEN** training completes successfully
- **THEN** `model.txt` and `train_meta.json` exist under `RECSYS_ARTIFACT_DIR` (or configured equivalent)

### Requirement: No online inference in train job
The train job SHALL only produce offline artifacts and MUST NOT expose an online recommend or predict API for end-user feeds.

#### Scenario: Train does not add feed predict route
- **WHEN** an operator inspects Phase 1 offline routes after train is implemented
- **THEN** there is still no public endpoint that returns ranked posts for a user feed request

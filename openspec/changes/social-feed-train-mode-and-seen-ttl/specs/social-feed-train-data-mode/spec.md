# social-feed-train-data-mode

## Purpose

Admin-configurable Social For You (`feed_ranker`) train data mode using file/RAM feed sim without shared-DB seed pollution.

## ADDED Requirements

### Requirement: Admin-configurable feed LTR train data mode

Feed train and retrain SHALL assemble datasets according to Admin system configs in Admin Postgres `system_configs`. Required keys:

| config_key | value_type | Default |
|------------|------------|---------|
| `social.feed.ltr.train_data_mode` | STRING | `SEED_ONLY` |
| `social.feed.ltr.seed_row_weight` | DECIMAL | `0.5` |
| `social.feed.ltr.real_only_min_impressions` | INTEGER | `5000` |

`train_data_mode` MUST be exactly one of `SEED_ONLY`, `HYBRID`, `REAL_ONLY`. Operators update these via system-configs tab. Train/retrain SHALL read active values at job start via Admin `GET /admin/api/v1/system-configs?q=<config_key>&is_active=true` with exact `configKey` match. Missing/invalid mode → fail closed. Env overrides MAY apply only when `RECSYS_FEED_CONFIG_FROM_ENV=1`. Resolved mode MUST be persisted in training metadata.

#### Scenario: Admin sets SEED_ONLY
- **WHEN** active mode is `SEED_ONLY`
- **THEN** the next feed train uses only file feed-sim corpora under `RECSYS_FEED_SIM_DIR`

#### Scenario: Admin sets HYBRID
- **WHEN** active mode is `HYBRID`
- **THEN** the job unions real cleaned extracts with feed-sim files
- **AND** each training row is tagged `data_source` as `SEED` or `REAL`

#### Scenario: REAL_ONLY with insufficient impressions
- **WHEN** mode is `REAL_ONLY` and real impression count is below `social.feed.ltr.real_only_min_impressions`
- **THEN** the job fails closed without substituting seed data

#### Scenario: Invalid enum rejected
- **WHEN** train_data_mode is not one of the three allowed values
- **THEN** the job rejects with a clear validation error

### Requirement: Optional seed row weight for HYBRID

When mode is `HYBRID`, SEED rows SHALL use sample weight from `social.feed.ltr.seed_row_weight` (default 0.5) and REAL rows weight 1.0.

#### Scenario: HYBRID down-weights seed rows
- **WHEN** mode is `HYBRID` and seed_row_weight is 0.5
- **THEN** SEED training rows use sample weight 0.5 and REAL rows use 1.0

### Requirement: File/RAM feed sim without shared database seed

Feed cold-start simulation SHALL generate personas, posts, impressions, and engagements in memory / on disk only. The feed bootstrap train path MUST NOT insert sim users, posts, likes, saves, search rows, shops, products, or orders into Auth, Social, or Commerce databases used by running applications.

#### Scenario: feed-sim does not write Auth users
- **WHEN** an operator runs feed-sim under `SEED_ONLY`
- **THEN** no INSERT into Auth `users` occurs
- **AND** cleaned CSV corpora exist under the configured feed sim directory

### Requirement: Mode-aware feed build and retrain orchestration

Retrain orchestration SHALL: resolve mode → feed-sim (skip if REAL_ONLY) → clean-real (if HYBRID or REAL_ONLY) → build-dataset → split → train → evaluate → export-activate. Shared-DB `seed-db --simulate` MUST NOT be required for SEED_ONLY train.

#### Scenario: REAL_ONLY skips feed-sim
- **WHEN** mode is `REAL_ONLY` and feed-retrain runs
- **THEN** feed-sim is skipped
- **AND** build uses real cleaned extracts only

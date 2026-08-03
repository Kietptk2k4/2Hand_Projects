## ADDED Requirements

### Requirement: Admin-configurable Home LTR train data mode
Commerce Home train and retrain SHALL assemble datasets and CF/AR/export inputs according to Admin system configs stored in **Admin Postgres `system_configs`** (existing table; no new Admin schema). Required keys:

| config_key | value_type | Default |
|------------|------------|---------|
| `commerce.home.ltr.train_data_mode` | STRING | `SEED_ONLY` |
| `commerce.home.ltr.seed_row_weight` | DECIMAL | `0.5` |
| `commerce.home.ltr.real_only_min_impressions` | INTEGER | `5000` |

`train_data_mode` MUST be exactly one of `SEED_ONLY`, `HYBRID`, `REAL_ONLY`. Operators SHALL create/update these via `section=systemOperations` → `tab=system-configs` with `SYSTEM_CONFIG_UPDATE`. Train/retrain SHALL read active values at job start via Admin `GET /admin/api/v1/system-configs?q=<config_key>&is_active=true` and select the row whose `configKey` equals the key exactly (`SYSTEM_CONFIG_VIEW`). Missing/invalid mode → fail closed. Env overrides MAY apply only when `RECSYS_HOME_CONFIG_FROM_ENV=1` (CLI smoke). Resolved mode MUST be persisted in training metadata / evaluate reports.

#### Scenario: Admin sets SEED_ONLY via system-configs
- **WHEN** an admin with `SYSTEM_CONFIG_UPDATE` sets `commerce.home.ltr.train_data_mode` to `SEED_ONLY` on the system-configs tab
- **THEN** the next Home train/retrain orchestration uses only file/RAM Home sim corpora for dataset and CF/AR/export inputs

#### Scenario: Admin sets HYBRID
- **WHEN** the active mode is `HYBRID`
- **THEN** the job unions real service extracts with Home sim files
- **AND** each training row is tagged `data_source` as `SEED` or `REAL`

#### Scenario: Admin sets REAL_ONLY with insufficient impressions
- **WHEN** the active mode is `REAL_ONLY`
- **AND** real Home impression count is below `commerce.home.ltr.real_only_min_impressions` (default 5000)
- **THEN** the job fails closed
- **AND** does not silently substitute seed data

#### Scenario: Invalid enum value rejected
- **WHEN** a caller attempts to store a string other than `SEED_ONLY`, `HYBRID`, or `REAL_ONLY` for train_data_mode (job preflight and/or Admin validation)
- **THEN** the update or job is rejected with a clear validation error

#### Scenario: Orchestrator reads exact config key from Admin list
- **WHEN** recsys-offline resolves train_data_mode without env override
- **THEN** it queries Admin system-configs with q equal to the config key
- **AND** uses only the item whose configKey matches exactly

### Requirement: Optional seed row weight for HYBRID
When mode is `HYBRID`, the pipeline SHALL apply sample weight from optional Admin config `commerce.home.ltr.seed_row_weight` (`value_type = DECIMAL`, default **0.5** when unset) to rows with `data_source = SEED`, and weight **1.0** to `REAL` rows. `SEED_ONLY` and `REAL_ONLY` MAY ignore this weight config.

#### Scenario: HYBRID down-weights seed rows
- **WHEN** mode is `HYBRID` and `seed_row_weight` is 0.5
- **THEN** SEED training rows use sample weight 0.5
- **AND** REAL rows use sample weight 1.0

### Requirement: File/RAM Home sim without shared database seed
Home cold-start simulation SHALL generate personas, in-memory catalog from persona niches, Home impressions, engages, and derived CF/AR/social-export tables **in memory / on disk only**. The Home bootstrap path MUST NOT insert sim users, posts, likes, saves, search rows, shops, products, or orders into the Auth, Social, or Commerce databases used by running applications. A separate Docker sim database is NOT required for v1.

#### Scenario: simulate-home does not write Auth users
- **WHEN** an operator runs Home sim bootstrap under `SEED_ONLY`
- **THEN** no INSERT into Auth `users` occurs as part of that bootstrap
- **AND** Home impression corpora exist as files under the configured Home sim directory

#### Scenario: In-memory catalog from niches
- **WHEN** Home sim builds a catalog
- **THEN** products/shops/categories/brands are generated from persona niche config in memory
- **AND** are not required to already exist as rows in live Commerce Postgres for cold-start train

### Requirement: Mode-aware CF, social export, and AR inputs
Entity co-occurrence, social interest export, and AR mining for Commerce Home SHALL select inputs according to the same `commerce.home.ltr.train_data_mode` value as LTR train: `SEED_ONLY` → sim files only; `REAL_ONLY` → real Social/Commerce extracts only; `HYBRID` → union/merge of both. Results MAY be written first as files and then applied to Commerce via a load-artifact job.

#### Scenario: SEED_ONLY CF uses sim pairs only
- **WHEN** mode is `SEED_ONLY` and the CF job runs
- **THEN** co-occurrence is computed from sim order/cart pair files
- **AND** not from live Commerce `orders`/`cart_items` extracts

### Requirement: load-artifact upserts Commerce tables without event pollution
A Home load-artifact job SHALL upsert file-built `entity_cooccur`, `user_social_interest_export`, and `social_tag_category_ar` into Commerce Postgres for online serve after cold-start. It MUST NOT write fake impression/engage/order/post/user event rows into Auth, Social, or Commerce as a side effect. Export-activate of `commerce_home_ranker` remains a separate gated step.

#### Scenario: load-artifact writes CF table only
- **WHEN** load-artifact runs with a valid `entity_cooccur` file
- **THEN** Commerce `entity_cooccur` rows are upserted
- **AND** Auth `users` and Social `post_impression_log` are unchanged by that job

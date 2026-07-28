# recsys-purchase-profile-export

## Purpose

Offline job/CLI that reads Commerce COMPLETED order history and writes `user_purchase_profile.csv` for build-dataset cross-domain features, with optional train-cutoff (`T_cut`) filtering.

## Requirements

### Requirement: Export purchase profile from Commerce
The offline recsys package SHALL provide a job or CLI that reads Commerce purchase/cart history (Postgres) and writes `user_purchase_profile.csv` with columns `user_id`, `category_ids`, and `shop_ids` under the configured dataset output directory for consumption by build-dataset.

#### Scenario: Successful export
- **WHEN** an operator runs export-purchase-profile with a valid Commerce database URL and eligible COMPLETED orders exist
- **THEN** `user_purchase_profile.csv` is written with one row per user that has eligible history
- **AND** `category_ids` / `shop_ids` are JSON lists of commerce identifiers

#### Scenario: Missing Commerce URL fails closed
- **WHEN** the Commerce database URL is not configured
- **THEN** the job fails with an explicit error and does not report success

### Requirement: As-of train cutoff filtering
When an as-of timestamp `T_cut` is provided (the train/val split boundary `shown_at`), the export SHALL include only orders that are COMPLETED with completion time ≤ `T_cut`, and MUST exclude later purchases from the profile used for the final training dataset build.

#### Scenario: Post-cutoff purchase excluded
- **WHEN** a user has a COMPLETED order after `T_cut` and another before `T_cut`
- **THEN** the exported profile for that user includes category/shop ids only from orders at or before `T_cut`

#### Scenario: No as_of uses all completed (provisional)
- **WHEN** export runs without `as_of`
- **THEN** the job may include all COMPLETED orders for sizing/debug
- **AND** documentation states the final DoD build MUST re-export with `T_cut` before train

### Requirement: Read-only relative to Commerce domain writes
The purchase-profile export SHALL NOT update or delete Commerce business rows; it only reads and writes filesystem CSV outputs (and logs).

#### Scenario: No commerce mutation
- **WHEN** export-purchase-profile completes successfully
- **THEN** Commerce order/cart/product tables are unmodified by the export job

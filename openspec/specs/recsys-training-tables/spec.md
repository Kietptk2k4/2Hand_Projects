# recsys-training-tables

## Purpose

Social Postgres tables that support Recommended Feed labeling, candidate dedupe, and model artifact versioning for offline training and Java serving.

## Requirements

### Requirement: Model artifacts registry
The Social Postgres schema SHALL store model artifact metadata including `model_name`, integer `version`, `format`, `artifact_path`, optional `metrics` JSON, `is_active`, and `trained_at`, with uniqueness on `(model_name, version)`.

#### Scenario: Unique model version
- **WHEN** a second artifact is inserted with the same `model_name` and `version` as an existing row
- **THEN** the database MUST reject the insert

### Requirement: Single active artifact per model name
The schema SHALL enforce that at most one row per `model_name` has `is_active = TRUE` (partial unique index).

#### Scenario: Second active rejected
- **WHEN** an artifact for `feed_ranker` is already active
- **AND** another insert or update would set a different version of `feed_ranker` to active without deactivating the first
- **THEN** the database MUST reject the change

#### Scenario: Transactional activate
- **WHEN** an operator activates version N for `feed_ranker` inside a transaction that first sets prior active rows to inactive
- **THEN** exactly one `feed_ranker` row remains active after commit

### Requirement: Post impression log for labeling
The Social Postgres schema SHALL store recommendation impressions with `user_id`, `post_id`, `shown_at`, optional `rank_position`, optional integer `model_version`, and optional `request_id`.

#### Scenario: Impression with ML version
- **WHEN** recommend returns a ranked post using active artifact version 3
- **THEN** the impression row MAY store `model_version = 3`

#### Scenario: Impression under rule-based fallback
- **WHEN** recommend ranks using rule-based fallback without an active ONNX model
- **THEN** the impression row MUST store `model_version` as NULL

#### Scenario: Query by user recency
- **WHEN** training or analytics queries impressions for a user ordered by time
- **THEN** an index on `(user_id, shown_at DESC)` SHALL support the access pattern

### Requirement: User seen posts for candidate filtering
The Social Postgres schema SHALL store `(user_id, post_id)` pairs with `seen_at` and primary key `(user_id, post_id)` so recommend can exclude already-seen posts.

#### Scenario: Upsert seen post
- **WHEN** the same user is shown the same post again
- **THEN** the pair remains a single row (upsert updates `seen_at` or leaves one row) and MUST NOT create duplicate primary keys

#### Scenario: List recent seen for user
- **WHEN** candidate filtering loads seen posts for a user
- **THEN** an index on `(user_id, seen_at DESC)` SHALL support retrieval

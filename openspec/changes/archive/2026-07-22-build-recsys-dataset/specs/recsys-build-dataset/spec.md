## ADDED Requirements

### Requirement: Impression-based dataset rows
The build-dataset pipeline SHALL create one training sample per usable `post_impression_log` row joined to its post, and SHALL NOT generate synthetic negative samples from non-impressed pairs.

#### Scenario: One row per impression
- **WHEN** cleaned impressions contain N rows with matching posts
- **THEN** the exported dataset contains N feature+label rows (after dropping unusable joins)

#### Scenario: No negative sampling
- **WHEN** build-dataset runs in the default Phase 1 configuration
- **THEN** it does not add user–post pairs that lack an impression solely to create label 0

### Requirement: Twenty-four hour interaction label
The pipeline SHALL set `label = 1` when the same user likes, saves, or comments on the impressed post with event time in `[shown_at, shown_at + 24 hours]`; otherwise `label = 0`.

#### Scenario: Positive within window
- **WHEN** a user likes the impressed post 2 hours after `shown_at`
- **THEN** the sample label is 1

#### Scenario: Interaction after window is negative for that impression
- **WHEN** a user likes the impressed post 25 hours after `shown_at` and has no earlier interaction in the window
- **THEN** the sample label is 0

#### Scenario: Pre-impression interaction does not create label 1
- **WHEN** a user liked the post before `shown_at` and does not interact again in the window
- **THEN** the sample label is 0

### Requirement: Point-in-time features
Feature signals derived from user history (likes, saves, search, follows, purchases) SHALL only use events with `created_at` strictly before `shown_at`. Recency SHALL use `shown_at - post.created_at` instead of wall-clock now.

#### Scenario: History after shown_at excluded from features
- **WHEN** a like occurs after `shown_at`
- **THEN** that like MUST NOT contribute to hashtag_match or author_affinity for that sample

### Requirement: Feature formula parity
The six feature columns SHALL follow the same formulas as Social `PostFeatureBuilder` (recency half-life 7d; engagement log combo; hashtag weights 1.0/0.8/0.4; author affinity follow+0.5/0.6; mutual follow direct-or-Jaccard; cross_domain as specified in cross-domain capability), including min-max normalization consistent with the documented batch grouping strategy.

#### Scenario: Export columns present
- **WHEN** dataset.parquet is written
- **THEN** it includes `recency_score`, `engagement_score`, `hashtag_match_score`, `author_affinity_score`, `mutual_follow_score`, `cross_domain_product_score`, and `label`

### Requirement: Parquet export and job trigger
The offline service SHALL expose a build-dataset job that writes `dataset.parquet` under the configured output directory and returns a summary (row count, positive rate, warnings).

#### Scenario: Successful export
- **WHEN** an operator triggers build-dataset with valid cleaned inputs and at least one usable impression
- **THEN** `dataset.parquet` is written and the job summary reports row counts

#### Scenario: Missing cleaned inputs
- **WHEN** required cleaned sources are missing
- **THEN** the job fails with an explicit error and does not silently write a success status

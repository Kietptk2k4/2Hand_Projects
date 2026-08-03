## MODIFIED Requirements

### Requirement: Simulation seed for training data

Shared-DB `seed-db --simulate` remains available for legacy demos but MUST NOT be the required path for `feed_ranker` cold-start train. Cold-start train SHALL use file/RAM feed-sim under `RECSYS_FEED_SIM_DIR` (see social-feed-train-data-mode). Documentation MUST mark shared-DB seed as deprecated for train and provide a cleanup checklist for previously polluted tables including `user_seen_posts`.

#### Scenario: File feed-sim preferred for train
- **WHEN** an operator trains feed_ranker in SEED_ONLY mode
- **THEN** they use feed-sim / feed-retrain file corpora
- **AND** are not required to run `seed-db --simulate` into shared databases

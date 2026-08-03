## ADDED Requirements

### Requirement: Feed file-sim and feed-retrain offline jobs

recsys-offline SHALL expose feed cold-start and retrain jobs that respect Admin `social.feed.ltr.train_data_mode` and write/read file corpora under `RECSYS_FEED_SIM_DIR` without requiring shared-DB seed-db for SEED_ONLY.

#### Scenario: Operator runs feed-sim
- **WHEN** `POST /jobs/feed-sim` is called with `RECSYS_SIM_ALLOW=1`
- **THEN** cleaned CSV sources are written under the feed sim directory
- **AND** Auth/Social/Commerce application databases are not written by that job

#### Scenario: Operator runs feed-retrain
- **WHEN** `POST /jobs/feed-retrain` runs
- **THEN** mode is resolved first
- **AND** steps follow resolve → (feed-sim) → (clean-real) → build → split → train → evaluate → export-activate

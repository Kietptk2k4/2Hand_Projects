## ADDED Requirements

### Requirement: Time-ordered 80/10/10 split
The split-dataset pipeline SHALL sort `dataset.parquet` rows by `shown_at` ascending with stable tie-break on `user_id` then `post_id`, SHALL NOT shuffle, and SHALL assign the first 80% of rows to train, the next 10% to val, and the remaining 10% to test (integer cut indices `int(n*0.80)` and `int(n*0.90)`).

#### Scenario: Ratio on sufficient rows
- **WHEN** the dataset has at least 10 rows with valid `shown_at`
- **THEN** train receives approximately 80% of rows, val approximately 10%, and test the remainder, with train then val then test in time order

#### Scenario: No shuffle
- **WHEN** split-dataset runs
- **THEN** row order within each split preserves the global sorted `shown_at` order

### Requirement: Temporal leak checks fail closed
When both sides of a cut are non-empty, the pipeline SHALL require `max(train.shown_at) ≤ min(val.shown_at)` and `max(val.shown_at) ≤ min(test.shown_at)`. Violation MUST fail the job with an explicit error and MUST NOT write success status.

#### Scenario: Ordered splits pass
- **WHEN** sorted cuts yield non-decreasing time boundaries across train → val → test
- **THEN** the job succeeds and writes partition parquet files

#### Scenario: Temporal violation fails
- **WHEN** a non-empty later split contains a `shown_at` earlier than the max of the previous non-empty split
- **THEN** the job fails closed without claiming success

### Requirement: Overlap and positive-rate report
The pipeline SHALL write `split_meta.json` (and include equivalent fields in the job summary) with per-split row counts, positive rates, and user/post overlap percentages for train∩val, train∩test, and val∩test using Jaccard `|A∩B|/|A∪B|` (0 when union empty). Entity overlap MUST NOT by itself fail the job.

#### Scenario: Meta includes overlap fields
- **WHEN** split-dataset completes successfully
- **THEN** `split_meta.json` contains overlap percentage fields and positive rates per non-empty split

#### Scenario: Overlap does not fail
- **WHEN** the same `user_id` appears in both train and test
- **THEN** the job still succeeds if temporal checks pass
- **AND** the overlap is reflected in the report

### Requirement: Empty slice warning on small datasets
If val or test is empty after integer cuts, the pipeline SHALL still succeed when temporal checks for non-empty adjacent pairs pass (or no pair exists), and SHALL record an explicit warning in the summary.

#### Scenario: Tiny dataset warns
- **WHEN** `n` is too small for a non-empty val or test under 80/10/10 integer cuts
- **THEN** empty partitions may be written
- **AND** the summary includes a warning naming the empty split

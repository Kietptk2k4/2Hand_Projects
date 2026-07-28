## ADDED Requirements

### Requirement: Dev-only Mongo post timestamp repair
The recsys-offline package SHALL provide a guarded, dry-run-capable repair entrypoint that updates existing Social Mongo `posts` documents whose `created_at` is a string and/or outside a configurable recent window, converting those fields to BSON Date values distributed within `[now − window_days, now]` (and aligning `updated_at`), so online candidate recall date filters can see sim inventory without a full re-seed.

#### Scenario: Dry-run reports type and window stats
- **WHEN** an operator runs the repair entrypoint with dry-run enabled and `RECSYS_SIM_ALLOW` set
- **THEN** the tool reports counts of posts by `created_at` BSON type (string vs date)
- **AND** reports how many would be updated vs already eligible inside the configured window
- **AND** does not modify Mongo documents

#### Scenario: Apply converts string timestamps to Date in window
- **WHEN** an operator runs the repair entrypoint in apply mode against documents with string `created_at`
- **THEN** those documents' `created_at` and `updated_at` become BSON Date values inside `[now − window_days, now]`
- **AND** a summary includes the number of documents updated

#### Scenario: Guard rejects when sim allow flag unset
- **WHEN** the repair entrypoint is invoked without the sim allow environment guard
- **THEN** it refuses to run (dry-run or apply)
- **AND** leaves Mongo unchanged

#### Scenario: Default scope prefers sim posts
- **WHEN** the repair runs with default filters
- **THEN** it targets sim-seeded posts (for example caption prefix `Sim `) rather than arbitrary non-sim documents
- **AND** an explicit override may widen scope for known-dev databases

## ADDED Requirements

### Requirement: Mongo seeded posts use BSON Date on shared sim clock
The simulation Social Mongo writer SHALL persist each post's `created_at` and `updated_at` as BSON Date values (via timezone-aware UTC `datetime` objects), SHALL NOT write ISO-8601 strings for those fields, and SHALL assign timestamps spread across the shared simulation time window ending at a configurable end instant (defaulting to current UTC) spanning `sim_days` from skeleton/config so seeded posts remain eligible for online candidate recall date-range queries.

#### Scenario: Writer emits datetime not string
- **WHEN** `write_social_posts_mongo` builds documents for skeleton posts
- **THEN** each document's `created_at` and `updated_at` are timezone-aware UTC datetimes (not `str`)
- **AND** after upsert, Mongo stores those fields as BSON Date

#### Scenario: Timestamps fall inside serving-aligned window
- **WHEN** the writer runs with default end instant equal to current UTC and `sim_days` from config
- **THEN** every seeded post's `created_at` lies in `[end − sim_days, end]`
- **AND** not all posts share a single identical timestamp when more than one post is seeded

#### Scenario: No hard-coded stale literal date
- **WHEN** the default writer path runs without an explicit fixed historical override
- **THEN** it does not assign the literal string or date `2025-12-20T10:00:00Z` as every post's `created_at`

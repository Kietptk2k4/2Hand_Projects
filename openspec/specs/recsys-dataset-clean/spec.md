# recsys-dataset-clean

## Purpose

Read-only extract-and-clean pipeline that produces training-ready entity datasets from Social Mongo/Postgres without mutating production rows.

## Requirements

### Requirement: Read-only clean pipeline
The dataset clean pipeline SHALL read from Social MongoDB and Postgres sources and MUST NOT UPDATE or DELETE production database rows as part of cleaning.

#### Scenario: No mutation of source tables
- **WHEN** the clean job runs successfully
- **THEN** source tables/collections used for extract remain unmodified by the clean job
- **AND** outputs are written only to configured filesystem dataset paths (and logs)

### Requirement: Source coverage for Phase 1
The clean pipeline SHALL extract and clean at least: Mongo `posts` and `comments`; Postgres `post_likes`, `post_saves`, `follows`, `search_history`; and `post_impression_log` when the table exists and has data.

#### Scenario: Missing impression table or empty impressions
- **WHEN** `post_impression_log` is empty or unavailable
- **THEN** the job SHALL still clean other available sources
- **AND** SHALL log a warning that label construction will be incomplete

#### Scenario: Excluded infra tables
- **WHEN** clean runs
- **THEN** it MUST NOT require `outbox_events` or `processed_domain_events` as training sample sources

### Requirement: Null and duplicate removal
The pipeline SHALL drop records with required nulls and remove duplicates according to natural keys, logging counts per reason.

#### Scenario: Drop null author on post
- **WHEN** a post document lacks `author_id` or `created_at`
- **THEN** that post is excluded from the clean posts output
- **AND** the drop counter for the corresponding reason increments

#### Scenario: Deduplicate likes
- **WHEN** duplicate like rows share the same `(user_id, post_id)`
- **THEN** only one like remains in the clean likes output
- **AND** duplicates dropped are counted

### Requirement: Timestamp normalization
The pipeline SHALL normalize timestamps to UTC for cleaned outputs.

#### Scenario: Naive timestamp treated as UTC
- **WHEN** a source timestamp has no timezone
- **THEN** the cleaned output stores an unambiguous UTC timestamp representation

### Requirement: Hashtag normalization
The pipeline SHALL normalize post hashtags by trimming, stripping a leading `#`, and lowercasing, consistent with Java recommend feature matching.

#### Scenario: Normalize messy hashtag
- **WHEN** a post hashtag value is `#Sneaker `
- **THEN** the cleaned hashtag value is `sneaker`

#### Scenario: Dedupe hashtags within a post
- **WHEN** a post contains duplicate hashtags after normalization
- **THEN** the cleaned post keeps a unique set of hashtags

### Requirement: Validation rules
The pipeline SHALL validate entity integrity and drop invalid rows with logged reasons (for example invalid UUID user ids, self-follow, empty search keywords, non-ACTIVE/DELETED posts excluded from ranking corpus as configured).

#### Scenario: Reject self-follow
- **WHEN** a follow row has `follower_id = followee_id`
- **THEN** it is dropped and counted

#### Scenario: Filter deleted posts from ranking corpus extract
- **WHEN** a post has `status = DELETED` or `status = DRAFT`
- **THEN** it is excluded from the cleaned posts corpus used for recommendation training extracts

### Requirement: Drop logging
The pipeline SHALL emit a structured summary of input counts, output counts, and drops by reason per source entity.

#### Scenario: Summary after clean
- **WHEN** clean completes
- **THEN** logs or a summary file include per-source kept/dropped totals and reason breakdowns

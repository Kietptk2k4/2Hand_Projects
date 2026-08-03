# social-feed-seen-posts-retention

## Purpose

Periodic TTL cleanup of Social `user_seen_posts` so For You candidates can reappear after a configurable retention window.

## ADDED Requirements

### Requirement: Admin-configurable seen-posts retention days

Social SHALL use Admin system config `social.feed.seen_posts_retention_days` (`value_type = INTEGER`, default **7**). Retention days MUST be >= 1. Operators set this via system-configs tab.

#### Scenario: Admin sets retention to 14 days
- **WHEN** an admin sets `social.feed.seen_posts_retention_days` to `14`
- **THEN** the next cleanup run deletes rows with `seen_at` older than 14 days

### Requirement: Daily TTL purge of expired seen posts

social-service SHALL run a scheduled job (default cron `0 15 3 * * *`) that deletes from `user_seen_posts` where `seen_at < now() - retention_days`. The job SHALL log deleted row count, retention days, and cutoff. If Admin is unreachable, the job MAY fall back to configured property/env default and MUST log a warning (must not skip cleanup forever).

#### Scenario: Expired seen rows removed
- **WHEN** the retention cron runs with retention_days = 7
- **AND** a row has `seen_at` older than 7 days
- **THEN** that row is deleted
- **AND** newer rows remain

#### Scenario: Posts can re-enter For You after TTL
- **WHEN** a user's seen row for a post is deleted by retention
- **THEN** CandidatePoolService no longer excludes that post_id for that user solely due to seen history

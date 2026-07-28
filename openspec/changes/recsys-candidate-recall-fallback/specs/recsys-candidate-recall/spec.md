## ADDED Requirements

### Requirement: Progressive time-window recall fallback
The Recommend Feed candidate pool SHALL retrieve ACTIVE PUBLIC moderation-safe posts with `created_at` within a progressive wall-clock window ladder of **7 days, then 30 days, then 90 days**. The service SHALL advance to the next wider window when the number of candidates remaining after seen-post exclusion (and normal merge of followee-priority then public fill) is strictly less than the configured minimum pool size **N** (default **20**). It SHALL stop at the first window that yields at least **N** candidates, or return the result of the widest window when every step is still below **N**.

#### Scenario: Seven-day window sufficient
- **WHEN** a user requests recommend feed and the unseen candidate count within 7 days is >= N
- **THEN** the pool is built using only the 7-day window
- **AND** wider windows are not queried

#### Scenario: Fallback to thirty days
- **WHEN** the unseen candidate count within 7 days is &lt; N
- **AND** the unseen count within 30 days is >= N
- **THEN** the pool used for ranking is the 30-day window result

#### Scenario: Fallback to ninety days
- **WHEN** unseen counts for both 7-day and 30-day windows are &lt; N
- **THEN** the pool is built with the 90-day window
- **AND** that result is returned even if its size is still &lt; N (including empty)

#### Scenario: Empty after widest window
- **WHEN** after the 90-day window there are zero unseen eligible posts
- **THEN** recommend feed returns an empty item list with success (not a model/load error)

### Requirement: Preserve existing eligibility and merge rules
Within each attempted window, candidate selection SHALL continue to require ACTIVE status, PUBLIC visibility, and safe moderation status; SHALL exclude posts present in `user_seen_posts` for that user; and SHALL prefer posts from accepted followees before filling from the general public corpus up to the existing max pool size.

#### Scenario: Seen posts excluded across windows
- **WHEN** a post is in `user_seen_posts` for the user and otherwise matches a widened window
- **THEN** it MUST NOT appear in the candidate pool

#### Scenario: Followee priority retained
- **WHEN** the user follows authors who have eligible posts inside the chosen window
- **THEN** those followee posts are considered before general public fill for that window

### Requirement: Ranking freshness half-life unchanged
Widening the recall window MUST NOT change the Recommend Feed feature formula for `recency_score`; the recency half-life SHALL remain **7 days** so older recalled posts may enter the pool but receive lower recency contribution than recent posts.

#### Scenario: Older recalled post still scored with seven-day half-life
- **WHEN** a candidate older than 7 days is included because of window fallback
- **THEN** its `recency_score` is computed with the same 7-day half-life as before this change
- **AND** ranking still runs (LightGBM when session loaded, otherwise rule-based)

### Requirement: Configurable minimum pool size and windows
The minimum pool size **N** and the ordered window day list SHALL be configurable via Social recommendation settings (with defaults N=20 and windows 7,30,90).

#### Scenario: Default configuration
- **WHEN** no override env is set
- **THEN** recall uses N=20 and windows 7 → 30 → 90 days

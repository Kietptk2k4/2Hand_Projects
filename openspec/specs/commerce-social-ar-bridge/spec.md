# commerce-social-ar-bridge

## Purpose

Social→Commerce interest bridge for Home: weighted tag export into `user_social_interest_export`, offline tag→category association-rule mining into `social_tag_category_ar`, and online AR-gated cross-domain retrieval without Social feed HTTP.

## Requirements

### Requirement: Weighted social interest tags
The Social→Commerce bridge SHALL aggregate per-user interest tags using weights search×4, save×3, comment×2, and like×1 over a 90-day window, apply export-time decay \(2^{-\Delta/14d}\) relative to the export `as_of`, and MUST NOT use like-only signals as the sole social interest source.

#### Scenario: Search-heavy tag without likes
- **WHEN** a user searches tag Jordan multiple times and saves related posts but never likes
- **THEN** Jordan still receives a positive interest score from search and save weights

### Requirement: Commerce consumer table for social interest export
An offline/batch job SHALL publish per-user social interest rows into a Commerce-owned table `user_social_interest_export` (or equivalent) with at least `user_id`, `tag_type` (`HASHTAG`|`KEYWORD`), normalized `tag`, decayed weighted `score`, `window_days`, `computed_at`, and `as_of`, keyed by `(user_id, tag_type, tag)`. The job cadence SHALL be configurable with default **daily**. For each user in a batch, the job SHALL prefer full refresh (replace all tags for that user for the export `as_of`) over partial upsert of a subset. Commerce Home online profile building SHALL read this table (or its latest batch) and max-normalize scores within each `tag_type`, and MUST NOT call Social HTTP APIs to rebuild tag scores on the recommend hot path. Missing rows for a user SHALL yield empty social facets. Export rows older than a freshness warning threshold (e.g. 7 days) MAY still be used.

#### Scenario: Online profile reads export without Social feed call
- **WHEN** Commerce Home recommend builds social facets for a user
- **THEN** it reads `user_social_interest_export` (or equivalent consumer store)
- **AND** it does not call Social recommend-feed or other Social HTTP APIs for that purpose

#### Scenario: No export rows means empty social facets
- **WHEN** a user has no rows in the social interest export table
- **THEN** hashtag and keyword maps are empty
- **AND** cross-domain retrieval contributes no candidates from social tags

#### Scenario: Daily export applies decay at as_of
- **WHEN** the social interest export job runs with as_of = T
- **THEN** each tag score is weighted 4/3/2/1 over events in the 90d window before T
- **AND** decay \(2^{-\Delta/14d}\) is applied relative to T before write

### Requirement: Offline association-rule mining parameters
An offline job SHALL mine association rules of the form **interest_tag → commerce_category** only (MUST NOT emit category→tag as the primary mapping). Mining baskets SHALL be per-user over a rolling window (default **90 days**): basket items = social interest tags from the same weighted sources as the export job (undecayed counts MAY be used for mining) ∪ leaf categories from COMPLETED purchases. The miner SHALL use **Apriori** or **FP-Growth** frequent-itemset association only (default **Apriori** for fixtures/first implementation) with identical support/confidence semantics; MUST NOT invent neural/graph substitutes. Support SHALL be the fraction of in-window user-baskets containing the itemset (denominator = users with non-empty baskets). Default thresholds SHALL be configuration-driven: **min_support = 0.01**, **min_confidence = 0.05** (aligned with online AR confidence gate default 0.05).

#### Scenario: Only tag→category rules published
- **WHEN** the AR mining job completes
- **THEN** published rows are interest_tag → category_id with support and confidence
- **AND** the job does not publish category→tag as the primary consumer contract

#### Scenario: Below min_support rules dropped
- **WHEN** a candidate rule has support below the configured min_support
- **THEN** that rule is not written to the mapping table

### Requirement: social_tag_category_ar consume schema
The offline AR job SHALL publish into a Commerce-owned table (name `social_tag_category_ar` or equivalent) with at least: `tag`, `tag_type`, `category_id`, `support`, `confidence`, `updated_at`, primary key `(tag_type, tag, category_id)`.

#### Scenario: Mapping available for retrieval
- **WHEN** a high-confidence rule exists for interest tag Gaming → category Laptop
- **THEN** Commerce Home cross-domain retrieval can select ACTIVE Laptop products for a user whose profile includes Gaming
- **AND** the candidate `arScore` equals `clip(confidence × tag_score_norm, 0, 1)` (taking max across matching rules)

### Requirement: Online AR gate uses configured confidence threshold
Cross-domain retrieval SHALL admit association rules only when `confidence` is greater than or equal to a configured threshold (default value supplied in configuration, e.g. 0.05) and MUST NOT treat a single numeric threshold as an immutable hard-coded business constant in application logic that cannot be retuned via config.

#### Scenario: Below-threshold rules excluded
- **WHEN** a rule's confidence is below the configured threshold
- **THEN** that rule does not contribute categories or candidates for the request

### Requirement: Commerce reads social signals without owning Social feed
Commerce-service SHALL obtain social interest inputs via the batch export consumer contract above and MUST NOT require Social `/feed/for-you` to serve Commerce Home.

#### Scenario: Home recommend without Social feed call
- **WHEN** Commerce Home recommend runs for an authenticated user
- **THEN** it does not call Social recommend-feed HTTP APIs as part of building the product list

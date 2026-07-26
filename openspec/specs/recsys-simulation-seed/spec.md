# recsys-simulation-seed

## Purpose

Dev-only fashion simulation seed and bot package that generates Auth/Commerce/Social training corpus for Recommended Feed offline ML (impressions, engagement, cart/purchase) without calling online RecommendPosts.

## Requirements

### Requirement: Fashion-only simulation scope
The simulation seed and bot package SHALL generate data only within the Fashion Social + Fashion Commerce MVP vertical and MUST NOT introduce non-fashion catalog verticals (for example laptop or gaming) solely to enrich training data.

#### Scenario: Persona niches are fashion
- **WHEN** personas are loaded from simulation config
- **THEN** each persona maps only to fashion hashtags and fashion category identifiers from the Commerce fashion catalog (existing or fashion-extended leaves)

#### Scenario: Reject non-fashion vertical expansion as required seed input
- **WHEN** an operator runs the default Phase-1 simulation configuration
- **THEN** seeded products and posts do not require non-fashion root categories outside the fashion catalog conventions

### Requirement: Target corpus volumes
The default simulation configuration SHALL target approximately 120 bot users, 40 seller/authors, 600 posts, 400 ACTIVE products with stock 1, 40 shops, 12 sessions per user, and feed size 20, producing enough unique `(user_id, post_id)` impressions that after clean dedupe the build-dataset row count is at least 20 000.

#### Scenario: Minimum impression rows after clean
- **WHEN** seed and simulation complete successfully under default config and clean+build-dataset run
- **THEN** `dataset_meta` (or equivalent summary) reports `rows >= 20000`

### Requirement: Early sessions count toward row budget
Early timeline sessions (pre-history/warm feature phase) SHALL still write `post_impression_log` rows that participate in the ≥20k training row budget.

#### Scenario: Early session creates impressions
- **WHEN** a bot runs an early session before mature affinity history exists
- **THEN** impressions for that session are persisted with `shown_at` on the sim clock
- **AND** those impressions are eligible as build-dataset samples

### Requirement: Persona affinity matrix drives behavior
The simulator SHALL assign each bot a fashion persona and SHALL use a configured affinity matrix (persona × niche) to drive feed sampling weights and engagement probabilities for like, save, comment, follow, search, cart, and purchase.

#### Scenario: Higher affinity increases engage probability
- **WHEN** a candidate post niche has high affinity for the bot persona
- **THEN** the probability of like/save/comment for that impression is higher than for a low-affinity mismatch post under the same config

#### Scenario: Search keywords match hashtag vocabulary
- **WHEN** a bot records search history
- **THEN** each keyword equals a normalized hashtag token used by posts (lowercase, no leading `#`) so hashtag_match can fire

### Requirement: Consistent sim-clock timeline
All simulation writes SHALL use a shared simulation clock such that for each positive engagement intended to label an impression, `created_at` lies in `[shown_at, shown_at + 24 hours]`, and cart/purchase events follow engagement on that timeline without breaking Commerce stock/status invariants for seeded listings.

#### Scenario: Label window respected
- **WHEN** the simulator creates a like for an impressed post
- **THEN** the like timestamp is at or after `shown_at` and strictly before `shown_at + 24h`

#### Scenario: Impression and seen posts written together
- **WHEN** the simulator records a feed impression for `(user, post)`
- **THEN** it inserts `post_impression_log` with `request_id` and `rank_position`
- **AND** upserts `user_seen_posts` for the same pair
- **AND** does not re-impress that pair later in the run

### Requirement: Affinity-weighted feed ranking
When inserting impressions for a session, the simulator SHALL sample and order candidate posts using persona affinity weights (with intentional mismatch mix) rather than calling the online RecommendPosts API.

#### Scenario: No online recommend dependency
- **WHEN** simulation runs
- **THEN** it does not call Social RecommendPosts HTTP endpoints to obtain the feed

#### Scenario: Request grouping for metrics
- **WHEN** a session writes its feed impressions
- **THEN** all posts in that session share one `request_id`
- **AND** `rank_position` reflects the affinity-weighted order used at insert time

### Requirement: Cart and purchase are mandatory in simulation DoD
The default simulation SHALL create cart and COMPLETED purchase activity for a majority of bots so exported purchase profiles yield non-zero `cross_domain_product_score` on a material fraction of samples.

#### Scenario: Majority buyers
- **WHEN** simulation completes under default config
- **THEN** at least 60% of bot users have at least one COMPLETED order in Commerce

### Requirement: Distribution KPI gate
After simulation and the offline clean/build (and split when required for cutoff checks), a KPI gate SHALL verify dataset health and MUST fail closed when thresholds are missed.

#### Scenario: KPI pass
- **WHEN** rows ≥ 20000, positive_rate ∈ [0.12, 0.25], share of rows with `cross_domain_product_score > 0` ≥ 0.15, share with `hashtag_match_score > 0` ≥ 0.40, positives exist in train/val/test after split, and purchase profile respects train cutoff
- **THEN** the KPI gate reports success

#### Scenario: KPI fail closed
- **WHEN** any mandatory KPI threshold fails
- **THEN** the gate exits non-zero (or returns structured failure) and MUST NOT claim simulation DoD success

### Requirement: Dev-only safety
Simulation seed and bot writers SHALL require an explicit dev allow configuration and MUST refuse to run against unmarked production targets.

#### Scenario: Missing allow flag
- **WHEN** `RECSYS_SIM_ALLOW` (or equivalent) is not enabled
- **THEN** seed/simulate commands fail with an explicit error and write no simulation rows

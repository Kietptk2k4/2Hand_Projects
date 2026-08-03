## ADDED Requirements

### Requirement: Entity-based co-occurrence not product_id pairs
The Collaborative Filtering subsystem for Commerce Home SHALL compute co-occurrence over stable entities (at least leaf category and brand) and MUST NOT rely on product_id→product_id edges as the primary CF graph, because second-hand listings with stock 1 leave the catalog after sale. Shop entity edges are deferred in v1.

#### Scenario: Offline entity edges
- **WHEN** the offline CF job runs over eligible cart and completed-order events in the configured time window
- **THEN** it writes entity–entity scores for category and brand pairs
- **AND** does not require surviving product_id pairs for those scores to exist

### Requirement: Offline co-occurrence window and pair construction
The offline entity co-occurrence job SHALL use a rolling window of **180 days** before job `as_of`. Entity types in v1 SHALL be `LEAF_CATEGORY` and `BRAND` only.

**Completed pairs:** Within each COMPLETED order, every unordered pair of distinct entities among that order’s line-item leaf categories and brands SHALL contribute weight **1.0**. An entity MUST NOT form a self-pair with itself in the same order.

**Cart pairs:** For the same user, cart-add events (or live `cart_items` when add events are unavailable) whose entities co-occur within a **24-hour** wall-clock window SHALL contribute weight **0.6** per unordered pair. Per-user-day dedupe is optional; simple weighted counting is allowed.

#### Scenario: Completed order produces category–brand pairs
- **WHEN** a COMPLETED order contains leaf category C1 with brand B1 and leaf category C2 with brand B2
- **THEN** the job accumulates weight 1.0 for each distinct unordered entity pair among {C1, B1, C2, B2}
- **AND** does not write a self-pair for C1–C1 or B1–B1

#### Scenario: Cart window uses 0.6 weight
- **WHEN** a user has two cart entities that co-occur within 24 hours and no completed pair for that entity pair
- **THEN** that unordered pair accumulates weight 0.6 (not 1.0)

### Requirement: Co-occurrence score and top-M neighbors
For each directed or undirected storage of an entity→neighbor edge, the offline job SHALL set `score = log1p(sum of pair weights)` for that entity–neighbor pair and retain at most the top **M** neighbors per entity by score descending. Default **M = 50** SHALL be configuration-driven.

#### Scenario: Score uses log1p of weight sum
- **WHEN** an entity–neighbor pair has accumulated weight sum W from completed and cart contributions
- **THEN** the stored score equals log1p(W)

#### Scenario: Neighbor fan-out capped
- **WHEN** an entity has more than M scored neighbors
- **THEN** only the top M by score are persisted for online lookup

### Requirement: entity_cooccur consume schema
The offline job SHALL publish into a Commerce-owned table (name `entity_cooccur` or equivalent) with at least: `entity_type`, `entity_id`, `neighbor_type`, `neighbor_id`, `score`, `updated_at`, with primary key `(entity_type, entity_id, neighbor_type, neighbor_id)`.

#### Scenario: Table is queryable by seed entity
- **WHEN** online CF retrieval looks up neighbors for a seed `(entity_type, entity_id)`
- **THEN** it can read neighbor rows ordered by `score` descending without scanning product_id pair tables

### Requirement: Seed weights completed and cart
Online and offline CF seeding SHALL weight Completed order interactions at 1.0 and Cart interactions at 0.6 when accumulating seed entity strength for a user.

#### Scenario: Mixed seed
- **WHEN** a user has a completed purchase in brand Nike and a cart line in category Socks
- **THEN** both entities contribute to CF seeds
- **AND** the completed brand seed is weighted at least as strongly as the cart category seed under the 1.0 vs 0.6 rule

### Requirement: HYBRID entity_cooccur uses max merge
When train_data_mode is `HYBRID` and both REAL and SEED co-occurrence scores exist for the same entity–neighbor primary key, the published `entity_cooccur.score` SHALL be **max(real_score, seed_score)**. The job MUST NOT sum REAL and SEED weights for the same edge.

#### Scenario: HYBRID keeps stronger edge
- **WHEN** REAL score for an edge is 2.0 and SEED score is 1.1
- **THEN** the persisted score is 2.0

### Requirement: Online neighbor products from configurable Top-N entities
Given user seed entities, the CF retriever SHALL look up the top-N neighbor entities by precomputed score (N is a configuration parameter, not a hard-coded constant in the requirement) and retrieve ACTIVE in-stock products belonging to those neighbor entities up to the Entity CF soft quota. Products within a neighbor entity SHALL be ordered by completed_order_items count descending then `created_at` descending, with a configurable per-neighbor product soft cap (default 20 in config). Each emitted `CandidateProduct` SHALL carry `cfScore` in **[0, 1]** computed as request-local max-normalization of `raw = edge_score × seed_strength` among source-E candidates.

#### Scenario: CF contributes candidates with cfScore
- **WHEN** neighbor entities exist for the user seeds
- **THEN** eligible products under those entities may enter the Home candidate pool under the CF source flag
- **AND** each such candidate carries a `cfScore` in [0, 1] for the feature builder
- **AND** sold-out or inactive products are excluded

#### Scenario: Neighbor count is configurable
- **WHEN** operators change the configured max neighbor count
- **THEN** online CF retrieval uses the new Top-N without a code change to the normative cap logic

## ADDED Requirements

### Requirement: Entity-based co-occurrence not product_id pairs
The Collaborative Filtering subsystem for Commerce Home SHALL compute co-occurrence over stable entities (at least leaf category and brand) and MUST NOT rely on product_id→product_id edges as the primary CF graph, because second-hand listings with stock 1 leave the catalog after sale.

#### Scenario: Offline entity edges
- **WHEN** the offline CF job runs over eligible cart and completed-order events in the configured time window
- **THEN** it writes entity–entity scores for category and brand pairs
- **AND** does not require surviving product_id pairs for those scores to exist

### Requirement: Seed weights completed and cart
Online and offline CF seeding SHALL weight Completed order interactions at 1.0 and Cart interactions at 0.6 when accumulating seed entity strength for a user.

#### Scenario: Mixed seed
- **WHEN** a user has a completed purchase in brand Nike and a cart line in category Socks
- **THEN** both entities contribute to CF seeds
- **AND** the completed brand seed is weighted at least as strongly as the cart category seed under the 1.0 vs 0.6 rule

### Requirement: Online neighbor products from entities
Given user seed entities, the CF retriever SHALL look up top neighbor entities by precomputed score and retrieve ACTIVE in-stock products belonging to those neighbor entities up to the Entity CF soft quota.

#### Scenario: CF contributes candidates
- **WHEN** neighbor entities exist for the user seeds
- **THEN** eligible products under those entities may enter the Home candidate pool under the Entity CF source flag
- **AND** sold-out or inactive products are excluded

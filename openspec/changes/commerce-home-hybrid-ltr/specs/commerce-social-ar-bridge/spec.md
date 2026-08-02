## ADDED Requirements

### Requirement: Weighted social interest tags
The Social→Commerce bridge SHALL aggregate per-user interest tags using weights search×4, save×3, comment×2, and like×1 (before optional normalization), and MUST NOT use like-only signals as the sole social interest source.

#### Scenario: Search-heavy tag without likes
- **WHEN** a user searches tag Jordan multiple times and saves related posts but never likes
- **THEN** Jordan still receives a positive interest score from search and save weights

### Requirement: Offline association rules to commerce categories
An offline job SHALL mine association rules from social interest tags to Commerce categories (support/confidence) and publish a mapping table consumable by commerce-service for cross-domain retrieval and `cross_domain_score`.

#### Scenario: Mapping available for retrieval
- **WHEN** a high-confidence rule exists for interest tag Gaming → category Laptop
- **THEN** Commerce Home cross-domain retrieval can select ACTIVE Laptop products for a user whose profile includes Gaming
- **AND** the candidate may carry a non-zero cross_domain_score derived from rule confidence

### Requirement: Commerce reads social signals without owning Social feed
Commerce-service SHALL obtain social interest inputs via batch export and/or internal read contracts and MUST NOT require Social `/feed/for-you` to serve Commerce Home.

#### Scenario: Home recommend without Social feed call
- **WHEN** Commerce Home recommend runs for an authenticated user
- **THEN** it does not call Social recommend-feed HTTP APIs as part of building the product list

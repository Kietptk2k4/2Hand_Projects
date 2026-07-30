## ADDED Requirements

### Requirement: Soft-quota multi-source candidate pool
The Commerce Home candidate builder SHALL union retrieval sources with soft upper caps — Popular up to 100, Personal up to 150, Entity CF up to 150, Cross-domain AR up to 150, Semantic fill remaining — then apply inventory hard filters and deduplicate to a pool of at most 500 products while preserving multi-source membership flags.

#### Scenario: Authed pool mixes sources
- **WHEN** an authenticated user has personal history and available CF/AR neighbors
- **THEN** the pool may include products from Popular, Personal, Entity CF, and Cross-domain sources within their caps
- **AND** after dedupe the pool size is ≤ 500

#### Scenario: Semantic only fills remaining
- **WHEN** union of Popular, Personal, Entity CF, and Cross-domain yields fewer than 500 eligible products
- **THEN** Semantic/content similarity retrieval may add candidates only to fill the remainder
- **AND** Semantic MUST NOT replace the primary sources when they already fill the pool

#### Scenario: Inventory hard filter on all sources
- **WHEN** any retrieval source proposes a product
- **THEN** that product is excluded unless it passes ACTIVE, stock-available, and shop-sellable business rules before entering the final pool

#### Scenario: Multi-source flags retained
- **WHEN** the same product_id is retrieved from more than one source
- **THEN** the pool keeps a single product entry
- **AND** source flags for all contributing sources remain true for feature building

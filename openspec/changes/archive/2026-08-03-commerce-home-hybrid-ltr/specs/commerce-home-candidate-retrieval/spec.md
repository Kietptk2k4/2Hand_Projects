## ADDED Requirements

### Requirement: Soft-quota multi-source pool without semantic fill
The Commerce Home candidate builder SHALL union retrieval sources A (Popular/new/rating), B (Personal), E (Entity CF), and C (Cross-domain AR) with soft upper caps 100 / 150 / 150 / 150 respectively, SHALL apply inventory hard filters and exclude the caller's own-shop products, and SHALL deduplicate to a pool of at most 500 `CandidateProduct` rows while OR-merging source membership. Semantic/ANN retrieval (source D) MUST NOT run in v1. Pool size MAY be strictly less than 500; 500 is an upper bound only, not a fill target.

#### Scenario: Authed pool mixes sources
- **WHEN** an authenticated user has personal history and available CF/AR neighbors
- **THEN** the pool may include products from Popular, Personal, Entity CF, and Cross-domain sources within their caps
- **AND** after dedupe the pool size is ≤ 500

#### Scenario: Underfull pool is acceptable
- **WHEN** the union of A, B, E, and C yields fewer than 500 eligible products
- **THEN** the builder returns that smaller pool
- **AND** does not invent semantic/embedding candidates to pad to 500

#### Scenario: Inventory, own-shop, and vacation hard filter
- **WHEN** any retrieval source proposes a product
- **THEN** that product is excluded unless it is ACTIVE with available stock under Commerce sellable rules
- **AND** products belonging to the caller's own shop are excluded
- **AND** products whose shop is in vacation mode are excluded

#### Scenario: Multi-source flags retained
- **WHEN** the same product_id is retrieved from more than one source
- **THEN** the pool keeps a single `CandidateProduct`
- **AND** the source set contains all contributing sources for feature building

### Requirement: CandidateProduct carries product snapshot and source scores
Retrieval SHALL return `CandidateProduct` values that include a product read-model/snapshot with attributes required by the Home feature builder (at least category, brand, shop, price, created time, rating summary), an `EnumSet` (or equivalent) of retrieval sources, and optional `personalScore`, `cfScore`, and `arScore`. When present, `cfScore` and `arScore` MUST already lie in **[0, 1]** (retrieval owns raw-to-unit mapping). Retrieval MUST NOT attach a shared cross-source `retrievalScore` and MUST NOT normalize heterogeneous source scores into one shared scalar for LTR.

#### Scenario: Feature builder can avoid re-fetch
- **WHEN** a candidate is emitted for ranking
- **THEN** the embedded product snapshot includes the listing attributes needed to compute match, recency, rating, and price features
- **AND** the feature builder is not required to load the same product row again solely to obtain those attributes

#### Scenario: No shared retrievalScore
- **WHEN** candidates are assembled from multiple sources
- **THEN** each candidate exposes only source-native scores (`personalScore`, `cfScore`, `arScore` as applicable)
- **AND** no single normalized `retrievalScore` field is required or used for LTR input

#### Scenario: CF and AR scores are unit-interval
- **WHEN** a candidate includes `cfScore` or `arScore`
- **THEN** that score is in [0, 1]
- **AND** the feature builder may only defensively clip without applying CF/AR-specific remapping

### Requirement: Source A Popular slices New / Popular-90d / Rating
Source A SHALL retrieve a soft cap of 100 candidates as the union of three global slices with soft shares New 40, Popular 40, and Rating 20: New ranked by `created_at` descending; Popular ranked by completed order-item counts over a 90-day window; Rating ranked by `rating_avg` among products with `rating_count ≥ 3`. Source A MUST NOT personalize by the user profile. Membership sets the Popular retrieval source flag.

#### Scenario: Popular uses 90-day completed items
- **WHEN** Source A Popular slice ranks catalog products
- **THEN** popularity for that slice uses completed order-item counts in the last 90 days
- **AND** does not require a blended recency×popularity×rating score inside retrieval

### Requirement: Source B Personal ranks by facet max only
Source B SHALL map the caller's profile category, brand, and shop facets to ACTIVE in-stock products in those entities, rank by `personalScore = max(category_score, brand_score, shop_score)` for the product's matching facets, apply the Personal soft cap of 150, and MUST NOT multiply `personalScore` by listing recency at retrieval time.

#### Scenario: No recency multiply on personal retrieval
- **WHEN** two products share the same max facet score but different listing ages
- **THEN** Source B does not reorder them by applying a recency multiplier to `personalScore`
- **AND** listing recency remains a concern of the LTR `recency_score` feature

#### Scenario: Empty profile yields empty Personal
- **WHEN** the caller has empty commerce facet maps
- **THEN** Source B contributes no candidates

### Requirement: Source E CF product pick and request-local cfScore
Source E SHALL expand configurable top-N neighbor entities into ACTIVE in-stock products ordered by completed_order_items count descending then `created_at` descending, SHALL apply a configurable soft per-neighbor product cap (default 20 in config), SHALL dedupe by product keeping the maximum raw score, and SHALL set `cfScore = raw / max(raw among source-E candidates in the request, ε)` where `raw = edge_score(seed→neighbor) × seed_strength` and `seed_strength` is the user's facet score in [0,1], yielding `cfScore` in (0,1] (or 0 when no positive raw).

#### Scenario: cfScore is max-normalized within the request
- **WHEN** Source E emits multiple candidates with positive raw scores
- **THEN** the maximum `cfScore` among those candidates is 1.0
- **AND** other candidates scale proportionally to their raw values

### Requirement: Source C AR product pick and arScore formula
Source C SHALL map social tags through association rules passing the configured confidence threshold into Commerce categories, SHALL retrieve ACTIVE products per category ordered by completed_order_items count descending then `created_at` descending with a configurable soft per-category product cap (default 20 in config), and SHALL set `arScore = clip(confidence × tag_score_norm, 0, 1)` using the max-normalized profile tag score, taking the maximum `arScore` when multiple rules match the same product.

#### Scenario: arScore uses confidence times tag strength
- **WHEN** a rule with confidence 0.5 matches a tag whose normalized profile score is 0.8
- **THEN** the candidate `arScore` is 0.4 before any later max across rules

### Requirement: Retrieval does not perform LTR feature engineering
Candidate retrieval SHALL use only source-native rank keys (e.g. created_at, 90d completed counts, rating, facet max, CF edge scores, AR confidence) and MUST NOT compute or apply `price_affinity`, `recency_score`, `semantic_similarity`, popularity z-scale, or other HOME_FEATURE_ORDER transforms inside the retrieval stage.

#### Scenario: Price affinity not used to admit candidates
- **WHEN** building the candidate pool
- **THEN** admission and source-internal ranking do not depend on `price_affinity`
- **AND** `price_affinity` is computed later in the feature builder

## ADDED Requirements

### Requirement: Fixed Home LTR feature order
The Commerce Home learning-to-rank pipeline SHALL use a fixed feature order shared by offline training and online scoring, including recency, popularity, rating, category_match, brand_match, shop_match, price_affinity, cross_domain_score, cf_score, semantic_similarity, and multi-hot source flags is_popular, is_personal, is_cf, is_cross_domain, is_semantic.

#### Scenario: Training matrix columns
- **WHEN** the Home train job builds matrix X
- **THEN** columns follow the locked Home feature order
- **AND** cf_score and source flags are present even when zero for a row

### Requirement: Binary engage label
Home training labels SHALL be binary: positive if the user clicks or adds to cart or purchases the impressed product within the configured label window (default 24 hours), otherwise negative. Sample-weighting by action type is deferred and MUST NOT be required for v1 train.

#### Scenario: Cart without purchase is positive
- **WHEN** a user adds an impressed product to cart within the label window and never purchases
- **THEN** the training label for that impression is 1

#### Scenario: No engage is negative
- **WHEN** a user receives an impression and performs none of click, cart, or purchase in the label window
- **THEN** the training label is 0

### Requirement: Home ranker artifact distinct from post feed_ranker
Offline export-activate for Commerce Home SHALL register a distinct model name for the Home ranker (e.g. `commerce_home_ranker`), apply a metric gate against a rule baseline on the Home evaluate report, and activate only when the gate passes; soft-reject MUST leave the previous active Home model unchanged.

#### Scenario: Gate reject keeps prior active
- **WHEN** export-activate runs and Home LightGBM metrics fail the gate versus baseline
- **THEN** the new artifact is not activated
- **AND** the previously active Home ranker remains active if one existed

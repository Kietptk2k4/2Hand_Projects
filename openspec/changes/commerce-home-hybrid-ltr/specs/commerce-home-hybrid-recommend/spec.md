## ADDED Requirements

### Requirement: Commerce owns Home recommend serve path
The commerce-service SHALL expose a Commerce Home recommend API that returns up to 50 ranked ACTIVE in-stock products for the caller, SHALL perform ranking with the Commerce Home LightGBM/ONNX model when loaded, and MUST NOT call Social recommend-feed HTTP endpoints on the hot path.

#### Scenario: Authenticated Top 50
- **WHEN** an authenticated buyer requests Commerce Home recommendations
- **THEN** the service returns at most 50 products after candidate generation, feature scoring, and diversity re-ranking
- **AND** each item is ACTIVE with available stock under Commerce inventory rules

#### Scenario: Guest uses non-personalized retrieval
- **WHEN** an unauthenticated caller requests Commerce Home recommendations
- **THEN** candidates are drawn from popular/new/high-rating business retrieval only
- **AND** the response still returns at most 50 products after ranking or degraded sort

#### Scenario: Model missing degrades safely
- **WHEN** the Home ranker ONNX session is unavailable
- **THEN** the service still returns up to 50 products from the candidate pool using a degraded popularity/recency sort
- **AND** records a fallback reason for ops visibility

### Requirement: LightGBM ranks pool and does not generate candidates
The Home recommend pipeline SHALL treat LightGBM/ONNX solely as a scorer over an already-built candidate pool and MUST NOT use the model to invent candidates outside that pool.

#### Scenario: Score then Top K
- **WHEN** a candidate pool of size P (P ≤ 500) is built for a user
- **THEN** each candidate receives a model score (or degraded sort key)
- **AND** diversity re-ranking is applied after scoring
- **AND** the API returns the top 50 after diversity

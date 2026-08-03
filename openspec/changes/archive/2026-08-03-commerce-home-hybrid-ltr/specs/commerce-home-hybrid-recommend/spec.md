## ADDED Requirements

### Requirement: Commerce owns Home recommend HTTP contract
The commerce-service SHALL expose `GET /commerce/api/v1/home/recommendations` for authenticated buyers, SHALL return at most 50 ranked ACTIVE in-stock products, SHALL include `request_id` and `ranking_mode` (`LIGHTGBM` or `DEGRADED`) in the success payload, and MUST NOT call Social recommend-feed HTTP endpoints on the hot path. The API MUST require authentication and MUST NOT serve a guest/anonymous recommend path. When the feature flag `COMMERCE_HOME_RECOMMEND_ENABLED` (or equivalent config) is false, the endpoint SHALL respond with HTTP 404 and a stable feature-disabled error code. When the candidate pool is empty after filters, the endpoint SHALL return HTTP 200 with an empty `items` array.

Each element of `items` SHALL include at least: `id`, `title`, `price`, `thumbnail`, `shop` (minimal identity), and `rating` (summary or null). Specs MUST NOT introduce a separate Home-only price taxonomy; listing price uses Commerce `effective_price` domain resolution.

#### Scenario: Authenticated Top 50 with request metadata
- **WHEN** an authenticated buyer requests Home recommendations and the feature is enabled
- **THEN** the service returns at most 50 products after candidate generation, feature scoring, and diversity re-ranking
- **AND** the response includes `request_id` and `ranking_mode`
- **AND** each item is ACTIVE with available stock under Commerce inventory rules
- **AND** each item includes the minimum product card fields

#### Scenario: Unauthenticated caller is rejected
- **WHEN** an unauthenticated caller requests Commerce Home recommendations
- **THEN** the service rejects the request (e.g. 401)
- **AND** does not return a popular-only guest Top K list

#### Scenario: Feature flag disabled
- **WHEN** the Home recommend feature flag is disabled
- **THEN** the endpoint returns HTTP 404 with a stable feature-disabled code
- **AND** does not return a ranked Top K list

#### Scenario: Empty pool returns empty list
- **WHEN** no eligible candidates remain after retrieval and filters
- **THEN** the response is HTTP 200 with `items` empty
- **AND** is not treated as a server error

### Requirement: HomeModelLoader mirrors Social reload lifecycle
Commerce-service SHALL load the active `commerce_home_ranker` ONNX artifact from a configured model root using a portable basename, SHALL load matching `feature_order` and PopularityNormalizer constants with the artifact, SHALL perform an initial load at startup and periodic reload via configurable schedule (and MAY support force reload), and SHALL use degraded scoring when the session, feature order, or normalizer is unavailable. Soft-reject of a new artifact MUST remain an offline activate concern and MUST NOT force online activation of a failed gate.

#### Scenario: Scheduled reload picks up newly activated model
- **WHEN** a new Home ranker version is activated in the Commerce artifact registry and the reload cron runs
- **THEN** subsequent LightGBM requests may use the new session after a successful load

#### Scenario: Load failure stays degraded
- **WHEN** the active artifact file is missing or fails to load
- **THEN** Home recommend continues with `ranking_mode = DEGRADED`
- **AND** records a fallback reason

### Requirement: LightGBM ranks pool and does not generate candidates
The Home recommend pipeline SHALL treat LightGBM/ONNX solely as a scorer over an already-built candidate pool and MUST NOT use the model to invent candidates outside that pool. After scoring, candidates SHALL be sorted by model score descending with tie-breakers `created_at` descending then `product_id` ascending (same deterministic ties as degraded), then diversity.

#### Scenario: Score then Top K
- **WHEN** a candidate pool of size P (P ≤ 500) is built for a user
- **THEN** each candidate receives a model score (or degraded sort key)
- **AND** equal scores break ties by newer `created_at` then smaller `product_id`
- **AND** diversity re-ranking is applied after scoring
- **AND** the API returns the top 50 after diversity

### Requirement: Degraded sort uses fixed popularity-recency composite
When the Home ONNX session is unavailable or PopularityNormalizer constants required for LightGBM scoring are missing, commerce-service SHALL score the existing candidate pool with `degraded_key = 0.7 * popularity_score + 0.3 * recency_score`, sort descending by that key with tie-breakers `created_at` descending then `product_id` ascending, apply the same diversity step as the LightGBM path, set `ranking_mode = DEGRADED`, and MUST NOT introduce a separate full rule-based ranker.

#### Scenario: Model missing uses composite key
- **WHEN** the Home ranker ONNX session is unavailable for an authenticated request
- **THEN** candidates are ordered by the 0.7/0.3 degraded key before diversity
- **AND** the response `ranking_mode` is `DEGRADED`
- **AND** a fallback reason is recorded for ops visibility

### Requirement: Point-in-time UserInterestProfile with locked facet math
Before retrieval, commerce-service SHALL build a `UserInterestProfile` at `as_of` with separate commerce facets (category, brand, shop) and social facets (hashtag, keyword), MUST NOT collapse social tags into category scores, and MUST use only commerce events strictly before serve `as_of`. Commerce facets SHALL use a 180-day window with COMPLETED lines weighted 1.0 and cart interactions weighted 0.6, each multiplied by decay \(2^{-\Delta/30d}\), then max-normalized per facet keeping top 20 categories, 20 brands, and 10 shops. Cart MVP SHALL prefer a cart-add event table when present, otherwise live `cart_items.created_at` in the window (excluding soft-deleted cart rows). Social facets SHALL be loaded from the Commerce `user_social_interest_export` consumer (decayed weighted scores produced offline) and max-normalized within tag maps online — not rebuilt from Social HTTP on the hot path. Price percentiles SHALL be empirical p25/p50/p75 of COMPLETED line `effective_price` in the 180-day window; if fewer than 3 samples, price stats are missing.

#### Scenario: Authenticated profile uses 180d commerce window and decay
- **WHEN** an authenticated Home recommend runs at time T
- **THEN** commerce facet scores include COMPLETED lines (weight 1.0) and cart interactions in [T−180d, T) (weight 0.6) with \(2^{-\Delta/30d}\) decay
- **AND** events at or after T are excluded
- **AND** at most 20 categories, 20 brands, and 10 shops remain after max-norm

#### Scenario: Sparse price history leaves percentiles missing
- **WHEN** the user has fewer than 3 COMPLETED priced lines in the 180d window
- **THEN** profile price percentiles are treated as missing
- **AND** downstream `price_affinity` uses its missing-stats default

#### Scenario: Social facets come from export consumer
- **WHEN** social interest export rows exist for the user
- **THEN** hashtag/keyword maps are built by max-norming those export scores
- **AND** those tags remain in social facet maps until association-rule projection at retrieval
- **AND** Social recommend-feed HTTP is not called

### Requirement: Configurable greedy hard-cap diversity after scoring
After model or degraded scoring, commerce-service SHALL re-rank the scored candidate list with a configurable greedy hard-cap on leaf category (and optionally brand and shop), SHALL apply configured default cap values when unset, SHALL backfill by descending score if fewer than K items are selected under caps, and MUST NOT modify the pre-score candidate pool size. Numeric cap values MUST be configuration parameters (not fixed constants in the normative requirement). Category capping SHALL be available in v1; brand and shop caps MAY be disabled via config (e.g. max 0).

#### Scenario: Category cap limits mono-category feed
- **WHEN** diversity is enabled and `max_per_category` is set to N
- **AND** more than N of the highest-scoring products share one leaf category
- **THEN** at most N products from that category are admitted before backfill
- **AND** lower-scoring products from other categories may appear in the Top K result

#### Scenario: Backfill when caps undershoot K
- **WHEN** constrained selection yields fewer than K products but more scored candidates remain
- **THEN** the service backfills by score without applying caps until K is reached or candidates are exhausted

#### Scenario: Diversity can be disabled
- **WHEN** diversity is disabled via configuration
- **THEN** the Top K list is the top K by score alone
- **AND** the candidate pool is unchanged

### Requirement: Async served impression log with ranking_mode and provenance
After a successful authenticated Home recommend response is prepared, commerce-service SHALL asynchronously persist one impression row per returned product for the caller. Each row MUST include: `user_id`, `product_id`, `shown_at`, `rank_position` (after diversity), `request_id`, `ranking_mode` (`LIGHTGBM` or `DEGRADED`), and **minimal feature provenance** `sources`, `personal_score`, `cf_score`, `ar_score` (nullable scores allowed). Column `sources` SHALL be a **JSONB array of strings** from the closed set `POPULAR`, `PERSONAL`, `CF`, `CROSS_DOMAIN` (e.g. `["PERSONAL","CF"]`). Impression logging MUST NOT block or fail the recommend HTTP response. The system MUST NOT maintain a suppress-recently-shown product set for Home; the same product MAY be recommended and impressed on multiple requests. Only the Top K returned items SHALL be logged (not the full candidate pool). The system MUST NOT require persisting the full 15-dim feature vector on the impression row. Required indexes SHALL cover `(user_id, shown_at)`, `(user_id, product_id, shown_at)`, and `(request_id)`.

#### Scenario: Impression rows for Top K
- **WHEN** an authenticated Home recommend returns N products (N ≤ K) with a request id
- **THEN** N impression rows are written asynchronously for that user
- **AND** each row shares the same `request_id`
- **AND** each row has a `rank_position` in 1..N after diversity
- **AND** each row records `ranking_mode`
- **AND** each row records retrieval `sources` and applicable personal/cf/ar scores from the served CandidateProduct

#### Scenario: Degraded mode recorded on impressions
- **WHEN** the recommend path uses degraded sort because the ONNX model is unavailable
- **THEN** impression rows for that response have `ranking_mode = DEGRADED`

#### Scenario: Log failure does not fail recommend
- **WHEN** persisting impressions fails after the Top K list is ready
- **THEN** the recommend API still returns success with the product list
- **AND** the failure is recorded for ops visibility

#### Scenario: Repeated recommend re-impresses
- **WHEN** the same authenticated user requests Home recommend twice and the same product appears in both Top K lists
- **THEN** two separate impression rows are written for that user and product
- **AND** neither request is blocked by a seen-product suppress list

### Requirement: Home click from attributed product detail
Commerce-service SHALL treat an authenticated buyer product-detail view as a Home click engage when the request is attributed from Home (e.g. `from=home`), SHALL persist the click asynchronously without failing the detail response on log errors, and MUST NOT count product-detail views without Home attribution as Home clicks. A missing `request_id` with valid Home attribution SHALL still record the click.

#### Scenario: Attributed detail is a click
- **WHEN** an authenticated buyer opens product detail with Home attribution for a product
- **THEN** a CLICK engage event is recorded for that user and product
- **AND** the detail response still succeeds if click logging fails

#### Scenario: Unattributed detail is not a Home click
- **WHEN** an authenticated buyer opens product detail without Home attribution
- **THEN** no Home CLICK engage is recorded for that view

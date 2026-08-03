## ADDED Requirements

### Requirement: Fixed Home LTR feature order shared by train and serve
The Commerce Home learning-to-rank pipeline SHALL use the locked `HOME_FEATURE_ORDER` constant identical in offline Python training and online Java scoring, in this exact order: recency_score, popularity_score, rating_score, category_match, brand_match, shop_match, price_affinity, cross_domain_score, cf_score, semantic_similarity, is_popular, is_personal, is_cf, is_cross_domain, is_semantic. Export SHALL persist the order (e.g. feature_order.json); serve MUST reject load when order or dimension mismatches.

#### Scenario: Training matrix columns
- **WHEN** the Home train job builds matrix X
- **THEN** columns follow `HOME_FEATURE_ORDER` exactly
- **AND** cf_score and source flags are present even when zero for a row

#### Scenario: Serve asserts same order
- **WHEN** commerce-service loads the Home ranker artifact
- **THEN** it verifies the persisted feature order matches the Java `HOME_FEATURE_ORDER` constant
- **AND** fails closed (degraded serve) if the order or length differs

### Requirement: Feature Builder inputs and PopularityNormalizer
The Home feature builder SHALL compute the 15-dimensional vector from `CandidateProduct`, `UserInterestProfile` at `as_of`, and a `PopularityNormalizer` loaded with the active Home ranker artifact (sidecar/`(z_lo,z_hi)`). The builder MUST NOT depend on an unbound train_meta concept outside the model load path, MUST NOT re-implement CF or AR scoring algorithms, and MUST NOT apply CF/AR-specific transforms such as `x/(1+x)`. Online serve uses `as_of = now`; offline training rows use `as_of = shown_at` with events strictly before `as_of`.

#### Scenario: Normalizer missing with LightGBM path
- **WHEN** the serve path would use LightGBM but PopularityNormalizer constants are unavailable
- **THEN** the service treats the model as unloadable and uses degraded serve

#### Scenario: Builder does not know CF internals
- **WHEN** building `cf_score` for a candidate
- **THEN** the builder uses only `clip(candidate.cfScore, 0, 1)` (null/absent → 0)
- **AND** does not inspect co-occurrence tables for that row when metadata is present

### Requirement: Normative formulas for all HOME_FEATURE_ORDER dimensions
The feature builder SHALL compute each dimension as follows (`clip(x,0,1) = max(0, min(1, x))`). Continuous features are in [0,1]; source flags are 0 or 1.

1. **recency_score:** If `product.created_at` is missing → 0. Else \(\Delta = \max(0, t - created\_at)\) in seconds with `t` the point-in-time clock, and \(2^{-\Delta / (7 \cdot 86400)}\) (half-life 7 days).
2. **popularity_score:** `raw` = COUNT of completed `order_items` for the product with `completed_at < as_of`; `z = log1p(raw)`; then `PopularityNormalizer.normalize(z)` using artifact `(z_lo, z_hi)`. MUST NOT min-max within the per-request candidate pool.
3. **rating_score:** If `rating_count < 3` → 0.5; else `clip(rating_avg / 5, 0, 1)`.
4. **category_match:** `profile.category_scores.get(product.category_id, 0)`.
5. **brand_match:** If brand is null → 0; else `profile.brand_scores.get(product.brand_id, 0)`.
6. **shop_match:** `profile.shop_scores.get(product.shop_id, 0)`.
7. **price_affinity:** Let `price` be the product `effective_price`, and `p25`/`p75` from the profile (COMPLETED-derived). If price stats are missing or `IQR = p75 - p25 ≤ 0` → 0.5. If `price ∈ [p25, p75]` → 1.0. If `price < p25` → `clip(1 - (p25 - price)/IQR, 0, 1)`. If `price > p75` → `clip(1 - (price - p75)/IQR, 0, 1)`.
8. **cross_domain_score:** `clip(candidate.arScore ?? 0, 0, 1)`.
9. **cf_score:** `clip(candidate.cfScore ?? 0, 0, 1)`.
10. **semantic_similarity:** 0 while semantic retrieval (D) is disabled.
11. **is_popular:** 1 iff Popular source ∈ candidate sources, else 0.
12. **is_personal:** 1 iff Personal source ∈ candidate sources, else 0.
13. **is_cf:** 1 iff CF source ∈ candidate sources, else 0.
14. **is_cross_domain:** 1 iff Cross-domain source ∈ candidate sources, else 0.
15. **is_semantic:** 0 while semantic retrieval is disabled.

#### Scenario: Missing created_at yields zero recency
- **WHEN** a product snapshot has no created_at
- **THEN** `recency_score` is 0

#### Scenario: Price inside IQR band
- **WHEN** profile price percentiles exist with positive IQR
- **AND** effective_price lies in [p25, p75]
- **THEN** `price_affinity` is 1.0

#### Scenario: Price more than one IQR outside band
- **WHEN** effective_price is below `p25 - IQR` (or above `p75 + IQR`)
- **THEN** `price_affinity` is 0

#### Scenario: Sparse ratings default
- **WHEN** `rating_count` is less than 3
- **THEN** `rating_score` is 0.5

#### Scenario: Semantic dims idle under D0
- **WHEN** Home recommend runs without a semantic retrieval source
- **THEN** `semantic_similarity` is 0
- **AND** `is_semantic` is 0

### Requirement: popularity_score from completed_order_items with fixed normalization
The feature `popularity_score` SHALL be derived from `raw = COUNT` of `order_items` with `status = COMPLETED` for that `product_id` and `completed_at < as_of`, transformed as `log1p(raw)`, then scaled to [0,1] via `PopularityNormalizer` using train-fit constants `(z_lo, z_hi)` persisted with the Home model artifact. The system MUST NOT min-max-normalize `popularity_score` within the per-request candidate pool of ~500.

#### Scenario: Raw definition
- **WHEN** popularity is computed for a product at as_of
- **THEN** raw equals the number of completed order_item rows for that product before as_of
- **AND** does not use order header counts or sold-quantity sums as the primary raw

#### Scenario: Stable across requests
- **WHEN** the same product is scored in two different Home recommend requests with the same as_of and model version
- **THEN** `popularity_score` is identical
- **AND** does not depend on which other candidates appear in the pool

### Requirement: Binary engage label with nearest-impression attribution
Home training labels SHALL be binary: positive if the user clicks or adds to cart or purchases the impressed product within the configured label window (default 24 hours) under nearest-impression attribution, otherwise negative. Sample-weighting by action type is deferred and MUST NOT be required for v1 train. When the same user has multiple impressions of the same product, each engage event at time t SHALL be attributed only to the nearest prior impression — the impression with the greatest `shown_at` such that `shown_at ≤ t` and `t` is still inside that impression's label window. Older impressions of the same product MUST NOT become positive solely because of that engage.

#### Scenario: Cart without purchase is positive
- **WHEN** a user adds an impressed product to cart within the label window of the nearest prior impression and never purchases
- **THEN** the training label for that nearest impression is 1

#### Scenario: No engage is negative
- **WHEN** a user receives an impression and performs none of click, cart, or purchase attributable to that impression in the label window
- **THEN** the training label is 0

#### Scenario: Repeated impressions use nearest only
- **WHEN** a user has impression I1 then later I2 for the same product
- **AND** an engage occurs after I2 within I2's label window
- **THEN** I2 is labeled positive
- **AND** I1 is not labeled positive from that engage

### Requirement: Home dataset unit, point-in-time, and time split
Each training row SHALL correspond to one Home impression (from `home_impression_log` and/or Home sim impression files per train-data mode). Feature vectors SHALL use `as_of = shown_at` with profile and popularity raw events strictly before `shown_at`, using the same normative formulas as serve. Dataset split SHALL be time-ordered by `shown_at` into **80% train / 10% validation / 10% test** with no random shuffle. `PopularityNormalizer` `(z_lo, z_hi)` SHALL be fit on the **train** partition only (prefer p1–p99 of `log1p(raw)` on train) and persisted with the artifact. Corpus selection SHALL follow Admin `commerce.home.ltr.train_data_mode` (`SEED_ONLY` | `HYBRID` | `REAL_ONLY`) as specified in `commerce-home-train-data-mode`. Production jobs MUST NOT invent seed labels when mode is `REAL_ONLY`.

#### Scenario: Time split is chronological
- **WHEN** the Home build-dataset job splits rows
- **THEN** train rows have shown_at ≤ all val shown_at ≤ all test shown_at (within the contiguous time-order split)
- **AND** rows are not randomly shuffled before split

#### Scenario: Normalizer fit excludes val/test
- **WHEN** PopularityNormalizer constants are computed
- **THEN** they are derived from train-partition popularity raw only

#### Scenario: REAL_ONLY refuses silent seed fallback
- **WHEN** mode is `REAL_ONLY` and real impression count is below the configured minimum
- **THEN** the job fails closed or skips train rather than inventing unlabeled or seed positives

### Requirement: Home train objective and evaluate baseline
Home v1 train SHALL use LightGBM **binary classification** on labels y∈{0,1} (ranker objective optional later). Hyperparameters SHALL live in a checked-in Home train config (may start from Post defaults). Evaluate SHALL report metrics for both LightGBM and a **baseline ranker** whose score key is `0.7 * popularity_score + 0.3 * recency_score` (identical to online degraded serve). Required metrics SHALL include **AUC** and **Precision@10**.

**Precision@10 (locked, mirror Social Post):** group test rows by **`request_id`**. For each group, rank items by model (or baseline) score descending, compute precision among the top `min(10, group_size)` positions, then **Precision@10 = mean of per-request precisions**. AUC SHALL be computed globally on test rows (scores vs binary labels). Evaluate MUST NOT invent an alternate group key (e.g. user_id-only) as the normative P@10 definition.

#### Scenario: Baseline key matches degraded serve
- **WHEN** evaluate scores the baseline on a Home feature row
- **THEN** baseline score equals 0.7 × popularity_score + 0.3 × recency_score

#### Scenario: Precision@10 groups by request_id
- **WHEN** evaluate computes precision_at_10 on the Home test set
- **THEN** rows are grouped by `request_id`
- **AND** precision_at_10 equals the mean of per-group precisions at k=10 (or group size if smaller)

### Requirement: Offline features use serve-logged provenance
Home build-dataset SHALL reconstruct `CandidateProduct` `sources`, `personalScore`, `cfScore`, and `arScore` from columns persisted on each impression at serve time, then apply Feature Builder formulas with `UserInterestProfile(as_of=shown_at)` and product attributes. The job MUST NOT replay full retrieval A/B/E/C to recover those fields, and MUST NOT require a logged full 15-dim feature vector on the impression row.

#### Scenario: Provenance drives is_cf and cf_score
- **WHEN** an impression row records CF in `sources` and a non-null `cf_score`
- **THEN** the training vector’s `is_cf` is 1 and `cf_score` equals clip of the logged value

### Requirement: Retrain orchestration order
Home train and retrain SHALL execute this ordered orchestration: resolve train_data_mode → (optional Home sim) → entity CF → social interest export → AR mine → load-artifact when needed → build-dataset → split → train → evaluate → export-activate. Skipping the order or treating retrain as “train-only without CF/AR refresh” is non-compliant unless a documented ops exception applies; v1 default is full order. Trigger is operator/CLI (cron optional later). Soft-reject MUST NOT activate.

#### Scenario: Retrain runs CF before dataset
- **WHEN** a Home retrain orchestration starts
- **THEN** entity CF and social export and AR complete before build-dataset
- **AND** export-activate runs only after evaluate

### Requirement: Home ranker artifact distinct from post feed_ranker
Offline export-activate for Commerce Home SHALL register a distinct model name for the Home ranker (e.g. `commerce_home_ranker`) in a Commerce `model_artifacts` table with required columns `id`, `model_name`, `version`, `format`, `artifact_path`, `metrics`, `is_active`, `trained_at`, unique `(model_name, version)`, and at most one active row per `model_name`. The gate SHALL pass only if `lightgbm.auc >= baseline.auc` AND `lightgbm.precision_at_10 >= baseline.precision_at_10`; a null required metric SHALL fail closed. On pass, export SHALL write ONNX + `feature_order.json` + PopularityNormalizer `(z_lo, z_hi)` under Commerce `MODEL_ROOT` as a portable basename and activate the Home model. Soft-reject MUST leave the previous active Home model unchanged.

#### Scenario: Gate reject keeps prior active
- **WHEN** export-activate runs and Home LightGBM metrics fail the gate versus baseline
- **THEN** the new artifact is not activated
- **AND** the previously active Home ranker remains active if one existed

#### Scenario: Gate requires AUC and Precision@10
- **WHEN** evaluate reports lightgbm.auc and precision_at_10 both ≥ baseline counterparts
- **THEN** export-activate may activate the new Home artifact
- **AND** when either metric is null or below baseline, activation MUST NOT occur

### Requirement: Python and Java Feature Builder parity tests
The change SHALL include automated tests proving Python and Java Feature Builders produce the same length-15 vector in `HOME_FEATURE_ORDER` for identical fixture inputs at the same point-in-time `as_of`, and SHALL include a provenance round-trip fixture (logged scores/sources → offline reconstruct → same vector as serve Feature Builder).

#### Scenario: Same input same vector
- **WHEN** the parity fixture runs in Python and Java
- **THEN** both emit identical 15 floats in order
- **AND** dimension count is 15

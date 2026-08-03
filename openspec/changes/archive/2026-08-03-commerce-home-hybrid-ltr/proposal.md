## Why

Commerce Home today is essentially catalog browse (sort/filter), not a personalized recommend feed. Social already has hybrid retrieval + LightGBM LTR for posts; Commerce has purchase history and inventory but no owned Home recommend path. Second-hand stock=1 makes classic item–item CF on `product_id` die quickly, so the platform needs an entity-stable hybrid (rules + entity CF + cross-domain AR + LightGBM ranking + diversity) served from Commerce.

## What Changes

- Add **Commerce Home hybrid recommend** owned by **commerce-service**: build user interest profile, multi-source candidate pool (≤500), feature vector per `(user, product)`, **LightGBM ranking** (not rule ranking), diversity re-rank, return **Top 50**.
- **Auth-only**: JWT required; no guest/anonymous Home recommend path. Retrieval = **A** Popular/new/rating ∪ **B** Personal ∪ **E** Entity CF ∪ **C** Cross-domain AR (no semantic fill in v1), inventory filter + own-shop exclude + dedupe into **`CandidateProduct`** (product snapshot + `personalScore`/`cfScore`/`arScore` ∈[0,1]; no shared `retrievalScore`). Online locks include degraded `0.7*pop+0.3*rec`, E/C unit-score maps + ≤20 products per neighbor/category, and `GET /commerce/api/v1/home/recommendations` (flag→404, empty→`[]`).
- **Entity-based CF** (not product_id→product_id): offline 180d co-occur (`entity_cooccur`); COMPLETED pairs ×1.0 + cart 24h pairs ×0.6; `score=log1p`; top-M=50; types leaf category/brand (shop deferred); online Top-N neighbors **configurable**.
- **Cross-domain**: offline AR `interest_tag → category` (min_support 0.01 / min_confidence 0.05) → `social_tag_category_ar`; daily social export → `user_social_interest_export`; online gate `confidence ≥ configured threshold` (default 0.05).
- **Semantic / ANN**: deferred (D0); pool may be &lt;500; `semantic_*` features stay 0.
- **Impression / engage**: async served Top-K impression log with `ranking_mode`; click v1 = attributed product-detail (`from=home`); no suppress-recently-shown; train labels use **nearest prior impression** attribution for click∪cart∪buy.
- **LTR labels**: binary engage = click ∪ add-to-cart ∪ purchase; sample weights deferred (same direction as Post).
- **Features**: locked `HOME_FEATURE_ORDER` (15 dims) with **normative formulas** in `commerce-home-ltr`; `PopularityNormalizer` from model artifact; `cfScore`/`arScore` ∈ [0,1] on `CandidateProduct` (FE only clips); `UserInterestProfile` facets with cart **add events over 180d** (×0.6) and COMPLETED (×1.0).
- **Rule-based**: baseline metrics in offline evaluate reports + inventory/business retrieval only — **not** the production Top-K ranker.
- Wire FE/mobile Commerce Home “Đề xuất” (or equivalent rail) to the new API when ready.
- **Out of scope (this change):** Post-feed retrain orchestrator/drift; ALS/BPR matrix factorization; Neo4j; Stage-1 rule ranker for Home; viewport impression beacons; guest Home rail; semantic ANN retrieval; Post-style shared-DB `seed-db` for Home; dedicated Docker sim DB for Home v1.
- **Train data modes (Batch 4):** Admin `system_configs` key `commerce.home.ltr.train_data_mode` ∈ {`SEED_ONLY`, `HYBRID`, `REAL_ONLY`} drives cold-start file/RAM sim vs hybrid vs real-only corpora for train/retrain; Home sim catalog is in-memory from personas; sim CF/AR/export land as files then **load-artifact** into Commerce tables — without inserting fake users/posts/orders into app DBs.

## Capabilities

### New Capabilities
- `commerce-home-hybrid-recommend`: Online Commerce Home recommend pipeline (auth-only profile → retrieval → pool → features → LightGBM → diversity → Top 50), impression/click logging, degraded serve when model missing.
- `commerce-home-candidate-retrieval`: Multi-source soft-quota candidate generation (A/B/E/C; D deferred) returning `CandidateProduct` with inventory hard filters, own-shop exclude, and multi-source dedupe flags — no shared `retrievalScore`.
- `commerce-entity-cf`: Offline entity co-occurrence graph/table and online neighbor lookup for second-hand-stable CF seeds (completed + cart weights).
- `commerce-social-ar-bridge`: Offline association rules + `user_social_interest_export` consumer; Commerce consumption without Social feed HTTP; cross-domain retrieval and `cross_domain_score`.
- `commerce-home-ltr`: Home LTR formulas; provenance-based dataset; P@10 by `request_id`; retrain orchestration; `model_artifacts` columns; Python↔Java parity tests; gate AUC+P@10; soft-reject; `commerce_home_ranker`.
- `commerce-home-train-data-mode`: Admin `SEED_ONLY`|`HYBRID`|`REAL_ONLY`; file/RAM Home sim; load-artifact.

### Modified Capabilities
- `recsys-offline-ops`: Extend offline ops with Home sim files, mode-aware CF/AR/export/LTR jobs, load-artifact, and Admin config resolution — still never called on online recommend hot path.

## Impact

- **commerce-service**: new recommend use case(s), candidate builders, feature builder, ONNX/model loader (or shared pattern with Social), diversity step, HTTP API under commerce `/api/v1/...`, Home impression log + attributed product-detail click engage.
- **recsys-offline**: Home sim (file/RAM), mode-aware CF/AR/export/LTR, load-artifact; read Admin `commerce.home.ltr.train_data_mode` at job start.
- **admin-service**: seed/maintain `system_configs` entries for Home train mode (+ optional seed_row_weight); no new DB ownership of training rows.
- **social-service**: read-only export inputs for REAL/HYBRID — no ownership of Home rank; no Home sim writes into Social DB.
- **Frontend / mobile**: Commerce Home rail/grid consumes Top 50 recommend API; Admin system-configs tab edits train mode enum.
- **Docs**: API FE behavior + architecture note for Hybrid Commerce Home strategy naming; D19 inventory of tables/collections.

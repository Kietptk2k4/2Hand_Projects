## Why

Commerce Home today is essentially catalog browse (sort/filter), not a personalized recommend feed. Social already has hybrid retrieval + LightGBM LTR for posts; Commerce has purchase history and inventory but no owned Home recommend path. Second-hand stock=1 makes classic item–item CF on `product_id` die quickly, so the platform needs an entity-stable hybrid (rules + entity CF + cross-domain AR + content fallback + LightGBM ranking + diversity) served from Commerce.

## What Changes

- Add **Commerce Home hybrid recommend** owned by **commerce-service**: build user interest profile, multi-source candidate pool (~500), feature vector per `(user, product)`, **LightGBM ranking** (not rule ranking), diversity re-rank, return **Top 50**.
- **Guest**: popular / new / high-rating retrieval only (business rules). **Authed**: Personal history ∪ Entity-based CF ∪ Association Rule (Social→Commerce) ∪ Content similarity **fill-remaining**, then inventory filter + dedupe.
- **Entity-based CF** (not product_id→product_id): co-occurrence on stable entities (category / brand / optional shop); seeds from Completed×1.0 + Cart×0.6.
- **Cross-domain**: offline AR `interest_tag → category` from weighted social signals (search×4, save×3, comment×2, like×1); Commerce reads Social signals via **export or internal API** (no Social HTTP on every Home request if avoidable).
- **Content similarity**: fallback / fill only when other sources underfill — not full-catalog embedding per request.
- **LTR labels**: binary engage = click ∪ add-to-cart ∪ purchase; sample weights deferred (same direction as Post).
- **Rule-based**: baseline metrics in offline evaluate reports + inventory/business retrieval only — **not** the production Top-K ranker.
- Wire FE/mobile Commerce Home “Đề xuất” (or equivalent rail) to the new API when ready.
- **Out of scope (this change):** Post-feed retrain orchestrator/drift; ALS/BPR matrix factorization; Neo4j; Stage-1 rule ranker for Home.

## Capabilities

### New Capabilities
- `commerce-home-hybrid-recommend`: Online Commerce Home recommend pipeline (profile → retrieval → pool → features → LightGBM → diversity → Top 50), guest vs authed behavior, degraded serve when model missing.
- `commerce-home-candidate-retrieval`: Multi-source soft-quota candidate generation (Popular, Personal, Entity CF, Cross-domain AR, Semantic fill) with inventory hard filters and multi-source dedupe flags.
- `commerce-entity-cf`: Offline entity co-occurrence graph/table and online neighbor lookup for second-hand-stable CF seeds (completed + cart weights).
- `commerce-social-ar-bridge`: Offline association rules from Social interest tags to Commerce categories; Commerce consumption of Social interest exports/internal reads for cross-domain retrieval and `cross_domain_score`.
- `commerce-home-ltr`: Home product LTR feature contract, binary engage labels, offline train/evaluate/export-activate pattern for a Commerce Home ranker artifact (parallel to Social `feed_ranker`).

### Modified Capabilities
- `recsys-offline-ops`: Extend offline ops surface with jobs (or CLI) to build entity co-occur, AR mappings, and Home LTR dataset/train/export for the Commerce Home model — still never called on online recommend hot path.

## Impact

- **commerce-service**: new recommend use case(s), candidate builders, feature builder, ONNX/model loader (or shared pattern with Social), diversity step, HTTP API under commerce `/api/v1/...`, impression/event logging hooks for Home.
- **recsys-offline**: pipelines/jobs for entity CF, AR bridge, Home dataset/train/export; optional reuse of LightGBM→ONNX activate pattern with a distinct `model_name` (e.g. `commerce_home_ranker`).
- **social-service**: read-only export or internal contract for interest signals (search/save/comment/like aggregates) — no ownership of Home rank.
- **Frontend / mobile**: Commerce Home rail/grid consumes Top 50 recommend API.
- **Docs**: API FE behavior + architecture note for Hybrid Commerce Home strategy naming.
- **Dependencies**: Social DB/Mongo reads offline; Commerce Postgres for orders/cart/products; model artifact storage analogous to Social `model_artifacts` or Commerce-owned registry (design decision).

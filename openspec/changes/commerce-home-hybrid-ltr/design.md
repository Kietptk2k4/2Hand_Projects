## Context

Social Post recommend already uses candidate pool + LightGBM ONNX + rule baseline. Commerce Home is still list/sort. Cross-domain today flows Commerce→Social (`user_purchase_profile` → post features). This change builds the reverse path for **Commerce Home Top 50**, owned by **commerce-service**.

Second-hand constraint: listings often have `stock = 1` and leave ACTIVE after sale, so product_id→product_id CF collapses. Entity-stable co-occurrence (category/brand/shop) is required.

Locked product decisions:
- Top K = **50**
- Serving owner = **commerce-service** (Social signals via export / internal read, not Social-owned Home)
- Label = **binary engage** (click ∪ cart ∪ buy); sample weights later
- Ranking = **LightGBM only** (no Stage-1 rule ranker for Top K)
- Rules = inventory/business retrieval + offline baseline metrics only

## Goals / Non-Goals

**Goals:**

- End-to-end Hybrid Commerce Home: profile → multi-source retrieval → ~500 pool → features → LightGBM → diversity → Top 50.
- Entity-based CF with Completed×1.0 + Cart×0.6 seeds.
- Offline AR Social interest_tag → Commerce category; content similarity only as fill/fallback.
- Offline train/evaluate/export-activate for `commerce_home_ranker` (or equivalent model_name).
- Guest-safe popular path; authed personalized path; degraded serve if ONNX missing (popular/recency on same pool).

**Non-Goals:**

- Post retrain orchestrator / online drift auto-retrain.
- ALS/BPR/neural CF; Neo4j.
- Full-catalog embedding scan per request.
- Rule-based production Top-K ranking.
- Sample-weight training in v1 (document hook only).

## Decisions

### D1 — Commerce owns serving

- **Choice:** `RecommendCommerceHomeUseCase` (name TBD) in commerce-service; FE calls Commerce API.
- **Why:** Products, inventory, cart/orders live in Commerce; avoids Social becoming a catalog proxy.
- **Alternatives:** Social ranks product IDs — rejected (wrong ownership, inventory lag).

### D2 — Online pipeline vs offline artifacts

```
OFFLINE                              ONLINE (Commerce)
entity_cooccur table                 Build UserInterestProfile
AR interest_tag→category             Soft-quota retrieval A/B/E/C + D fill
Home dataset + LightGBM→ONNX         Inventory filter + dedupe → ≤500
                                     Feature build → ONNX score → diversity → 50
```

- Social interest aggregates: prefer **batch export** consumed by Commerce (Redis/DB table) over per-request Social HTTP; allow internal API for bootstrap.

### D3 — UserInterestProfile = unified scores + provenance

- Namespaced interests: `category:`, `brand:`, `hashtag:`, `keyword:`.
- Social weight: `4×search + 3×save + 2×comment + 1×like` (optional time decay later).
- Commerce facets from orders/carts for personal retrieval and match features.

### D4 — Soft quotas (not hard equal slices)

| Source | Cap |
|--------|-----|
| A Popular / new / rating | up to 100 |
| B Personal history | up to 150 |
| E Entity CF | up to 150 |
| C Cross-domain AR | up to 150 |
| D Semantic | fill remaining to 500 |

- Dedupe by `product_id` but **OR multi-hot source flags** (`is_popular`, `is_personal`, `is_cf`, `is_cross_domain`, `is_semantic`).

### D5 — Entity-based CF (not item–item SKU)

- Co-occur on `leaf_category` and `brand` (shop optional).
- Edge weight sums session/order pairs with completed=1.0, cart=0.6 over a rolling window (e.g. 180d).
- Online: seed entities from user → top neighbor entities → ACTIVE in-stock products in those entities.
- Taxonomy walk (parent/sibling) deferred; document as optional L2.

### D6 — Feature contract (fixed order for ONNX)

`recency_score`, `popularity_score`, `rating_score`, `category_match`, `brand_match`, `shop_match`, `price_affinity`, `cross_domain_score`, `cf_score`, `semantic_similarity`, `is_popular`, `is_personal`, `is_cf`, `is_cross_domain`, `is_semantic` (booleans as 0/1).

### D7 — Labels

- Positive if any of click / add-cart / purchase in label window after impression (window aligned with Post-style 24h unless Home analytics dictate otherwise — default **24h**).
- Binary y∈{0,1}; sample weights out of v1.

### D8 — Model registry

- Distinct `model_name = commerce_home_ranker`; store ONNX under Commerce `MODEL_ROOT` (or shared mount) with portable basename; mirror Social activate/soft-reject gate vs rule baseline on Home evaluate report.

### D9 — Diversity after LTR

- Post-score re-rank: enforce category diversity on Top 50 (e.g. cap per leaf category / ensure ≥N categories in top window). Does not shrink the 500 pool.

### D10 — Degraded serve

- If model unload/fail: sort pool by popularity/recency composite; log fallback reason — do not invent a second full rule-ranker.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Cold Social AR / empty CF for new users | Soft quotas + Popular + Personal; Semantic fill |
| Stock=1 entity still sparse | Prefer category/brand over SKU; widen window; fallback Popular |
| Cross-service stale interest export | Batch refresh cadence + as-of timestamp on profile |
| Train without Home impressions | Bootstrap from cart/purchase as engage proxy + sim seed; add impression log ASAP |
| Latency building 500 features | Batch DB reads; cache popular set; CF/AR lookups O(neighbors) |
| Feature order drift Java↔Python | Shared FEATURE_ORDER constant / checked in export smoke |

## Migration Plan

1. Schema/tables: entity co-occur, AR mapping, Home impression log (Commerce), model artifacts for home ranker.
2. Offline jobs: build CF + AR → train stub dataset → export-activate when metrics exist.
3. Commerce online path behind flag `COMMERCE_HOME_RECOMMEND_ENABLED`.
4. FE rail “Đề xuất” → new API; keep existing product list for browse/sort.
5. Rollback: flag off → previous Home list API.

## Open Questions

- Exact Home click event source (FE beacon vs product-detail open) — default: log impression on recommend response + engage from cart/order + optional click beacon.
- Shop entity in CF v1: include or defer — default **defer shop edges**, keep shop_match feature from personal history only.
- Whether Commerce shares Social Postgres `model_artifacts` table vs own table — default **Commerce-owned** `commerce_model_artifacts` (or namespaced rows) to avoid cross-DB writes from offline job into Social only.

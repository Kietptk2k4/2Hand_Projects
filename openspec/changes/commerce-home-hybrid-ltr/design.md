## Context

Social Post recommend already uses candidate pool + LightGBM ONNX + rule baseline. Commerce Home is still list/sort. Cross-domain today flows Commerce→Social (`user_purchase_profile` → post features). This change builds the reverse path for **Commerce Home Top 50**, owned by **commerce-service**.

Second-hand constraint: listings often have `stock = 1` and leave ACTIVE after sale, so product_id→product_id CF collapses. Entity-stable co-occurrence (category/brand/shop) is required.

Locked product decisions:
- Top K = **50**
- Serving owner = **commerce-service** (Social signals via export / internal read, not Social-owned Home)
- Label = **binary engage** (click ∪ cart ∪ buy); sample weights later
- Ranking = **LightGBM only** (no Stage-1 rule ranker for Top K)
- Rules = inventory/business retrieval + offline baseline metrics only
- **popularity_score** raw = **completed_order_items**; fixed train-time normalization (not per-request batch min-max)
- **FEATURE_ORDER** = shared constant Python train ↔ Java serve
- Cart in profile = **cart add events over 180d** (weight 0.6), not only current cart snapshot
- Home API = **authenticated only**; no guest recommend path
- Impression = served Top K async log with **`ranking_mode`**; click = **product-detail `from=home`**; **no** suppress recently shown; label attribution = **nearest prior impression**
- Retrieval = **A/B/E/C** (D0 no semantic); **CandidateProduct** with product snapshot + source scores ∈[0,1]; **no** `retrievalScore`; exclude own shop; A slices 40/40/20 with Popular **90d**
- Features = **15 normative formulas** in spec; **PopularityNormalizer** with model artifact; missing `created_at` → recency **0**; `price_affinity` IQR; FE only `clip` cf/ar
- Batch-1 online locks: degraded `0.7*pop+0.3*rec`; cf/ar unit maps; E/C ≤20 products/entity|cat; `GET .../home/recommendations` + flag→404; empty=`[]`; profile D3 numbers + cart_items MVP + percentiles ≥3
- Batch-2: `user_social_interest_export` consume; HomeModelLoader PostConstruct+cron; **exclude vacation** shops; empty pool confirmed 200+`[]`
- Batch-3 offline: CF pair/score/schema; AR mine mins; social export job; Home dataset time-split 80/10/10; baseline=D10 key; gate AUC+P@10; soft-reject
- Batch-4 data modes: file/RAM Home sim (no shared-DB seed); in-memory catalog from personas; sim CF/AR files + load-artifact; Admin `system_configs` enum `SEED_ONLY`|`HYBRID`|`REAL_ONLY`
- Batch-5 gaps: serve logs **minimal feature provenance**; evaluate P@10 by `request_id`; retrain runbook; DDL required columns/indexes; min product card; LightGBM tie-break; Apriori/FP-Growth AR; Commerce `model_artifacts` columns; Python↔Java feature parity tests
- Batch-5b: Admin config HTTP+DB keys; `sources` JSONB; HYBRID CF max-merge; REAL_ONLY min 5000; shared Model Registry tab with model selector

## Goals / Non-Goals

**Goals:**

- End-to-end Hybrid Commerce Home: profile → multi-source retrieval (A/B/E/C) → pool ≤500 → features → LightGBM → diversity → Top 50.
- Entity-based CF with Completed×1.0 + Cart×0.6 seeds.
- Offline AR Social interest_tag → Commerce category; **semantic/ANN fill deferred (D0)**.
- Offline train/evaluate/export-activate for `commerce_home_ranker` (or equivalent model_name).
- **Auth-only** Home recommend (JWT required; no guest serve path on web).
- Degraded serve if ONNX missing (popular/recency on same pool).
- Point-in-time `UserInterestProfile(as_of)` and feature formulas locked for train/serve parity.
- Home impression log + product-detail-as-click; labels via **nearest prior impression**.
- Retrieval returns **`CandidateProduct`** (product snapshot + source scores) — no shared `retrievalScore`.

**Non-Goals:**

- Post retrain orchestrator / online drift auto-retrain.
- ALS/BPR/neural CF; Neo4j.
- Full-catalog embedding scan / semantic ANN retrieval in v1 (D deferred).
- Rule-based production Top-K ranking.
- Sample-weighting **by engage action type** (click vs cart vs buy) in v1 — deferred; **HYBRID `seed_row_weight` is allowed** (D17).
- Per-request min-max of `popularity_score` over the candidate pool of 500.
- Normalizing a shared retrieval score or doing feature engineering inside retrieval.

## Decisions

### D1 — Commerce owns serving

- **Choice:** `RecommendCommerceHomeUseCase` (name TBD) in commerce-service; FE calls Commerce API.
- **Why:** Products, inventory, cart/orders live in Commerce; avoids Social becoming a catalog proxy.
- **Alternatives:** Social ranks product IDs — rejected (wrong ownership, inventory lag).

### D2 — Online pipeline vs offline artifacts

```
OFFLINE                              ONLINE (Commerce)
entity_cooccur table                 Build UserInterestProfile(as_of)
AR interest_tag→category             Soft-quota retrieval A/B/E/C (no D)
Home dataset + LightGBM→ONNX         Inventory filter + dedupe → ≤500 CandidateProducts
popularity (z_lo,z_hi) w/ model   Feature build (+ PopularityNormalizer) → ONNX → diversity → 50
```

- Social interest aggregates: prefer **batch export** consumed by Commerce (Redis/DB table) over per-request Social HTTP; allow internal API for bootstrap.

### D3 — UserInterestProfile = facets + provenance (not one blended bag)

Profile is for **retrieval + match features**, not the LTR vector itself.

```
UserInterestProfile {
  user_id, as_of
  category_scores, brand_scores, shop_scores   // commerce, max-norm [0,1], top-K
  price_p25, price_p50, price_p75               // from COMPLETED only; may be missing
  hashtag_scores, keyword_scores               // social, separate namespaces
}
```

**Commerce raw (window 180d before `as_of`):**
- COMPLETED order line → weight **1.0** × decay \(2^{-\Delta/30d}\)
- Cart interactions → weight **0.6** × same decay
- **Cart MVP source (locked):** prefer dedicated cart-add events if the table exists; else use **live** `cart_items.created_at` in the 180d window (do **not** include soft-deleted cart rows)
- Normalize per facet by max; keep **top 20 categories, 20 brands, 10 shops**

**Price percentiles (locked):**
- Sample = `effective_price` of COMPLETED order lines in the 180d window before `as_of`
- If `sample_count < 3` → price stats **missing** (Feature Builder uses `price_affinity = 0.5`)
- Else `p25`, `p50`, `p75` = empirical percentiles on that sample

**Social raw (window 90d; weights search×4, save×3, comment×2, like×1):**
- **Offline export job** applies time decay \(2^{-\Delta/14d}\) relative to export `as_of` and writes **already-decayed weighted** tag scores into Commerce consumer table `user_social_interest_export` (see **D13**).
- **Online profile** loads the latest export rows for the user and **max-norms** within hashtag and keyword maps separately — it MUST NOT re-scan Social event stores on the hot path and MUST NOT require per-event decay online.
- Stale export (e.g. `computed_at` older than 7 days) is still usable; emit ops warning/metric, do not blank facets solely for staleness.
- Missing rows → empty social facets (Source C empty; `cross_domain_score` = 0).
- Do **not** merge hashtags into `category_scores`; AR projects tags→categories at retrieval time.

**Point-in-time:** train uses `as_of = shown_at` (events strictly before); serve uses `now` for commerce facets; social facets follow latest export `as_of` (batch lag accepted).

**Auth:** Home recommend requires an authenticated buyer; unauthenticated callers are rejected (no empty-profile guest rail).

### D4 — Retrieval source contracts + CandidateProduct (no FE-in-retrieval)

**Separation:** Retrieval only finds candidates and attaches **source-native** scores. It MUST NOT apply feature normalization, time-decay for LTR, `price_affinity`, `recency_score`, or a shared cross-source `retrievalScore` (scales differ; normalizing here would become feature engineering).

**Eligibility (all sources):** `ACTIVE`, available stock &gt; 0, shop/category sellable rules, **shop not in vacation mode**, and **exclude products from the caller's own shop**.

**Soft caps (upper bounds; pool need not reach 500):**

| Source | Cap | Role |
|--------|-----|------|
| A Popular / new / rating | ≤100 | Coverage / cold-start |
| B Personal history | ≤150 | Facet affinity |
| E Entity CF | ≤150 | Co-occur neighbors |
| C Cross-domain AR | ≤150 | Social→category |
| D Semantic / ANN | **off (D0)** | Deferred until embeddings exist |

Dedupe by `product_id` with **OR** `EnumSet&lt;RetrievalSource&gt;` → LTR flags `is_popular`, `is_personal`, `is_cf`, `is_cross_domain`; `is_semantic` stays **false** in v1.

#### A — Popular / new / rating (≤100), three slices

| Slice | Share | Retrieval rank key |
|-------|-------|--------------------|
| A1 New | 40 | `created_at DESC` |
| A2 Popular | 40 | `COUNT(completed_order_items)` in a **90-day** window, then `created_at` |
| A3 Rating | 20 | `rating_avg DESC` where `rating_count ≥ 3` |

Global (not personalized). After union/dedupe within A, cap at 100. Sets `POPULAR` on the candidate. No popular scalar on `CandidateProduct` — LTR uses D11 `popularity_score` from product stats.

#### B — Personal (≤150)

- Map profile top facets (categories / brands / shops) → ACTIVE in-stock products in those entities.
- `personalScore = max(category_score, brand_score, shop_score)` for the product's matching facets.
- **Do not** multiply by listing recency at retrieval (avoids double-count with LTR `recency_score`).
- Empty facets → B empty.

#### E — Entity CF (≤150)

- Seeds from user category/brand facet strength → look up **top-N neighbor entities** (N **configurable**, e.g. `commerce.cf.max-neighbors`, default 30 in config only).
- For each neighbor (neighbor score DESC): take ACTIVE in-stock products in that entity, ordered by **completed_order_items count DESC**, then `created_at DESC`; soft cap **≤20 products per neighbor entity** (config default 20).
- Union unique `product_id`; if a product appears via multiple neighbors keep **max** raw before unit-scale.
- **`cfScore` (locked v1):** `raw = edge_score(seed→neighbor) × seed_strength` (seed_strength = facet score ∈ [0,1]). Within one request’s source-E candidates, `cfScore = raw / max(raw_E, ε)` so scores ∈ (0,1]; empty/zero → 0.
- Spec MUST NOT hard-code N; per-entity product cap defaults live in config.

#### C — Cross-domain AR (≤150)

- User social tags → association rules with `confidence ≥ configured threshold` (default **0.05** in config only) → Commerce categories.
- Order categories by best rule score DESC; within each category take ACTIVE products ordered by **completed_order_items count DESC**, then `created_at DESC`; soft cap **≤20 products per category** (config default 20).
- Union unique products; multiple rules → **max** `arScore`.
- **`arScore` (locked v1):** `clip(confidence × tag_score_norm, 0, 1)` where `tag_score_norm` is the profile tag score after max-norm ∈ [0,1].
- Spec MUST NOT hard-code the confidence threshold.

#### D — Semantic (D0)

- No embedding/ANN in this change. Do **not** run a semantic source; do **not** set `is_semantic` or fabricate `semantic_similarity &gt; 0`.
- Pool size may be 430–470 etc.; **500 is an upper bound**, not a fill target.
- True semantic retrieval is a later additive source.

#### CandidateProduct output

```
CandidateProduct {
  Product product;   // snapshot with attrs needed by feature builder
                     // (category, brand, shop, price, createdAt, ratings, …)
  EnumSet<RetrievalSource> sources;
  Float personalScore;  // when PERSONAL; else null/0; ∈ [0,1]
  Float cfScore;        // when CF; else null/0; MUST be ∈ [0,1]
  Float arScore;        // when CROSS_DOMAIN; else null/0; MUST be ∈ [0,1]
}
```

- **No** `retrievalScore`.
- Prefer embedding a product **read model**, not bare `productId`, so Feature Builder does not re-query the same listing attributes.
- Feature Builder inputs: `CandidateProduct` + `UserInterestProfile` + **`PopularityNormalizer`** (loaded with the Home ranker artifact). It MUST NOT depend on a free-floating “train_meta” concept outside the model load path. It `clip`s `cfScore`/`arScore` defensively and MUST NOT re-derive CF/AR algorithm internals.

### D5 — Entity-based CF (offline build + online serve)

**Types (v1):** `LEAF_CATEGORY`, `BRAND` only (shop edges deferred).

**Offline co-occur job (locked):**
- Window: **180d** before job `as_of`.
- **Completed pairs:** within each COMPLETED order, every unordered pair of distinct entities among line items’ leaf categories and brands contributes weight **1.0** (same entity twice in one order does not self-pair).
- **Cart pairs:** for the same user, cart-add (or live `cart_items`) entities that co-occur within a **24h** wall-clock window contribute weight **0.6** per unordered pair (dedupe per user-day optional; simple count OK).
- **Score:** `score = log1p(sum of pair weights)`; keep top **M=50** neighbors per entity (config default).
- **Table (Commerce):**

```
entity_cooccur (
  entity_type, entity_id,
  neighbor_type, neighbor_id,
  score, updated_at,
  PRIMARY KEY (entity_type, entity_id, neighbor_type, neighbor_id)
)
```

**Online:** seed from user facets → top-N neighbors by `score` → products (D4 E). Taxonomy walk deferred.

### D6 — Feature Engineering Specification (normative formulas)

**Inputs:** `CandidateProduct`, `UserInterestProfile(as_of)`, `PopularityNormalizer` (from Home model artifact).  
**Output:** `float[15]` in `HOME_FEATURE_ORDER`.  
**Point-in-time:** online `t`/`as_of` = now; offline train = impression `shown_at` (events strictly before).

**`HOME_FEATURE_ORDER` (exact order, shared Python ↔ Java):**

1. `recency_score`
2. `popularity_score`
3. `rating_score`
4. `category_match`
5. `brand_match`
6. `shop_match`
7. `price_affinity`
8. `cross_domain_score`
9. `cf_score`
10. `semantic_similarity`
11. `is_popular`
12. `is_personal`
13. `is_cf`
14. `is_cross_domain`
15. `is_semantic`

**Formulas (`clip(x,0,1)=max(0,min(1,x))`; continuous ∈ [0,1] unless flags):**

| Feature | Formula |
|---------|---------|
| `recency_score` | Missing `created_at` → **0**. Else \(\Delta=\max(0, t - created\_at)\) seconds; \(2^{-\Delta/(7\cdot86400)}\) (half-life **7 days**) |
| `popularity_score` | `z=log1p(raw)` with raw per **D11**; `PopularityNormalizer.normalize(z)` → [0,1] |
| `rating_score` | `rating_count < 3` → **0.5**; else `clip(rating_avg/5, 0, 1)` |
| `category_match` | `profile.category_scores.get(product.category_id, 0)` |
| `brand_match` | null brand → **0**; else `profile.brand_scores.get(brand_id, 0)` |
| `shop_match` | `profile.shop_scores.get(product.shop_id, 0)` |
| `price_affinity` | `price = effective_price`. Missing profile price stats or `IQR=p75-p25 ≤ 0` → **0.5**. If `price ∈ [p25,p75]` → **1.0**. If `price < p25` → `clip(1 - (p25-price)/IQR, 0, 1)`. If `price > p75` → `clip(1 - (price-p75)/IQR, 0, 1)` |
| `cross_domain_score` | `clip(candidate.arScore ?? 0, 0, 1)` — MUST NOT re-mine AR |
| `cf_score` | `clip(candidate.cfScore ?? 0, 0, 1)` — MUST NOT re-walk CF graph |
| `semantic_similarity` | **0** (D0) |
| `is_popular` … `is_cross_domain` | `1` iff corresponding source ∈ `CandidateProduct.sources`, else `0` |
| `is_semantic` | **0** (D0) |

- FE MUST NOT apply `x/(1+x)` or other CF/AR-specific transforms — unit interval is Retrieval’s contract.
- Export: `feature_order.json` + popularity `(z_lo,z_hi)` with the model; Java asserts order/dim=15; missing normalizer with LightGBM path → degraded (fail closed).
- Unit/smoke tests MUST fail if Python and Java orders or formulas diverge on fixtures.

### D7 — Labels (nearest impression)

- Positive if any of click / add-cart / purchase is attributed to that impression (default label window **24h**).
- Binary y∈{0,1}; sample weights out of v1.
- **Attribution when the same product is impressed repeatedly (locked):** each engage event at time `t` is assigned only to the **nearest prior impression** for that `(user_id, product_id)` — i.e. the impression with maximum `shown_at` such that `shown_at ≤ t` and `t < shown_at + label_window`. Older impressions of the same product that are not the nearest do **not** become positive from that engage.
- See **D12** for click/cart/buy sources.

### D8 — Model registry + online load/reload (mirror Social)

**Offline / registry:**
- Distinct `model_name = commerce_home_ranker`; Commerce-owned artifact table (not Social `model_artifacts`).
- Soft-reject on evaluate gate leaves prior active unchanged (offline export-activate only — online never auto-activates a failed gate).

**Online HomeModelLoader (locked, mirror Social `ModelLoader`):**
- Resolve active artifact by `model_name`; ONNX path = `COMMERCE_HOME_MODEL_ROOT` / `commerce.home.recommendation.model-root` + **portable basename only** (reject `..`, separators, absolute paths).
- Load sidecars with the model: `feature_order.json` + PopularityNormalizer `(z_lo, z_hi)`.
- `@PostConstruct` initial load; `@Scheduled` reload cron (configurable, e.g. every 5 minutes); optional ops `forceReload`.
- On request: session present **and** feature_order matches **and** normalizer present → `LIGHTGBM`; else → **DEGRADED** (D10) with fallback reason (`file_not_found`, `load_error`, `onnx_session_missing`, `feature_order_mismatch`, `normalizer_missing`, etc.).

### D9 — Diversity after LTR (configurable greedy hard-cap)

- **Placement:** After LightGBM (or degraded) scores; **does not** shrink or rebuild the ~500 pool; does not change ONNX features.
- **Algorithm (v1):** Greedy hard-cap — sort by score DESC; append item if under configured caps; if result size &lt; K after a full pass, **backfill** remaining by score (ignore caps) so K is filled when the pool allows.
- **Not in v1:** MMR; round-robin buckets; min-distinct-category swap phase (optional later).
- **Spec MUST NOT hard-code numeric caps** — only the mechanism + configurability. Defaults live in config for the default Home K=50.

**Config keys (examples):**

```
commerce.home.diversity.enabled=true
commerce.home.diversity.k=50
commerce.home.diversity.max-per-category=8   # default for K=50; required dim in v1
commerce.home.diversity.max-per-brand=5      # default; 0 = disable brand cap
commerce.home.diversity.max-per-shop=4       # default; 0 = disable shop cap
```

- Same mechanism applies for Top 20 / 50 / 100 by changing `k` and optionally retuning defaults (e.g. heuristic `max(2, ceil(k/6))` for category — ops choice, not a normative formula).
- Null/missing brand → skip brand cap for that item (or treat as no brand limit).
- Authed LightGBM and degraded paths use the **same** diversity step when `enabled=true`.
- `enabled=false` → return top K by score only (debug / A/B).

### D10 — Degraded serve (locked composite)

When ONNX is unloaded/fails **or** PopularityNormalizer constants are missing for the LightGBM path:

1. Still build features needed for the sort key (at least `popularity_score` and `recency_score`; full 15-dim build is allowed).
2. `degraded_key = 0.7 * popularity_score + 0.3 * recency_score`
3. Sort pool by `degraded_key` DESC; tie-break `created_at` DESC, then `product_id` ASC.
4. Apply the **same** diversity step as LightGBM; return Top K.
5. Set `ranking_mode = DEGRADED`; log a fallback reason for ops.
6. MUST NOT invent a second full rule-based ranker.

**LightGBM path sort (locked, same tie-break):** after ONNX scores, sort by `model_score` DESC; tie-break `created_at` DESC, then `product_id` ASC; then diversity. Sort MUST be deterministic.

### D10b — Home recommend HTTP contract (minimal)

```
GET /commerce/api/v1/home/recommendations
Auth: required (JWT buyer)

Feature flag: COMMERCE_HOME_RECOMMEND_ENABLED (or commerce.home.recommend.enabled)
  false → HTTP 404 with stable business/feature-disabled code; FE keeps browse list / hides rail

Success 200 data (minimum):
  request_id       (UUID string; required — also used for impressions + FE PDP attribution)
  ranking_mode     LIGHTGBM | DEGRADED (required)
  model_name       string | null
  model_version    int | null
  items            product cards, length ≤ 50

Empty candidate pool after filters → 200 with items: [] (not an error).
Unauthenticated → 401 (existing auth behavior).
```

**Minimum product card per `items[]` element (locked — not a full DTO):**

| Field | Notes |
|-------|--------|
| `id` | product id |
| `title` | display name |
| `price` | listing display price (Commerce domain resolves from `effective_price` / price tables) |
| `thumbnail` | primary image URL or null |
| `shop` | minimal shop identity for card (e.g. id + name) |
| `rating` | rating summary for card (e.g. avg + count, or null if sparse) |

FE may consume additional fields Commerce already exposes on product cards; specs MUST NOT invent parallel price taxonomies (`sale_price` / `discount_price` / …) inside Home recommend — use Commerce’s existing `effective_price` concept at the domain boundary.
### D11 — `popularity_score` (completed_order_items + fixed normalization)

**Raw (locked):**

```
raw(product_id, as_of) =
  COUNT(*) of order_items
  WHERE product_id = ?
    AND status = 'COMPLETED'
    AND completed_at < as_of
```

Not `order_count`, not Σ sold quantity. Second-hand ACTIVE listings often have raw=0 — acceptable; do not redefine popularity as category heat inside this feature.

**Normalization (locked — NOT batch-500 min-max):**

```
z = log1p(raw)
popularity_score = PopularityNormalizer.normalize(z)
               = clip( (z - z_lo) / (z_hi - z_lo + ε), 0, 1 )
```

- `(z_lo, z_hi)` fit once on the **train** set (prefer p1–p99 of `log1p(raw)`), persisted with the **Home ranker model artifact** / sidecar and exposed online as **`PopularityNormalizer`** loaded on model activate (same lifecycle as ONNX).
- Feature Builder depends on `PopularityNormalizer`, not on an unbound “train_meta” bag.
- Online and offline scoring MUST reuse the same constants so the same product gets a stable score across requests.
- Intentional difference from Post `engagement_score` batch min-max: Home catalog comparisons must be cross-request stable.
- If LightGBM is selected but normalizer constants are missing → treat as model unload / **degraded** serve.

### D12 — Home impression + click logging (auth-only, no suppress)

**Serve impression (mirror Social pattern):**

- After building the **Top K response** (post-diversity), commerce-service logs **served** impressions asynchronously (MUST NOT block or fail the recommend HTTP response on log failure).
- Log **only authenticated** callers; only the K returned products (not the ~500 pool).
- **No** `user_seen_products` / suppress-recently-shown: the same product MAY appear and be impressed on many Home requests.

**`home_impression_log` required columns (logical — migration supplies types):**

| Column | Notes |
|--------|--------|
| `id` | Primary key |
| `user_id` | Required (auth-only) |
| `product_id` | |
| `shown_at` | Serve time |
| `rank_position` | 1..K after diversity |
| `model_name`, `model_version` | Set when `ranking_mode=LIGHTGBM`; null/omit when degraded |
| `request_id` | Groups the K rows of one recommend call; returned to FE |
| `ranking_mode` | Required: `LIGHTGBM` \| `DEGRADED` |
| **`sources`** | Minimal provenance: retrieval source set for this served item (e.g. POPULAR\|PERSONAL\|CF\|CROSS_DOMAIN) |
| **`personal_score`** | Nullable; CandidateProduct value at serve (∈[0,1] or null) |
| **`cf_score`** | Nullable; CandidateProduct value at serve |
| **`ar_score`** | Nullable; CandidateProduct value at serve |

**`sources`:** JSONB array of strings from closed set `POPULAR`|`PERSONAL`|`CF`|`CROSS_DOMAIN` (D22).  
**Required indexes (logical):** `(user_id, shown_at DESC)`; `(user_id, product_id, shown_at)`; `(request_id)`.

**Minimal feature provenance (locked — Batch 5):** Serve MUST persist the provenance columns above on each Top-K impression. Offline build-dataset MUST reconstruct `CandidateProduct` scores/sources from these columns + product snapshot + `UserInterestProfile(as_of=shown_at)`, then run Feature Builder (D6). MUST NOT replay full retrieval A/B/E/C to recover source flags. MUST NOT require logging the full 15-dim feature vector on the impression row (vector is recomputed offline/online from the same formulas).

**`home_engage_event` required columns (logical):** `id`, `user_id`, `product_id`, `event_type` (`CLICK`\|`CART`\|`BUY` or equiv.), `occurred_at`, optional `request_id`. Index: `(user_id, product_id, occurred_at)`.

**Click v1 = product-detail-as-click:**

- Home response **MUST** expose `request_id` and `ranking_mode` (see D10b).
- FE navigates from Home rail with attribution query, e.g. `from=home` and `request_id`.
- Buyer `ViewProductDetail` (or side-effect logger): if authenticated and `from=home`, async insert a **CLICK** engage (`home_engage_event` or equivalent) with `user_id`, `product_id`, `occurred_at`, optional `request_id`.
- Missing `request_id` but `from=home` → still log click (do not drop label).
- PDP without `from=home` (search, shop, deep link) → **not** a Home click.
- Refresh of attributed PDP URL may insert another CLICK (simple v1; offline may dedupe later).

**Cart / buy:**

- Offline join from Commerce cart add / COMPLETED order lines (no FE beacon required).
- Combined with CLICK under D7 nearest-impression attribution.

**Non-goals for D12:** viewport-visible beacons; guest/anonymous impression rows; suppress-on-seen; logging full 15-float vectors on every impression.

### D13 — Social interest consume schema (Commerce consumer)

Offline job publishes into Commerce Postgres (v1; Redis optional later):

```
user_social_interest_export (
  user_id       UUID NOT NULL,
  tag_type      VARCHAR(16) NOT NULL,  -- HASHTAG | KEYWORD
  tag           VARCHAR(128) NOT NULL, -- normalized lowercase
  score         DOUBLE PRECISION NOT NULL, -- weighted + decayed at export as_of
  window_days   INT NOT NULL DEFAULT 90,
  computed_at   TIMESTAMPTZ NOT NULL,
  as_of         TIMESTAMPTZ NOT NULL,
  PRIMARY KEY (user_id, tag_type, tag)
)
```

- Job (daily default): read Social search/save/comment/like (and equivalent) events in 90d before `as_of`; weight 4/3/2/1; apply \(2^{-\Delta/14d}\); **REPLACE/UPSERT** all rows for touched users in the batch (full refresh per user preferred).
- Cadence: configurable (default daily).
- Online Home profile: read by `user_id`; max-norm per `tag_type`; no Social HTTP.
- Empty table / no user rows → empty social facets (safe).

### D14 — Association-rule mining (offline)

- **Basket:** per user in a rolling window (default **90d**), items = social interest tags (from same weighted sources as D13, undecayed counts OK for mining) ∪ leaf categories from COMPLETED purchases.
- **Rule form:** `interest_tag → commerce_category` only (not category→tag).
- **Algorithm (locked):** frequent-itemset association mining via **Apriori** or **FP-Growth** only (same support/confidence semantics). MUST NOT invent neural/graph “AR” substitutes. **Default for fixtures and first implementation: Apriori.**
- **Support:** fraction of user-baskets in the window that contain the itemset (denominator = number of users with a non-empty basket in-window).
- **Defaults (config):** `min_support = 0.01`, `min_confidence = 0.05` (align online AR τ default 0.05).
- Publish:

```
social_tag_category_ar (
  tag, tag_type, category_id,
  support, confidence, updated_at,
  PRIMARY KEY (tag_type, tag, category_id)
)
```

- Online retrieval uses D4 C + configured confidence threshold.

### D15 — Home LTR dataset, train, evaluate, export-activate, retrain

**Dataset unit:** one row per Home impression (DB or sim file per D17).  
**as_of / features:** `shown_at`; rebuild profile + product attrs with events strictly before `shown_at`; reconstruct CandidateProduct **from logged provenance** (D12); formulas = D6.  
**Label:** D7 nearest-impression engage within **24h** (click∪cart∪buy).  
**Split:** time-ordered by `shown_at`, **80% / 10% / 10%** train/val/test (mirror Post; no shuffle).  
**PopularityNormalizer:** fit `(z_lo, z_hi)` on **train** only (prefer p1–p99 of `log1p(raw)`); persist with artifact.  
**Bootstrap:** D16 file/RAM under `SEED_ONLY` / `HYBRID` (D17).

**Train:** LightGBM binary on y∈{0,1}; Home train config checked in; HYBRID applies `seed_row_weight`.

**Evaluate (locked, mirror Post):**
- **Group key** = `request_id` (all impressed items from one Home recommend call).
- **Precision@10** = mean over groups of (hits in top-10 by score within that group / min(10, group_size)) — same aggregation style as Social Post evaluate.
- **AUC** = global ROC-AUC on test rows (scores vs binary labels).
- **Baseline** scores with `0.7 * popularity_score + 0.3 * recency_score`.
- Report sections `lightgbm` vs `baseline` MUST include `auc` and `precision_at_10`.

**Export-activate gate:** pass only if `lightgbm.auc >= baseline.auc` AND `lightgbm.precision_at_10 >= baseline.precision_at_10` (null metric → fail closed). On pass: write ONNX + `feature_order.json` + `(z_lo,z_hi)` under Commerce `MODEL_ROOT`; register/activate `commerce_home_ranker`. On fail: **soft-reject**.

**Retrain runbook (normative requirement — not optional “docs only”):**  
Train and retrain SHALL execute the same ordered orchestration (v1 trigger = operator/CLI; cron optional later). Steps MUST run in this order:

```
1. Resolve Admin train_data_mode (+ seed_row_weight if HYBRID)
2. If SEED_ONLY or HYBRID needs fresh sim → Home sim → files (D16)
3. Entity CF build
4. Social interest export
5. AR mine (Apriori/FP-Growth)
6. load-artifact (when online tables must be refreshed from files / job outputs)
7. build-dataset (labels + features from provenance)
8. split 80/10/10
9. train
10. evaluate
11. export-activate (gate → activate or soft-reject)
```

Online serve picks up a newly activated artifact via HomeModelLoader reload (D8). Failed gate MUST NOT activate.

**Ops entrypoints (`recsys-offline`):** jobs/CLI for the steps above — **no** public online Home predict API.

### D16 — Home sim bootstrap: file/RAM only (no shared-DB seed)

**Locked (Batch 4):** Cold-start and seed corpora for Home MUST be generated **in-memory** (reuse/extend Post `personas.yaml` + bot engine patterns) and written only under a dedicated disk dir (e.g. `RECSYS_HOME_SIM_DIR` / `data/home_sim/`), **not** into the Auth / Social / Commerce databases that apps use.

**Catalog:** Generate shops/products/categories/brands **in-memory from persona niches** (fashion second-hand). MUST NOT INSERT fake catalog rows into live Commerce Postgres for Home bootstrap. MUST NOT require a separate Docker sim DB.

**Sim outputs (files):**
- Home impression rows shaped like `home_impression_log` (including provenance columns from D12)
- Engage events (click / cart / buy) for nearest-impression labels
- Synthetic orders/carts/tags needed to rebuild profile features, CF pairs, AR baskets, social interest scores **on disk**
- Derived tables as files: `entity_cooccur.parquet|csv`, `social_tag_category_ar.*`, `user_social_interest_export.*`

**Forbidden for Home cold-start (contrast with Post `seed-db --simulate`):**
- Writing sim users into Auth `users`
- Writing sim posts/likes/saves/impressions into Social Mongo/Postgres
- Writing sim shops/products/orders into Commerce Postgres as a substitute for file bootstrap

**Optional later:** dedicated `*_SIM_*` DSN — **out of scope** for v1; not required when D16 holds.

### D17 — Train data mode enum (Admin system_configs)

**Config key (locked):** `commerce.home.ltr.train_data_mode`  
**Storage:** Admin Postgres `system_configs` (`value_type = STRING`, `is_active = true`)  
**Allowed values (exact enum strings):**

| Value | Meaning |
|-------|---------|
| `SEED_ONLY` | Dataset + CF/AR/export inputs come **only** from Home sim files (D16). Ignore real Home impressions / real Social·Commerce extracts for train corpus even if present. |
| `HYBRID` | **Union** real extracts + sim files. Sim identities MUST use reserved deterministic UUID namespaces so they never collide with real users/products. Each training row MUST carry `data_source ∈ {SEED, REAL}`. Optional config `commerce.home.ltr.seed_row_weight` (DECIMAL, default **0.5**) down-weights SEED rows in train (sample weight); REAL rows weight 1.0. |
| `REAL_ONLY` | Dataset + CF/AR/export inputs from **real** service stores only. If real Home impression count &lt; `commerce.home.ltr.real_only_min_impressions` (default **5000**), job **fails closed** (do not silently fall back to seed). |

**Default:** `SEED_ONLY` until operators promote mode after Home logging has volume.

**Admin UX:** Create/update via existing System Configs UI — `AdminPage?section=systemOperations&tab=system-configs` (permission `SYSTEM_CONFIG_UPDATE`). No new admin section required for the enum itself; description MUST list the three allowed values. Invalid string on update → validation error (Admin policy or Home-job preflight).

**Who reads it:** At the start of Home train/retrain orchestration (D15), `recsys-offline` SHALL resolve the active value (Admin HTTP read of `system_configs` by key, or a checked-in local override **only** for CLI smoke). The resolved mode MUST be recorded in `train_meta` / evaluate report.

**Train vs retrain:** Both use the **current** enum value at job start and the **same** D15 step order — there is no separate “retrain mode”.

### D18 — Dataset + artifact assembly by mode

```
Admin system_configs
  commerce.home.ltr.train_data_mode
            │
            ▼
   ┌──────────────── train / retrain orchestrator (D15) ──────────┐
   │ SEED_ONLY │  Home sim (RAM) → files only                     │
   │ HYBRID    │  real extract ∪ sim files (tag data_source)      │
   │ REAL_ONLY │  real extract only (fail if too thin)            │
   └───────────┬──────────────────────────────────────────────────┘
               │
               ├─ CF → social-export → AR → load-artifact
               └─ build-dataset → split → train → evaluate → export-activate
```

**Real extract inputs (read-only)** — see **D19 inventory**.  
**Sim inputs** — D16 files only.  
**HYBRID merge (locked):** concatenate impression corpora; time-split **after** merge still by `shown_at` globally 80/10/10; do not shuffle. Prefer keeping SEED and REAL interleaved by time if sim clock is mapped into a past window so chronological split remains meaningful (sim `shown_at` MUST lie strictly before “now” and SHOULD occupy an early contiguous window or a documented offset so REAL recent rows dominate val/test when both exist).

### D19 — Data inventory: reads, writes, what “seed/load” means

#### A. MUST NOT seed into app DBs for Home bootstrap (file/RAM only)

| Service | Store | Name | Note |
|---------|-------|------|------|
| auth-service | Postgres | `users`, `user_profiles` | Post sim wrote here — **forbidden** for Home D16 |
| social-service | Mongo | `posts`, `comments`, `user_projections` | Post sim — **forbidden** for Home D16 |
| social-service | Postgres | `post_impression_log`, `post_likes`, `post_saves`, `follows`, `search_history`, `user_seen_posts` | Post sim — **forbidden** for Home D16 |
| commerce-service | Postgres | `seller_shops`, `products`, `product_inventories`, `product_prices`, `orders`, `order_items`, `carts`, `cart_items` | Post sim stock-burn — **forbidden** for Home D16 catalog/orders |

#### B. Real **read-only** extracts (HYBRID / REAL_ONLY)

| Service | Store | Name | Used for |
|---------|-------|------|----------|
| commerce | PG | `orders`, `order_items` | Profile, CF completed pairs, popularity, buy labels, price samples |
| commerce | PG | `carts`, `cart_items` | Profile cart×0.6, CF cart pairs, cart labels (exclude `REMOVED`) |
| commerce | PG | `products`, `product_categories`, `brands`, `seller_shops`, `shop_settings`, `product_inventories`, `product_prices` | Entities, eligibility context, features |
| commerce | PG | `reviews` | `rating_score` / Source A aggregates |
| commerce | PG | `home_impression_log` | Train rows + provenance (**planned** online write; offline **R**) |
| commerce | PG | `home_engage_event` (or equiv.) | Click labels (**planned**; offline **R**) |
| social | PG | `search_history`, `post_saves`, `post_likes` | Social export weights 4/3/1 |
| social | Mongo | `comments`, `posts` (hashtags) | Social export weight 2 + HASHTAG tags |
| admin | PG | `system_configs` | Read `commerce.home.ltr.train_data_mode` (+ optional seed_row_weight) |
| notification | — | — | **Unused** |

#### C. Commerce **writes** from offline jobs / load-artifact (not “event seed”)

| Destination | Written by | Mode notes |
|-------------|------------|------------|
| `entity_cooccur` | CF job or **load-artifact** from sim/real-built files | SEED: from sim files; REAL: from Commerce extract; HYBRID: merged scores then replace/upsert |
| `user_social_interest_export` | Social export job or load-artifact | Same mode rule |
| `social_tag_category_ar` | AR job or load-artifact | Same mode rule |
| Commerce `model_artifacts` (D20) + `COMMERCE_HOME_MODEL_ROOT` files | export-activate | All modes after gate pass |
| `home_impression_log` / `home_engage_event` | **Online** Commerce only | Sim MUST NOT insert these into live DB; sim keeps file copies |

#### D. load-artifact job (locked intent)

`load-home-artifacts` (name TBD) SHALL upsert file-built CF/AR/export tables into Commerce Postgres so online retrieval can serve after cold-start **without** having run pair-mining against an empty live catalog. It MUST NOT create fake buyers/orders/posts in Auth/Social/Commerce. ONNX activate remains the separate export-activate path (D15).

### D20 — Commerce `model_artifacts` required columns

Commerce-owned registry for `commerce_home_ranker` SHALL include at least (mirror Social V3 intent):

| Column | Notes |
|--------|--------|
| `id` | PK |
| `model_name` | e.g. `commerce_home_ranker` |
| `version` | int; unique with model_name |
| `format` | e.g. `onnx` |
| `artifact_path` | portable basename or path under MODEL_ROOT |
| `metrics` | JSON (evaluate report subset) |
| `is_active` | at most one active per `model_name` |
| `trained_at` | timestamp |

**Required indexes/constraints:** unique `(model_name, version)`; unique partial active per `model_name`; index `(model_name, is_active)`.

### D21 — Train/serve feature parity tests (Batch 5)

Normative test matrix MUST include:

1. **Python FeatureBuilder ≡ Java FeatureBuilder:** same fixture inputs (`CandidateProduct` + `UserInterestProfile` + `PopularityNormalizer`) → same length-15 vector, same `HOME_FEATURE_ORDER`, same PIT `as_of`.
2. **Provenance round-trip:** logged sources/scores → offline reconstruct → features match serve-time Feature Builder for that impression fixture.
3. **Evaluate grouping:** Precision@10 uses `request_id` groups.
4. **Retrain order:** orchestration invokes CF → social export → AR → dataset → split → train → evaluate → export-activate (assert call order in unit/integration of the orchestrator).
5. **Mode fail-closed:** `REAL_ONLY` with thin impressions does not activate seed fallback.

### D22 — Batch-5b wiring locks (Admin HTTP, sources encoding, HYBRID CF, REAL_ONLY min)

#### Admin train-mode config — how it works + DB

**Yes — stored in Admin DB**, table already exists: `admin_db.system_configs` (+ `system_config_history` on every change). No new Admin table.

| Key | value_type | Default | UI |
|-----|------------|---------|-----|
| `commerce.home.ltr.train_data_mode` | STRING | `SEED_ONLY` | `systemOperations` → `system-configs` |
| `commerce.home.ltr.seed_row_weight` | DECIMAL | `0.5` | same tab |
| `commerce.home.ltr.real_only_min_impressions` | INTEGER | **5000** | same tab |

**Ops flow:**
1. Migration/seed or admin **Create** rows once (permission `SYSTEM_CONFIG_UPDATE`).
2. Operator edits value on System Configs tab (history + outbox `SYSTEM_CONFIG_UPDATED` as today).
3. Home train/retrain orchestrator **reads at job start**.

**HTTP read contract for `recsys-offline` (locked v1):**
- Call Admin `GET /admin/api/v1/system-configs?q=<exact-config-key>&is_active=true` with a service/admin credential that has `SYSTEM_CONFIG_VIEW`.
- Select the item whose `configKey` **equals** the requested key (do not substring-match siblings).
- If missing / inactive / not exact → **fail closed** (except CLI smoke).
- **CLI/local override only:** env `HOME_LTR_TRAIN_DATA_MODE` / `HOME_LTR_SEED_ROW_WEIGHT` / `HOME_LTR_REAL_ONLY_MIN_IMPRESSIONS` MAY override when `RECSYS_HOME_CONFIG_FROM_ENV=1` (dev/smoke). Production orchestrator MUST prefer Admin DB values.
- Optional later: dedicated `GET .../system-configs/by-key/{configKey}` — not required for v1 if list+exact filter works.

**Not stored in Commerce DB:** train mode enum lives in Admin; Commerce only stores online artifacts (`model_artifacts`, CF/AR/export tables).

#### `sources` encoding on `home_impression_log`

**Locked:** column `sources` is **JSONB array of strings**, unordered unique set, values from closed enum:
`POPULAR` | `PERSONAL` | `CF` | `CROSS_DOMAIN`  
(empty array allowed if somehow none — should not happen for served items).  
Example: `["PERSONAL","CF"]`.  
Offline reconstruct maps to `EnumSet` / flags. MUST NOT invent free-text source names.

#### HYBRID CF merge

When building `entity_cooccur` under `HYBRID`, for each primary key edge `(entity_type, entity_id, neighbor_type, neighbor_id)`:
- Compute score from REAL extract and from SEED files independently when both exist.
- **Persisted score = max(real_score, seed_score)** (missing side treated as absent, not 0).
- Then apply top-M per entity. MUST NOT sum weights across REAL+SEED (avoids double-counting inflated edges).

#### `REAL_ONLY` minimum rows

Gate: `COUNT(home_impression_log)` (or real impression file rows) in the train window / available corpus **≥** `commerce.home.ltr.real_only_min_impressions` (default **5000**). Below threshold → fail closed, no seed fallback.

#### Model Registry tab — shared UI, two model names

**Yes — keep one tab** `admin?section=systemOperations&tab=model-registry`.

| model_name | Owner service | Registry store | Serve path |
|------------|---------------|----------------|------------|
| `feed_ranker` | social-service | Social `model_artifacts` | Social For You feed |
| `commerce_home_ranker` | commerce-service | Commerce `model_artifacts` | Commerce Home recommend |

**FE behavior (locked):**
- Add a **model selector** on Model Registry (dropdown or segmented control): Social For You (`feed_ranker`) vs Commerce Home (`commerce_home_ranker`).
- When `feed_ranker`: existing Social admin APIs (`/api/v1/social/admin/recommendation-model-artifacts?modelName=feed_ranker` + status).
- When `commerce_home_ranker`: **Commerce** admin APIs (new, mirror Social): e.g. `GET /commerce/api/v1/admin/recommendation-model-artifacts?modelName=commerce_home_ranker` + status — read-only list/runtime; **no** activate/export from UI (same as Post today).
- Distinguish clearly in copy: “Social For You” vs “Commerce Home Đề xuất”; never mix rows from two DBs in one undifferentiable list.
- Runtime status card MUST bind to the selected model_name / owner service.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Cold Social AR / empty CF for new users | Soft quotas A/B; pool may be &lt;500 without D fill |
| Stock=1 → product popularity often 0 | Accept thin signal; optional future `category_popularity` feature (separate) |
| Missing cart_add event table | MVP: `cart_items.created_at` in 180d; backlog true add events |
| Cross-service stale interest export | Batch refresh; still serve stale facets; warn if computed_at &gt; 7d |
| Vacation shops in recommend | Excluded at eligibility (not discovery-visible-only) |
| Train without Home impressions | D16 file/RAM sim + D17 `SEED_ONLY`/`HYBRID`; never Post-style shared-DB seed |
| HYBRID contaminates val/test with sim | Map sim timestamps into earlier window; down-weight SEED rows; promote to `REAL_ONLY` when volume allows |
| Admin enum typo / unknown mode | Validate against closed set; job preflight fail closed |
| HYBRID CF merge | **max(real, seed)** per edge (D22); never sum across corpora |
| Admin config unreachable | Fail closed; env override only when `RECSYS_HOME_CONFIG_FROM_ENV=1` |
| PDP refresh / missing request_id click noise | Require `from=home`; optional offline dedupe; nearest-impression limits multi-positive |
| Latency building 500 features | Batch DB reads; cache popular set; CF/AR lookups O(neighbors) |
| Feature order drift Java↔Python | D21 parity tests + shared `HOME_FEATURE_ORDER` + export `feature_order.json` |
| Missing provenance on old impressions | Serve MUST log sources/scores; train rows without provenance treat scores/flags as 0 or exclude from REAL corpus |
| Batch min-max popularity leak | Forbidden by D11 |

## Migration Plan

1. Schema: entity_cooccur, social_tag_category_ar, user_social_interest_export, home_impression_log (+ provenance), home_engage_event, Commerce model_artifacts (required columns/indexes per D12/D20).
2. Admin: `commerce.home.ltr.train_data_mode` = `SEED_ONLY` (+ optional `seed_row_weight`).
3. Offline: Home sim → CF → social-export → AR → load-artifact → dataset → split → train → evaluate → export-activate.
4. Commerce online behind `COMMERCE_HOME_RECOMMEND_ENABLED`.
5. FE rail + min product card fields; PDP `from=home` + `request_id`.
6. Promote mode SEED_ONLY → HYBRID → REAL_ONLY as impressions grow.
7. Rollback: flag off; soft-reject keeps prior active model.

## Open Questions

- Shop entity in CF v1: include or defer — default **defer shop edges**, keep shop_match feature from personal history only.
- Optional Admin `GET by-key` endpoint — nice-to-have; v1 uses list+exact `q` (D22).

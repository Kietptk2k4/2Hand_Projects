## Context

Recommended Feed offline ML already exists (`Services/recsys-offline`: clean → build-dataset → split → train → evaluate → export-activate). Social serving uses six features including `cross_domain_product_score`. Fixtures are tiny (~2 impressions). `clean_posts` currently omits `product_tags`, so a real clean run zeros cross-domain even when Mongo has tags. `user_purchase_profile.csv` is optional and not produced from Commerce.

Product MVP is Fashion Social + Fashion Commerce (`docs/product-vision/fashion-secondhand-vertical.md`). Simulation must stay inside that vertical while diversifying via niches, hashtags, leaf categories, and posts with/without product tags.

Labels require impressions: `label=1` only if like/save/comment in `[shown_at, shown_at+24h]`. Impression clean dedupes `(user_id, post_id)`, matching `user_seen_posts` semantics.

## Goals / Non-Goals

**Goals:**
- Produce ≥20k unique impression pairs for training with positive_rate ~12–25%
- Make `cross_domain_product_score` meaningful via Cart/Purchase + product tags on posts
- Preserve `product_tags` through clean; export Commerce purchase profile as-of train cutoff `T_cut`
- Fashion-only personas with affinity matrix; bot ranks feed by weighted random affinity
- Direct-insert simulation with a shared sim clock (impression → engage → cart → purchase)
- Early sessions count toward the 20k row budget
- KPI gates that fail closed if distribution is unhealthy

**Non-Goals:**
- Expanding catalog to laptop/gaming or non-fashion verticals
- Calling online RecommendPosts HTTP for bots
- Implementing commerceHome hybrid recommendation
- Seeding production environments
- Perfect point-in-time engagement counts (`like_count` snapshot leak accepted and documented)

## Decisions

### D1 — Direct DB insert vs RecommendPosts API
- **Choice:** Direct inserts into Auth / Social Mongo+Postgres / Commerce Postgres.
- **Why:** Volume (~28k raw impressions) and full control of timestamps; avoids JWT/rate-limit flakiness.
- **Must write:** `post_impression_log` (`request_id`, `rank_position`, `model_version=NULL`) and `user_seen_posts` together.
- **Alternative considered:** HTTP recommend — better production fidelity, worse for bulk + clock control.

### D2 — Volume model
| Entity | Target |
|--------|--------:|
| Users (bots) | 120 |
| Sellers/authors | 40 |
| Posts | 600 (~50% with productTags) |
| Products (stock=1) | 400 |
| Shops | 40 |
| Sessions/user | 12 over ~21 sim days |
| Feed size | 20 |
| Raw impressions | ~28.8k → cleaned rows ≥20k |
| Personas | 5 fashion niches |

### D3 — Personas stay inside fashion
Five personas (e.g. SneakerHead, StreetwearKid, VintageLover, BagCollector, OOTDMinimal) map to existing/extended **fashion leaf** categories and hashtag vocab. Affinity matrix `persona × niche ∈ [0,1]` drives engage probabilities (`p_like ≈ 0.03 + 0.35·a²`, save/comment as fractions of like). Search keywords MUST be exact hashtag tokens after lowercase normalize.

### D4 — Early sessions = pre-history that still creates impressions
No separate warm-up outside the dataset. First ~30% of impressions (by sim time) are “early” (weaker features); later sessions are “mature”. All count toward ≥20k.

### D5 — Feed ranking at insert time
Sample posts with weights proportional to persona affinity (plus forced mismatch mix so min-max within `request_id` has spread). `rank_position` is metadata only (not a model feature).

### D6 — Fix `clean_posts` in this change
Cleaned posts CSV MUST include serialized `product_tags` preserving `categoryId`/`shopId` (and product id when present). Without this, Cart/Purchase DoD cannot affect features.

### D7 — Purchase profile as-of `T_cut`
1. Clean (with product_tags) → build-dataset (may use provisional profile or empty) → split → read `T_cut` from split boundary `shown_at`.
2. Export `user_purchase_profile.csv` from Commerce: include only COMPLETED orders with `completed_at ≤ T_cut` (aggregate `category_ids`, `shop_ids` per user).
3. Rebuild dataset with that profile → re-split (same time order; cuts deterministic) → train → evaluate.

Preferred strengthening (same change if cheap): during build-dataset, if order-level events are available, apply per-row PIT (`completed_at < shown_at`). DoD minimum remains **no purchase after `T_cut` in the profile used for the final build**.

### D8 — Package layout
Under `Services/recsys-offline`:
- `config/personas.yaml` (affinity + probabilities + hashtag/category maps)
- `pipelines/` or `scripts/simulation/`: seed skeleton, simulate, kpi_gate, export_purchase_profile
- FastAPI: at least `POST /jobs/export-purchase-profile` (optional `as_of`); simulation may be CLI-first with README

### D9 — KPI gates (fail closed)
After sim + clean + final build-dataset (+ split):
- `rows ≥ 20000`
- `positive_rate ∈ [0.12, 0.25]`
- `% cross_domain > 0 ≥ 15%`
- `% hashtag_match_score > 0 ≥ 40%`
- median `request_id` group size ~20
- positives present in train, val, and test
- ≥60% users have ≥1 COMPLETED order
- purchase profile respects `T_cut`

### D10 — Idempotency & safety
- Fixed UUID namespaces; seed/sim guarded by env flag (e.g. `RECSYS_SIM_ALLOW=1`) and/or explicit `--dev-only`
- Re-run clears or upserts sim-tagged rows; MUST NOT target production DSNs without hard fail

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Engagement count snapshot leak | Keep counts moderate; document in design/README |
| Static profile milder than per-row PIT | DoD = as-of `T_cut`; optional PIT if order events wired |
| `(user,post)` dedupe drops revisits | Never re-impress seen posts; size pool accordingly |
| Exact hashtag/search match | Search keywords = hashtag vocab only |
| Stock=1 burns products | Seed 400 products; limit purchases ~200–350 |
| Direct insert skips online validators | Validate required FKs/status enums in seed scripts |
| Cross-DB tooling | Offline-only read of Commerce URL; never from Social online path |

## Migration Plan

1. Deploy/code merge to repo (no production migration required).
2. On **dev** stacks only: configure Social + Commerce (+ Auth) URLs; set sim allow flag.
3. Run seed → simulate → clean → build → split → export-purchase-profile(`T_cut`) → rebuild → re-split → train → evaluate.
4. Rollback: drop/truncate sim UUID ranges or restore DB snapshots; remove sim artifacts under `data/`.

## Open Questions

- Exact leaf category UUID additions (if any) beyond current fashion catalog seed — resolve during implementation against `commerce-catalog-seed.md`.
- Whether simulation is CLI-only or also exposed as FastAPI jobs (export-purchase-profile is required as job; sim CLI is acceptable).

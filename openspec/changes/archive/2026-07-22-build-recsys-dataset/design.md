## Context

Phase 1 Recommended Feed already has: Candidate Pool, `PostFeatureBuilder` (6 features; `cross_domain` mocked), ONNX/`RuleBased` serving, impression/seen/`model_artifacts` tables, and `recsys-offline` clean → CSV. Training still needs labeled samples aligned with Java features.

Decisions from explore:
- Mirror Java feature formulas; do not invent a second feature set.
- Label window `[shown_at, shown_at + 24h]` for like/save/comment.
- Feature history point-in-time: events with `created_at < shown_at`.
- Implement real `cross_domain_product_score`.
- No negative sampling this phase.
- Extend `productTags` with `categoryId` + `shopId`.

## Goals / Non-Goals

**Goals:**
- Build `dataset.parquet` from cleaned Social extracts (+ commerce profile for cross-domain).
- One row = impression sample with 6 features + label.
- Java and Python share cross-domain rule after Mongo tag enrichment.
- FastAPI job to run build-dataset offline.

**Non-Goals:**
- Negative sampling.
- LightGBM train/evaluate/export (follow-up).
- Online Python inference.
- Backfilling old Mongo posts with categoryId/shopId (optional; missing → score 0).
- Changing recommend API response contract.

## Decisions

### D1 — Input = cleaned files, not live dirty DB

- Prefer `RECSYS_DATASET_OUTPUT_DIR` CSVs from clean job; optionally re-extract if configured.
- Why: single clean contract; build focuses on join/label/feature.

### D2 — Base samples = impressions only

- Each `post_impression_log` row → one training sample after join to post.
- Drop if post missing / not usable.
- `label=0` when no positive interaction in window → natural negatives; **no** synthetic negatives.

### D3 — Label definition

```
label = 1 iff exists like OR save OR comment
  by same user_id on same post_id
  with created_at in [shown_at, shown_at + 24 hours]
else 0
```

Comment = comment on that post (`comments.post_id`).

### D4 — Feature parity with `PostFeatureBuilder`

| Feature | Formula (serve) | Train adaptation |
|---------|-----------------|------------------|
| recency | `2^(-Δ/7d)`, Δ = now − created_at | Δ = shown_at − created_at |
| engagement | log likes + 2 log comments; min-max in batch | same; min-max per `request_id` group if present, else per-user batch or global train set (document choice: prefer `request_id`) |
| hashtag_match | search 1.0 / saved 0.8 / liked 0.4; min-max | history tags/keywords only from events `< shown_at` |
| author_affinity | follow + 0.5 likedAuthor + 0.6 savedAuthor; min-max | same cutoff |
| mutual_follow | direct follow 1.0 else Jaccard | follows accepted before shown_at |
| cross_domain | **new** overlap categoryId/shopId | user profile from orders/cart before shown_at |

### D5 — Cross-domain score (shared)

```
user_cats, user_shops = categories/shops from user's orders (+ cart) before t
post_cats, post_shops = from productTags on candidate post

score = 0.0
if post has no tags: 0.0
else:
  cat_hit = 1.0 if intersection(user_cats, post_cats) else 0.0
  shop_hit = 1.0 if intersection(user_shops, post_shops) else 0.0
  score = 0.6 * cat_hit + 0.4 * shop_hit  # clipped to [0,1]; locked in tasks.md
```

**Locked formula for implementation:**

```
raw = 0.6 * has_category_overlap + 0.4 * has_shop_overlap
score = raw  # already in [0,1]
```

Java serving builds user profile at request time (recent orders/cart via read API or local projection). Python uses cleaned commerce extract or SQL read-only. If commerce unavailable, score = 0.0 and log warning (dataset still builds).

### D6 — Mongo productTags

- Add optional `categoryId`, `shopId` on snapshot; keep `category`, `name`, `imageUrl`, `price`, `available`, `productId`.
- Populate on Create/Edit when resolving product from Commerce.
- Old docs without IDs remain valid; cross_domain treats missing IDs as no overlap.

### D7 — Output schema

```
user_id, post_id, shown_at,
recency_score, engagement_score, hashtag_match_score,
author_affinity_score, mutual_follow_score, cross_domain_product_score,
label
optional: model_version, request_id, rank_position
```

File: `{output_dir}/dataset.parquet` (+ optional `dataset_meta.json` with counts, pos/neg ratio, warnings).

### D8 — Time split (in this change as helper)

- After parquet: scripts/job step writing `train/val/test` by `shown_at` quantiles 70/15/15 (no shuffle).
- Can be same job flag `--split` or separate `/jobs/split-dataset`.

## Risks / Trade-offs

- [Empty impressions] → empty parquet; fail with clear error or empty file + warning — prefer non-zero exit if zero rows when `--require-rows`.
- [Train/serve skew on min-max] → group by `request_id` when available.
- [Commerce coupling] → graceful score 0 if commerce down; document env `COMMERCE_POSTGRES_URL` optional.
- [Old posts without categoryId] → lower cross_domain signal until new tags written.
- [Comment soft-delete] → only ACTIVE comments count for label.

## Migration Plan

1. Deploy Social code that accepts/persists `categoryId`/`shopId` on productTags (backward compatible).
2. Deploy Java `PostFeatureBuilder` cross_domain (profile source).
3. Ship Python build-dataset + `/jobs/build-dataset`.
4. Run clean → build-dataset on env with impressions.
5. Rollback: feature score can revert to 0.0; parquet job undeploy independent of serve.

## Open Questions

- Exact Commerce tables/columns for profile (orders vs order_items + products.category_id/shop_id) — resolve at apply by reading commerce schema.
- Whether user purchase profile is cached in Social vs live Commerce read at recommend time (perf); Phase 1 may use lightweight JDBC/API.

## Why

The Recommended Feed offline pipeline (`clean → build-dataset → split → train → evaluate`) only has tiny fixtures (~2 impressions). Without a large, fashion-aligned, temporally consistent simulation corpus, LightGBM cannot be trained or evaluated meaningfully, and `cross_domain_product_score` stays near zero because cleaned posts drop `product_tags` and Commerce purchase profiles are not exported. We need a repeatable seed + persona + bot simulation for Fashion Social + Fashion Commerce so train/evaluate results are trustworthy for the MVP vertical.

## What Changes

- Add a **dev-only** simulation package that:
  - Seeds Auth / Social / Commerce with fixed UUIDs (direct DB inserts; no HTTP recommend path)
  - Defines fashion personas + affinity matrix (hashtag, leaf category, ± product tags)
  - Runs a sim-clock bot timeline: impression → engage → follow/search → cart → purchase
  - Targets **≥ 20k** unique `(user_id, post_id)` impressions (early sessions count toward the 20k)
- Fix `clean_posts` so cleaned posts **retain `product_tags`** (`categoryId` / `shopId`) for cross-domain features
- Add Commerce → `user_purchase_profile` export with **as-of train cutoff** (`T_cut` from time split) so purchases after the train boundary do not leak into features
- Add distribution **KPI gates** after seed/sim (rows, positive rate, cross_domain coverage, hashtag coverage, orders per user, etc.)
- Keep MVP positioning: **Fashion Social + Fashion Commerce only** (no laptop/gaming vertical expansion)

## Capabilities

### New Capabilities
- `recsys-simulation-seed`: Persona/affinity config, skeleton seed volumes, bot simulation timeline, ranking-by-affinity, and post-seed KPI gates for Recommended Feed training data
- `recsys-purchase-profile-export`: Offline export of `user_purchase_profile` from Commerce orders/carts with as-of train cutoff semantics for build-dataset

### Modified Capabilities
- `recsys-dataset-clean`: Cleaned posts output MUST preserve product-tag commerce identifiers needed for `cross_domain_product_score`
- `recsys-offline-ops`: Add offline job/CLI hooks for purchase-profile export (and optional simulation/KPI entrypoints) without online predict APIs
- `recsys-build-dataset`: Consume exported purchase profile under as-of/`T_cut` rules so cross-domain features are non-trivial and temporally safe relative to the train cut

## Impact

- **New code**: `Services/recsys-offline` scripts/pipelines for seed, simulate, KPI report, purchase-profile export; config for personas/affinity
- **Modified**: `pipelines/normalize.py` (`clean_posts`), possibly `clean_data.py` / `extract.py` / `build_dataset.py` / `app/main.py` / `app/config.py`
- **Databases (dev only)**: Auth (`users` + related), Social (Mongo posts/comments + Postgres likes/saves/follows/search/impressions/seen), Commerce (shops/products/inventory/carts/orders)
- **Docs**: `recsys-offline/README.md` workflow; optional note in catalog seed if leaf/hashtag vocab is extended within fashion
- **Out of scope**: HTTP RecommendPosts bot path; commerceHome hybrid recsys; expanding vertical beyond fashion; production seeding

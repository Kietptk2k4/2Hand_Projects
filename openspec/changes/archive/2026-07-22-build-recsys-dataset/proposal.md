## Why

Recommended Feed offline training needs labeled `(user, post)` samples with the same six ranking features Java uses at serve time. Clean entity CSVs already exist, but there is no build-dataset step to join impressions, apply a 24h interaction label, compute point-in-time features (including a real `cross_domain_product_score`), or export `dataset.parquet`. Without this, LightGBM train/evaluate cannot start.

## What Changes

- Add offline **Build Dataset** pipeline in `recsys-offline`: load cleaned extracts → base on `post_impression_log` → join posts/engagements/follows/search/(commerce profile) → label in `[shown_at, shown_at+24h]` → features mirroring `PostFeatureBuilder` with history cutoff `< shown_at` → export `dataset.parquet`.
- **No negative sampling** in this phase (impression `label=0` is the negative class).
- Extend Mongo `posts.productTags` snapshot with `categoryId` and `shopId` (keep existing `category` text); wire Create/Edit Post snapshot from Commerce when tagging products.
- Implement real **`cross_domain_product_score`** in Java `PostFeatureBuilder` and the same formula in Python build-dataset (stop mocking `0.0`).
- Expose `POST /jobs/build-dataset` on offline FastAPI (still not used on recommend path).
- Optional time-based split helpers (70/15/15 by `shown_at`) after parquet export.

## Capabilities

### New Capabilities

- `recsys-build-dataset`: Offline job that builds labeled training rows and exports `dataset.parquet` with six features + label, point-in-time safe, no negative sampling.
- `recsys-cross-domain-affinity`: Product-tag snapshot enrichment (`categoryId`, `shopId`) and real cross-domain score shared by Java serving and Python training.

### Modified Capabilities

- `recsys-offline-ops`: Add build-dataset job trigger alongside existing clean/train stubs (still no online predict).

## Impact

- **Python** `Services/recsys-offline`: new pipeline module, parquet dependency (`pyarrow`), job route, unit tests for label window and feature formulas.
- **Social** Mongo document / domain `ProductTag`, Create/Edit post product snapshot mapping; `PostFeatureBuilder` cross-domain implementation (may read user purchase/cart affinity from Social projection or Commerce read-only query — design TBD without cross-DB writes).
- **Commerce** (read-only for affinity): orders/cart used to build user category/shop profile; no schema ownership change required if Social only stores tag snapshot.
- **Depends on**: cleaned outputs + populated `post_impression_log` (and bot/seed later for volume).
- **Non-goals**: negative sampling, online Python inference, full LightGBM train (next change), Kafka affinity consumer (optional later if replaced by direct profile build).

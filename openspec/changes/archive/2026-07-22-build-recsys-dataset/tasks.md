## 1. Product tag snapshot (Mongo / Social)

- [x] 1.1 Extend domain `ProductTag` and `PostDocument.ProductTagDocument` with optional `categoryId` and `shopId`
- [x] 1.2 Map Create/Edit Post product-tag snapshot to populate `categoryId`/`shopId` from Commerce product (or existing snapshot fields when already provided)
- [x] 1.3 Update API/docs behavior notes so FE/docs know new optional snapshot fields; ensure legacy posts without IDs still deserialize
- [x] 1.4 Unit tests for ProductTag mapping with and without new fields

## 2. Cross-domain feature (Java serving)

- [x] 2.1 Define user purchase/cart profile loader (read-only Commerce or projection) returning categoryId and shopId sets
- [x] 2.2 Implement `cross_domain_product_score = 0.6 * category_overlap + 0.4 * shop_overlap` in `PostFeatureBuilder` (replace mock 0.0)
- [x] 2.3 Unit tests: category hit, shop hit, no profile → 0, no productTags → 0

## 3. Build dataset pipeline (Python)

- [x] 3.1 Add `pyarrow` (or equivalent) to `recsys-offline` requirements for parquet export
- [x] 3.2 Implement load of cleaned impressions, posts, likes, saves, comments, follows, search_history (+ optional commerce extract)
- [x] 3.3 Implement label assignment: like/save/comment in `[shown_at, shown_at+24h]`; no negative sampling
- [x] 3.4 Implement point-in-time feature builder mirroring `PostFeatureBuilder` (recency vs `shown_at`, history `< shown_at`, min-max per `request_id` when present)
- [x] 3.5 Implement cross_domain in Python with the same 0.6/0.4 formula
- [x] 3.6 Export `dataset.parquet` (+ summary JSON: rows, positive rate, warnings)
- [x] 3.7 Unit tests: 24h label window, pre-impression like → label 0, history after shown_at excluded from features, parquet schema columns

## 4. Offline job API

- [x] 4.1 Add `POST /jobs/build-dataset` invoking the pipeline; fail closed on missing inputs
- [x] 4.2 Update README with build-dataset config and output path
- [x] 4.3 API/smoke test for endpoint success/failure paths

## 5. Time split helper

- [x] 5.1 Add split-by-`shown_at` 70/15/15 writer (train/val/test parquet or partitions) without random shuffle
- [x] 5.2 Document how to run split after build (flag or `/jobs/split-dataset`)

## 6. Verification

- [x] 6.1 Run clean → build-dataset on fixture or local data; confirm parquet + label distribution
- [x] 6.2 Confirm recommend path still does not call FastAPI; cross_domain unit tests pass in social-service

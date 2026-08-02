## 1. Contracts and schema

- [x] 1.1 Define shared `HOME_FEATURE_ORDER` (15 names, exact order) in Python + Java with export `feature_order.json` and mismatch guard tests
- [x] 1.2 Implement `PopularityNormalizer` from artifact `(z_lo,z_hi)`; raw=`completed_order_items` + log1p; forbid pool batch min-max; load with model activate
- [x] 1.3 Add Commerce schema with **required columns/indexes** (not inventing types ad hoc): `entity_cooccur`; `social_tag_category_ar`; `user_social_interest_export`; `home_impression_log` (+ provenance `sources`, `personal_score`, `cf_score`, `ar_score`); `home_engage_event`; Commerce `model_artifacts` (D20 columns)
- [x] 1.4 Document social export→`user_social_interest_export` contract and UserInterestProfile math
- [x] 1.5 Python↔Java FeatureBuilder parity fixtures (same input → same 15-vector / order / PIT); provenance round-trip fixture

## 2. Offline: Entity CF + social export + AR (Batch 3)

- [x] 2.1 Entity co-occur job: mode-aware inputs; window **180d** (real); COMPLETED ×**1.0**; cart **24h** ×**0.6**; `score=log1p`; top-**M=50**; `LEAF_CATEGORY`/`BRAND`; write files and/or `entity_cooccur`
- [x] 2.2 Social interest export: mode-aware; **4/3/2/1**, **90d**, decay; daily default; files and/or `user_social_interest_export`
- [x] 2.3 AR mining: mode-aware; **Apriori** (default) or FP-Growth; **90d** baskets; **tag→category**; mins **0.01/0.05**; files and/or `social_tag_category_ar`
- [x] 2.4 recsys-offline entrypoints + **retrain orchestrator** order: resolve mode → (sim) → CF → social export → AR → load → dataset → split → train → evaluate → export-activate

## 3. Offline: Home LTR (Batch 3 + 5)

- [x] 3.1 Build-dataset: mode-aware; reconstruct CandidateProduct from **logged provenance**; PIT profile; labels 24h nearest; split 80/10/10; fit `(z_lo,z_hi)` on train; `REAL_ONLY` fail-closed
- [x] 3.2 Train LightGBM binary; HYBRID `seed_row_weight`; evaluate **AUC** + **P@10 mean by `request_id`** vs baseline `0.7*pop+0.3*rec`; record mode in meta
- [x] 3.3 export-activate `commerce_home_ranker` into Commerce `model_artifacts`; soft-reject keeps prior
- [x] 3.4 Unit/integration: label/nearest/split/gate; P@10 grouping; retrain step order; provenance→features; mode fail-closed; formula fixtures

## 3b. Offline: train-data mode + Home sim (Batch 4)

- [x] 3b.1 Admin: seed `system_configs` keys `commerce.home.ltr.train_data_mode` (default SEED_ONLY), `seed_row_weight` (0.5), `real_only_min_impressions` (5000); editable on system-configs tab; orchestrator reads via Admin list+exact key (`SYSTEM_CONFIG_VIEW`)
- [x] 3b.2 Home sim CLI: personas + **in-memory niche catalog** → impression/engage/CF/AR/export **files** under `RECSYS_HOME_SIM_DIR`; **forbid** Auth/Social/Commerce shared-DB inserts (unlike Post `seed-db`)
- [x] 3b.3 Mode resolver: Admin HTTP (or env when `RECSYS_HOME_CONFIG_FROM_ENV=1`); HYBRID tag `data_source` + seed weight; HYBRID CF **max** merge; REAL_ONLY min rows fail-closed
- [x] 3b.4 `load-artifact` upserts `entity_cooccur`, `user_social_interest_export`, `social_tag_category_ar` from files — no fake users/posts/orders/impressions
- [x] 3b.5 Tests: SEED_ONLY no DB writers; HYBRID union+weights+CF max; REAL_ONLY thin fail; load-artifact table-only; invalid enum rejected

## 4. Commerce online: profile + retrieval

- [x] 4.1 Build UserInterestProfile: commerce 180d math; social from `user_social_interest_export` + max-norm; price percentiles ≥3 samples
- [x] 4.2 Implement retrieval A/B/E/C → `CandidateProduct`; caps 100/150/150/150; no D; exclude own-shop **and vacation shops**
- [x] 4.3 Source A slices 40/40/20 (New / Popular-90d / Rating count≥3)
- [x] 4.4 Source B/E/C as Batch-1 locks (personalScore; cfScore max-norm; arScore clip conf×tag; ≤20/entity|cat)
- [x] 4.5 Reject unauthenticated callers (no guest path)

## 5. Commerce online: rank + diversity + API

- [x] 5.1 Feature builder: CandidateProduct + profile + PopularityNormalizer → 15 normative formulas; LightGBM sort score DESC + tie `created_at` DESC / `product_id` ASC
- [x] 5.2 HomeModelLoader: PostConstruct + reload cron + MODEL_ROOT basename; feature_order + PopularityNormalizer; fail → degraded_key=`0.7*pop+0.3*rec`
- [x] 5.3 Diversity re-rank: configurable greedy hard-cap; backfill; enabled flag; Top K
- [x] 5.4 `GET .../home/recommendations`; flag→404; empty→[]; request_id + ranking_mode; **min card** id/title/price/thumbnail/shop/rating; async impressions **with provenance**
- [x] 5.5 Attributed PDP click (`from=home`) → async CLICK engage
- [x] 5.6 Unit tests: ModelLoader; vacation; social empty→no C; provenance logged; tie-break deterministic; Batch-1 retrieval/API cases

## 6. Clients and docs

- [x] 6.1 Wire FE Commerce Home “Đề xuất” rail to Top 50 API; PDP `from=home` + `request_id`
- [x] 6.2 Model Registry tab: shared `systemOperations` / `model-registry` with selector **Social For You (`feed_ranker`)** vs **Commerce Home (`commerce_home_ranker`)**; Social APIs vs new Commerce admin artifacts/status APIs; read-only (no activate from UI)
- [x] 6.3 API FE behavior + architecture note: Hybrid = Rules + Entity CF + Cross-domain AR + LTR + Diversity (semantic deferred)
- [ ] 6.4 Verify: authed Home ≤50; flag-off → 404; empty → []; PDP request_id; registry switches model without mixing lists

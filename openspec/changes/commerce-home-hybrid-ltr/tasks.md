## 1. Contracts and schema

- [ ] 1.1 Define Home feature order constant (shared doc/code) including cf_score + multi-hot source flags
- [ ] 1.2 Add Commerce schema for entity co-occur, AR mapping consumer table, Home impression/engage log, and Home model artifact registry
- [ ] 1.3 Document Social interest export/internal read contract (weights search×4, save×3, comment×2, like×1)

## 2. Offline: Entity CF + AR bridge

- [ ] 2.1 Implement entity co-occurrence job (category/brand; completed×1.0 + cart×0.6) writing scores table
- [ ] 2.2 Implement AR mining job interest_tag→category with support/confidence publish
- [ ] 2.3 Add recsys-offline job/CLI entrypoints for CF + AR (no online predict)

## 3. Offline: Home LTR

- [ ] 3.1 Build Home training rows from impressions + binary engage labels (click∪cart∪buy, 24h window default)
- [ ] 3.2 Train/evaluate/export-activate pipeline for `commerce_home_ranker` with gate vs rule baseline and soft-reject
- [ ] 3.3 Unit tests for label positivity (cart-only) and feature column order

## 4. Commerce online: profile + retrieval

- [ ] 4.1 Build UserInterestProfile (commerce facets + social interests from export) with provenance
- [ ] 4.2 Implement soft-quota retrieval A/B/E/C + semantic fill, inventory hard filters, dedupe with multi-source flags
- [ ] 4.3 Guest path: Popular/new/rating only → pool → rank/degraded → Top 50

## 5. Commerce online: rank + diversity + API

- [ ] 5.1 Feature builder for `(user, product)` matching locked Home feature order
- [ ] 5.2 ONNX Home ModelLoader + degraded popularity/recency fallback when unloaded
- [ ] 5.3 Diversity re-rank after scores; return Top 50
- [ ] 5.4 HTTP recommend API in commerce-service + feature flag; log impressions with model version
- [ ] 5.5 Unit tests: LightGBM does not expand pool; multi-source flags; guest vs authed

## 6. Clients and docs

- [ ] 6.1 Wire FE (and mobile if in scope) Commerce Home “Đề xuất” rail to Top 50 API
- [ ] 6.2 API FE behavior + README/architecture note: Hybrid = Rules + Entity CF + Content + Cross-domain AR + LTR + Diversity
- [ ] 6.3 Verify: authed Home returns ≤50 sellable products; flag-off restores prior list behavior

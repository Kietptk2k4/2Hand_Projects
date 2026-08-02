# Commerce Home Hybrid Recommend

Hybrid Home ranking for authenticated shoppers:

**Rules (A) + Entity CF (E) + Cross-domain AR (C) + LightGBM LTR + Diversity**

Semantic retrieval (source D) is deferred.

## Serving

- Owner: **commerce-service** (not social, not recsys-offline online)
- API: `GET /commerce/api/v1/home/recommendations` (auth required)
- Feature flag: `commerce.home.recommend.enabled` — when false → **404**
- Empty candidate pool → **200** with `items: []`
- Top K default **50** after diversity re-rank
- Ranking modes: `LIGHTGBM` or `DEGRADED` (`0.7*pop + 0.3*rec`)
- Impressions logged async with provenance (`sources`, `personal_score`, `cf_score`, `ar_score`)
- PDP attribution: `GET /commerce/api/v1/products/{id}?from=home&request_id=...` → async CLICK engage

## Offline

- Jobs under `Services/recsys-offline`: home-sim, CF/AR/export, load-artifact, build-dataset, train, evaluate, home-export-activate
- Retrain orchestrator order: resolve mode → (sim) → CF → social export → AR → load → dataset → split → train → evaluate → export-activate
- Train modes (`SEED_ONLY` | `HYBRID` | `REAL_ONLY`) via Admin `system_configs` keys `commerce.home.ltr.*`
- Model name: `commerce_home_ranker` (Commerce `model_artifacts`); Social remains `feed_ranker`

## Admin UI

Shared Model Registry tab (`admin?section=systemOperations&tab=model-registry`) with selector:

- Social For You → `feed_ranker` (Social admin APIs)
- Commerce Home → `commerce_home_ranker` (Commerce admin APIs)

Read-only; no activate from UI.

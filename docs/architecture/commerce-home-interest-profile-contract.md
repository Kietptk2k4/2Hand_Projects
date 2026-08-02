# Commerce Home — social interest export & UserInterestProfile (contract note)

Source of truth: `openspec/changes/commerce-home-hybrid-ltr/design.md` (D3, D13).

## Social → Commerce export

Offline job writes Commerce table `user_social_interest_export`:

| Column | Notes |
|--------|--------|
| user_id, tag_type (`HASHTAG`\|`KEYWORD`), tag | PK |
| score | weighted 4/3/2/1 then decayed \(2^{-\Delta/14d}\) at export `as_of` |
| window_days | default 90 |
| computed_at, as_of | timestamps |

Online Home profile **max-norms** scores within each `tag_type` and MUST NOT call Social HTTP on the recommend hot path.

## UserInterestProfile (commerce facets)

- Window 180d before `as_of`
- COMPLETED lines ×1.0 × \(2^{-\Delta/30d}\); cart ×0.6 × same decay (`cart_items` MVP excluding REMOVED)
- Max-norm; keep top 20 categories, 20 brands, 10 shops
- Price p25/p50/p75 from COMPLETED `effective_price`; &lt;3 samples → missing (feature `price_affinity` = 0.5)

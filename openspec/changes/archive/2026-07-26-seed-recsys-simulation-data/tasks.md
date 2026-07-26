## 1. Pipeline fix — product tags in clean

- [x] 1.1 Update `clean_posts` to retain `product_tags` / `productTags` with `categoryId`/`shopId`/`productId` when present
- [x] 1.2 Add/adjust unit tests proving tagged posts keep identifiers and untagged posts still clean
- [x] 1.3 Verify `build_dataset.parse_product_tag_ids` still reads the cleaned field names

## 2. Purchase profile export (as-of T_cut)

- [x] 2.1 Add `COMMERCE_POSTGRES_URL` (or equivalent) to `app/config.py` and document in README
- [x] 2.2 Implement export that aggregates COMPLETED orders → `user_purchase_profile.csv` (`user_id`, `category_ids`, `shop_ids`)
- [x] 2.3 Support optional `as_of` / `T_cut` filter (`completed_at <= T_cut`)
- [x] 2.4 Wire `POST /jobs/export-purchase-profile` (fail closed if Commerce URL missing)
- [x] 2.5 Add unit/integration tests for cutoff exclusion and no Commerce mutation

## 3. Persona + affinity config

- [x] 3.1 Create `config/personas.yaml` (5 fashion personas, affinity matrix, engage probabilities, hashtag vocab, category UUID maps)
- [x] 3.2 Map personas only to fashion catalog leaves (extend leaf/hashtag vocab within fashion if needed; no laptop/gaming)
- [x] 3.3 Document search keywords MUST equal normalized hashtag tokens

## 4. Skeleton seed (direct insert, fixed UUIDs)

- [x] 4.1 Implement Auth user seed (~120 bots + roles/profiles as required for valid users)
- [x] 4.2 Implement Commerce seed (~40 shops, ~400 ACTIVE products stock=1, prices/inventory)
- [x] 4.3 Implement Social post seed (~600 posts; ~50% with productTags; authors ~40)
- [x] 4.4 Guard all seed writers with `RECSYS_SIM_ALLOW` (or equivalent); idempotent/upsert strategy
- [x] 4.5 Add smoke test or dry-run that validates counts without requiring full sim

## 5. Bot simulation (sim clock)

- [x] 5.1 Implement shared sim clock (~21 days) and session loop (12 × feed 20 per user)
- [x] 5.2 Affinity-weighted feed sampling + mismatch mix; write `post_impression_log` + `user_seen_posts` (`request_id`, `rank_position`, `model_version=NULL`)
- [x] 5.3 Engage like/save/comment with timestamps in `[shown_at, shown_at+24h)`; follow + search from hashtag vocab
- [x] 5.4 Cart + COMPLETED purchase path (≥60% users with ≥1 order); respect stock=1
- [x] 5.5 Ensure early sessions still create impressions (count toward ≥20k)
- [x] 5.6 Never re-impress `(user, post)` already in `user_seen_posts`

## 6. KPI gate + end-to-end workflow

- [x] 6.1 Implement KPI gate script/job (rows, positive_rate, cross_domain%, hashtag%, group size, split class presence, buyer%, T_cut respect)
- [x] 6.2 Document operator workflow: seed → sim → clean → build → split → export-purchase-profile(`T_cut`) → rebuild → re-split → train → evaluate → KPI
- [x] 6.3 Update `Services/recsys-offline/README.md` with volumes, env vars, safety flag, and DoD
- [x] 6.4 Run one full dev pass (or fixture-scale smoke + documented scale command) and record that KPI thresholds are met or list gaps

## 7. Build-dataset final-cutoff wiring

- [x] 7.1 Ensure final build consumes cutoff-filtered `user_purchase_profile.csv` per design D7
- [x] 7.2 Add test or assertion that post-cutoff purchases do not appear in profile used for final dataset
- [x] 7.3 Confirm sample with matching tagged category yields `cross_domain_product_score >= 0.6`

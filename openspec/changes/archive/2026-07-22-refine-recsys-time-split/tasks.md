## 1. Core split logic

- [x] 1.1 Update `split_rows` to 80/10/10 cuts (`int(n*0.80)`, `int(n*0.90)`) with stable sort on `(shown_at, user_id, post_id)`
- [x] 1.2 Implement temporal leak assertions between non-empty adjacent splits; raise on violation
- [x] 1.3 Compute overlap report (Jaccard user/post for train∩val, train∩test, val∩test) and per-split positive rates; allow empty slices with warnings
- [x] 1.4 Persist enriched `split_meta.json` and return the same fields from `run_split_dataset`

## 2. API and docs

- [x] 2.1 Ensure `/jobs/split-dataset` surfaces temporal failures as 400/structured error and success summary includes overlap/leak fields
- [x] 2.2 Update `Services/recsys-offline/README.md` from 70/15/15 to 80/10/10 + leak/overlap notes

## 3. Tests and verification

- [x] 3.1 Unit tests: ratio on n≥10, sort order preserved, temporal violation fails, overlap math, tiny-n warning path
- [x] 3.2 Smoke: run split on fixture `dataset.parquet`; confirm meta + partitions

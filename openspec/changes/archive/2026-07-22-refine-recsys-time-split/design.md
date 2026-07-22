## Context

`Services/recsys-offline` already builds `dataset.parquet` and exposes `POST /jobs/split-dataset`. The helper currently sorts by `shown_at` and cuts **70/15/15** without asserting temporal integrity or reporting entity overlap. Explore decisions for Phase 1 LightGBM prep locked: **80/10/10**, temporal fail-closed checks, and overlap reporting (informational).

Constraints:
- Offline only — Social recommend path unchanged.
- No shuffle; split by row-count quantiles after time sort (not calendar absolute cut).
- Tiny fixture datasets may yield empty val/test — warn, do not invent rows.

## Goals / Non-Goals

**Goals:**
- Replace 70/15/15 with 80/10/10 train/val/test.
- Stable sort: `shown_at` ASC, tie-break `user_id`, `post_id`.
- Fail closed on temporal leak between adjacent splits.
- Write overlap % and positive rates into `split_meta.json` / job result.
- Unit tests covering ratio, order, leak failure, and overlap math.
- Docs/README aligned.

**Non-Goals:**
- LightGBM train/evaluate/export.
- User-disjoint or post-disjoint mandatory splits.
- Absolute time cut (`shown_at < T`).
- Changing build-dataset labels/features.

## Decisions

### D1 — Ratio 80/10/10 by sample count after sort
- **Choice:** `train_end = int(n * 0.80)`, `val_end = int(n * 0.90)`.
- **Why:** Matches graduation checklist (train-heavy) while keeping val for early stopping later.
- **Alt:** 80/20 only (no val) — rejected; val useful before train. 70/15/15 — superseded.

### D2 — Temporal leak = fail; entity overlap = report
- **Choice:** Assert `max(train.shown_at) ≤ min(val.shown_at)` and `max(val.shown_at) ≤ min(test.shown_at)` when both sides non-empty. Overlap of users/posts is logged only.
- **Why:** Time-travel across the cut invalidates evaluation; same user in train+test is expected for feed ranking.

### D3 — Stable tie-break
- **Choice:** Sort key `(shown_at, user_id, post_id)`.
- **Why:** Equal timestamps otherwise make adjacent-split membership non-deterministic.

### D4 — Empty slices on small n
- **Choice:** Allow empty val/test; add `warnings` (e.g. `empty_val`, `empty_test`, `small_n`).
- **Why:** Fixtures often have few rows; hard `n >= 10` blocks smoke tests.

### D5 — Meta payload
- **Choice:** `split_meta.json` includes counts, time ranges per split, positive rates, `user_overlap_*_pct`, `post_overlap_*_pct` for train∩val, train∩test, val∩test (denominator = |union| or |left|+|right| — lock: **Jaccard-style |A∩B|/|A∪B| * 100** when union non-empty else 0).
- **Why:** Single artifact for thesis appendix / operator review.

## Risks / Trade-offs

- [Skewed timeline] Count-based cut may put a short/long window in test → Accept for Phase 1; document calendar cut as future option.
- [Ties at boundary] Same `shown_at` on both sides of cut can pass `max(train) ≤ min(val)` while mixing concurrent impressions → Mitigate with stable tie-break; optional future: keep equal timestamps wholly in one side.
- [BREAKING ratio] Downstream notebooks assuming 70/15/15 break → Update README; bump job summary fields.

## Migration Plan

1. Deploy updated `split_dataset` + tests.
2. Re-run `/jobs/split-dataset` on existing `dataset.parquet` to regenerate partitions + meta.
3. Rollback: revert package; old 70/15/15 behavior returns (no DB migration).

## Open Questions

- None blocking; calendar-day cut deferred.

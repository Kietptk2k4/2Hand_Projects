## Why

The current split helper uses a **70/15/15** train/val/test cut without temporal leak assertions or entity-overlap reporting. Before LightGBM training, the offline pipeline needs a clearer time-ordered contract (**80/10/10**), fail-closed temporal leak checks, and informational overlap metrics so evaluation is trustworthy for the graduation Recommended Feed milestone.

## What Changes

- Change `POST /jobs/split-dataset` (and `split_dataset.py`) from **70/15/15** to **80/10/10** by `shown_at` after a stable sort (no shuffle).
- Add **BREAKING** output semantics relative to the prior helper: ratio boundaries move; callers/docs that assumed 70/15/15 must update.
- Fail closed when temporal order is violated (`max(train.shown_at) ≤ min(val.shown_at) ≤ max(val)…` / `max(val) ≤ min(test)`).
- Emit an **overlap report** (user/post overlap % across splits) and positive-rate per split into `split_meta.json` / job summary (informational; entity overlap does not fail the job).
- Allow empty val/test slices on tiny datasets with an explicit warning (Phase 1 fixtures).
- Update README and specs to match the new contract.

## Capabilities

### New Capabilities

- `recsys-time-split`: Time-ordered train/val/test split of `dataset.parquet` with 80/10/10 ratios, temporal leak guards, and overlap reporting.

### Modified Capabilities

- `recsys-offline-ops`: Document/require the split-dataset job behavior under the new 80/10/10 + leak-check contract (replace any implied 70/15/15 helper wording in docs/ops expectations).

## Impact

- **Code:** `Services/recsys-offline/pipelines/split_dataset.py`, tests, `README.md`, possibly job response fields in `app/main.py`.
- **Artifacts:** `dataset_train/val/test.parquet`, `split_meta.json` under `RECSYS_DATASET_OUTPUT_DIR`.
- **Downstream:** Future LightGBM train/eval jobs consume the new splits; no Social Service / online recommend path changes.
- **Non-goals:** LightGBM training, user-disjoint split, calendar-day absolute cut (`shown_at < T`).

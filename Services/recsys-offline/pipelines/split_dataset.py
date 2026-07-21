"""Time-based train/val/test split by shown_at (70/15/15, no shuffle)."""

from __future__ import annotations

import json
import logging
from pathlib import Path
from typing import Any

from app.config import Settings, get_settings
from pipelines.features import _parse_ts

logger = logging.getLogger(__name__)


def split_rows(rows: list[dict[str, Any]]) -> dict[str, list[dict[str, Any]]]:
    sortable = []
    for row in rows:
        ts = _parse_ts(row.get("shown_at"))
        if ts is None:
            continue
        sortable.append((ts, row))
    sortable.sort(key=lambda item: item[0])
    ordered = [row for _, row in sortable]
    n = len(ordered)
    if n == 0:
        return {"train": [], "val": [], "test": []}
    train_end = int(n * 0.70)
    val_end = int(n * 0.85)
    # ensure at least empty-safe slices
    return {
        "train": ordered[:train_end],
        "val": ordered[train_end:val_end],
        "test": ordered[val_end:],
    }


def run_split_dataset(settings: Settings | None = None) -> dict[str, Any]:
    settings = settings or get_settings()
    input_dir = Path(settings.recsys_dataset_output_dir)
    dataset_path = input_dir / "dataset.parquet"
    if not dataset_path.exists():
        raise ValueError(f"dataset.parquet not found at {dataset_path}")

    try:
        import pyarrow.parquet as pq
    except ImportError as exc:
        raise RuntimeError("pyarrow is required for split-dataset") from exc

    table = pq.read_table(dataset_path)
    rows = table.to_pylist()
    parts = split_rows(rows)

    for name, part_rows in parts.items():
        out = input_dir / f"dataset_{name}.parquet"
        if part_rows:
            import pyarrow as pa

            pq.write_table(pa.Table.from_pylist(part_rows), out)
        else:
            import pyarrow as pa

            pq.write_table(pa.table({"user_id": pa.array([], type=pa.string())}), out)

    summary = {
        "total": len(rows),
        "train": len(parts["train"]),
        "val": len(parts["val"]),
        "test": len(parts["test"]),
        "output_dir": str(input_dir.resolve()),
    }
    (input_dir / "split_meta.json").write_text(
        json.dumps(summary, indent=2), encoding="utf-8"
    )
    logger.info("Split dataset: %s", summary)
    return summary

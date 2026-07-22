"""Time-based train/val/test split by shown_at (80/10/10, no shuffle)."""

from __future__ import annotations

import json
import logging
from datetime import datetime
from pathlib import Path
from typing import Any

from app.config import Settings, get_settings
from pipelines.features import _parse_ts

logger = logging.getLogger(__name__)

TRAIN_RATIO = 0.80
VAL_RATIO = 0.10  # test = remainder (~0.10)


def _sort_key(row: dict[str, Any]) -> tuple[datetime, str, str]:
    ts = _parse_ts(row.get("shown_at"))
    assert ts is not None
    return (ts, str(row.get("user_id") or ""), str(row.get("post_id") or ""))


def order_rows(rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Sort by shown_at ASC with stable tie-break (user_id, post_id). Drops rows without shown_at."""
    sortable = [row for row in rows if _parse_ts(row.get("shown_at")) is not None]
    sortable.sort(key=_sort_key)
    return sortable


def split_rows(rows: list[dict[str, Any]]) -> dict[str, list[dict[str, Any]]]:
    ordered = order_rows(rows)
    n = len(ordered)
    if n == 0:
        return {"train": [], "val": [], "test": []}
    train_end = int(n * TRAIN_RATIO)
    val_end = int(n * (TRAIN_RATIO + VAL_RATIO))
    return {
        "train": ordered[:train_end],
        "val": ordered[train_end:val_end],
        "test": ordered[val_end:],
    }


def _max_shown_at(rows: list[dict[str, Any]]) -> datetime | None:
    times = [_parse_ts(r.get("shown_at")) for r in rows]
    times = [t for t in times if t is not None]
    return max(times) if times else None


def _min_shown_at(rows: list[dict[str, Any]]) -> datetime | None:
    times = [_parse_ts(r.get("shown_at")) for r in rows]
    times = [t for t in times if t is not None]
    return min(times) if times else None


def assert_temporal_integrity(parts: dict[str, list[dict[str, Any]]]) -> None:
    """Fail closed if a later non-empty split starts before the previous split ends."""
    sequence = ("train", "val", "test")
    prev_name: str | None = None
    prev_max: datetime | None = None
    for name in sequence:
        rows = parts.get(name) or []
        if not rows:
            continue
        cur_min = _min_shown_at(rows)
        cur_max = _max_shown_at(rows)
        if prev_max is not None and cur_min is not None and cur_min < prev_max:
            raise ValueError(
                f"Temporal leak: min({name}.shown_at)={cur_min.isoformat()} "
                f"< max({prev_name}.shown_at)={prev_max.isoformat()}"
            )
        prev_name = name
        prev_max = cur_max


def _jaccard_pct(left: set[str], right: set[str]) -> float:
    if not left and not right:
        return 0.0
    union = left | right
    if not union:
        return 0.0
    return 100.0 * len(left & right) / len(union)


def _ids(rows: list[dict[str, Any]], key: str) -> set[str]:
    return {str(r[key]) for r in rows if r.get(key) is not None and str(r[key]) != ""}


def _positive_rate(rows: list[dict[str, Any]]) -> float | None:
    if not rows:
        return None
    positives = 0
    for row in rows:
        try:
            if int(row.get("label", 0)) == 1:
                positives += 1
        except (TypeError, ValueError):
            continue
    return positives / len(rows)


def _time_range(rows: list[dict[str, Any]]) -> dict[str, str | None]:
    lo = _min_shown_at(rows)
    hi = _max_shown_at(rows)
    return {
        "min_shown_at": lo.isoformat().replace("+00:00", "Z") if lo else None,
        "max_shown_at": hi.isoformat().replace("+00:00", "Z") if hi else None,
    }


def build_split_report(parts: dict[str, list[dict[str, Any]]], total_input_rows: int) -> dict[str, Any]:
    train, val, test = parts["train"], parts["val"], parts["test"]
    warnings: list[str] = []
    usable = len(train) + len(val) + len(test)
    if usable < 10:
        warnings.append("small_n")
    if not val:
        warnings.append("empty_val")
    if not test:
        warnings.append("empty_test")
    if not train:
        warnings.append("empty_train")

    train_users, val_users, test_users = _ids(train, "user_id"), _ids(val, "user_id"), _ids(test, "user_id")
    train_posts, val_posts, test_posts = _ids(train, "post_id"), _ids(val, "post_id"), _ids(test, "post_id")

    return {
        "total_input_rows": total_input_rows,
        "usable_rows": usable,
        "train": len(train),
        "val": len(val),
        "test": len(test),
        "ratio": {"train": TRAIN_RATIO, "val": VAL_RATIO, "test": round(1.0 - TRAIN_RATIO - VAL_RATIO, 2)},
        "positive_rate": {
            "train": _positive_rate(train),
            "val": _positive_rate(val),
            "test": _positive_rate(test),
        },
        "time_range": {
            "train": _time_range(train),
            "val": _time_range(val),
            "test": _time_range(test),
        },
        "user_overlap_pct": {
            "train_val": _jaccard_pct(train_users, val_users),
            "train_test": _jaccard_pct(train_users, test_users),
            "val_test": _jaccard_pct(val_users, test_users),
        },
        "post_overlap_pct": {
            "train_val": _jaccard_pct(train_posts, val_posts),
            "train_test": _jaccard_pct(train_posts, test_posts),
            "val_test": _jaccard_pct(val_posts, test_posts),
        },
        "temporal_ok": True,
        "warnings": warnings,
    }


def run_split_dataset(settings: Settings | None = None) -> dict[str, Any]:
    settings = settings or get_settings()
    input_dir = Path(settings.recsys_dataset_output_dir)
    dataset_path = input_dir / "dataset.parquet"
    if not dataset_path.exists():
        raise ValueError(f"dataset.parquet not found at {dataset_path}")

    try:
        import pyarrow as pa
        import pyarrow.parquet as pq
    except ImportError as exc:
        raise RuntimeError("pyarrow is required for split-dataset") from exc

    table = pq.read_table(dataset_path)
    rows = table.to_pylist()
    parts = split_rows(rows)
    assert_temporal_integrity(parts)

    for name, part_rows in parts.items():
        out = input_dir / f"dataset_{name}.parquet"
        if part_rows:
            pq.write_table(pa.Table.from_pylist(part_rows), out)
        else:
            pq.write_table(pa.table({"user_id": pa.array([], type=pa.string())}), out)

    summary = build_split_report(parts, total_input_rows=len(rows))
    summary["output_dir"] = str(input_dir.resolve())
    (input_dir / "split_meta.json").write_text(
        json.dumps(summary, indent=2, ensure_ascii=False), encoding="utf-8"
    )
    logger.info("Split dataset: %s", summary)
    return summary

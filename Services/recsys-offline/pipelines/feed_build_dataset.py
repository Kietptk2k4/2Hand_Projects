"""Mode-aware Social feed build-dataset (SEED_ONLY / HYBRID / REAL_ONLY)."""

from __future__ import annotations

import csv
import json
import logging
from pathlib import Path
from typing import Any

from pipelines.build_dataset import _write_parquet, build_rows
from pipelines.feed_train_mode import FeedTrainModeConfig, validate_mode

logger = logging.getLogger(__name__)

SOURCE_NAMES = [
    "posts",
    "comments",
    "post_likes",
    "post_saves",
    "follows",
    "search_history",
    "post_impression_log",
    "user_purchase_profile",
]


def _read_csv(path: Path) -> list[dict[str, Any]]:
    if not path.exists() or path.stat().st_size == 0:
        return []
    with path.open("r", encoding="utf-8-sig", newline="") as handle:
        rows = list(csv.DictReader(handle))
    # Parse JSON-encoded list fields written by clean_data / feed_sim
    for row in rows:
        for key, value in list(row.items()):
            if isinstance(value, str) and value.startswith("[") and value.endswith("]"):
                try:
                    row[key] = json.loads(value)
                except json.JSONDecodeError:
                    pass
    return rows


def _load_cleaned(input_dir: Path) -> dict[str, list[dict[str, Any]]]:
    return {name: _read_csv(input_dir / f"{name}.csv") for name in SOURCE_NAMES}


def _impression_key(row: dict[str, Any]) -> tuple[str, str, str]:
    return (
        str(row.get("user_id") or ""),
        str(row.get("post_id") or ""),
        str(row.get("shown_at") or row.get("created_at") or ""),
    )


def _merge_sources(
    seed: dict[str, list[dict[str, Any]]],
    real: dict[str, list[dict[str, Any]]],
) -> dict[str, list[dict[str, Any]]]:
    """Union tables; impressions dedupe by (user, post, shown_at) preferring REAL."""
    merged: dict[str, list[dict[str, Any]]] = {}
    for name in SOURCE_NAMES:
        if name == "post_impression_log":
            by_key: dict[tuple[str, str, str], dict[str, Any]] = {}
            for row in seed.get(name) or []:
                tagged = dict(row)
                tagged["_data_source"] = "SEED"
                by_key[_impression_key(tagged)] = tagged
            for row in real.get(name) or []:
                tagged = dict(row)
                tagged["_data_source"] = "REAL"
                by_key[_impression_key(tagged)] = tagged
            merged[name] = list(by_key.values())
        elif name == "posts":
            by_id: dict[str, dict[str, Any]] = {}
            for row in (seed.get(name) or []) + (real.get(name) or []):
                pid = str(row.get("post_id") or row.get("_id") or row.get("id") or "")
                if pid:
                    by_id[pid] = row
            merged[name] = list(by_id.values())
        elif name == "user_purchase_profile":
            by_uid: dict[str, dict[str, Any]] = {}
            for row in (seed.get(name) or []) + (real.get(name) or []):
                uid = str(row.get("user_id") or "")
                if uid:
                    by_uid[uid] = row
            merged[name] = list(by_uid.values())
        else:
            merged[name] = list(seed.get(name) or []) + list(real.get(name) or [])
    return merged


def _tag_rows_with_weights(
    rows: list[dict[str, Any]],
    raw: dict[str, list[dict[str, Any]]],
    *,
    mode: str,
    seed_row_weight: float,
) -> list[dict[str, Any]]:
    source_by_imp: dict[tuple[str, str, str], str] = {}
    for imp in raw.get("post_impression_log") or []:
        ds = str(imp.get("_data_source") or ("SEED" if mode == "SEED_ONLY" else "REAL"))
        source_by_imp[_impression_key(imp)] = ds

    out: list[dict[str, Any]] = []
    for row in rows:
        key = (
            str(row.get("user_id") or ""),
            str(row.get("post_id") or ""),
            str(row.get("shown_at") or ""),
        )
        if mode == "SEED_ONLY":
            data_source = "SEED"
        elif mode == "REAL_ONLY":
            data_source = "REAL"
        else:
            data_source = source_by_imp.get(key, "REAL")
        tagged = dict(row)
        tagged["data_source"] = data_source
        if mode == "HYBRID" and data_source == "SEED":
            tagged["sample_weight"] = float(seed_row_weight)
        else:
            tagged["sample_weight"] = 1.0
        out.append(tagged)
    return out


def build_feed_dataset(
    *,
    mode_cfg: FeedTrainModeConfig,
    seed_dir: Path | None = None,
    real_dir: Path | None = None,
    output_dir: Path,
) -> dict[str, Any]:
    mode = validate_mode(mode_cfg.train_data_mode)
    seed_raw = _load_cleaned(Path(seed_dir)) if seed_dir and Path(seed_dir).exists() else {
        n: [] for n in SOURCE_NAMES
    }
    real_raw = _load_cleaned(Path(real_dir)) if real_dir and Path(real_dir).exists() else {
        n: [] for n in SOURCE_NAMES
    }

    if mode == "SEED_ONLY":
        raw = seed_raw
        for imp in raw.get("post_impression_log") or []:
            imp["_data_source"] = "SEED"
    elif mode == "REAL_ONLY":
        raw = real_raw
        for imp in raw.get("post_impression_log") or []:
            imp["_data_source"] = "REAL"
        n_imp = len(raw.get("post_impression_log") or [])
        if n_imp < mode_cfg.real_only_min_impressions:
            raise ValueError(
                f"REAL_ONLY requires >= {mode_cfg.real_only_min_impressions} impressions; got {n_imp}"
            )
    else:  # HYBRID
        raw = _merge_sources(seed_raw, real_raw)

    if not raw.get("posts") or not raw.get("post_impression_log"):
        raise ValueError("Feed build-dataset requires posts and post_impression_log inputs")

    rows, summary = build_rows(raw)
    rows = _tag_rows_with_weights(
        rows,
        raw,
        mode=mode,
        seed_row_weight=mode_cfg.seed_row_weight,
    )

    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    out_path = output_dir / "dataset.parquet"
    _write_parquet(out_path, rows)

    summary = dict(summary)
    summary["train_data_mode"] = mode
    summary["seed_row_weight"] = mode_cfg.seed_row_weight
    summary["real_only_min_impressions"] = mode_cfg.real_only_min_impressions
    summary["output_path"] = str(out_path.resolve())
    summary["seed_impressions"] = sum(
        1 for r in (raw.get("post_impression_log") or []) if r.get("_data_source") == "SEED"
    )
    summary["real_impressions"] = sum(
        1 for r in (raw.get("post_impression_log") or []) if r.get("_data_source") == "REAL"
    )
    meta_path = output_dir / "dataset_meta.json"
    meta_path.write_text(json.dumps(summary, indent=2, ensure_ascii=False), encoding="utf-8")
    logger.info(
        "Feed build-dataset mode=%s rows=%s -> %s",
        mode,
        summary.get("rows"),
        out_path,
    )
    return summary


def run_feed_build_dataset_job(
    *,
    mode_cfg: FeedTrainModeConfig,
    seed_dir: Path,
    real_dir: Path,
    output_dir: Path | None = None,
) -> dict[str, Any]:
    mode = validate_mode(mode_cfg.train_data_mode)
    out = Path(output_dir) if output_dir else Path(real_dir)
    return build_feed_dataset(
        mode_cfg=mode_cfg,
        seed_dir=seed_dir if mode in {"SEED_ONLY", "HYBRID"} else None,
        real_dir=real_dir if mode in {"HYBRID", "REAL_ONLY"} else None,
        output_dir=out,
    )

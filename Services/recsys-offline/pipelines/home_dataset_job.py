"""Home dataset split + PopularityNormalizer fit on train (80/10/10 by request_id)."""

from __future__ import annotations

import json
import random
from collections import defaultdict
from pathlib import Path
from typing import Any

from pipelines.home_build_dataset import build_home_dataset_from_sim_dir
from pipelines.home_feature_order import HOME_FEATURE_ORDER
from pipelines.home_popularity import PopularityNormalizer
from pipelines.home_train_mode import HomeTrainModeConfig, resolve_home_train_mode


def split_by_request_id(
    rows: list[dict[str, Any]],
    *,
    seed: int = 42,
    ratios: tuple[float, float, float] = (0.8, 0.1, 0.1),
) -> tuple[list[dict[str, Any]], list[dict[str, Any]], list[dict[str, Any]]]:
    groups: dict[str, list[dict[str, Any]]] = defaultdict(list)
    for row in rows:
        groups[str(row.get("request_id") or "")].append(row)
    keys = sorted(groups.keys())
    rng = random.Random(seed)
    rng.shuffle(keys)
    n = len(keys)
    n_train = int(n * ratios[0])
    n_val = int(n * ratios[1])
    train_keys = set(keys[:n_train])
    val_keys = set(keys[n_train : n_train + n_val])
    train, val, test = [], [], []
    for key, group in groups.items():
        if key in train_keys:
            train.extend(group)
        elif key in val_keys:
            val.extend(group)
        else:
            test.extend(group)
    return train, val, test


def fit_and_rewrite_popularity(
    train: list[dict[str, Any]],
    *others: list[dict[str, Any]],
) -> tuple[PopularityNormalizer, list[list[dict[str, Any]]]]:
    raws = [float(r.get("popularity_raw") or 0.0) for r in train]
    # popularity feature is already normalized in rows; refit using log1p of raw if present
    # Prefer reconstructing from feature column when raw missing: invert not available → use feature as z proxy
    if not any(r.get("popularity_raw") is not None for r in train):
        zs = [float(r.get("popularity") or 0.0) for r in train]
        if not zs:
            normalizer = PopularityNormalizer(0.0, 1.0)
        else:
            lo = sorted(zs)[max(0, int(0.01 * (len(zs) - 1)))]
            hi = sorted(zs)[min(len(zs) - 1, int(0.99 * (len(zs) - 1)))]
            if hi <= lo:
                hi = lo + 1.0
            normalizer = PopularityNormalizer(lo, hi)
    else:
        normalizer = PopularityNormalizer.fit_from_raw([int(x) for x in raws])

    rewritten: list[list[dict[str, Any]]] = []
    for split in (train, *others):
        next_rows = []
        for row in split:
            copy = dict(row)
            if "popularity_raw" in copy and copy["popularity_raw"] is not None:
                z = PopularityNormalizer.log1p_raw(copy["popularity_raw"])
                copy["popularity"] = normalizer.normalize(z)
            next_rows.append(copy)
        rewritten.append(next_rows)
    return normalizer, rewritten


def write_split_jsonl(path: Path, rows: list[dict[str, Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as handle:
        for row in rows:
            handle.write(json.dumps(row, ensure_ascii=False) + "\n")


def run_home_build_dataset_job(
    sim_dir: Path,
    artifact_dir: Path,
    *,
    mode_cfg: HomeTrainModeConfig | None = None,
) -> dict[str, Any]:
    cfg = mode_cfg or resolve_home_train_mode()
    rows, meta = build_home_dataset_from_sim_dir(sim_dir, mode_cfg=cfg)
    train, val, test = split_by_request_id(rows)
    normalizer, (train, val, test) = fit_and_rewrite_popularity(train, val, test)
    artifact_dir.mkdir(parents=True, exist_ok=True)
    write_split_jsonl(artifact_dir / "train.jsonl", train)
    write_split_jsonl(artifact_dir / "val.jsonl", val)
    write_split_jsonl(artifact_dir / "test.jsonl", test)
    (artifact_dir / "popularity_normalizer.json").write_text(
        json.dumps(normalizer.to_dict()), encoding="utf-8"
    )
    (artifact_dir / "feature_order.json").write_text(
        json.dumps(HOME_FEATURE_ORDER), encoding="utf-8"
    )
    summary = {
        **meta,
        "train_rows": len(train),
        "val_rows": len(val),
        "test_rows": len(test),
        "popularity_normalizer": normalizer.to_dict(),
    }
    (artifact_dir / "dataset_meta.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")
    return summary

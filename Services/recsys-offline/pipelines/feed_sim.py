"""File-only Social feed simulation for cold-start train (no shared-DB writers)."""

from __future__ import annotations

import csv
import json
from pathlib import Path
from typing import Any

from simulation.engine import result_to_cleaned_sources, run_simulation
from simulation.skeleton import build_skeleton, summarize_skeleton


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


def _write_csv(path: Path, rows: list[dict[str, Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    if not rows:
        path.write_text("", encoding="utf-8")
        return
    normalized: list[dict[str, Any]] = []
    fieldnames: list[str] = []
    seen_fields: set[str] = set()
    for row in rows:
        copy: dict[str, Any] = {}
        for key, value in row.items():
            if key not in seen_fields:
                seen_fields.add(key)
                fieldnames.append(key)
            copy[key] = json.dumps(value, ensure_ascii=False) if isinstance(value, list) else value
        normalized.append(copy)
    with path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(normalized)


def run_feed_sim_to_dir(
    sim_dir: Path,
    *,
    scale: str = "smoke",
    seed: int = 42,
    config_path: Path | str | None = None,
) -> dict[str, Any]:
    """
    Generate feed train corpora as CSVs under sim_dir.

    MUST NOT insert into Auth/Social/Commerce application databases.
    """
    sim_dir = Path(sim_dir)
    sim_dir.mkdir(parents=True, exist_ok=True)

    skeleton = build_skeleton(scale=scale, config_path=config_path)
    result = run_simulation(skeleton, seed=seed)
    sources = result_to_cleaned_sources(result)

    counts: dict[str, int] = {}
    for name in SOURCE_NAMES:
        rows = sources.get(name) or []
        _write_csv(sim_dir / f"{name}.csv", rows)
        counts[name] = len(rows)

    summary = {
        "sim_dir": str(sim_dir.resolve()),
        "scale": scale,
        "seed": seed,
        "skeleton": summarize_skeleton(skeleton),
        "sim_meta": result.meta,
        "counts": counts,
        "data_source": "SEED",
        "shared_db_writes": False,
    }
    (sim_dir / "sim_summary.json").write_text(
        json.dumps(summary, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )
    return summary

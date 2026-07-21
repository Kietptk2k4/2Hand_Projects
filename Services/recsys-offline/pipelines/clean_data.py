"""Read-only clean pipeline: extract → clean → CSV + summary (no DB writes)."""

from __future__ import annotations

import csv
import json
import logging
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from app.config import Settings, get_settings
from pipelines.extract import extract_all
from pipelines.normalize import (
    clean_comments,
    clean_follows,
    clean_posts,
    clean_search_history,
    clean_user_post_events,
)

logger = logging.getLogger(__name__)


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


def clean_extracted(
    raw: dict[str, list[dict[str, Any]]],
) -> tuple[dict[str, list[dict[str, Any]]], dict[str, Any]]:
    cleaned: dict[str, list[dict[str, Any]]] = {}
    summary: dict[str, Any] = {"sources": {}}

    posts, post_drops = clean_posts(raw.get("posts", []))
    cleaned["posts"] = posts
    summary["sources"]["posts"] = {
        "input": len(raw.get("posts", [])),
        "kept": len(posts),
        "dropped": dict(post_drops),
    }

    comments, comment_drops = clean_comments(raw.get("comments", []))
    cleaned["comments"] = comments
    summary["sources"]["comments"] = {
        "input": len(raw.get("comments", [])),
        "kept": len(comments),
        "dropped": dict(comment_drops),
    }

    likes, like_drops = clean_user_post_events(raw.get("post_likes", []), entity="likes")
    cleaned["post_likes"] = likes
    summary["sources"]["post_likes"] = {
        "input": len(raw.get("post_likes", [])),
        "kept": len(likes),
        "dropped": dict(like_drops),
    }

    saves, save_drops = clean_user_post_events(raw.get("post_saves", []), entity="saves")
    cleaned["post_saves"] = saves
    summary["sources"]["post_saves"] = {
        "input": len(raw.get("post_saves", [])),
        "kept": len(saves),
        "dropped": dict(save_drops),
    }

    follows, follow_drops = clean_follows(raw.get("follows", []))
    cleaned["follows"] = follows
    summary["sources"]["follows"] = {
        "input": len(raw.get("follows", [])),
        "kept": len(follows),
        "dropped": dict(follow_drops),
    }

    search, search_drops = clean_search_history(raw.get("search_history", []))
    cleaned["search_history"] = search
    summary["sources"]["search_history"] = {
        "input": len(raw.get("search_history", [])),
        "kept": len(search),
        "dropped": dict(search_drops),
    }

    impressions, imp_drops = clean_user_post_events(
        raw.get("post_impression_log", []), entity="impressions"
    )
    cleaned["post_impression_log"] = impressions
    summary["sources"]["post_impression_log"] = {
        "input": len(raw.get("post_impression_log", [])),
        "kept": len(impressions),
        "dropped": dict(imp_drops),
        "warning": (
            "empty_or_unavailable"
            if not raw.get("post_impression_log")
            else None
        ),
    }

    total_dropped = Counter()
    for source in summary["sources"].values():
        total_dropped.update(source.get("dropped") or {})
    summary["total_dropped_by_reason"] = dict(total_dropped)
    summary["finished_at"] = datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")
    return cleaned, summary


def run_clean_job(settings: Settings | None = None) -> dict[str, Any]:
    settings = settings or get_settings()
    settings.require_db_urls()

    raw = extract_all(settings)
    cleaned, summary = clean_extracted(raw)

    out_dir = Path(settings.recsys_dataset_output_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    for name, rows in cleaned.items():
        _write_csv(out_dir / f"{name}.csv", rows)

    summary_path = out_dir / "clean_summary.json"
    summary["output_dir"] = str(out_dir.resolve())
    summary_path.write_text(json.dumps(summary, indent=2, ensure_ascii=False), encoding="utf-8")
    logger.info("Clean job finished. Summary written to %s", summary_path)
    return summary

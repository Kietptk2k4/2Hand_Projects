"""Dev-only repair: convert string/stale Mongo post timestamps to BSON Date in-window."""

from __future__ import annotations

import logging
import re
from dataclasses import asdict, dataclass
from datetime import datetime, timedelta, timezone
from typing import Any

from simulation.timestamps import ensure_utc, parse_utc_datetime, spread_timestamps

logger = logging.getLogger(__name__)

DEFAULT_WINDOW_DAYS = 21
DEFAULT_CAPTION_PREFIX = "Sim "


@dataclass(frozen=True)
class RepairSummary:
    scanned: int
    string_created_at: int
    date_created_at: int
    other_created_at: int
    in_window_ok: int
    would_update: int
    updated: int
    dry_run: bool
    window_days: int
    caption_filter: str | None

    def to_dict(self) -> dict[str, Any]:
        return asdict(self)


def created_at_needs_repair(
    created_at: object,
    *,
    now: datetime,
    window_days: int,
) -> tuple[bool, str]:
    """Return (needs_repair, type_bucket) where type_bucket is string|date|other."""
    now_utc = ensure_utc(now)
    window_start = now_utc - timedelta(days=max(int(window_days), 1))
    if isinstance(created_at, str):
        return True, "string"
    if isinstance(created_at, datetime):
        ts = ensure_utc(created_at)
        if ts < window_start or ts > now_utc:
            return True, "date"
        return False, "date"
    parsed = parse_utc_datetime(created_at)
    if parsed is None:
        return True, "other"
    if parsed < window_start or parsed > now_utc:
        return True, "other"
    return False, "other"


def select_docs_needing_repair(
    docs: list[dict[str, Any]],
    *,
    now: datetime,
    window_days: int = DEFAULT_WINDOW_DAYS,
) -> tuple[RepairSummary, list[Any]]:
    """Classify docs and return ids that need timestamp repair (dry-run stats)."""
    string_n = date_n = other_n = in_ok = 0
    repair_ids: list[Any] = []
    for doc in docs:
        created = doc.get("created_at")
        needs, bucket = created_at_needs_repair(
            created, now=now, window_days=window_days
        )
        if bucket == "string":
            string_n += 1
        elif bucket == "date":
            date_n += 1
        else:
            other_n += 1
        if needs:
            repair_ids.append(doc.get("_id"))
        else:
            in_ok += 1
    summary = RepairSummary(
        scanned=len(docs),
        string_created_at=string_n,
        date_created_at=date_n,
        other_created_at=other_n,
        in_window_ok=in_ok,
        would_update=len(repair_ids),
        updated=0,
        dry_run=True,
        window_days=window_days,
        caption_filter=None,
    )
    return summary, repair_ids


def apply_timestamp_repairs(
    docs: list[dict[str, Any]],
    repair_ids: list[Any],
    *,
    now: datetime,
    window_days: int = DEFAULT_WINDOW_DAYS,
) -> list[dict[str, Any]]:
    """
    Return new doc copies with repaired timestamps for ids in repair_ids.
    Does not mutate input docs (immutable style).
    """
    now_utc = ensure_utc(now)
    stamps = spread_timestamps(len(repair_ids), now_utc, window_days)
    stamp_by_id = dict(zip(repair_ids, stamps, strict=True))
    out: list[dict[str, Any]] = []
    for doc in docs:
        doc_id = doc.get("_id")
        if doc_id not in stamp_by_id:
            out.append(dict(doc))
            continue
        ts = stamp_by_id[doc_id]
        updated = dict(doc)
        updated["created_at"] = ts
        updated["updated_at"] = ts
        out.append(updated)
    return out


def _build_query(*, caption_prefix: str | None) -> dict[str, Any]:
    if caption_prefix is None:
        return {}
    return {"caption": {"$regex": f"^{re.escape(caption_prefix)}"}}


def repair_mongo_post_timestamps(
    mongo_url: str,
    mongo_db: str,
    *,
    window_days: int = DEFAULT_WINDOW_DAYS,
    dry_run: bool = True,
    caption_prefix: str | None = DEFAULT_CAPTION_PREFIX,
    now: datetime | None = None,
) -> RepairSummary:
    """
    Scan (and optionally update) Mongo posts.

    Default ``caption_prefix='Sim '`` limits scope to sim seeds.
    Pass ``caption_prefix=None`` to widen to all posts (dev escape hatch).
    """
    from pymongo import MongoClient

    now_utc = ensure_utc(now or datetime.now(timezone.utc))
    client = MongoClient(mongo_url)
    try:
        col = client[mongo_db]["posts"]
        query = _build_query(caption_prefix=caption_prefix)
        docs = list(col.find(query, {"_id": 1, "created_at": 1, "caption": 1}))
        base_summary, repair_ids = select_docs_needing_repair(
            docs, now=now_utc, window_days=window_days
        )
        summary = RepairSummary(
            scanned=base_summary.scanned,
            string_created_at=base_summary.string_created_at,
            date_created_at=base_summary.date_created_at,
            other_created_at=base_summary.other_created_at,
            in_window_ok=base_summary.in_window_ok,
            would_update=base_summary.would_update,
            updated=0,
            dry_run=dry_run,
            window_days=window_days,
            caption_filter=caption_prefix,
        )
        if dry_run or not repair_ids:
            return summary

        stamps = spread_timestamps(len(repair_ids), now_utc, window_days)
        updated = 0
        for doc_id, ts in zip(repair_ids, stamps, strict=True):
            result = col.update_one(
                {"_id": doc_id},
                {"$set": {"created_at": ts, "updated_at": ts}},
            )
            updated += int(result.modified_count or 0)
        return RepairSummary(
            scanned=summary.scanned,
            string_created_at=summary.string_created_at,
            date_created_at=summary.date_created_at,
            other_created_at=summary.other_created_at,
            in_window_ok=summary.in_window_ok,
            would_update=summary.would_update,
            updated=updated,
            dry_run=False,
            window_days=window_days,
            caption_filter=caption_prefix,
        )
    finally:
        client.close()

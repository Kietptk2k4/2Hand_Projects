"""Label assignment for impression-based samples (no negative sampling)."""

from __future__ import annotations

from datetime import datetime, timedelta, timezone
from typing import Any


def _parse_ts(value: Any) -> datetime | None:
    if value is None:
        return None
    if isinstance(value, datetime):
        dt = value
        if dt.tzinfo is None:
            return dt.replace(tzinfo=timezone.utc)
        return dt.astimezone(timezone.utc)
    text = str(value).strip()
    if not text:
        return None
    try:
        return datetime.fromisoformat(text.replace("Z", "+00:00")).astimezone(timezone.utc)
    except ValueError:
        return None


def assign_label(
    *,
    user_id: str,
    post_id: str,
    shown_at: Any,
    likes: list[dict[str, Any]],
    saves: list[dict[str, Any]],
    comments: list[dict[str, Any]],
    window_hours: int = 24,
) -> int:
    shown = _parse_ts(shown_at)
    if shown is None:
        return 0
    end = shown + timedelta(hours=window_hours)

    def hit(rows: list[dict[str, Any]], ts_key: str = "created_at") -> bool:
        for row in rows:
            if str(row.get("user_id")) != str(user_id):
                continue
            if str(row.get("post_id")) != str(post_id):
                continue
            # comments may use author_id
            if "author_id" in row and str(row.get("user_id", row.get("author_id"))) != str(user_id):
                if str(row.get("author_id")) != str(user_id):
                    continue
            ts = _parse_ts(row.get(ts_key) or row.get("created_at"))
            if ts is None:
                continue
            if shown <= ts <= end:
                return True
        return False

    # likes / saves keyed by user_id
    if hit(likes) or hit(saves):
        return 1

    for row in comments:
        author = row.get("author_id") or row.get("user_id")
        if str(author) != str(user_id):
            continue
        if str(row.get("post_id")) != str(post_id):
            continue
        ts = _parse_ts(row.get("created_at"))
        if ts is not None and shown <= ts <= end:
            return 1
    return 0

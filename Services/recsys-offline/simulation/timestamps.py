"""Shared UTC timestamp helpers for sim seed and Mongo repair."""

from __future__ import annotations

from datetime import datetime, timedelta, timezone


def ensure_utc(dt: datetime) -> datetime:
    if dt.tzinfo is None:
        return dt.replace(tzinfo=timezone.utc)
    return dt.astimezone(timezone.utc)


def spread_timestamps(
    count: int,
    end_at: datetime,
    span_days: int,
) -> list[datetime]:
    """Evenly spread ``count`` UTC datetimes across ``[end_at - span_days, end_at]``."""
    end = ensure_utc(end_at)
    days = max(int(span_days), 1)
    if count <= 0:
        return []
    if count == 1:
        return [end]
    span = timedelta(days=days)
    start = end - span
    return [start + span * (i / (count - 1)) for i in range(count)]


def parse_utc_datetime(value: object) -> datetime | None:
    if isinstance(value, datetime):
        return ensure_utc(value)
    if isinstance(value, str):
        text = value.strip()
        if not text:
            return None
        if text.endswith("Z"):
            text = text[:-1] + "+00:00"
        try:
            return ensure_utc(datetime.fromisoformat(text))
        except ValueError:
            return None
    return None

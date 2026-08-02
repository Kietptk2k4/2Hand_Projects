"""Social interest export aggregation (D13) — file or DB write by caller."""

from __future__ import annotations

import math
from collections import defaultdict
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from typing import Iterable


WEIGHTS = {"search": 4.0, "save": 3.0, "comment": 2.0, "like": 1.0}
HALF_LIFE_DAYS = 14.0
DEFAULT_WINDOW_DAYS = 90


@dataclass(frozen=True)
class SocialEvent:
    user_id: str
    tag_type: str  # HASHTAG | KEYWORD
    tag: str
    event_kind: str  # search|save|comment|like
    occurred_at: datetime


def _aware(dt: datetime) -> datetime:
    if dt.tzinfo is None:
        return dt.replace(tzinfo=timezone.utc)
    return dt


def normalize_tag(tag: str) -> str:
    return tag.strip().lower()


def build_social_interest_export(
    events: Iterable[SocialEvent],
    as_of: datetime,
    window_days: int = DEFAULT_WINDOW_DAYS,
) -> list[dict]:
    as_of = _aware(as_of)
    start = as_of - timedelta(days=window_days)
    scores: dict[tuple[str, str, str], float] = defaultdict(float)
    for ev in events:
        kind = ev.event_kind.lower()
        if kind not in WEIGHTS:
            continue
        occurred = _aware(ev.occurred_at)
        if occurred < start or occurred >= as_of:
            continue
        tag = normalize_tag(ev.tag)
        if not tag:
            continue
        tag_type = ev.tag_type.upper()
        delta_days = max(0.0, (as_of - occurred).total_seconds() / 86400.0)
        decay = 2.0 ** (-delta_days / HALF_LIFE_DAYS)
        scores[(ev.user_id, tag_type, tag)] += WEIGHTS[kind] * decay

    rows = []
    for (user_id, tag_type, tag), score in scores.items():
        rows.append(
            {
                "user_id": user_id,
                "tag_type": tag_type,
                "tag": tag,
                "score": score,
                "window_days": window_days,
                "computed_at": as_of.isoformat().replace("+00:00", "Z"),
                "as_of": as_of.isoformat().replace("+00:00", "Z"),
            }
        )
    return rows

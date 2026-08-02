"""Nearest-impression Home labels (D7)."""

from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from typing import Iterable


@dataclass(frozen=True)
class Impression:
    user_id: str
    product_id: str
    shown_at: datetime
    request_id: str


@dataclass(frozen=True)
class Engage:
    user_id: str
    product_id: str
    occurred_at: datetime


def _aware(dt: datetime) -> datetime:
    if dt.tzinfo is None:
        return dt.replace(tzinfo=timezone.utc)
    return dt


def assign_nearest_impression_labels(
    impressions: Iterable[Impression],
    engages: Iterable[Engage],
    label_window: timedelta = timedelta(hours=24),
) -> list[tuple[Impression, int]]:
    """Return (impression, y) with nearest-prior-impression attribution."""
    imps = sorted(impressions, key=lambda i: (_aware(i.shown_at), i.request_id))
    eng_list = list(engages)
    positives: set[int] = set()

    for eng in eng_list:
        t = _aware(eng.occurred_at)
        best_idx = None
        best_shown = None
        for idx, imp in enumerate(imps):
            if imp.user_id != eng.user_id or imp.product_id != eng.product_id:
                continue
            shown = _aware(imp.shown_at)
            if shown <= t < shown + label_window:
                if best_shown is None or shown > best_shown:
                    best_shown = shown
                    best_idx = idx
        if best_idx is not None:
            positives.add(best_idx)

    return [(imp, 1 if i in positives else 0) for i, imp in enumerate(imps)]

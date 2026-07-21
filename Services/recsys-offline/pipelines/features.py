"""Feature formulas mirroring Social PostFeatureBuilder (+ point-in-time)."""

from __future__ import annotations

import json
import math
from datetime import datetime, timezone
from typing import Any


HALF_LIFE_SECONDS = 7.0 * 24.0 * 3600.0
W_CATEGORY = 0.6
W_SHOP = 0.4


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


def _parse_hashtags(raw: Any) -> list[str]:
    if raw is None:
        return []
    if isinstance(raw, list):
        tags = raw
    else:
        text = str(raw).strip()
        if not text:
            return []
        try:
            parsed = json.loads(text)
            tags = parsed if isinstance(parsed, list) else [text]
        except json.JSONDecodeError:
            tags = [text]
    out: list[str] = []
    for tag in tags:
        t = str(tag).strip().lstrip("#").lower()
        if t and t not in out:
            out.append(t)
    return out


def recency_score(post_created_at: Any, shown_at: Any) -> float:
    created = _parse_ts(post_created_at)
    shown = _parse_ts(shown_at)
    if created is None or shown is None:
        return 0.0
    delta = max(0.0, (shown - created).total_seconds())
    return math.pow(2.0, -delta / HALF_LIFE_SECONDS)


def engagement_raw(like_count: Any, comment_count: Any) -> float:
    likes = float(like_count or 0)
    comments = float(comment_count or 0)
    return math.log(1.0 + likes) + 2.0 * math.log(1.0 + comments)


def cross_domain_score(
    user_category_ids: set[str],
    user_shop_ids: set[str],
    post_category_ids: set[str],
    post_shop_ids: set[str],
) -> float:
    if not post_category_ids and not post_shop_ids:
        return 0.0
    cat_hit = 1.0 if user_category_ids.intersection(post_category_ids) else 0.0
    shop_hit = 1.0 if user_shop_ids.intersection(post_shop_ids) else 0.0
    return W_CATEGORY * cat_hit + W_SHOP * shop_hit


def min_max_normalize(values: list[float]) -> list[float]:
    if not values:
        return []
    lo = min(values)
    hi = max(values)
    if hi - lo == 0.0:
        return [0.0 for _ in values]
    return [(v - lo) / (hi - lo) for v in values]


def filter_before(rows: list[dict[str, Any]], cutoff: Any) -> list[dict[str, Any]]:
    shown = _parse_ts(cutoff)
    if shown is None:
        return []
    kept = []
    for row in rows:
        ts = _parse_ts(row.get("created_at") or row.get("shown_at"))
        if ts is not None and ts < shown:
            kept.append(row)
    return kept


def parse_product_tag_ids(raw_tags: Any) -> tuple[set[str], set[str]]:
    cats: set[str] = set()
    shops: set[str] = set()
    tags = raw_tags
    if isinstance(raw_tags, str):
        try:
            tags = json.loads(raw_tags)
        except json.JSONDecodeError:
            tags = []
    if not isinstance(tags, list):
        return cats, shops
    for tag in tags:
        if not isinstance(tag, dict):
            continue
        cid = tag.get("categoryId") or tag.get("category_id")
        sid = tag.get("shopId") or tag.get("shop_id")
        if cid:
            cats.add(str(cid))
        if sid:
            shops.add(str(sid))
    return cats, shops

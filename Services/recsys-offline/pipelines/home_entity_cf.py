"""Entity co-occurrence job for Commerce Home CF (D5)."""

from __future__ import annotations

import math
from collections import defaultdict
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from itertools import combinations
from typing import Any, Iterable


ENTITY_LEAF = "LEAF_CATEGORY"
ENTITY_BRAND = "BRAND"
COMPLETED_WEIGHT = 1.0
CART_WEIGHT = 0.6
DEFAULT_TOP_M = 50
DEFAULT_WINDOW_DAYS = 180
CART_COOCCUR_HOURS = 24


@dataclass(frozen=True)
class OrderLine:
    order_id: str
    user_id: str
    category_id: str | None
    brand_id: str | None
    completed_at: datetime
    status: str


@dataclass(frozen=True)
class CartLine:
    user_id: str
    category_id: str | None
    brand_id: str | None
    created_at: datetime
    removed: bool = False


def _aware(dt: datetime) -> datetime:
    if dt.tzinfo is None:
        return dt.replace(tzinfo=timezone.utc)
    return dt


def _entities(category_id: str | None, brand_id: str | None) -> list[tuple[str, str]]:
    out: list[tuple[str, str]] = []
    if category_id:
        out.append((ENTITY_LEAF, str(category_id)))
    if brand_id:
        out.append((ENTITY_BRAND, str(brand_id)))
    return out


def accumulate_completed_pairs(
    lines: Iterable[OrderLine],
    as_of: datetime,
    window_days: int = DEFAULT_WINDOW_DAYS,
) -> dict[tuple[str, str, str, str], float]:
    as_of = _aware(as_of)
    start = as_of - timedelta(days=window_days)
    by_order: dict[str, list[OrderLine]] = defaultdict(list)
    for line in lines:
        if line.status != "COMPLETED":
            continue
        completed = _aware(line.completed_at)
        if completed < start or completed >= as_of:
            continue
        by_order[line.order_id].append(line)

    weights: dict[tuple[str, str, str, str], float] = defaultdict(float)
    for order_lines in by_order.values():
        ents: list[tuple[str, str]] = []
        for line in order_lines:
            ents.extend(_entities(line.category_id, line.brand_id))
        unique = sorted(set(ents))
        for a, b in combinations(unique, 2):
            key_ab = (a[0], a[1], b[0], b[1])
            key_ba = (b[0], b[1], a[0], a[1])
            weights[key_ab] += COMPLETED_WEIGHT
            weights[key_ba] += COMPLETED_WEIGHT
    return dict(weights)


def accumulate_cart_pairs(
    lines: Iterable[CartLine],
    as_of: datetime,
    window_days: int = DEFAULT_WINDOW_DAYS,
) -> dict[tuple[str, str, str, str], float]:
    as_of = _aware(as_of)
    start = as_of - timedelta(days=window_days)
    by_user: dict[str, list[CartLine]] = defaultdict(list)
    for line in lines:
        if line.removed:
            continue
        created = _aware(line.created_at)
        if created < start or created >= as_of:
            continue
        by_user[line.user_id].append(line)

    weights: dict[tuple[str, str, str, str], float] = defaultdict(float)
    window = timedelta(hours=CART_COOCCUR_HOURS)
    for user_lines in by_user.values():
        ranked = sorted(user_lines, key=lambda x: _aware(x.created_at))
        for i, left in enumerate(ranked):
            left_ents = _entities(left.category_id, left.brand_id)
            left_t = _aware(left.created_at)
            for right in ranked[i + 1 :]:
                right_t = _aware(right.created_at)
                if right_t - left_t > window:
                    break
                right_ents = _entities(right.category_id, right.brand_id)
                pairs = sorted(set(left_ents + right_ents))
                for a, b in combinations(pairs, 2):
                    weights[(a[0], a[1], b[0], b[1])] += CART_WEIGHT
                    weights[(b[0], b[1], a[0], a[1])] += CART_WEIGHT
    return dict(weights)


def merge_weight_maps(
    *maps: dict[tuple[str, str, str, str], float],
) -> dict[tuple[str, str, str, str], float]:
    out: dict[tuple[str, str, str, str], float] = defaultdict(float)
    for m in maps:
        for k, v in m.items():
            out[k] += v
    return dict(out)


def hybrid_max_merge(
    real: dict[tuple[str, str, str, str], float],
    seed: dict[tuple[str, str, str, str], float],
) -> dict[tuple[str, str, str, str], float]:
    keys = set(real) | set(seed)
    return {k: max(real.get(k, float("-inf")), seed.get(k, float("-inf"))) for k in keys}


def scores_from_weights(
    weights: dict[tuple[str, str, str, str], float],
    top_m: int = DEFAULT_TOP_M,
) -> list[dict[str, Any]]:
    by_entity: dict[tuple[str, str], list[tuple[tuple[str, str], float]]] = defaultdict(list)
    for (et, eid, nt, nid), w in weights.items():
        score = math.log1p(w)
        by_entity[(et, eid)].append(((nt, nid), score))

    rows: list[dict[str, Any]] = []
    for (et, eid), neighbors in by_entity.items():
        neighbors.sort(key=lambda x: x[1], reverse=True)
        for (nt, nid), score in neighbors[:top_m]:
            rows.append(
                {
                    "entity_type": et,
                    "entity_id": eid,
                    "neighbor_type": nt,
                    "neighbor_id": nid,
                    "score": score,
                }
            )
    return rows

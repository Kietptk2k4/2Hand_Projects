"""Commerce Home Feature Builder — normative 15 formulas (parity with Java)."""

from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime, timezone
from typing import Any
from uuid import UUID

from pipelines.home_feature_order import HOME_FEATURE_ORDER
from pipelines.home_popularity import PopularityNormalizer

SECONDS_PER_DAY = 86400.0
RECENCY_HALF_LIFE_DAYS = 7.0


def clip01(x: float) -> float:
    return max(0.0, min(1.0, float(x)))


@dataclass(frozen=True)
class HomeCandidateInput:
    product_id: str
    category_id: str | None
    brand_id: str | None
    shop_id: str | None
    effective_price: float | None
    created_at: datetime | None
    rating_avg: float | None
    rating_count: int
    popularity_raw: int
    sources: frozenset[str] = field(default_factory=frozenset)
    personal_score: float | None = None
    cf_score: float | None = None
    ar_score: float | None = None


@dataclass(frozen=True)
class HomeProfileInput:
    category_scores: dict[str, float]
    brand_scores: dict[str, float]
    shop_scores: dict[str, float]
    price_p25: float | None
    price_p75: float | None


def _ensure_aware(dt: datetime) -> datetime:
    if dt.tzinfo is None:
        return dt.replace(tzinfo=timezone.utc)
    return dt


def build_home_feature_vector(
    candidate: HomeCandidateInput,
    profile: HomeProfileInput,
    normalizer: PopularityNormalizer,
    as_of: datetime,
) -> list[float]:
    t = _ensure_aware(as_of)

    # 1 recency_score
    if candidate.created_at is None:
        recency = 0.0
    else:
        created = _ensure_aware(candidate.created_at)
        delta = max(0.0, (t - created).total_seconds())
        recency = 2.0 ** (-delta / (RECENCY_HALF_LIFE_DAYS * SECONDS_PER_DAY))

    # 2 popularity_score
    z = PopularityNormalizer.log1p_raw(candidate.popularity_raw)
    popularity = normalizer.normalize(z)

    # 3 rating_score
    if candidate.rating_count < 3:
        rating = 0.5
    else:
        rating = clip01((candidate.rating_avg or 0.0) / 5.0)

    # 4-6 matches
    cat = 0.0
    if candidate.category_id:
        cat = float(profile.category_scores.get(str(candidate.category_id), 0.0))
    brand = 0.0
    if candidate.brand_id:
        brand = float(profile.brand_scores.get(str(candidate.brand_id), 0.0))
    shop = 0.0
    if candidate.shop_id:
        shop = float(profile.shop_scores.get(str(candidate.shop_id), 0.0))

    # 7 price_affinity
    price_aff = 0.5
    p25, p75 = profile.price_p25, profile.price_p75
    price = candidate.effective_price
    if price is not None and p25 is not None and p75 is not None:
        iqr = p75 - p25
        if iqr > 0:
            if p25 <= price <= p75:
                price_aff = 1.0
            elif price < p25:
                price_aff = clip01(1.0 - (p25 - price) / iqr)
            else:
                price_aff = clip01(1.0 - (price - p75) / iqr)

    # 8-9 cf/ar clips
    cross = clip01(candidate.ar_score if candidate.ar_score is not None else 0.0)
    cf = clip01(candidate.cf_score if candidate.cf_score is not None else 0.0)

    # 10 semantic idle
    semantic = 0.0

    sources = {s.upper() for s in candidate.sources}
    is_popular = 1.0 if "POPULAR" in sources else 0.0
    is_personal = 1.0 if "PERSONAL" in sources else 0.0
    is_cf = 1.0 if "CF" in sources else 0.0
    is_cross = 1.0 if "CROSS_DOMAIN" in sources else 0.0
    is_semantic = 0.0

    vector = [
        recency,
        popularity,
        rating,
        cat,
        brand,
        shop,
        price_aff,
        cross,
        cf,
        semantic,
        is_popular,
        is_personal,
        is_cf,
        is_cross,
        is_semantic,
    ]
    assert len(vector) == len(HOME_FEATURE_ORDER)
    return vector


def vector_as_dict(vector: list[float]) -> dict[str, float]:
    return {name: vector[i] for i, name in enumerate(HOME_FEATURE_ORDER)}

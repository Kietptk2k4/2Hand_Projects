"""Shared HOME_FEATURE_ORDER for Commerce Home LTR (Python train ↔ Java serve)."""

from __future__ import annotations

HOME_FEATURE_ORDER: list[str] = [
    "recency_score",
    "popularity_score",
    "rating_score",
    "category_match",
    "brand_match",
    "shop_match",
    "price_affinity",
    "cross_domain_score",
    "cf_score",
    "semantic_similarity",
    "is_popular",
    "is_personal",
    "is_cf",
    "is_cross_domain",
    "is_semantic",
]

HOME_FEATURE_DIM = len(HOME_FEATURE_ORDER)

assert HOME_FEATURE_DIM == 15, "HOME_FEATURE_ORDER must have exactly 15 dimensions"

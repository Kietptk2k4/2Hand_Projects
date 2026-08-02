"""HOME_FEATURE_ORDER constant and PopularityNormalizer unit tests."""

from pipelines.home_feature_order import HOME_FEATURE_DIM, HOME_FEATURE_ORDER
from pipelines.home_popularity import PopularityNormalizer


def test_home_feature_order_locked_15():
    assert HOME_FEATURE_DIM == 15
    assert HOME_FEATURE_ORDER == [
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


def test_popularity_normalizer_clips_and_scales():
    norm = PopularityNormalizer(z_lo=0.0, z_hi=math_log1p_10())
    assert norm.normalize(0.0) == 0.0
    assert abs(norm.normalize(math_log1p_10()) - 1.0) < 1e-9
    assert norm.normalize(-1.0) == 0.0
    assert norm.normalize(100.0) == 1.0


def test_popularity_fit_from_raw_uses_log1p():
    # raw 0 -> z=0; raw 9 -> z=log1p(9)≈2.302
    norm = PopularityNormalizer.fit_from_raw([0, 0, 9, 9], lo_pct=0.0, hi_pct=100.0)
    assert norm.z_lo == 0.0
    assert abs(norm.z_hi - PopularityNormalizer.log1p_raw(9)) < 1e-9
    assert abs(norm.normalize(0.0) - 0.0) < 1e-9
    assert abs(norm.normalize(PopularityNormalizer.log1p_raw(9)) - 1.0) < 1e-9


def math_log1p_10() -> float:
    import math

    return math.log1p(10)

"""Parity fixtures for Home Feature Builder formulas."""

from datetime import datetime, timezone

from pipelines.home_feature_order import HOME_FEATURE_ORDER
from pipelines.home_features import HomeCandidateInput, HomeProfileInput, build_home_feature_vector
from pipelines.home_popularity import PopularityNormalizer


def test_missing_created_at_and_price_iqr_and_flags():
    as_of = datetime(2026, 8, 1, tzinfo=timezone.utc)
    normalizer = PopularityNormalizer(z_lo=0.0, z_hi=PopularityNormalizer.log1p_raw(10))
    candidate = HomeCandidateInput(
        product_id="11111111-1111-4111-8111-111111111111",
        category_id="22222222-2222-4222-8222-222222222222",
        brand_id="33333333-3333-4333-8333-333333333333",
        shop_id="44444444-4444-4444-8444-444444444444",
        effective_price=100.0,
        created_at=None,
        rating_avg=4.0,
        rating_count=10,
        popularity_raw=0,
        sources=frozenset({"PERSONAL", "CF"}),
        personal_score=0.8,
        cf_score=0.5,
        ar_score=None,
    )
    profile = HomeProfileInput(
        category_scores={"22222222-2222-4222-8222-222222222222": 0.7},
        brand_scores={"33333333-3333-4333-8333-333333333333": 0.4},
        shop_scores={"44444444-4444-4444-8444-444444444444": 0.2},
        price_p25=80.0,
        price_p75=120.0,
    )
    vector = build_home_feature_vector(candidate, profile, normalizer, as_of)
    assert len(vector) == len(HOME_FEATURE_ORDER)
    assert vector[0] == 0.0
    assert vector[6] == 1.0
    assert vector[11] == 1.0
    assert vector[12] == 1.0
    assert abs(vector[8] - 0.5) < 1e-9
    assert vector[9] == 0.0
    assert vector[14] == 0.0


def test_sparse_rating_defaults_half():
    as_of = datetime(2026, 8, 1, tzinfo=timezone.utc)
    normalizer = PopularityNormalizer(z_lo=0.0, z_hi=1.0)
    candidate = HomeCandidateInput(
        product_id="p",
        category_id="c",
        brand_id=None,
        shop_id="s",
        effective_price=None,
        created_at=as_of,
        rating_avg=5.0,
        rating_count=2,
        popularity_raw=0,
        sources=frozenset({"POPULAR"}),
    )
    profile = HomeProfileInput({}, {}, {}, None, None)
    vector = build_home_feature_vector(candidate, profile, normalizer, as_of)
    assert vector[2] == 0.5
    assert vector[10] == 1.0

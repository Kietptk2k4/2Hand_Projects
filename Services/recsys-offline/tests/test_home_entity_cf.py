"""Unit tests for Home entity CF pair construction and HYBRID max merge."""

from datetime import datetime, timedelta, timezone

from pipelines.home_entity_cf import (
    CartLine,
    OrderLine,
    accumulate_cart_pairs,
    accumulate_completed_pairs,
    hybrid_max_merge,
    scores_from_weights,
)


AS_OF = datetime(2026, 8, 1, tzinfo=timezone.utc)


def test_completed_order_pairs_no_self():
    lines = [
        OrderLine("o1", "u1", "c1", "b1", AS_OF - timedelta(days=1), "COMPLETED"),
        OrderLine("o1", "u1", "c2", "b2", AS_OF - timedelta(days=1), "COMPLETED"),
    ]
    weights = accumulate_completed_pairs(lines, AS_OF)
    assert ("LEAF_CATEGORY", "c1", "LEAF_CATEGORY", "c1") not in weights
    assert weights[("LEAF_CATEGORY", "c1", "BRAND", "b1")] == 1.0
    assert weights[("LEAF_CATEGORY", "c1", "LEAF_CATEGORY", "c2")] == 1.0


def test_cart_pairs_use_0_6_within_24h():
    t0 = AS_OF - timedelta(days=2)
    lines = [
        CartLine("u1", "c1", None, t0),
        CartLine("u1", "c2", None, t0 + timedelta(hours=2)),
    ]
    weights = accumulate_cart_pairs(lines, AS_OF)
    assert weights[("LEAF_CATEGORY", "c1", "LEAF_CATEGORY", "c2")] == 0.6


def test_hybrid_max_merge_not_sum():
    real = {("LEAF_CATEGORY", "c1", "BRAND", "b1"): 2.0}
    seed = {("LEAF_CATEGORY", "c1", "BRAND", "b1"): 1.1}
    merged = hybrid_max_merge(real, seed)
    assert merged[("LEAF_CATEGORY", "c1", "BRAND", "b1")] == 2.0


def test_scores_log1p_and_top_m():
    weights = {
        ("LEAF_CATEGORY", "c1", "BRAND", f"b{i}"): float(i + 1) for i in range(3)
    }
    rows = scores_from_weights(weights, top_m=2)
    assert len(rows) == 2
    assert rows[0]["neighbor_id"] == "b2"
    import math

    assert abs(rows[0]["score"] - math.log1p(3.0)) < 1e-9

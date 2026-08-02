from datetime import datetime, timedelta, timezone

from pipelines.home_ar import Basket, mine_tag_category_rules
from pipelines.home_social_export import SocialEvent, build_social_interest_export
from pipelines.home_train_mode import validate_mode


def test_social_export_weights_and_decay():
    as_of = datetime(2026, 8, 1, tzinfo=timezone.utc)
    events = [
        SocialEvent("u1", "HASHTAG", "Jordan", "search", as_of - timedelta(days=1)),
        SocialEvent("u1", "HASHTAG", "Jordan", "save", as_of - timedelta(days=1)),
    ]
    rows = build_social_interest_export(events, as_of)
    assert len(rows) == 1
    assert rows[0]["tag"] == "jordan"
    assert rows[0]["score"] > 0


def test_ar_tag_to_category_only():
    baskets = [
        Basket("u1", frozenset({("HASHTAG", "gaming")}), frozenset({"cat-laptop"})),
        Basket("u2", frozenset({("HASHTAG", "gaming")}), frozenset({"cat-laptop"})),
        Basket("u3", frozenset({("HASHTAG", "fashion")}), frozenset({"cat-tee"})),
    ]
    # With n=3, gaming→laptop support=2/3
    rules = mine_tag_category_rules(baskets, min_support=0.5, min_confidence=0.5)
    assert any(r["tag"] == "gaming" and r["category_id"] == "cat-laptop" for r in rules)
    assert all("tag" in r and "category_id" in r for r in rules)


def test_validate_mode_rejects_unknown():
    assert validate_mode("hybrid") == "HYBRID"
    try:
        validate_mode("NOPE")
        assert False, "expected ValueError"
    except ValueError:
        pass

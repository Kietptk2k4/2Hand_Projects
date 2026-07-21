from datetime import datetime, timedelta, timezone

from pipelines.build_dataset import build_rows
from pipelines.features import cross_domain_score, filter_before


def test_cross_domain_weights():
    assert cross_domain_score({"c1"}, set(), {"c1"}, set()) == 0.6
    assert cross_domain_score(set(), {"s1"}, set(), {"s1"}) == 0.4
    assert cross_domain_score(set(), set(), {"c1"}, {"s1"}) == 0.0


def test_filter_before_excludes_post_impression_history():
    shown = datetime(2026, 1, 2, tzinfo=timezone.utc)
    rows = [
        {"created_at": (shown - timedelta(days=1)).isoformat(), "post_id": "a"},
        {"created_at": (shown + timedelta(hours=1)).isoformat(), "post_id": "b"},
    ]
    kept = filter_before(rows, shown)
    assert len(kept) == 1
    assert kept[0]["post_id"] == "a"


def test_build_rows_schema_and_label():
    shown = datetime(2026, 1, 1, 12, tzinfo=timezone.utc)
    raw = {
        "posts": [
            {
                "post_id": "p1",
                "author_id": "11111111-1111-1111-1111-111111111111",
                "hashtags": '["sneaker"]',
                "like_count": 3,
                "reply_count": 1,
                "created_at": (shown - timedelta(days=1)).isoformat(),
                "product_tags": "[]",
            }
        ],
        "comments": [],
        "post_likes": [
            {
                "user_id": "u1",
                "post_id": "p1",
                "created_at": (shown + timedelta(hours=1)).isoformat(),
            }
        ],
        "post_saves": [],
        "follows": [],
        "search_history": [],
        "post_impression_log": [
            {
                "user_id": "u1",
                "post_id": "p1",
                "shown_at": shown.isoformat(),
                "request_id": "r1",
                "rank_position": 1,
            }
        ],
        "user_purchase_profile": [],
    }
    rows, summary = build_rows(raw)
    assert summary["rows"] == 1
    assert rows[0]["label"] == 1
    for col in (
        "recency_score",
        "engagement_score",
        "hashtag_match_score",
        "author_affinity_score",
        "mutual_follow_score",
        "cross_domain_product_score",
        "label",
    ):
        assert col in rows[0]

from collections import Counter

from pipelines.normalize import (
    clean_follows,
    clean_posts,
    clean_user_post_events,
    normalize_hashtag,
    normalize_hashtags,
)


def test_normalize_hashtag_strips_hash_and_lowercases():
    assert normalize_hashtag("#Sneaker ") == "sneaker"
    assert normalize_hashtags(["#A", "a", "#B", ""]) == ["a", "b"]


def test_clean_posts_drops_null_author_and_draft():
    rows = [
        {"_id": "p1", "author_id": None, "created_at": "2026-01-01T00:00:00Z", "status": "ACTIVE"},
        {
            "_id": "p2",
            "author_id": "11111111-1111-1111-1111-111111111111",
            "created_at": "2026-01-01T00:00:00Z",
            "status": "DRAFT",
            "hashtags": ["#Sneaker"],
        },
        {
            "_id": "p3",
            "author_id": "11111111-1111-1111-1111-111111111111",
            "created_at": "2026-01-01T00:00:00Z",
            "status": "ACTIVE",
            "hashtags": ["#Sneaker ", "sneaker"],
        },
    ]
    kept, drops = clean_posts(rows)
    assert len(kept) == 1
    assert kept[0]["post_id"] == "p3"
    assert kept[0]["hashtags"] == ["sneaker"]
    assert drops["null_author"] == 1
    assert drops["status_draft"] == 1


def test_clean_follows_rejects_self_follow():
    uid = "11111111-1111-1111-1111-111111111111"
    kept, drops = clean_follows(
        [
            {
                "follower_id": uid,
                "followee_id": uid,
                "status": "ACCEPTED",
                "created_at": "2026-01-01T00:00:00Z",
            }
        ]
    )
    assert kept == []
    assert drops["self_follow"] == 1


def test_clean_likes_deduplicates():
    uid = "11111111-1111-1111-1111-111111111111"
    rows = [
        {"user_id": uid, "post_id": "p1", "created_at": "2026-01-01T00:00:00Z"},
        {"user_id": uid, "post_id": "p1", "created_at": "2026-01-02T00:00:00Z"},
    ]
    kept, drops = clean_user_post_events(rows, entity="likes")
    assert len(kept) == 1
    assert drops["duplicate_likes"] == 1

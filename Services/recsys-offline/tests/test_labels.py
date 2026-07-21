from datetime import datetime, timedelta, timezone

from pipelines.labels import assign_label


def test_label_positive_within_24h():
    shown = datetime(2026, 1, 1, tzinfo=timezone.utc)
    likes = [
        {
            "user_id": "u1",
            "post_id": "p1",
            "created_at": (shown + timedelta(hours=2)).isoformat(),
        }
    ]
    assert assign_label(user_id="u1", post_id="p1", shown_at=shown, likes=likes, saves=[], comments=[]) == 1


def test_label_zero_when_like_after_window():
    shown = datetime(2026, 1, 1, tzinfo=timezone.utc)
    likes = [
        {
            "user_id": "u1",
            "post_id": "p1",
            "created_at": (shown + timedelta(hours=25)).isoformat(),
        }
    ]
    assert assign_label(user_id="u1", post_id="p1", shown_at=shown, likes=likes, saves=[], comments=[]) == 0


def test_label_zero_when_like_before_impression():
    shown = datetime(2026, 1, 1, tzinfo=timezone.utc)
    likes = [
        {
            "user_id": "u1",
            "post_id": "p1",
            "created_at": (shown - timedelta(hours=1)).isoformat(),
        }
    ]
    assert assign_label(user_id="u1", post_id="p1", shown_at=shown, likes=likes, saves=[], comments=[]) == 0

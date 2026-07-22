from datetime import datetime, timedelta, timezone

import pytest

from pipelines.split_dataset import (
    assert_temporal_integrity,
    build_split_report,
    order_rows,
    split_rows,
    _jaccard_pct,
)


def _row(i: int, user: str = "u1", post: str = "p1", label: int = 0, day: int | None = None) -> dict:
    base = datetime(2026, 1, 1, tzinfo=timezone.utc)
    offset = day if day is not None else i
    return {
        "user_id": user,
        "post_id": f"{post}-{i}" if post == "p1" else post,
        "shown_at": (base + timedelta(days=offset)).isoformat().replace("+00:00", "Z"),
        "label": label,
    }


def test_split_ratio_eighty_ten_ten():
    rows = [_row(i) for i in range(20)]
    parts = split_rows(rows)
    assert len(parts["train"]) == 16
    assert len(parts["val"]) == 2
    assert len(parts["test"]) == 2


def test_sort_order_preserved_with_tie_break():
    rows = [
        _row(1, user="u2", post="p-b", day=1),
        _row(0, user="u1", post="p-a", day=1),
        _row(2, user="u1", post="p-c", day=0),
    ]
    ordered = order_rows(rows)
    assert [r["post_id"] for r in ordered] == ["p-c", "p-a", "p-b"]


def test_temporal_violation_fails():
    parts = {
        "train": [_row(2, day=2)],
        "val": [_row(1, day=1)],
        "test": [],
    }
    with pytest.raises(ValueError, match="Temporal leak"):
        assert_temporal_integrity(parts)


def test_jaccard_and_overlap_report():
    assert _jaccard_pct({"a", "b"}, {"b", "c"}) == pytest.approx(100.0 / 3.0)
    parts = {
        "train": [_row(0, user="u1", post="shared", day=0), _row(1, user="u2", post="t-only", day=1)],
        "val": [_row(2, user="u1", post="shared", day=2)],
        "test": [_row(3, user="u3", post="x", day=3)],
    }
    report = build_split_report(parts, total_input_rows=4)
    assert report["user_overlap_pct"]["train_val"] == pytest.approx(50.0)  # {u1,u2} ∩ {u1} / ∪ = 1/2
    assert report["post_overlap_pct"]["train_val"] == pytest.approx(50.0)
    assert report["temporal_ok"] is True


def test_tiny_n_empty_slice_warnings():
    parts = split_rows([_row(0), _row(1)])
    report = build_split_report(parts, total_input_rows=2)
    assert "small_n" in report["warnings"]
    # n=2 → train=1, val=0, test=1
    assert parts["train"]
    assert not parts["val"]
    assert parts["test"]
    assert "empty_val" in report["warnings"]

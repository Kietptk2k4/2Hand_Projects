"""Tests for Commerce → user_purchase_profile export (as-of T_cut)."""

from datetime import datetime, timezone

from pipelines.export_purchase_profile import (
    aggregate_purchase_profiles,
    run_export_purchase_profile,
    write_purchase_profile_csv,
)


def _row(buyer, cat, shop, completed, status="COMPLETED"):
    return {
        "buyer_id": buyer,
        "category_id": cat,
        "shop_id": shop,
        "completed_at": completed,
        "order_status": status,
    }


def test_aggregate_excludes_post_cutoff_purchases():
    t_cut = datetime(2026, 1, 10, tzinfo=timezone.utc)
    rows = [
        _row("u1", "c-old", "s1", "2026-01-05T00:00:00Z"),
        _row("u1", "c-new", "s2", "2026-01-15T00:00:00Z"),
        _row("u2", "c2", "s3", "2026-01-01T00:00:00Z", status="CREATED"),
    ]
    profiles = {p["user_id"]: p for p in aggregate_purchase_profiles(rows, as_of=t_cut)}
    assert "u1" in profiles
    assert profiles["u1"]["category_ids"] == ["c-old"]
    assert profiles["u1"]["shop_ids"] == ["s1"]
    assert "u2" not in profiles


def test_aggregate_without_as_of_includes_all_completed():
    rows = [
        _row("u1", "c-old", "s1", "2026-01-05T00:00:00Z"),
        _row("u1", "c-new", "s2", "2026-01-15T00:00:00Z"),
    ]
    profiles = aggregate_purchase_profiles(rows, as_of=None)
    assert profiles[0]["category_ids"] == ["c-new", "c-old"]
    assert set(profiles[0]["shop_ids"]) == {"s1", "s2"}


def test_run_export_writes_csv_without_db(tmp_path):
    from app.config import Settings

    rows = [
        _row("u1", "c1", "s1", "2026-01-05T00:00:00Z"),
        _row("u1", "c2", "s1", "2026-01-20T00:00:00Z"),
    ]
    settings = Settings(recsys_dataset_output_dir=str(tmp_path))
    summary = run_export_purchase_profile(
        settings,
        as_of="2026-01-10T00:00:00Z",
        order_rows=rows,
    )
    assert summary["users"] == 1
    assert summary["provisional"] is False
    out = tmp_path / "user_purchase_profile.csv"
    assert out.exists()
    text = out.read_text(encoding="utf-8")
    assert "c1" in text
    assert "c2" not in text


def test_write_csv_shape(tmp_path):
    path = tmp_path / "user_purchase_profile.csv"
    write_purchase_profile_csv(
        path,
        [{"user_id": "u1", "category_ids": ["c1"], "shop_ids": ["s1"]}],
    )
    lines = path.read_text(encoding="utf-8").strip().splitlines()
    assert lines[0] == "user_id,category_ids,shop_ids"

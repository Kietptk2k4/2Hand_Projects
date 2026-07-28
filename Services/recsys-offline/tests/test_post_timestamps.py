"""Tests for BSON Date post timestamps (writer + repair)."""

from __future__ import annotations

from datetime import datetime, timedelta, timezone

import pytest

from simulation.repair_post_timestamps import (
    apply_timestamp_repairs,
    created_at_needs_repair,
    select_docs_needing_repair,
)
from simulation.skeleton import build_skeleton
from simulation.writers import build_social_post_documents


def test_build_social_post_documents_use_datetime_not_string():
    skeleton = build_skeleton(scale="smoke")
    end = datetime(2026, 7, 26, 12, 0, tzinfo=timezone.utc)
    docs = build_social_post_documents(skeleton, end_at=end, sim_days=21)
    assert len(docs) == len(skeleton.posts) > 1
    created = [d["created_at"] for d in docs]
    assert all(isinstance(ts, datetime) for ts in created)
    assert all(not isinstance(ts, str) for ts in created)
    assert all(ts.tzinfo is not None for ts in created)
    assert len(set(created)) > 1
    assert min(created) >= end - timedelta(days=21)
    assert max(created) <= end
    assert "2025-12-20" not in str(created)


def test_created_at_string_always_needs_repair():
    now = datetime(2026, 7, 26, tzinfo=timezone.utc)
    needs, bucket = created_at_needs_repair(
        "2025-12-20T10:00:00Z", now=now, window_days=21
    )
    assert needs is True
    assert bucket == "string"


def test_select_and_apply_repairs_without_mutation():
    now = datetime(2026, 7, 26, 12, 0, tzinfo=timezone.utc)
    original = [
        {
            "_id": "a",
            "caption": "Sim street look",
            "created_at": "2025-12-20T10:00:00Z",
        },
        {
            "_id": "b",
            "caption": "Sim chic look",
            "created_at": "2025-12-20T10:00:00Z",
        },
        {
            "_id": "c",
            "caption": "Real user post",
            "created_at": now - timedelta(days=2),
        },
    ]
    summary, repair_ids = select_docs_needing_repair(
        original, now=now, window_days=21
    )
    assert summary.dry_run is True
    assert summary.string_created_at == 2
    assert summary.would_update == 2
    assert repair_ids == ["a", "b"]
    # dry-run path: original untouched
    assert original[0]["created_at"] == "2025-12-20T10:00:00Z"

    repaired = apply_timestamp_repairs(
        original, repair_ids, now=now, window_days=21
    )
    assert original[0]["created_at"] == "2025-12-20T10:00:00Z"
    assert isinstance(repaired[0]["created_at"], datetime)
    assert isinstance(repaired[1]["created_at"], datetime)
    window_start = now - timedelta(days=21)
    assert window_start <= repaired[0]["created_at"] <= now
    assert window_start <= repaired[1]["created_at"] <= now
    assert repaired[0]["created_at"] != repaired[1]["created_at"]
    assert repaired[2]["created_at"] == original[2]["created_at"]


def test_build_social_user_projections_cover_all_users():
    from simulation.writers import build_social_user_projection_documents

    skeleton = build_skeleton(scale="smoke")
    docs = build_social_user_projection_documents(skeleton)
    assert len(docs) == len(skeleton.users)
    assert all(d["status"] == "ACTIVE" for d in docs)
    assert all(d["user_id"] == d["_id"] for d in docs)
    assert {d["user_id"] for d in docs} == {u["user_id"] for u in skeleton.users}


def test_repair_cli_requires_sim_allow(monkeypatch):
    from simulation import cli as sim_cli

    monkeypatch.delenv("RECSYS_SIM_ALLOW", raising=False)
    monkeypatch.setenv("SOCIAL_MONGO_URL", "mongodb://localhost:27017")
    # Force fresh settings without allow flag
    from app import config as app_config

    monkeypatch.setattr(
        app_config,
        "get_settings",
        lambda: app_config.Settings(
            social_mongo_url="mongodb://localhost:27017",
            recsys_sim_allow=False,
        ),
    )
    monkeypatch.setattr(sim_cli, "get_settings", app_config.get_settings)
    with pytest.raises(ValueError, match="RECSYS_SIM_ALLOW"):
        sim_cli.cmd_repair_post_timestamps(
            type(
                "Args",
                (),
                {
                    "window_days": 21,
                    "apply": False,
                    "caption_prefix": "Sim ",
                    "all_posts": False,
                },
            )()
        )

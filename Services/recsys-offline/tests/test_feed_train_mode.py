"""Tests for Social feed train-mode (file seed) pipeline."""

from __future__ import annotations

import csv
import json
from pathlib import Path

import pytest

from pipelines.feed_build_dataset import build_feed_dataset
from pipelines.feed_orchestrator import FeedRetrainOrchestrator
from pipelines.feed_sim import run_feed_sim_to_dir
from pipelines.feed_train_mode import (
    FeedTrainModeConfig,
    resolve_feed_train_mode,
    resolve_from_env,
    validate_mode,
)


def test_validate_mode_rejects_invalid():
    with pytest.raises(ValueError, match="Invalid train_data_mode"):
        validate_mode("SEED")


def test_resolve_from_env(monkeypatch):
    monkeypatch.setenv("RECSYS_FEED_CONFIG_FROM_ENV", "1")
    monkeypatch.setenv("FEED_LTR_TRAIN_DATA_MODE", "HYBRID")
    monkeypatch.setenv("FEED_LTR_SEED_ROW_WEIGHT", "0.25")
    monkeypatch.setenv("FEED_LTR_REAL_ONLY_MIN_IMPRESSIONS", "100")
    cfg = resolve_from_env()
    assert cfg is not None
    assert cfg.train_data_mode == "HYBRID"
    assert cfg.seed_row_weight == 0.25
    assert cfg.real_only_min_impressions == 100


def test_resolve_feed_train_mode_requires_admin_without_env(monkeypatch):
    monkeypatch.delenv("RECSYS_FEED_CONFIG_FROM_ENV", raising=False)
    with pytest.raises(RuntimeError, match="Admin credentials"):
        resolve_feed_train_mode()


def test_feed_sim_writes_files_only(tmp_path: Path):
    summary = run_feed_sim_to_dir(tmp_path, scale="smoke", seed=1)
    assert summary["shared_db_writes"] is False
    assert (tmp_path / "posts.csv").exists()
    assert (tmp_path / "post_impression_log.csv").exists()
    assert (tmp_path / "sim_summary.json").exists()


def test_feed_sim_module_does_not_import_writers():
    import pipelines.feed_sim as feed_sim

    source = Path(feed_sim.__file__).read_text(encoding="utf-8")
    assert "simulation.writers" not in source
    assert "write_auth_users" not in source


def _write_minimal_csvs(dir_path: Path, *, tag: str, n: int = 4) -> None:
    dir_path.mkdir(parents=True, exist_ok=True)
    posts = [
        {
            "post_id": f"{tag}-p{i}",
            "author_id": f"{tag}-a{i}",
            "hashtags": "[]",
            "like_count": 1,
            "reply_count": 0,
            "created_at": "2026-01-01T10:00:00Z",
            "product_tags": "[]",
            "status": "ACTIVE",
            "visibility": "PUBLIC",
        }
        for i in range(n)
    ]
    impressions = [
        {
            "user_id": f"{tag}-u0",
            "post_id": f"{tag}-p{i}",
            "shown_at": f"2026-01-02T10:0{i}:00Z",
            "request_id": f"{tag}-r0",
            "rank_position": i,
            "model_version": "",
        }
        for i in range(n)
    ]
    for name, rows in {
        "posts": posts,
        "post_impression_log": impressions,
        "comments": [],
        "post_likes": [],
        "post_saves": [],
        "follows": [],
        "search_history": [],
        "user_purchase_profile": [],
    }.items():
        path = dir_path / f"{name}.csv"
        if not rows:
            path.write_text("", encoding="utf-8")
            continue
        with path.open("w", encoding="utf-8", newline="") as handle:
            writer = csv.DictWriter(handle, fieldnames=list(rows[0].keys()))
            writer.writeheader()
            writer.writerows(rows)


def test_seed_only_build(tmp_path: Path):
    seed = tmp_path / "seed"
    out = tmp_path / "out"
    _write_minimal_csvs(seed, tag="s")
    captured: dict = {}

    def _capture(path, rows):
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text("parquet-stub", encoding="utf-8")
        captured["rows"] = rows

    from unittest.mock import patch

    with patch("pipelines.feed_build_dataset._write_parquet", side_effect=_capture):
        summary = build_feed_dataset(
            mode_cfg=FeedTrainModeConfig("SEED_ONLY", 0.5, 5000),
            seed_dir=seed,
            real_dir=None,
            output_dir=out,
        )
    assert summary["train_data_mode"] == "SEED_ONLY"
    assert summary["rows"] == 4
    assert all(r["data_source"] == "SEED" for r in captured["rows"])
    assert all(r["sample_weight"] == 1.0 for r in captured["rows"])
    meta = json.loads((out / "dataset_meta.json").read_text(encoding="utf-8"))
    assert meta["seed_impressions"] == 4


def test_hybrid_applies_seed_weight(tmp_path: Path):
    seed = tmp_path / "seed"
    real = tmp_path / "real"
    out = tmp_path / "out"
    _write_minimal_csvs(seed, tag="s", n=2)
    _write_minimal_csvs(real, tag="r", n=2)
    captured: dict = {}

    def _capture(path, rows):
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text("parquet-stub", encoding="utf-8")
        captured["rows"] = rows

    from unittest.mock import patch

    with patch("pipelines.feed_build_dataset._write_parquet", side_effect=_capture):
        summary = build_feed_dataset(
            mode_cfg=FeedTrainModeConfig("HYBRID", 0.5, 5000),
            seed_dir=seed,
            real_dir=real,
            output_dir=out,
        )
    assert summary["rows"] == 4
    assert summary["seed_impressions"] == 2
    assert summary["real_impressions"] == 2
    weights = {r["data_source"]: r["sample_weight"] for r in captured["rows"]}
    assert weights["SEED"] == 0.5
    assert weights["REAL"] == 1.0


def test_real_only_fail_closed(tmp_path: Path):
    real = tmp_path / "real"
    out = tmp_path / "out"
    _write_minimal_csvs(real, tag="r", n=3)
    with pytest.raises(ValueError, match="REAL_ONLY requires"):
        build_feed_dataset(
            mode_cfg=FeedTrainModeConfig("REAL_ONLY", 0.5, 5000),
            seed_dir=None,
            real_dir=real,
            output_dir=out,
        )


def test_orchestrator_skips_sim_on_real_only():
    orch = FeedRetrainOrchestrator()
    ran = orch.run(mode_cfg=FeedTrainModeConfig("REAL_ONLY", 0.5, 10), sim_dir=None, jobs={})
    assert "feed_sim" not in ran
    assert ran[0] == "resolve_mode"
    assert "clean_real" in ran


def test_orchestrator_skips_clean_on_seed_only(tmp_path: Path):
    orch = FeedRetrainOrchestrator()
    called = {"sim": 0}

    def fake_sim():
        called["sim"] += 1

    ran = orch.run(
        mode_cfg=FeedTrainModeConfig("SEED_ONLY", 0.5, 10),
        sim_dir=tmp_path,
        jobs={"feed_sim": fake_sim},
    )
    assert "feed_sim" in ran
    assert "clean_real" not in ran
    assert called["sim"] == 1

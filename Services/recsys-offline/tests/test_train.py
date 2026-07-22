from pathlib import Path

import pyarrow as pa
import pyarrow.parquet as pq
import pytest

from app.config import Settings
from pipelines.train import FEATURE_ORDER, load_xy, run_train_job, train_lightgbm


def _write_split(path: Path, n: int, label_mod: int = 3) -> None:
    rows = []
    for i in range(n):
        row = {col: float((i % 5) / 5.0) for col in FEATURE_ORDER}
        row["label"] = 1 if i % label_mod == 0 else 0
        rows.append(row)
    path.parent.mkdir(parents=True, exist_ok=True)
    pq.write_table(pa.Table.from_pylist(rows), path)


def test_missing_train_fails(tmp_path: Path):
    settings = Settings(
        recsys_dataset_output_dir=str(tmp_path / "data"),
        recsys_artifact_dir=str(tmp_path / "artifacts"),
    )
    (tmp_path / "data").mkdir()
    with pytest.raises(ValueError, match="not found"):
        run_train_job(settings)


def test_train_with_val_early_stopping(tmp_path: Path):
    data = tmp_path / "data"
    arts = tmp_path / "artifacts"
    _write_split(data / "dataset_train.parquet", 80)
    _write_split(data / "dataset_val.parquet", 20)
    summary = run_train_job(
        Settings(recsys_dataset_output_dir=str(data), recsys_artifact_dir=str(arts))
    )
    assert summary["used_early_stopping"] is True
    assert summary["feature_order"] == FEATURE_ORDER
    assert (arts / "model.txt").exists()
    assert (arts / "train_meta.json").exists()
    assert "no_early_stopping" not in summary["warnings"]


def test_train_without_val_warns(tmp_path: Path):
    data = tmp_path / "data"
    arts = tmp_path / "artifacts"
    _write_split(data / "dataset_train.parquet", 40)
    summary = run_train_job(
        Settings(recsys_dataset_output_dir=str(data), recsys_artifact_dir=str(arts))
    )
    assert summary["used_early_stopping"] is False
    assert "no_early_stopping" in summary["warnings"]
    assert (arts / "model.txt").exists()


def test_feature_order_constant():
    assert FEATURE_ORDER == [
        "recency_score",
        "engagement_score",
        "hashtag_match_score",
        "author_affinity_score",
        "mutual_follow_score",
        "cross_domain_product_score",
    ]


def test_load_xy_fills_nan(tmp_path: Path):
    path = tmp_path / "t.parquet"
    row = {col: 0.1 for col in FEATURE_ORDER}
    row["recency_score"] = float("nan")
    row["label"] = 0
    pq.write_table(pa.Table.from_pylist([row]), path)
    x, y, nan_filled = load_xy(path)
    assert y == [0]
    assert x[0][0] == 0.0
    assert nan_filled >= 1


def test_train_lightgbm_direct():
    x = [[0.1 * (i % 6) for _ in range(6)] for i in range(30)]
    y = [i % 2 for i in range(30)]
    booster, info = train_lightgbm(x, y, num_boost_round=5)
    assert info["used_early_stopping"] is False
    assert "no_early_stopping" in info["warnings"]
    assert booster is not None

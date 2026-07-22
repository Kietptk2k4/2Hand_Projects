import json
from pathlib import Path

import pytest

from pipelines.export_activate import (
    ONNX_VERIFY_THRESHOLD,
    evaluate_metric_gate,
    load_evaluate_report,
    run_export_activate_job,
    verify_onnx_parity,
    convert_lightgbm_to_onnx,
)
from app.config import Settings


def test_gate_passes_when_auc_and_precision_ge_baseline():
    report = {
        "lightgbm": {"auc": 0.8, "precision_at_10": 0.4},
        "baseline": {"auc": 0.7, "precision_at_10": 0.3},
    }
    gate = evaluate_metric_gate(report)
    assert gate["passed"] is True
    assert gate["reason"] is None
    assert gate["failed"] == []


def test_gate_fails_when_precision_below_baseline():
    report = {
        "lightgbm": {"auc": 0.9, "precision_at_10": 0.2},
        "baseline": {"auc": 0.7, "precision_at_10": 0.3},
    }
    gate = evaluate_metric_gate(report)
    assert gate["passed"] is False
    assert gate["reason"] == "rejected_by_metrics"
    assert "precision_at_10" in gate["failed"]


def test_gate_fails_closed_when_auc_undefined():
    report = {
        "lightgbm": {"auc": None, "precision_at_10": 0.4},
        "baseline": {"auc": 0.7, "precision_at_10": 0.3},
    }
    gate = evaluate_metric_gate(report)
    assert gate["passed"] is False
    assert "auc_undefined" in gate["failed"]


def test_load_evaluate_report_missing(tmp_path):
    with pytest.raises(ValueError, match="not found"):
        load_evaluate_report(tmp_path / "evaluate_report.json")


def test_run_export_missing_model(tmp_path):
    with pytest.raises(ValueError, match="Model file not found"):
        run_export_activate_job(
            Settings(
                recsys_artifact_dir=str(tmp_path),
                recsys_dataset_output_dir=str(tmp_path),
            )
        )


def test_run_export_missing_report(tmp_path):
    (tmp_path / "model.txt").write_text("placeholder", encoding="utf-8")
    with pytest.raises(ValueError, match="Evaluate report not found"):
        run_export_activate_job(
            Settings(
                recsys_artifact_dir=str(tmp_path),
                recsys_dataset_output_dir=str(tmp_path),
            )
        )


def _make_fixture(tmp_path: Path, *, auc_lgbm=0.9, auc_base=0.5, p_lgbm=0.5, p_base=0.2):
    import lightgbm as lgb
    import numpy as np
    import pandas as pd

    from pipelines.train import FEATURE_ORDER

    arts = tmp_path / "arts"
    data = tmp_path / "data"
    arts.mkdir()
    data.mkdir()

    rng = np.random.default_rng(7)
    n = 40
    rows = []
    for i in range(n):
        feats = rng.random(6).tolist()
        row = {
            "user_id": f"u{i % 4}",
            "post_id": f"p{i}",
            "request_id": f"r{i // 4}",
            "label": int(feats[1] > 0.4),
        }
        for c, v in zip(FEATURE_ORDER, feats):
            row[c] = v
        rows.append(row)
    df = pd.DataFrame(rows)
    df.to_parquet(data / "dataset_test.parquet", index=False)

    train = lgb.Dataset(
        df[FEATURE_ORDER].values,
        label=df["label"].values,
        feature_name=FEATURE_ORDER,
    )
    booster = lgb.train(
        {"objective": "binary", "metric": "auc", "verbosity": -1, "num_leaves": 8},
        train,
        num_boost_round=20,
    )
    booster.save_model(str(arts / "model.txt"))

    report = {
        "k": 10,
        "feature_order": FEATURE_ORDER,
        "baseline_weights": {
            "recency_score": 0.12,
            "engagement_score": 0.28,
            "hashtag_match_score": 0.22,
            "author_affinity_score": 0.13,
            "mutual_follow_score": 0.13,
            "cross_domain_product_score": 0.12,
        },
        "lightgbm": {
            "auc": auc_lgbm,
            "precision_at_10": p_lgbm,
            "recall_at_10": 0.5,
            "hit_rate_at_10": 0.6,
        },
        "baseline": {
            "auc": auc_base,
            "precision_at_10": p_base,
            "recall_at_10": 0.4,
            "hit_rate_at_10": 0.5,
        },
        "delta": {},
    }
    (arts / "evaluate_report.json").write_text(json.dumps(report), encoding="utf-8")
    (arts / "train_meta.json").write_text("{}", encoding="utf-8")
    return arts, data


class _FakeCursor:
    def __init__(self, store: dict):
        self.store = store
        self._result = None

    def execute(self, sql, params=None):
        sql_norm = " ".join(sql.split()).lower()
        if "coalesce(max(version)" in sql_norm:
            self._result = (self.store["next_version"],)
        elif sql_norm.startswith("update model_artifacts"):
            self.store["deactivated"] = True
        elif sql_norm.startswith("insert into model_artifacts"):
            self.store["inserted"] = {
                "model_name": params[0],
                "version": params[1],
                "format": params[2],
                "artifact_path": params[3],
                "metrics": params[4],
                "is_active": params[5],
            }
        else:
            raise AssertionError(f"Unexpected SQL: {sql}")

    def fetchone(self):
        return self._result

    def __enter__(self):
        return self

    def __exit__(self, *args):
        return False


class _FakeConn:
    def __init__(self, store: dict):
        self.store = store

    def cursor(self):
        return _FakeCursor(self.store)

    def commit(self):
        self.store["committed"] = True

    def __enter__(self):
        return self

    def __exit__(self, *args):
        return False


def test_onnx_convert_and_parity(tmp_path):
    arts, data = _make_fixture(tmp_path)
    onnx_path = arts / "smoke.onnx"
    convert_lightgbm_to_onnx(arts / "model.txt", onnx_path)
    result = verify_onnx_parity(arts / "model.txt", onnx_path, data / "dataset_test.parquet")
    assert result["passed"] is True
    assert result["n_samples"] >= 32
    assert result["max_abs_diff"] <= ONNX_VERIFY_THRESHOLD


def test_smoke_export_activates_when_gate_passes(tmp_path):
    arts, data = _make_fixture(tmp_path, auc_lgbm=0.9, auc_base=0.5, p_lgbm=0.5, p_base=0.2)
    store = {"next_version": 1}

    def factory():
        return _FakeConn(store)

    summary = run_export_activate_job(
        Settings(
            recsys_artifact_dir=str(arts),
            recsys_dataset_output_dir=str(data),
            social_postgres_url="postgresql://unused",
        ),
        connect_factory=factory,
    )
    assert summary["status"] == "activated"
    assert summary["version"] == 1
    assert store["inserted"]["is_active"] is True
    assert (arts / "feed_ranker_v1.onnx").exists()


def test_smoke_export_rejects_when_gate_fails(tmp_path):
    arts, data = _make_fixture(tmp_path, auc_lgbm=0.4, auc_base=0.8, p_lgbm=0.1, p_base=0.4)
    store = {"next_version": 2}

    summary = run_export_activate_job(
        Settings(
            recsys_artifact_dir=str(arts),
            recsys_dataset_output_dir=str(data),
            social_postgres_url="postgresql://unused",
        ),
        connect_factory=lambda: _FakeConn(store),
    )
    assert summary["status"] == "exported_not_activated"
    assert store["inserted"]["is_active"] is False
    metrics = json.loads(store["inserted"]["metrics"])
    assert metrics["gate"]["reason"] == "rejected_by_metrics"

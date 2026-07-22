from pipelines.evaluate import (
    ranking_at_k,
    roc_auc,
    rule_based_score,
    _topk_metrics_for_group,
    run_evaluate_job,
)
from app.config import Settings


def test_rule_based_weights_include_cross_domain():
    feats = [0.0, 0.0, 0.0, 0.0, 0.0, 1.0]
    assert abs(rule_based_score(feats) - 0.12) < 1e-9
    assert abs(rule_based_score([1.0] * 6) - 1.0) < 1e-9


def test_precision_uses_min_k_n():
    # 7 candidates, 2 relevant in all 7 → precision = 2/7
    labels = [1, 0, 1, 0, 0, 0, 0]
    scores = [0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3]
    m = _topk_metrics_for_group(labels, scores, k=10)
    assert m is not None
    assert abs(m["precision"] - (2 / 7)) < 1e-9
    assert m["hit_rate"] == 1.0
    assert abs(m["recall"] - 1.0) < 1e-9


def test_hit_and_recall_no_relevant():
    labels = [0, 0, 0]
    scores = [0.3, 0.2, 0.1]
    m = _topk_metrics_for_group(labels, scores, k=10)
    assert m is not None
    assert m["hit_rate"] == 0.0
    assert m["recall"] is None


def test_ranking_at_k_macro():
    groups = {
        "r1": [(1, 0.9), (0, 0.1)],
        "r2": [(0, 0.8), (0, 0.2)],
    }
    out = ranking_at_k(groups, k=10)
    assert out["groups_used"] == 2
    assert out["hit_rate_at_10"] == 0.5
    assert out["recall_groups_used"] == 1


def test_roc_auc_undefined_single_class():
    assert roc_auc([1, 1, 1], [0.1, 0.2, 0.3]) is None


def test_smoke_evaluate_writes_report(tmp_path):
    import lightgbm as lgb
    import numpy as np
    import pandas as pd

    cols = [
        "recency_score",
        "engagement_score",
        "hashtag_match_score",
        "author_affinity_score",
        "mutual_follow_score",
        "cross_domain_product_score",
    ]
    data_dir = tmp_path / "data"
    arts = tmp_path / "arts"
    data_dir.mkdir()
    arts.mkdir()
    rng = np.random.default_rng(1)
    rows = []
    for i in range(32):
        feats = rng.random(6).tolist()
        row = {
            "user_id": f"u{i % 4}",
            "post_id": f"p{i}",
            "request_id": f"r{i // 4}",
            "shown_at": f"2026-01-01T00:{i:02d}:00Z",
            "label": int(feats[1] > 0.4),
        }
        for c, v in zip(cols, feats):
            row[c] = v
        rows.append(row)
    df = pd.DataFrame(rows)
    df.to_parquet(data_dir / "dataset_test.parquet", index=False)
    train = lgb.Dataset(df[cols].values, label=df["label"].values, feature_name=cols)
    booster = lgb.train(
        {"objective": "binary", "metric": "auc", "verbosity": -1, "num_leaves": 8},
        train,
        num_boost_round=15,
    )
    booster.save_model(str(arts / "model.txt"))

    report = run_evaluate_job(
        Settings(
            recsys_dataset_output_dir=str(data_dir),
            recsys_artifact_dir=str(arts),
        )
    )
    assert (arts / "evaluate_report.json").exists()
    assert report["test_rows"] == 32
    assert report["group_by"] == "request_id"
    assert "auc" in report["lightgbm"]
    assert "auc" in report["baseline"]
    assert report["k"] == 10

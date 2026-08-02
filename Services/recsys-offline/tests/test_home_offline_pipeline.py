from datetime import datetime, timedelta, timezone

from pipelines.home_dataset_job import split_by_request_id
from pipelines.home_evaluate import evaluate_home_rows, gate_passes, precision_at_k_by_request
from pipelines.home_labels import Engage, Impression, assign_nearest_impression_labels
from pipelines.home_orchestrator import HomeRetrainOrchestrator
from pipelines.home_sim import run_home_sim_to_dir
from pipelines.home_train_mode import HomeTrainModeConfig


def test_nearest_impression_only():
    t0 = datetime(2026, 8, 1, tzinfo=timezone.utc)
    imps = [
        Impression("u", "p", t0, "r1"),
        Impression("u", "p", t0 + timedelta(hours=2), "r2"),
    ]
    engs = [Engage("u", "p", t0 + timedelta(hours=3))]
    labeled = assign_nearest_impression_labels(imps, engs)
    assert labeled[0][1] == 0
    assert labeled[1][1] == 1


def test_precision_at_10_groups_by_request_id():
    rows = [
        {"request_id": "a", "label": 1, "s": 0.9},
        {"request_id": "a", "label": 0, "s": 0.1},
        {"request_id": "b", "label": 1, "s": 0.2},
        {"request_id": "b", "label": 0, "s": 0.8},
    ]
    # request a: top1 is label1 → p=1; request b: top1 is label0 → p=0; mean=0.5
    p = precision_at_k_by_request(
        [{"request_id": r["request_id"], "label": r["label"], "score": r["s"]} for r in rows],
        "score",
        k=1,
    )
    assert p == 0.5


def test_gate_requires_both_metrics():
    report = {
        "lightgbm": {"auc": 0.8, "precision_at_10": 0.2},
        "baseline": {"auc": 0.7, "precision_at_10": 0.3},
    }
    assert gate_passes(report) is False
    report["lightgbm"]["precision_at_10"] = 0.3
    assert gate_passes(report) is True


def test_orchestrator_order_skips_sim_for_real_only():
    orch = HomeRetrainOrchestrator()
    order = orch.run(mode_cfg=HomeTrainModeConfig("REAL_ONLY"))
    assert "home_sim" not in order
    assert order[0] == "resolve_mode"
    assert order[-1] == "export_activate"
    assert order.index("entity_cf") < order.index("build_dataset")


def test_home_sim_writes_files_only(tmp_path):
    summary = run_home_sim_to_dir(tmp_path, n_users=8, n_products=16, sessions_per_user=1, seed=1)
    assert summary["impressions"] > 0
    assert (tmp_path / "home_impression_log.jsonl").exists()
    assert (tmp_path / "entity_cooccur.jsonl").exists()
    assert (tmp_path / "social_tag_category_ar.jsonl").exists()


def test_split_by_request_id_keeps_groups_together():
    rows = [
        {"request_id": "r1", "label": 1},
        {"request_id": "r1", "label": 0},
        {"request_id": "r2", "label": 1},
        {"request_id": "r3", "label": 0},
        {"request_id": "r4", "label": 1},
        {"request_id": "r5", "label": 0},
    ]
    train, val, test = split_by_request_id(rows, seed=0)
    for split in (train, val, test):
        ids = {r["request_id"] for r in split}
        for rid in ids:
            assert all(
                (r["request_id"] != rid) or (r in split)
                for r in rows
            )
    all_ids = {r["request_id"] for r in rows}
    split_ids = {r["request_id"] for r in train + val + test}
    assert all_ids == split_ids

"""Offline evaluate: LightGBM vs rule-based baseline metrics + JSON report."""

from __future__ import annotations

import json
import logging
import math
from collections import defaultdict
from pathlib import Path
from typing import Any

from app.config import Settings, get_settings
from pipelines.train import FEATURE_ORDER, load_xy

logger = logging.getLogger(__name__)

K = 10
BASELINE_WEIGHTS = {
    "recency_score": 0.12,
    "engagement_score": 0.28,
    "hashtag_match_score": 0.22,
    "author_affinity_score": 0.13,
    "mutual_follow_score": 0.13,
    "cross_domain_product_score": 0.12,
}


def rule_based_score(features: list[float] | dict[str, float]) -> float:
    if isinstance(features, dict):
        return sum(float(features.get(col, 0.0) or 0.0) * w for col, w in BASELINE_WEIGHTS.items())
    return sum(float(features[i]) * BASELINE_WEIGHTS[col] for i, col in enumerate(FEATURE_ORDER))


def roc_auc(y_true: list[int], y_score: list[float]) -> float | None:
    positives = sum(1 for y in y_true if y == 1)
    negatives = len(y_true) - positives
    if positives == 0 or negatives == 0:
        return None
    try:
        from sklearn.metrics import roc_auc_score
    except ImportError as exc:
        raise RuntimeError("scikit-learn is required for ROC-AUC. pip install scikit-learn") from exc
    return float(roc_auc_score(y_true, y_score))


def _topk_metrics_for_group(labels: list[int], scores: list[float], k: int = K) -> dict[str, float] | None:
    n = len(labels)
    if n == 0:
        return None
    order = sorted(range(n), key=lambda i: scores[i], reverse=True)
    top_n = min(k, n)
    top_idx = order[:top_n]
    hits = sum(1 for i in top_idx if labels[i] == 1)
    relevant = sum(1 for y in labels if y == 1)
    precision = hits / top_n
    recall = hits / relevant if relevant > 0 else None
    hit_rate = 1.0 if hits > 0 else 0.0
    return {
        "precision": precision,
        "recall": recall,
        "hit_rate": hit_rate,
        "relevant": relevant,
    }


def ranking_at_k(
    groups: dict[str, list[tuple[int, float]]],
    k: int = K,
) -> dict[str, Any]:
    precisions: list[float] = []
    recalls: list[float] = []
    hit_rates: list[float] = []
    for rows in groups.values():
        labels = [r[0] for r in rows]
        scores = [r[1] for r in rows]
        m = _topk_metrics_for_group(labels, scores, k=k)
        if m is None:
            continue
        precisions.append(m["precision"])
        hit_rates.append(m["hit_rate"])
        if m["recall"] is not None:
            recalls.append(m["recall"])
    return {
        f"precision_at_{k}": (sum(precisions) / len(precisions)) if precisions else None,
        f"recall_at_{k}": (sum(recalls) / len(recalls)) if recalls else None,
        f"hit_rate_at_{k}": (sum(hit_rates) / len(hit_rates)) if hit_rates else None,
        "groups_used": len(precisions),
        "recall_groups_used": len(recalls),
    }


def _load_test_rows(path: Path) -> list[dict[str, Any]]:
    if not path.exists():
        raise ValueError(f"Test parquet not found: {path}")
    try:
        import pyarrow.parquet as pq
    except ImportError as exc:
        raise RuntimeError("pyarrow is required") from exc
    rows = pq.read_table(path).to_pylist()
    if not rows:
        raise ValueError(f"Test parquet is empty: {path}")
    return rows


def _predict_lgbm(model_path: Path, x_rows: list[list[float]]) -> list[float]:
    try:
        import lightgbm as lgb
        import numpy as np
    except ImportError as exc:
        raise RuntimeError("lightgbm and numpy are required for evaluate") from exc
    if not model_path.exists():
        raise ValueError(f"Model file not found: {model_path}")
    booster = lgb.Booster(model_file=str(model_path))
    preds = booster.predict(np.asarray(x_rows, dtype=np.float64))
    return [float(p) for p in preds]


def _build_groups(
    rows: list[dict[str, Any]],
    scores: list[float],
) -> tuple[dict[str, list[tuple[int, float]]], str, list[str]]:
    warnings: list[str] = []
    request_ids = [str(r.get("request_id") or "").strip() for r in rows]
    non_blank = sum(1 for r in request_ids if r)
    if non_blank >= max(1, len(rows) // 2):
        group_by = "request_id"
        keys = [rid if rid else f"missing-request:{i}" for i, rid in enumerate(request_ids)]
        if non_blank < len(rows):
            warnings.append("partial_request_id")
    else:
        group_by = "user_id"
        warnings.append("group_by_user_id")
        keys = [str(r.get("user_id") or f"missing-user:{i}") for i, r in enumerate(rows)]

    groups: dict[str, list[tuple[int, float]]] = defaultdict(list)
    for i, row in enumerate(rows):
        try:
            label = int(row.get("label", 0))
        except (TypeError, ValueError):
            continue
        if label not in (0, 1):
            continue
        groups[keys[i]].append((label, scores[i]))
    return groups, group_by, warnings


def _row_features(row: dict[str, Any]) -> list[float]:
    feats: list[float] = []
    for col in FEATURE_ORDER:
        raw = row.get(col)
        try:
            val = float(raw)
        except (TypeError, ValueError):
            feats.append(0.0)
            continue
        if math.isnan(val) or math.isinf(val):
            feats.append(0.0)
        else:
            feats.append(val)
    return feats


def evaluate_scores(
    y_true: list[int],
    scores: list[float],
    groups: dict[str, list[tuple[int, float]]],
    k: int = K,
) -> dict[str, Any]:
    auc = roc_auc(y_true, scores)
    ranking = ranking_at_k(groups, k=k)
    out = {
        "auc": auc,
        f"precision_at_{k}": ranking[f"precision_at_{k}"],
        f"recall_at_{k}": ranking[f"recall_at_{k}"],
        f"hit_rate_at_{k}": ranking[f"hit_rate_at_{k}"],
        "groups_used": ranking["groups_used"],
        "recall_groups_used": ranking["recall_groups_used"],
    }
    return out


def run_evaluate_job(settings: Settings | None = None) -> dict[str, Any]:
    settings = settings or get_settings()
    data_dir = Path(settings.recsys_dataset_output_dir)
    artifact_dir = Path(settings.recsys_artifact_dir)
    test_path = data_dir / "dataset_test.parquet"
    model_path = artifact_dir / "model.txt"

    rows = _load_test_rows(test_path)
    if not model_path.exists():
        raise ValueError(f"Model file not found: {model_path}")

    # Ensure feature columns exist via load_xy contract
    load_xy(test_path)

    x_rows = [_row_features(r) for r in rows]
    y_true: list[int] = []
    usable_rows: list[dict[str, Any]] = []
    usable_x: list[list[float]] = []
    for row, feats in zip(rows, x_rows):
        try:
            label = int(row.get("label", 0))
        except (TypeError, ValueError):
            continue
        if label not in (0, 1):
            continue
        y_true.append(label)
        usable_rows.append(row)
        usable_x.append(feats)

    if not usable_rows:
        raise ValueError(f"No usable test rows in {test_path}")

    lgbm_scores = _predict_lgbm(model_path, usable_x)
    baseline_scores = [rule_based_score(x) for x in usable_x]

    lgbm_groups, group_by, group_warnings = _build_groups(usable_rows, lgbm_scores)
    baseline_groups, _, _ = _build_groups(usable_rows, baseline_scores)

    warnings = list(group_warnings)
    lightgbm_metrics = evaluate_scores(y_true, lgbm_scores, lgbm_groups)
    baseline_metrics = evaluate_scores(y_true, baseline_scores, baseline_groups)

    if lightgbm_metrics["auc"] is None or baseline_metrics["auc"] is None:
        warnings.append("auc_undefined")

    def _delta(a: float | None, b: float | None) -> float | None:
        if a is None or b is None:
            return None
        return a - b

    k = K
    delta = {
        "auc": _delta(lightgbm_metrics["auc"], baseline_metrics["auc"]),
        f"precision_at_{k}": _delta(
            lightgbm_metrics[f"precision_at_{k}"], baseline_metrics[f"precision_at_{k}"]
        ),
        f"recall_at_{k}": _delta(
            lightgbm_metrics[f"recall_at_{k}"], baseline_metrics[f"recall_at_{k}"]
        ),
        f"hit_rate_at_{k}": _delta(
            lightgbm_metrics[f"hit_rate_at_{k}"], baseline_metrics[f"hit_rate_at_{k}"]
        ),
    }

    artifact_dir.mkdir(parents=True, exist_ok=True)
    report_path = artifact_dir / "evaluate_report.json"
    report = {
        "k": k,
        "group_by": group_by,
        "test_rows": len(usable_rows),
        "baseline_weights": BASELINE_WEIGHTS,
        "feature_order": FEATURE_ORDER,
        "model_path": str(model_path.resolve()),
        "test_path": str(test_path.resolve()),
        "lightgbm": lightgbm_metrics,
        "baseline": baseline_metrics,
        "delta": delta,
        "warnings": warnings,
    }
    report_path.write_text(json.dumps(report, indent=2, ensure_ascii=False), encoding="utf-8")
    report["report_path"] = str(report_path.resolve())
    logger.info("Evaluate finished: %s", report_path)
    return report

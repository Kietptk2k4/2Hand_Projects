"""Home evaluate: AUC + Precision@10 grouped by request_id (mirror Post)."""

from __future__ import annotations

from collections import defaultdict
from typing import Any


def precision_at_k_by_request(
    rows: list[dict[str, Any]],
    score_key: str,
    *,
    k: int = 10,
) -> float | None:
    groups: dict[str, list[dict[str, Any]]] = defaultdict(list)
    for row in rows:
        rid = str(row.get("request_id") or "")
        if not rid:
            continue
        groups[rid].append(row)
    if not groups:
        return None
    precisions: list[float] = []
    for group in groups.values():
        ordered = sorted(group, key=lambda r: float(r.get(score_key) or 0.0), reverse=True)
        top_n = min(k, len(ordered))
        if top_n == 0:
            continue
        hits = sum(1 for r in ordered[:top_n] if int(r.get("label") or 0) == 1)
        precisions.append(hits / top_n)
    if not precisions:
        return None
    return sum(precisions) / len(precisions)


def roc_auc(y_true: list[int], y_score: list[float]) -> float | None:
    positives = sum(1 for y in y_true if y == 1)
    negatives = len(y_true) - positives
    if positives == 0 or negatives == 0:
        return None
    from sklearn.metrics import roc_auc_score

    return float(roc_auc_score(y_true, y_score))


def baseline_score(row: dict[str, Any]) -> float:
    pop = float(row.get("popularity") or row.get("popularity_score") or 0.0)
    rec = float(row.get("recency") or row.get("recency_score") or 0.0)
    return 0.7 * pop + 0.3 * rec


def evaluate_home_rows(
    rows: list[dict[str, Any]],
    model_scores: list[float],
) -> dict[str, Any]:
    if len(rows) != len(model_scores):
        raise ValueError("rows and model_scores length mismatch")
    labeled = []
    y_true: list[int] = []
    for row, score in zip(rows, model_scores):
        item = dict(row)
        item["model_score"] = score
        item["baseline_score"] = baseline_score(row)
        labeled.append(item)
        y_true.append(int(row.get("label") or 0))

    baseline_scores = [float(r["baseline_score"]) for r in labeled]
    return {
        "lightgbm": {
            "auc": roc_auc(y_true, model_scores),
            "precision_at_10": precision_at_k_by_request(labeled, "model_score"),
        },
        "baseline": {
            "auc": roc_auc(y_true, baseline_scores),
            "precision_at_10": precision_at_k_by_request(labeled, "baseline_score"),
        },
        "rows": len(rows),
        "groups": len({str(r.get("request_id")) for r in rows if r.get("request_id")}),
    }


def gate_passes(report: dict[str, Any]) -> bool:
    lg = report.get("lightgbm") or {}
    bl = report.get("baseline") or {}
    auc_l, auc_b = lg.get("auc"), bl.get("auc")
    p_l, p_b = lg.get("precision_at_10"), bl.get("precision_at_10")
    if auc_l is None or auc_b is None or p_l is None or p_b is None:
        return False
    return auc_l >= auc_b and p_l >= p_b

"""KPI gate for simulation / dataset health."""

from __future__ import annotations

from collections import Counter, defaultdict
from typing import Any


DEFAULT_THRESHOLDS = {
    "min_rows": 20_000,
    "positive_rate_min": 0.12,
    "positive_rate_max": 0.25,
    "cross_domain_min_share": 0.15,
    "hashtag_min_share": 0.40,
    "buyer_rate_min": 0.60,
    "median_group_size_min": 8,
}


SMOKE_THRESHOLDS = {
    "min_rows": 50,
    "positive_rate_min": 0.05,
    "positive_rate_max": 0.55,
    "cross_domain_min_share": 0.05,
    "hashtag_min_share": 0.10,
    "buyer_rate_min": 0.30,
    "median_group_size_min": 3,
}


def _median(values: list[int]) -> float:
    if not values:
        return 0.0
    ordered = sorted(values)
    mid = len(ordered) // 2
    if len(ordered) % 2:
        return float(ordered[mid])
    return (ordered[mid - 1] + ordered[mid]) / 2.0


def evaluate_kpis(
    dataset_rows: list[dict[str, Any]],
    *,
    buyer_rate: float | None = None,
    split_positive_counts: dict[str, int] | None = None,
    profile_as_of_ok: bool | None = None,
    thresholds: dict[str, float] | None = None,
    scale: str = "full",
) -> dict[str, Any]:
    thr = dict(SMOKE_THRESHOLDS if scale == "smoke" else DEFAULT_THRESHOLDS)
    if thresholds:
        thr.update(thresholds)

    n = len(dataset_rows)
    positives = sum(1 for r in dataset_rows if int(r.get("label") or 0) == 1)
    positive_rate = (positives / n) if n else 0.0
    cross_share = (
        sum(1 for r in dataset_rows if float(r.get("cross_domain_product_score") or 0) > 0) / n
        if n
        else 0.0
    )
    hash_share = (
        sum(1 for r in dataset_rows if float(r.get("hashtag_match_score") or 0) > 0) / n
        if n
        else 0.0
    )
    groups: dict[str, int] = Counter(str(r.get("request_id") or "") for r in dataset_rows)
    median_group = _median(list(groups.values())) if groups else 0.0

    checks: dict[str, bool] = {
        "rows": n >= thr["min_rows"],
        "positive_rate": thr["positive_rate_min"] <= positive_rate <= thr["positive_rate_max"],
        "cross_domain_share": cross_share >= thr["cross_domain_min_share"],
        "hashtag_share": hash_share >= thr["hashtag_min_share"],
        "median_group_size": median_group >= thr["median_group_size_min"],
    }
    if buyer_rate is not None:
        checks["buyer_rate"] = buyer_rate >= thr["buyer_rate_min"]
    if split_positive_counts is not None:
        checks["split_positives"] = all(
            int(split_positive_counts.get(part, 0)) > 0 for part in ("train", "val", "test")
        )
    if profile_as_of_ok is not None:
        checks["purchase_profile_as_of"] = bool(profile_as_of_ok)

    ok = all(checks.values())
    return {
        "ok": ok,
        "checks": checks,
        "metrics": {
            "rows": n,
            "positives": positives,
            "positive_rate": positive_rate,
            "cross_domain_share": cross_share,
            "hashtag_share": hash_share,
            "median_group_size": median_group,
            "buyer_rate": buyer_rate,
            "split_positive_counts": split_positive_counts,
            "profile_as_of_ok": profile_as_of_ok,
        },
        "thresholds": thr,
    }

"""LightGBM binary training from split parquet (offline only)."""

from __future__ import annotations

import json
import logging
import math
from pathlib import Path
from typing import Any

from app.config import Settings, get_settings

logger = logging.getLogger(__name__)

FEATURE_ORDER: list[str] = [
    "recency_score",
    "engagement_score",
    "hashtag_match_score",
    "author_affinity_score",
    "mutual_follow_score",
    "cross_domain_product_score",
]

DEFAULT_PARAMS: dict[str, Any] = {
    "objective": "binary",
    "metric": ["binary_logloss", "auc"],
    "learning_rate": 0.05,
    "num_leaves": 31,
    "feature_fraction": 0.8,
    "bagging_fraction": 0.8,
    "bagging_freq": 1,
    "verbosity": -1,
    "seed": 42,
}

NUM_BOOST_ROUND = 200
EARLY_STOPPING_ROUNDS = 30


def _require_lightgbm():
    try:
        import lightgbm as lgb
    except ImportError as exc:
        raise RuntimeError(
            "lightgbm is required for train. Install with: pip install lightgbm"
        ) from exc
    return lgb


def load_xy(parquet_path: Path) -> tuple[list[list[float]], list[int], int]:
    """Load X/y from parquet. Returns (X, y, nan_filled_count)."""
    try:
        import pyarrow.parquet as pq
    except ImportError as exc:
        raise RuntimeError("pyarrow is required to read training parquet") from exc

    if not parquet_path.exists():
        raise ValueError(f"Training parquet not found: {parquet_path}")

    rows = pq.read_table(parquet_path).to_pylist()
    if not rows:
        raise ValueError(f"Training parquet is empty: {parquet_path}")

    present = set(rows[0].keys())
    absent = [c for c in FEATURE_ORDER if c not in present]
    if absent:
        raise ValueError(
            f"Missing required feature columns in {parquet_path}: {', '.join(absent)}"
        )
    if "label" not in present:
        raise ValueError(f"Missing required column 'label' in {parquet_path}")

    x_rows: list[list[float]] = []
    y_vals: list[int] = []
    nan_filled = 0

    for row in rows:
        features: list[float] = []
        for col in FEATURE_ORDER:
            raw = row.get(col)
            if raw is None:
                features.append(0.0)
                nan_filled += 1
                continue
            try:
                val = float(raw)
            except (TypeError, ValueError):
                features.append(0.0)
                nan_filled += 1
                continue
            if math.isnan(val) or math.isinf(val):
                features.append(0.0)
                nan_filled += 1
            else:
                features.append(val)

        label_raw = row.get("label")
        try:
            label = int(label_raw)
        except (TypeError, ValueError):
            continue
        if label not in (0, 1):
            continue
        x_rows.append(features)
        y_vals.append(label)

    if not x_rows:
        raise ValueError(f"No usable training rows in {parquet_path}")

    return x_rows, y_vals, nan_filled


def _try_load_val(path: Path) -> tuple[list[list[float]], list[int], int] | None:
    if not path.exists():
        return None
    try:
        return load_xy(path)
    except ValueError:
        return None


def train_lightgbm(
    x_train: list[list[float]],
    y_train: list[int],
    x_val: list[list[float]] | None = None,
    y_val: list[int] | None = None,
    params: dict[str, Any] | None = None,
    num_boost_round: int = NUM_BOOST_ROUND,
    early_stopping_rounds: int = EARLY_STOPPING_ROUNDS,
) -> tuple[Any, dict[str, Any]]:
    lgb = _require_lightgbm()
    try:
        import numpy as np
    except ImportError as exc:
        raise RuntimeError("numpy is required for LightGBM training") from exc

    train_params = dict(DEFAULT_PARAMS)
    if params:
        train_params.update(params)

    x_train_np = np.asarray(x_train, dtype=np.float64)
    y_train_np = np.asarray(y_train, dtype=np.float64)
    dtrain = lgb.Dataset(
        x_train_np, label=y_train_np, feature_name=FEATURE_ORDER, free_raw_data=False
    )
    valid_sets = [dtrain]
    valid_names = ["train"]
    callbacks: list[Any] = []
    used_early_stopping = False
    warnings: list[str] = []

    if x_val is not None and y_val is not None and len(x_val) > 0:
        x_val_np = np.asarray(x_val, dtype=np.float64)
        y_val_np = np.asarray(y_val, dtype=np.float64)
        dval = lgb.Dataset(
            x_val_np,
            label=y_val_np,
            feature_name=FEATURE_ORDER,
            reference=dtrain,
            free_raw_data=False,
        )
        valid_sets.append(dval)
        valid_names.append("val")
        callbacks.append(lgb.early_stopping(early_stopping_rounds, verbose=False))
        used_early_stopping = True
    else:
        warnings.append("no_early_stopping")

    evals_result: dict[str, Any] = {}
    callbacks.append(lgb.record_evaluation(evals_result))
    callbacks.append(lgb.log_evaluation(period=0))

    booster = lgb.train(
        train_params,
        dtrain,
        num_boost_round=num_boost_round,
        valid_sets=valid_sets,
        valid_names=valid_names,
        callbacks=callbacks,
    )

    best_iteration = getattr(booster, "best_iteration", None) or booster.current_iteration()
    metrics: dict[str, Any] = {"evals_result": evals_result}
    if used_early_stopping and "val" in evals_result:
        for metric_name, series in evals_result["val"].items():
            if series:
                idx = min(max(best_iteration - 1, 0), len(series) - 1)
                metrics[f"val_{metric_name}_best"] = series[idx]
    if "train" in evals_result:
        for metric_name, series in evals_result["train"].items():
            if series:
                metrics[f"train_{metric_name}_last"] = series[-1]

    meta_extra = {
        "used_early_stopping": used_early_stopping,
        "best_iteration": int(best_iteration) if best_iteration else None,
        "warnings": warnings,
        "metrics": metrics,
        "params": train_params,
        "num_boost_round": num_boost_round,
        "early_stopping_rounds": early_stopping_rounds if used_early_stopping else None,
    }
    return booster, meta_extra


def run_train_job(settings: Settings | None = None) -> dict[str, Any]:
    settings = settings or get_settings()
    data_dir = Path(settings.recsys_dataset_output_dir)
    artifact_dir = Path(settings.recsys_artifact_dir)
    train_path = data_dir / "dataset_train.parquet"
    val_path = data_dir / "dataset_val.parquet"

    x_train, y_train, nan_train = load_xy(train_path)
    warnings: list[str] = []
    if nan_train:
        warnings.append(f"nan_filled_train={nan_train}")

    val_loaded = _try_load_val(val_path)
    x_val = y_val = None
    nan_val = 0
    if val_loaded is None:
        warnings.append("no_early_stopping")
    else:
        x_val, y_val, nan_val = val_loaded
        if nan_val:
            warnings.append(f"nan_filled_val={nan_val}")

    # If val path missing, train_lightgbm will also add no_early_stopping — dedupe later
    booster, train_info = train_lightgbm(
        x_train,
        y_train,
        x_val=x_val,
        y_val=y_val,
    )
    for w in train_info.get("warnings") or []:
        if w not in warnings:
            warnings.append(w)

    artifact_dir.mkdir(parents=True, exist_ok=True)
    model_path = artifact_dir / "model.txt"
    meta_path = artifact_dir / "train_meta.json"
    booster.save_model(str(model_path))

    summary: dict[str, Any] = {
        "feature_order": FEATURE_ORDER,
        "train_rows": len(y_train),
        "val_rows": len(y_val) if y_val is not None else 0,
        "positive_rate_train": (sum(y_train) / len(y_train)) if y_train else None,
        "positive_rate_val": (sum(y_val) / len(y_val)) if y_val else None,
        "model_path": str(model_path.resolve()),
        "meta_path": str(meta_path.resolve()),
        "used_early_stopping": train_info["used_early_stopping"],
        "best_iteration": train_info["best_iteration"],
        "params": train_info["params"],
        "metrics": {
            k: v
            for k, v in (train_info.get("metrics") or {}).items()
            if k != "evals_result"
        },
        "warnings": warnings,
        "num_boost_round": train_info["num_boost_round"],
        "early_stopping_rounds": train_info["early_stopping_rounds"],
    }
    # Keep truncated evals for debugging (last values only already in metrics)
    meta_path.write_text(json.dumps(summary, indent=2, ensure_ascii=False), encoding="utf-8")
    logger.info(
        "Train finished: train_rows=%s val_rows=%s early_stop=%s -> %s",
        summary["train_rows"],
        summary["val_rows"],
        summary["used_early_stopping"],
        model_path,
    )
    return summary

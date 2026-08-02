"""Train commerce_home_ranker LightGBM from Home JSONL splits."""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any

from pipelines.home_evaluate import evaluate_home_rows
from pipelines.home_feature_order import HOME_FEATURE_ORDER


def _load_jsonl(path: Path) -> list[dict[str, Any]]:
    if not path.exists():
        return []
    rows = []
    with path.open("r", encoding="utf-8") as handle:
        for line in handle:
            line = line.strip()
            if line:
                rows.append(json.loads(line))
    return rows


def _xy(rows: list[dict[str, Any]]) -> tuple[list[list[float]], list[int], list[float]]:
    x_rows: list[list[float]] = []
    y_vals: list[int] = []
    weights: list[float] = []
    for row in rows:
        x_rows.append([float(row.get(name) or 0.0) for name in HOME_FEATURE_ORDER])
        y_vals.append(int(row.get("label") or 0))
        weights.append(float(row.get("sample_weight") or 1.0))
    return x_rows, y_vals, weights


def run_home_train_job(artifact_dir: Path) -> dict[str, Any]:
    try:
        import lightgbm as lgb
        import numpy as np
    except ImportError as exc:
        raise RuntimeError("lightgbm (and numpy) required for home train") from exc

    train = _load_jsonl(artifact_dir / "train.jsonl")
    val = _load_jsonl(artifact_dir / "val.jsonl")
    test = _load_jsonl(artifact_dir / "test.jsonl")
    if len(train) < 8:
        raise ValueError(f"Need more train rows for Home LTR; got {len(train)}")

    x_train, y_train, w_train = _xy(train)
    x_val, y_val, _ = _xy(val) if val else (x_train, y_train, w_train)

    dtrain = lgb.Dataset(
        np.asarray(x_train, dtype=np.float32),
        label=np.asarray(y_train, dtype=np.float32),
        weight=np.asarray(w_train, dtype=np.float32),
        feature_name=list(HOME_FEATURE_ORDER),
        free_raw_data=False,
    )
    dval = lgb.Dataset(
        np.asarray(x_val, dtype=np.float32),
        label=np.asarray(y_val, dtype=np.float32),
        feature_name=list(HOME_FEATURE_ORDER),
        free_raw_data=False,
        reference=dtrain,
    )
    params = {
        "objective": "binary",
        "metric": ["binary_logloss", "auc"],
        "learning_rate": 0.05,
        "num_leaves": 31,
        "verbosity": -1,
        "seed": 42,
    }
    booster = lgb.train(
        params,
        dtrain,
        num_boost_round=120,
        valid_sets=[dval],
        callbacks=[lgb.early_stopping(20), lgb.log_evaluation(0)],
    )
    model_path = artifact_dir / "lightgbm_home.txt"
    booster.save_model(str(model_path))

    eval_rows = test or val or train
    x_eval, _, _ = _xy(eval_rows)
    scores = [float(s) for s in booster.predict(np.asarray(x_eval, dtype=np.float32)).tolist()]
    report = evaluate_home_rows(eval_rows, scores)
    meta_path = artifact_dir / "dataset_meta.json"
    mode = None
    if meta_path.exists():
        mode = json.loads(meta_path.read_text(encoding="utf-8")).get("train_data_mode")
    popularity = {}
    pop_path = artifact_dir / "popularity_normalizer.json"
    if pop_path.exists():
        popularity = json.loads(pop_path.read_text(encoding="utf-8"))
    report["train_data_mode"] = mode
    report["popularity_normalizer"] = popularity
    (artifact_dir / "evaluate_report.json").write_text(json.dumps(report, indent=2), encoding="utf-8")

    # optional parquet for ONNX smoke in export-activate
    try:
        import pyarrow as pa
        import pyarrow.parquet as pq

        table = pa.Table.from_pylist(eval_rows)
        pq.write_table(table, artifact_dir / "test.parquet")
    except ImportError:
        pass

    return {"model_path": str(model_path), "evaluate": report, "train_rows": len(train)}

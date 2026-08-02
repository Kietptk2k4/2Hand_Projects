"""Export-activate commerce_home_ranker into Commerce model_artifacts."""

from __future__ import annotations

import json
import logging
from pathlib import Path
from typing import Any

from app.config import Settings
from pipelines.home_feature_order import HOME_FEATURE_ORDER
from pipelines.export_activate import (
    FORMAT_ONNX,
    ONNX_SMOKE_MAX,
    ONNX_SMOKE_MIN,
    evaluate_metric_gate,
    load_evaluate_report,
)

logger = logging.getLogger(__name__)

MODEL_NAME = "commerce_home_ranker"
FEATURE_VERSION = 1


def _require_deps():
    try:
        import lightgbm as lgb
        import numpy as np
        import onnxruntime as ort
        from onnxmltools import convert_lightgbm
        from onnxmltools.convert.common.data_types import FloatTensorType
        from onnxmltools.utils import save_model
    except ImportError as exc:
        raise RuntimeError(
            "home export-activate requires lightgbm, onnxmltools, onnxruntime"
        ) from exc
    return lgb, np, ort, convert_lightgbm, save_model, FloatTensorType


def _load_smoke_matrix(test_parquet: Path, n_samples: int) -> Any:
    import numpy as np

    try:
        import pyarrow.parquet as pq
    except ImportError as exc:
        raise RuntimeError("pyarrow is required") from exc
    if not test_parquet.exists():
        raise ValueError(f"Test parquet not found: {test_parquet}")
    rows = pq.read_table(test_parquet).to_pylist()
    take = min(n_samples, ONNX_SMOKE_MAX, len(rows))
    if take < ONNX_SMOKE_MIN:
        raise ValueError(
            f"Need at least {ONNX_SMOKE_MIN} test rows for ONNX smoke, got {len(rows)}"
        )
    matrix = []
    for row in rows[:take]:
        matrix.append([float(row.get(name) or 0.0) for name in HOME_FEATURE_ORDER])
    return np.asarray(matrix, dtype=np.float32)


def _next_version(conn, model_name: str) -> int:
    with conn.cursor() as cur:
        cur.execute(
            "SELECT COALESCE(MAX(version), 0) + 1 FROM model_artifacts WHERE model_name = %s",
            (model_name,),
        )
        return int(cur.fetchone()[0])


def run_home_export_activate(settings: Settings, *, home_artifact_dir: Path | None = None) -> dict[str, Any]:
    """Export LightGBM → ONNX, write sidecars, version Commerce model_artifacts."""
    import psycopg

    lgb, np, ort, convert_lightgbm, save_model, FloatTensorType = _require_deps()
    settings.require_commerce_url()

    artifact_dir = Path(home_artifact_dir or settings.recsys_home_artifact_dir)
    artifact_dir.mkdir(parents=True, exist_ok=True)
    model_txt = artifact_dir / "lightgbm_home.txt"
    evaluate_path = artifact_dir / "evaluate_report.json"
    test_parquet = artifact_dir / "test.parquet"
    if not model_txt.exists():
        raise ValueError(f"Missing trained model: {model_txt}")

    report = load_evaluate_report(evaluate_path)
    gate = evaluate_metric_gate(report)

    booster = lgb.Booster(model_file=str(model_txt))
    initial_types = [("input", FloatTensorType([None, len(HOME_FEATURE_ORDER)]))]
    onnx_model = convert_lightgbm(booster, initial_types=initial_types, target_opset=12)

    version_hint = 1
    basename = f"{MODEL_NAME}_v{{version}}.onnx"
    # provisional path until DB version known
    onnx_tmp = artifact_dir / f"{MODEL_NAME}.onnx"
    save_model(onnx_model, str(onnx_tmp))

    smoke = _load_smoke_matrix(test_parquet, ONNX_SMOKE_MIN)
    sess = ort.InferenceSession(str(onnx_tmp), providers=["CPUExecutionProvider"])
    input_name = sess.get_inputs()[0].name
    outs = sess.run(None, {input_name: smoke})
    if not outs:
        raise ValueError("ONNX smoke produced empty output")

    popularity = report.get("popularity_normalizer") or {"z_lo": 0.0, "z_hi": 1.0}
    feature_order_path = artifact_dir / f"{MODEL_NAME}.feature_order.json"
    normalizer_path = artifact_dir / f"{MODEL_NAME}.popularity_normalizer.json"
    feature_order_path.write_text(json.dumps(HOME_FEATURE_ORDER), encoding="utf-8")
    normalizer_path.write_text(json.dumps(popularity), encoding="utf-8")

    metrics = {
        "gate": gate,
        "evaluate": report,
        "feature_version": FEATURE_VERSION,
        "train_data_mode": report.get("train_data_mode"),
        "model_name": MODEL_NAME,
    }

    with psycopg.connect(settings.commerce_postgres_url) as conn:
        version = _next_version(conn, MODEL_NAME)
        final_name = f"{MODEL_NAME}_v{version}.onnx"
        final_path = artifact_dir / final_name
        if onnx_tmp != final_path:
            onnx_tmp.replace(final_path)
            # keep loader-friendly sidecars next to basename used online
            (artifact_dir / f"{final_name.replace('.onnx', '')}.feature_order.json").write_text(
                feature_order_path.read_text(encoding="utf-8"), encoding="utf-8"
            )
            (artifact_dir / f"{final_name.replace('.onnx', '')}.popularity_normalizer.json").write_text(
                normalizer_path.read_text(encoding="utf-8"), encoding="utf-8"
            )

        with conn.cursor() as cur:
            cur.execute(
                """
                INSERT INTO model_artifacts (
                    model_name, version, format, artifact_path, metrics, is_active, trained_at
                ) VALUES (
                    %s, %s, %s, %s, %s::jsonb, FALSE, NOW()
                )
                """,
                (MODEL_NAME, version, FORMAT_ONNX, final_name, json.dumps(metrics)),
            )
            status = "exported_not_activated"
            if gate.get("passed"):
                cur.execute(
                    "UPDATE model_artifacts SET is_active = FALSE WHERE model_name = %s AND is_active = TRUE",
                    (MODEL_NAME,),
                )
                cur.execute(
                    "UPDATE model_artifacts SET is_active = TRUE WHERE model_name = %s AND version = %s",
                    (MODEL_NAME, version),
                )
                status = "activated"
        conn.commit()

    return {
        "status": status,
        "model_name": MODEL_NAME,
        "version": version,
        "artifact_path": final_name,
        "gate": gate,
    }

"""Export LightGBM → ONNX, verify, version model_artifacts, metric gate + activate."""

from __future__ import annotations

import json
import logging
from pathlib import Path
from typing import Any, Callable

from app.config import Settings, get_settings
from pipelines.train import FEATURE_ORDER

logger = logging.getLogger(__name__)

MODEL_NAME = "feed_ranker"
FEATURE_VERSION = 1
ONNX_VERIFY_THRESHOLD = 1e-4
ONNX_SMOKE_MIN = 32
ONNX_SMOKE_MAX = 64
FORMAT_ONNX = "ONNX"


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
            "export-activate requires lightgbm, onnxmltools, onnxruntime. "
            "pip install -r requirements.txt"
        ) from exc
    return lgb, np, ort, convert_lightgbm, save_model, FloatTensorType


def load_evaluate_report(path: Path) -> dict[str, Any]:
    if not path.exists():
        raise ValueError(f"Evaluate report not found: {path}")
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        raise ValueError(f"Invalid evaluate_report.json: {exc}") from exc
    if not isinstance(data, dict):
        raise ValueError("evaluate_report.json must be a JSON object")
    return data


def evaluate_metric_gate(report: dict[str, Any]) -> dict[str, Any]:
    """Gate: AUC >= baseline AND Precision@10 >= baseline; null → fail closed."""
    lightgbm = report.get("lightgbm") or {}
    baseline = report.get("baseline") or {}
    failed: list[str] = []

    auc_l = lightgbm.get("auc")
    auc_b = baseline.get("auc")
    p_l = lightgbm.get("precision_at_10")
    p_b = baseline.get("precision_at_10")

    if auc_l is None or auc_b is None:
        failed.append("auc_undefined")
    elif float(auc_l) < float(auc_b):
        failed.append("auc")

    if p_l is None or p_b is None:
        failed.append("precision_at_10_undefined")
    elif float(p_l) < float(p_b):
        failed.append("precision_at_10")

    passed = len(failed) == 0
    reason = None if passed else "rejected_by_metrics"
    return {
        "passed": passed,
        "reason": reason,
        "failed": failed,
        "auc_lightgbm": auc_l,
        "auc_baseline": auc_b,
        "precision_at_10_lightgbm": p_l,
        "precision_at_10_baseline": p_b,
    }


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
        feats = []
        for col in FEATURE_ORDER:
            try:
                feats.append(float(row.get(col) or 0.0))
            except (TypeError, ValueError):
                feats.append(0.0)
        matrix.append(feats)
    return np.asarray(matrix, dtype=np.float32)


def convert_lightgbm_to_onnx(model_txt: Path, onnx_path: Path) -> Path:
    lgb, _np, _ort, convert_lightgbm, save_model, FloatTensorType = _require_deps()
    if not model_txt.exists():
        raise ValueError(f"Model file not found: {model_txt}")
    booster = lgb.Booster(model_file=str(model_txt))
    initial_types = [("input", FloatTensorType([None, len(FEATURE_ORDER)]))]
    onnx_model = convert_lightgbm(
        booster,
        initial_types=initial_types,
        target_opset=12,
        zipmap=False,
    )
    onnx_path.parent.mkdir(parents=True, exist_ok=True)
    save_model(onnx_model, str(onnx_path))
    return onnx_path


def _onnx_positive_scores(session: Any, x: Any) -> Any:
    import numpy as np

    input_name = session.get_inputs()[0].name
    outputs = session.run(None, {input_name: x})
    # Classifier often returns [labels, probabilities]; regressor returns scores.
    if len(outputs) >= 2:
        probs = np.asarray(outputs[1], dtype=np.float64)
        if probs.ndim == 2 and probs.shape[1] >= 2:
            return probs[:, 1]
        return probs.reshape(-1)
    raw = np.asarray(outputs[0], dtype=np.float64)
    if raw.ndim == 2 and raw.shape[1] >= 2:
        return raw[:, 1]
    return raw.reshape(-1)


def verify_onnx_parity(
    model_txt: Path,
    onnx_path: Path,
    test_parquet: Path,
    *,
    threshold: float = ONNX_VERIFY_THRESHOLD,
    n_samples: int = ONNX_SMOKE_MAX,
) -> dict[str, Any]:
    lgb, np, ort, *_ = _require_deps()
    if not model_txt.exists():
        raise ValueError(f"Model file not found: {model_txt}")
    if not onnx_path.exists():
        raise ValueError(f"ONNX file not found: {onnx_path}")

    x = _load_smoke_matrix(test_parquet, n_samples)
    booster = lgb.Booster(model_file=str(model_txt))
    native = np.asarray(booster.predict(x.astype(np.float64)), dtype=np.float64)

    session = ort.InferenceSession(str(onnx_path), providers=["CPUExecutionProvider"])
    onnx_scores = _onnx_positive_scores(session, x)
    if onnx_scores.shape[0] != native.shape[0]:
        raise ValueError(
            f"ONNX output length {onnx_scores.shape[0]} != native {native.shape[0]}"
        )

    max_abs_diff = float(np.max(np.abs(native - onnx_scores)))
    passed = max_abs_diff <= threshold
    result = {
        "passed": passed,
        "n_samples": int(x.shape[0]),
        "max_abs_diff": max_abs_diff,
        "threshold": threshold,
    }
    if not passed:
        raise ValueError(
            f"ONNX parity failed: max_abs_diff={max_abs_diff} > threshold={threshold}"
        )
    return result


def _postgres_dsn(settings: Settings) -> str:
    url = settings.social_postgres_url
    if not url:
        raise ValueError("Missing required database configuration: SOCIAL_POSTGRES_URL")
    return (
        url.replace("postgresql+psycopg://", "postgresql://")
        .replace("postgresql+psycopg2://", "postgresql://")
    )


def next_artifact_version(conn: Any, model_name: str) -> int:
    with conn.cursor() as cur:
        cur.execute(
            "SELECT COALESCE(MAX(version), 0) + 1 FROM model_artifacts WHERE model_name = %s",
            (model_name,),
        )
        row = cur.fetchone()
        return int(row[0])


def persist_model_artifact(
    conn: Any,
    *,
    model_name: str,
    version: int,
    artifact_path: str,
    metrics: dict[str, Any],
    activate: bool,
) -> None:
    import json as _json

    metrics_json = _json.dumps(metrics, ensure_ascii=False)
    with conn.cursor() as cur:
        if activate:
            cur.execute(
                "UPDATE model_artifacts SET is_active = FALSE WHERE model_name = %s AND is_active = TRUE",
                (model_name,),
            )
        cur.execute(
            """
            INSERT INTO model_artifacts (
                model_name, version, format, artifact_path, metrics, is_active, trained_at
            ) VALUES (
                %s, %s, %s, %s, %s::jsonb, %s, NOW()
            )
            """,
            (
                model_name,
                version,
                FORMAT_ONNX,
                artifact_path,
                metrics_json,
                activate,
            ),
        )
    conn.commit()


def build_metrics_payload(
    report: dict[str, Any],
    *,
    train_meta_path: Path,
    evaluate_report_path: Path,
    onnx_verify: dict[str, Any],
    gate: dict[str, Any],
) -> dict[str, Any]:
    lightgbm = report.get("lightgbm") or {}
    baseline = report.get("baseline") or {}
    return {
        "auc": lightgbm.get("auc"),
        "precision_at_10": lightgbm.get("precision_at_10"),
        "recall_at_10": lightgbm.get("recall_at_10"),
        "hit_rate_at_10": lightgbm.get("hit_rate_at_10"),
        "baseline": {
            "auc": baseline.get("auc"),
            "precision_at_10": baseline.get("precision_at_10"),
            "recall_at_10": baseline.get("recall_at_10"),
            "hit_rate_at_10": baseline.get("hit_rate_at_10"),
        },
        "delta": report.get("delta"),
        "feature_order": report.get("feature_order") or FEATURE_ORDER,
        "feature_version": FEATURE_VERSION,
        "baseline_weights": report.get("baseline_weights"),
        "train_meta_path": str(train_meta_path.resolve()) if train_meta_path.exists() else str(train_meta_path),
        "evaluate_report_path": str(evaluate_report_path.resolve()),
        "onnx_verify": onnx_verify,
        "gate": gate,
    }


ConnectFactory = Callable[[], Any]


def run_export_activate_job(
    settings: Settings | None = None,
    *,
    connect_factory: ConnectFactory | None = None,
) -> dict[str, Any]:
    settings = settings or get_settings()
    artifact_dir = Path(settings.recsys_artifact_dir)
    data_dir = Path(settings.recsys_dataset_output_dir)

    model_txt = artifact_dir / "model.txt"
    evaluate_report_path = artifact_dir / "evaluate_report.json"
    train_meta_path = artifact_dir / "train_meta.json"
    test_parquet = data_dir / "dataset_test.parquet"

    if not model_txt.exists():
        raise ValueError(f"Model file not found: {model_txt}")
    report = load_evaluate_report(evaluate_report_path)

    # Convert + verify before any DB write
    version_hint_path = artifact_dir / "feed_ranker_pending.onnx"
    convert_lightgbm_to_onnx(model_txt, version_hint_path)
    onnx_verify = verify_onnx_parity(model_txt, version_hint_path, test_parquet)

    gate = evaluate_metric_gate(report)
    activate = bool(gate["passed"])

    import psycopg

    def _default_connect():
        return psycopg.connect(_postgres_dsn(settings))

    factory = connect_factory or _default_connect
    with factory() as conn:
        version = next_artifact_version(conn, MODEL_NAME)
        onnx_path = artifact_dir / f"feed_ranker_v{version}.onnx"
        if version_hint_path.exists():
            version_hint_path.replace(onnx_path)
        else:
            convert_lightgbm_to_onnx(model_txt, onnx_path)

        metrics = build_metrics_payload(
            report,
            train_meta_path=train_meta_path,
            evaluate_report_path=evaluate_report_path,
            onnx_verify=onnx_verify,
            gate=gate,
        )
        persist_model_artifact(
            conn,
            model_name=MODEL_NAME,
            version=version,
            artifact_path=str(onnx_path.resolve()),
            metrics=metrics,
            activate=activate,
        )

    status = "activated" if activate else "exported_not_activated"
    summary = {
        "status": status,
        "model_name": MODEL_NAME,
        "version": version,
        "artifact_path": str(onnx_path.resolve()),
        "is_active": activate,
        "gate": gate,
        "onnx_verify": onnx_verify,
        "feature_version": FEATURE_VERSION,
    }
    logger.info("export-activate finished: %s v%s (%s)", MODEL_NAME, version, status)
    return summary

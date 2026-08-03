"""Offline ML ops FastAPI — not used for online recommend serving."""

from __future__ import annotations

import logging
from typing import Any

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from app.config import get_settings
from pipelines.build_dataset import run_build_dataset
from pipelines.clean_data import run_clean_job
from pipelines.split_dataset import run_split_dataset
from pipelines.train import run_train_job
from pipelines.evaluate import run_evaluate_job
from pipelines.export_activate import run_export_activate_job
from pipelines.export_purchase_profile import run_export_purchase_profile

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="2Hands Recsys Offline",
    description=(
        "Offline jobs only (clean / build-dataset / train / evaluate / export). "
        "Social Service must NOT call this during recommend-feed requests."
    ),
    version="0.4.0",
)


class JobAccepted(BaseModel):
    status: str
    detail: str
    result: dict[str, Any] | None = None


class ExportPurchaseProfileRequest(BaseModel):
    as_of: str | None = Field(
        default=None,
        description="Train cutoff ISO timestamp (T_cut). Omit for provisional full export.",
    )


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok", "service": "recsys-offline"}


@app.post("/jobs/clean", response_model=JobAccepted)
def jobs_clean() -> JobAccepted:
    settings = get_settings()
    try:
        summary = run_clean_job(settings)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Clean job failed")
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    return JobAccepted(status="success", detail="Clean dataset completed", result=summary)


@app.post("/jobs/build-dataset", response_model=JobAccepted)
def jobs_build_dataset() -> JobAccepted:
    settings = get_settings()
    try:
        summary = run_build_dataset(settings)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Build dataset failed")
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    return JobAccepted(status="success", detail="Build dataset completed", result=summary)


@app.post("/jobs/split-dataset", response_model=JobAccepted)
def jobs_split_dataset() -> JobAccepted:
    settings = get_settings()
    try:
        summary = run_split_dataset(settings)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Split dataset failed")
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    return JobAccepted(status="success", detail="Split dataset completed", result=summary)


@app.post("/jobs/train", response_model=JobAccepted)
def jobs_train() -> JobAccepted:
    settings = get_settings()
    try:
        summary = run_train_job(settings)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Train job failed")
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    return JobAccepted(status="success", detail="Train LightGBM completed", result=summary)


@app.post("/jobs/evaluate", response_model=JobAccepted)
def jobs_evaluate() -> JobAccepted:
    settings = get_settings()
    try:
        summary = run_evaluate_job(settings)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Evaluate job failed")
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    return JobAccepted(status="success", detail="Evaluate completed", result=summary)


@app.post("/jobs/export-activate", response_model=JobAccepted)
def jobs_export_activate() -> JobAccepted:
    settings = get_settings()
    try:
        summary = run_export_activate_job(settings)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Export-activate job failed")
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    job_status = str(summary.get("status") or "success")
    detail = (
        "Model activated"
        if job_status == "activated"
        else "Model exported but not activated (gate rejected)"
        if job_status == "exported_not_activated"
        else "Export-activate completed"
    )
    return JobAccepted(status=job_status, detail=detail, result=summary)


@app.post("/jobs/export-purchase-profile", response_model=JobAccepted)
def jobs_export_purchase_profile(
    body: ExportPurchaseProfileRequest | None = None,
) -> JobAccepted:
    settings = get_settings()
    payload = body or ExportPurchaseProfileRequest()
    try:
        settings.require_commerce_url()
        summary = run_export_purchase_profile(settings, as_of=payload.as_of)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Export purchase profile failed")
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    return JobAccepted(
        status="success",
        detail="Purchase profile export completed",
        result=summary,
    )


@app.post("/jobs/home-build-dataset", response_model=JobAccepted)
def jobs_home_build_dataset() -> JobAccepted:
    settings = get_settings()
    try:
        from pathlib import Path

        from pipelines.home_dataset_job import run_home_build_dataset_job
        from pipelines.home_train_mode import resolve_home_train_mode

        mode_cfg = resolve_home_train_mode(
            admin_base_url=settings.admin_base_url,
            admin_token=settings.admin_service_token,
        )
        summary = run_home_build_dataset_job(
            Path(settings.recsys_home_sim_dir),
            Path(settings.recsys_home_artifact_dir),
            mode_cfg=mode_cfg,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Home build-dataset failed")
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    return JobAccepted(status="success", detail="Home build-dataset completed", result=summary)


@app.post("/jobs/home-train", response_model=JobAccepted)
def jobs_home_train() -> JobAccepted:
    settings = get_settings()
    try:
        from pathlib import Path

        from pipelines.home_train import run_home_train_job

        summary = run_home_train_job(Path(settings.recsys_home_artifact_dir))
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Home train failed")
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    return JobAccepted(status="success", detail="Home LightGBM train completed", result=summary)


@app.post("/jobs/home-sim", response_model=JobAccepted)
def jobs_home_sim() -> JobAccepted:
    settings = get_settings()
    try:
        settings.require_sim_allow()
        from pathlib import Path

        from pipelines.home_sim import run_home_sim_to_dir

        sim_dir = Path(settings.recsys_home_sim_dir)
        summary = run_home_sim_to_dir(sim_dir)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Home sim job failed")
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    return JobAccepted(status="success", detail="Home sim completed", result=summary)


@app.post("/jobs/home-load-artifact", response_model=JobAccepted)
def jobs_home_load_artifact() -> JobAccepted:
    settings = get_settings()
    try:
        settings.require_commerce_url()
        from pathlib import Path

        from pipelines.home_load_artifact import load_home_artifacts

        counts = load_home_artifacts(
            settings.commerce_postgres_url,
            Path(settings.recsys_home_sim_dir),
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Home load-artifact failed")
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    return JobAccepted(status="success", detail="Home artifacts loaded", result=counts)


@app.post("/jobs/home-export-activate", response_model=JobAccepted)
def jobs_home_export_activate() -> JobAccepted:
    settings = get_settings()
    try:
        from pipelines.home_export_activate import run_home_export_activate

        summary = run_home_export_activate(settings)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Home export-activate failed")
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    job_status = str(summary.get("status") or "success")
    detail = (
        "Home model activated"
        if job_status == "activated"
        else "Home model exported but not activated (gate rejected)"
        if job_status == "exported_not_activated"
        else "Home export-activate completed"
    )
    return JobAccepted(status=job_status, detail=detail, result=summary)


@app.post("/jobs/home-retrain", response_model=JobAccepted)
def jobs_home_retrain() -> JobAccepted:
    """Run Home retrain orchestrator step order (jobs may be dry until dataset/train wired)."""
    settings = get_settings()
    try:
        from pathlib import Path

        from pipelines.home_export_activate import run_home_export_activate
        from pipelines.home_load_artifact import load_home_artifacts
        from pipelines.home_orchestrator import HomeRetrainOrchestrator
        from pipelines.home_train_mode import resolve_home_train_mode

        mode_cfg = resolve_home_train_mode(
            admin_base_url=settings.admin_base_url,
            admin_token=settings.admin_service_token,
        )
        sim_dir = Path(settings.recsys_home_sim_dir)
        orchestrator = HomeRetrainOrchestrator()
        jobs = {
            "load_artifact": lambda: load_home_artifacts(
                settings.commerce_postgres_url, sim_dir
            )
            if settings.commerce_postgres_url
            else None,
            "export_activate": lambda: run_home_export_activate(settings),
        }
        ran = orchestrator.run(mode_cfg=mode_cfg, sim_dir=sim_dir, jobs=jobs)
        summary = {"steps": ran, "train_data_mode": mode_cfg.train_data_mode}
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Home retrain orchestrator failed")
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    return JobAccepted(status="success", detail="Home retrain orchestrator completed", result=summary)


@app.post("/jobs/feed-sim", response_model=JobAccepted)
def jobs_feed_sim() -> JobAccepted:
    settings = get_settings()
    try:
        settings.require_sim_allow()
        from pathlib import Path

        from pipelines.feed_sim import run_feed_sim_to_dir

        summary = run_feed_sim_to_dir(Path(settings.recsys_feed_sim_dir))
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Feed sim job failed")
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    return JobAccepted(status="success", detail="Feed sim completed", result=summary)


@app.post("/jobs/feed-build-dataset", response_model=JobAccepted)
def jobs_feed_build_dataset() -> JobAccepted:
    settings = get_settings()
    try:
        from pathlib import Path

        from pipelines.feed_build_dataset import run_feed_build_dataset_job
        from pipelines.feed_train_mode import resolve_feed_train_mode

        mode_cfg = resolve_feed_train_mode(
            admin_base_url=settings.admin_base_url,
            admin_token=settings.admin_service_token,
        )
        summary = run_feed_build_dataset_job(
            mode_cfg=mode_cfg,
            seed_dir=Path(settings.recsys_feed_sim_dir),
            real_dir=Path(settings.recsys_dataset_output_dir),
            output_dir=Path(settings.recsys_dataset_output_dir),
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Feed build-dataset failed")
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    return JobAccepted(status="success", detail="Feed build-dataset completed", result=summary)


@app.post("/jobs/feed-retrain", response_model=JobAccepted)
def jobs_feed_retrain() -> JobAccepted:
    """Mode-aware feed retrain: resolve → sim → clean → build → split → train → evaluate → export."""
    settings = get_settings()
    try:
        from pathlib import Path

        from pipelines.feed_build_dataset import run_feed_build_dataset_job
        from pipelines.feed_orchestrator import FeedRetrainOrchestrator
        from pipelines.feed_train_mode import resolve_feed_train_mode

        mode_cfg = resolve_feed_train_mode(
            admin_base_url=settings.admin_base_url,
            admin_token=settings.admin_service_token,
        )
        sim_dir = Path(settings.recsys_feed_sim_dir)
        data_dir = Path(settings.recsys_dataset_output_dir)

        def _clean_real() -> None:
            run_clean_job(settings)

        def _build() -> None:
            run_feed_build_dataset_job(
                mode_cfg=mode_cfg,
                seed_dir=sim_dir,
                real_dir=data_dir,
                output_dir=data_dir,
            )

        def _split() -> None:
            run_split_dataset(settings)

        def _train() -> None:
            run_train_job(settings)

        def _evaluate() -> None:
            run_evaluate_job(settings)

        def _export() -> None:
            run_export_activate_job(settings)

        orchestrator = FeedRetrainOrchestrator()
        jobs = {
            "clean_real": _clean_real,
            "build_dataset": _build,
            "split": _split,
            "train": _train,
            "evaluate": _evaluate,
            "export_activate": _export,
        }
        if mode_cfg.train_data_mode == "SEED_ONLY":
            settings.require_sim_allow()
        ran = orchestrator.run(mode_cfg=mode_cfg, sim_dir=sim_dir, jobs=jobs)
        summary = {"steps": ran, "train_data_mode": mode_cfg.train_data_mode}
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Feed retrain orchestrator failed")
        raise HTTPException(status_code=500, detail=str(exc)) from exc
    return JobAccepted(status="success", detail="Feed retrain orchestrator completed", result=summary)

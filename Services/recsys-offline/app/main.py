"""Offline ML ops FastAPI — not used for online recommend serving."""

from __future__ import annotations

import logging
from typing import Any

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from app.config import get_settings
from pipelines.build_dataset import run_build_dataset
from pipelines.clean_data import run_clean_job
from pipelines.split_dataset import run_split_dataset
from pipelines.train import run_train_job
from pipelines.evaluate import run_evaluate_job
from pipelines.export_activate import run_export_activate_job

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="2Hands Recsys Offline",
    description=(
        "Offline jobs only (clean / build-dataset / train / evaluate / export). "
        "Social Service must NOT call this during recommend-feed requests."
    ),
    version="0.2.0",
)


class JobAccepted(BaseModel):
    status: str
    detail: str
    result: dict[str, Any] | None = None


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

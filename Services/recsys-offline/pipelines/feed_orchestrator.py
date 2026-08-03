"""Feed train/retrain orchestrator order."""

from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path
from typing import Callable

from pipelines.feed_sim import run_feed_sim_to_dir
from pipelines.feed_train_mode import FeedTrainModeConfig, resolve_feed_train_mode


@dataclass
class OrchestrationStep:
    name: str
    ran: bool = False


@dataclass
class FeedRetrainOrchestrator:
    steps: list[OrchestrationStep] = field(default_factory=list)

    def run(
        self,
        *,
        mode_cfg: FeedTrainModeConfig | None = None,
        sim_dir: Path | None = None,
        jobs: dict[str, Callable[[], None]] | None = None,
    ) -> list[str]:
        jobs = jobs or {}
        cfg = mode_cfg or resolve_feed_train_mode()
        order = [
            "resolve_mode",
            "feed_sim",
            "clean_real",
            "build_dataset",
            "split",
            "train",
            "evaluate",
            "export_activate",
        ]
        ran: list[str] = []

        for step in order:
            if step == "feed_sim" and cfg.train_data_mode == "REAL_ONLY":
                continue
            if step == "clean_real" and cfg.train_data_mode == "SEED_ONLY":
                continue

            if step == "feed_sim" and sim_dir is not None and "feed_sim" not in jobs:
                run_feed_sim_to_dir(sim_dir)
            elif step in jobs:
                jobs[step]()
            elif step == "resolve_mode":
                pass

            self.steps.append(OrchestrationStep(name=step, ran=True))
            ran.append(step)
        return ran

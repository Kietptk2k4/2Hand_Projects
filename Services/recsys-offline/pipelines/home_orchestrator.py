"""Home train/retrain orchestrator order (D15)."""

from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path
from typing import Callable

from pipelines.home_sim import run_home_sim_to_dir
from pipelines.home_train_mode import HomeTrainModeConfig, resolve_home_train_mode


@dataclass
class OrchestrationStep:
    name: str
    ran: bool = False


@dataclass
class HomeRetrainOrchestrator:
    """Records step order; callers inject concrete job callables."""

    steps: list[OrchestrationStep] = field(default_factory=list)

    def run(
        self,
        *,
        mode_cfg: HomeTrainModeConfig | None = None,
        sim_dir: Path | None = None,
        jobs: dict[str, Callable[[], None]] | None = None,
    ) -> list[str]:
        jobs = jobs or {}
        cfg = mode_cfg or resolve_home_train_mode()
        order = [
            "resolve_mode",
            "home_sim",
            "entity_cf",
            "social_export",
            "ar_mine",
            "load_artifact",
            "build_dataset",
            "split",
            "train",
            "evaluate",
            "export_activate",
        ]
        ran: list[str] = []

        def _run(name: str) -> None:
            if name == "home_sim" and cfg.train_data_mode == "REAL_ONLY":
                return
            if name == "home_sim" and sim_dir is not None and "home_sim" not in jobs:
                run_home_sim_to_dir(sim_dir)
            elif name in jobs:
                jobs[name]()
            elif name == "resolve_mode":
                pass
            else:
                # allow dry orchestration without every job wired
                pass
            self.steps.append(OrchestrationStep(name=name, ran=True))
            ran.append(name)

        for step in order:
            if step == "home_sim" and cfg.train_data_mode == "REAL_ONLY":
                continue
            _run(step)
        return ran

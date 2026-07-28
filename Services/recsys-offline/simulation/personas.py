"""Load personas.yaml configuration."""

from __future__ import annotations

from pathlib import Path
from typing import Any

import yaml

DEFAULT_CONFIG_PATH = Path(__file__).resolve().parents[1] / "config" / "personas.yaml"


def load_persona_config(path: Path | str | None = None) -> dict[str, Any]:
    cfg_path = Path(path) if path else DEFAULT_CONFIG_PATH
    with cfg_path.open("r", encoding="utf-8") as handle:
        data = yaml.safe_load(handle)
    if not isinstance(data, dict):
        raise ValueError("personas config must be a mapping")
    if data.get("vertical") != "fashion-secondhand":
        raise ValueError("personas config vertical must be fashion-secondhand")
    return data


def volumes_for(config: dict[str, Any], scale: str = "full") -> dict[str, int]:
    volumes = config.get("volumes") or {}
    if scale not in volumes:
        raise ValueError(f"Unknown scale '{scale}'; expected one of {list(volumes)}")
    return dict(volumes[scale])


def niche_names(config: dict[str, Any]) -> list[str]:
    return list((config.get("niches") or {}).keys())


def affinity(config: dict[str, Any], persona_id: str, niche: str) -> float:
    matrix = config.get("affinity") or {}
    row = matrix.get(persona_id) or {}
    return float(row.get(niche, 0.05))

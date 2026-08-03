"""Resolve Social feed LTR train_data_mode from Admin or env."""

from __future__ import annotations

import os
from dataclasses import dataclass
from typing import Any

import httpx

ALLOWED_MODES = frozenset({"SEED_ONLY", "HYBRID", "REAL_ONLY"})
KEY_MODE = "social.feed.ltr.train_data_mode"
KEY_SEED_WEIGHT = "social.feed.ltr.seed_row_weight"
KEY_MIN_IMPRESSIONS = "social.feed.ltr.real_only_min_impressions"


@dataclass(frozen=True)
class FeedTrainModeConfig:
    train_data_mode: str
    seed_row_weight: float = 0.5
    real_only_min_impressions: int = 5000


def validate_mode(mode: str) -> str:
    value = (mode or "").strip().upper()
    if value not in ALLOWED_MODES:
        raise ValueError(f"Invalid train_data_mode={mode!r}; allowed={sorted(ALLOWED_MODES)}")
    return value


def resolve_from_env() -> FeedTrainModeConfig | None:
    if os.getenv("RECSYS_FEED_CONFIG_FROM_ENV", "").strip() not in {"1", "true", "TRUE"}:
        return None
    mode = validate_mode(os.getenv("FEED_LTR_TRAIN_DATA_MODE", "SEED_ONLY"))
    weight = float(os.getenv("FEED_LTR_SEED_ROW_WEIGHT", "0.5"))
    min_rows = int(os.getenv("FEED_LTR_REAL_ONLY_MIN_IMPRESSIONS", "5000"))
    return FeedTrainModeConfig(mode, weight, min_rows)


def _pick_exact(items: list[dict[str, Any]], config_key: str) -> dict[str, Any] | None:
    for item in items:
        if str(item.get("configKey") or item.get("config_key") or "") == config_key:
            return item
    return None


def _fetch_key(client: httpx.Client, key: str) -> dict[str, Any] | None:
    response = client.get(
        "/admin/api/v1/system-configs",
        params={"q": key, "is_active": "true", "size": 50},
    )
    response.raise_for_status()
    payload = response.json()
    data = payload.get("data") or {}
    items = data.get("items") or data.get("content") or []
    if isinstance(data, list):
        items = data
    return _pick_exact(items, key)


def resolve_from_admin(
    base_url: str,
    token: str,
    *,
    timeout: float = 15.0,
) -> FeedTrainModeConfig:
    headers = {"Authorization": f"Bearer {token}"}
    with httpx.Client(base_url=base_url.rstrip("/"), timeout=timeout, headers=headers) as client:
        mode_item = _fetch_key(client, KEY_MODE)
        if mode_item is None:
            raise RuntimeError(f"Missing active Admin config {KEY_MODE}")
        mode = validate_mode(str(mode_item.get("configValue") or mode_item.get("config_value")))
        weight_item = _fetch_key(client, KEY_SEED_WEIGHT)
        min_item = _fetch_key(client, KEY_MIN_IMPRESSIONS)
        weight = float(
            (weight_item or {}).get("configValue")
            or (weight_item or {}).get("config_value")
            or 0.5
        )
        min_rows = int(
            (min_item or {}).get("configValue")
            or (min_item or {}).get("config_value")
            or 5000
        )
        return FeedTrainModeConfig(mode, weight, min_rows)


def resolve_feed_train_mode(
    *,
    admin_base_url: str | None = None,
    admin_token: str | None = None,
) -> FeedTrainModeConfig:
    env_cfg = resolve_from_env()
    if env_cfg is not None:
        return env_cfg
    if not admin_base_url or not admin_token:
        raise RuntimeError(
            "Feed train mode requires Admin credentials or RECSYS_FEED_CONFIG_FROM_ENV=1"
        )
    return resolve_from_admin(admin_base_url, admin_token)

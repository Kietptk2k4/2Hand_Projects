"""Build Home LTR dataset rows from impressions + provenance + labels."""

from __future__ import annotations

import json
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from pipelines.home_feature_order import HOME_FEATURE_ORDER
from pipelines.home_features import HomeCandidateInput, HomeProfileInput, build_home_feature_vector
from pipelines.home_labels import Engage, Impression, assign_nearest_impression_labels
from pipelines.home_popularity import PopularityNormalizer
from pipelines.home_train_mode import HomeTrainModeConfig, validate_mode


def _parse_dt(value: str | None) -> datetime | None:
    if not value:
        return None
    text = value.replace("Z", "+00:00")
    return datetime.fromisoformat(text)


def _read_jsonl(path: Path) -> list[dict[str, Any]]:
    if not path.exists():
        return []
    rows = []
    with path.open("r", encoding="utf-8") as handle:
        for line in handle:
            line = line.strip()
            if line:
                rows.append(json.loads(line))
    return rows


def build_home_dataset_from_sim_dir(
    sim_dir: Path,
    *,
    mode_cfg: HomeTrainModeConfig,
    products: list[dict[str, Any]] | None = None,
) -> tuple[list[dict[str, Any]], dict[str, Any]]:
    mode = validate_mode(mode_cfg.train_data_mode)
    impressions_raw = _read_jsonl(sim_dir / "home_impression_log.jsonl")
    engages_raw = _read_jsonl(sim_dir / "home_engage_event.jsonl")
    if mode == "REAL_ONLY" and len(impressions_raw) < mode_cfg.real_only_min_impressions:
        raise ValueError(
            f"REAL_ONLY requires >= {mode_cfg.real_only_min_impressions} impressions; got {len(impressions_raw)}"
        )

    products = products or json.loads((sim_dir / "products.json").read_text(encoding="utf-8"))
    product_by_id = {str(p["product_id"]): p for p in products}

    imps = [
        Impression(
            str(r["user_id"]),
            str(r["product_id"]),
            _parse_dt(r["shown_at"]) or datetime.now(timezone.utc),
            str(r["request_id"]),
        )
        for r in impressions_raw
    ]
    engs = [
        Engage(
            str(r["user_id"]),
            str(r["product_id"]),
            _parse_dt(r["occurred_at"]) or datetime.now(timezone.utc),
        )
        for r in engages_raw
    ]
    labeled = assign_nearest_impression_labels(imps, engs)

    # provisional normalizer from zeros/ones until fit on train split
    normalizer = PopularityNormalizer(z_lo=0.0, z_hi=1.0)
    empty_profile = HomeProfileInput({}, {}, {}, None, None)

    rows: list[dict[str, Any]] = []
    for imp, y in labeled:
        prod = product_by_id.get(imp.product_id, {})
        raw_imp = next(
            (
                r
                for r in impressions_raw
                if str(r["user_id"]) == imp.user_id
                and str(r["product_id"]) == imp.product_id
                and str(r["request_id"]) == imp.request_id
            ),
            {},
        )
        sources = raw_imp.get("sources") or []
        candidate = HomeCandidateInput(
            product_id=imp.product_id,
            category_id=prod.get("category_id"),
            brand_id=prod.get("brand_id"),
            shop_id=prod.get("shop_id"),
            effective_price=prod.get("effective_price"),
            created_at=_parse_dt(prod.get("created_at")),
            rating_avg=prod.get("rating_avg"),
            rating_count=int(prod.get("rating_count") or 0),
            popularity_raw=0,
            sources=frozenset(str(s).upper() for s in sources),
            personal_score=raw_imp.get("personal_score"),
            cf_score=raw_imp.get("cf_score"),
            ar_score=raw_imp.get("ar_score"),
        )
        vector = build_home_feature_vector(candidate, empty_profile, normalizer, imp.shown_at)
        row = {
            "user_id": imp.user_id,
            "product_id": imp.product_id,
            "request_id": imp.request_id,
            "shown_at": imp.shown_at.isoformat().replace("+00:00", "Z"),
            "label": y,
            "data_source": raw_imp.get("data_source") or ("SEED" if mode != "REAL_ONLY" else "REAL"),
            "sample_weight": (
                mode_cfg.seed_row_weight
                if (raw_imp.get("data_source") or "SEED") == "SEED" and mode == "HYBRID"
                else 1.0
            ),
        }
        for i, name in enumerate(HOME_FEATURE_ORDER):
            row[name] = vector[i]
        rows.append(row)

    meta = {
        "train_data_mode": mode,
        "rows": len(rows),
        "feature_order": HOME_FEATURE_ORDER,
        "positives": sum(1 for r in rows if r["label"] == 1),
    }
    return rows, meta

"""File/RAM Home sim bootstrap (D16) — no shared DB writes."""

from __future__ import annotations

import json
import random
from datetime import datetime, timedelta, timezone
from pathlib import Path
from typing import Any
from uuid import uuid4

from pipelines.home_ar import Basket, mine_tag_category_rules
from pipelines.home_entity_cf import (
    CartLine,
    OrderLine,
    accumulate_cart_pairs,
    accumulate_completed_pairs,
    merge_weight_maps,
    scores_from_weights,
)
from pipelines.home_social_export import SocialEvent, build_social_interest_export


NICHES = ("minimal", "streetwear", "vintage", "athleisure")


def run_home_sim_to_dir(
    output_dir: Path,
    *,
    n_users: int = 40,
    n_products: int = 80,
    sessions_per_user: int = 3,
    seed: int = 42,
    as_of: datetime | None = None,
) -> dict[str, Any]:
    """Generate Home impressions/engages + CF/AR/export files under output_dir."""
    rng = random.Random(seed)
    as_of = as_of or datetime(2026, 8, 1, tzinfo=timezone.utc)
    output_dir.mkdir(parents=True, exist_ok=True)

    users = [f"a1000000-0000-4000-8000-{i:012d}" for i in range(1, n_users + 1)]
    personas = [NICHES[i % len(NICHES)] for i in range(n_users)]

    categories = {n: f"c3000000-0000-4000-8000-{i:012d}" for i, n in enumerate(NICHES, start=1)}
    brands = {n: f"b2000000-0000-4000-8000-{i:012d}" for i, n in enumerate(NICHES, start=1)}

    products: list[dict[str, Any]] = []
    for i in range(n_products):
        niche = NICHES[i % len(NICHES)]
        products.append(
            {
                "product_id": f"c3000000-1111-4000-8000-{i:012d}",
                "category_id": categories[niche],
                "brand_id": brands[niche],
                "shop_id": f"b2000000-2222-4000-8000-{(i % 10) + 1:012d}",
                "niche": niche,
                "created_at": (as_of - timedelta(days=rng.randint(1, 60))).isoformat().replace("+00:00", "Z"),
                "effective_price": float(rng.randint(50, 500)),
                "rating_avg": round(rng.uniform(3.0, 5.0), 2),
                "rating_count": rng.randint(0, 20),
            }
        )

    order_lines: list[OrderLine] = []
    cart_lines: list[CartLine] = []
    social_events: list[SocialEvent] = []
    impressions: list[dict[str, Any]] = []
    engages: list[dict[str, Any]] = []

    for ui, user_id in enumerate(users):
        niche = personas[ui]
        # seed purchase + social interest in niche
        p = rng.choice([x for x in products if x["niche"] == niche] or products)
        oid = f"e5000000-0000-4000-8000-{ui + 1:012d}"
        completed = as_of - timedelta(days=rng.randint(5, 40))
        order_lines.append(
            OrderLine(oid, user_id, p["category_id"], p["brand_id"], completed, "COMPLETED")
        )
        social_events.append(
            SocialEvent(user_id, "HASHTAG", niche, "search", as_of - timedelta(days=3))
        )
        social_events.append(
            SocialEvent(user_id, "HASHTAG", niche, "like", as_of - timedelta(days=2))
        )

        for s in range(sessions_per_user):
            request_id = str(uuid4())
            shown_at = as_of - timedelta(days=sessions_per_user - s, hours=rng.randint(0, 12))
            pool = [x for x in products if x["niche"] == niche][:20] or products[:20]
            served = rng.sample(pool, k=min(10, len(pool)))
            for rank, prod in enumerate(served, start=1):
                sources = ["POPULAR"]
                if prod["niche"] == niche:
                    sources.append("PERSONAL")
                impressions.append(
                    {
                        "user_id": user_id,
                        "product_id": prod["product_id"],
                        "shown_at": shown_at.isoformat().replace("+00:00", "Z"),
                        "rank_position": rank,
                        "request_id": request_id,
                        "ranking_mode": "DEGRADED",
                        "sources": sources,
                        "personal_score": 0.8 if "PERSONAL" in sources else None,
                        "cf_score": None,
                        "ar_score": None,
                        "data_source": "SEED",
                    }
                )
                if rng.random() < 0.2:
                    eng_at = shown_at + timedelta(minutes=rng.randint(5, 120))
                    engages.append(
                        {
                            "user_id": user_id,
                            "product_id": prod["product_id"],
                            "event_type": "CLICK",
                            "occurred_at": eng_at.isoformat().replace("+00:00", "Z"),
                            "request_id": request_id,
                        }
                    )
                    cart_lines.append(
                        CartLine(user_id, prod["category_id"], prod["brand_id"], eng_at)
                    )

    weights = merge_weight_maps(
        accumulate_completed_pairs(order_lines, as_of),
        accumulate_cart_pairs(cart_lines, as_of),
    )
    cf_rows = scores_from_weights(weights, top_m=50)
    export_rows = build_social_interest_export(social_events, as_of)
    baskets = []
    for ui, user_id in enumerate(users):
        niche = personas[ui]
        baskets.append(
            Basket(
                user_id,
                frozenset({("HASHTAG", niche)}),
                frozenset({categories[niche]}),
            )
        )
    ar_rows = mine_tag_category_rules(baskets, min_support=0.01, min_confidence=0.05)

    _write_jsonl(output_dir / "home_impression_log.jsonl", impressions)
    _write_jsonl(output_dir / "home_engage_event.jsonl", engages)
    _write_jsonl(output_dir / "entity_cooccur.jsonl", cf_rows)
    _write_jsonl(output_dir / "user_social_interest_export.jsonl", export_rows)
    _write_jsonl(output_dir / "social_tag_category_ar.jsonl", ar_rows)
    _write_json(output_dir / "products.json", products)
    summary = {
        "users": n_users,
        "products": n_products,
        "impressions": len(impressions),
        "engages": len(engages),
        "cf_edges": len(cf_rows),
        "ar_rules": len(ar_rows),
        "export_rows": len(export_rows),
        "as_of": as_of.isoformat().replace("+00:00", "Z"),
    }
    _write_json(output_dir / "sim_summary.json", summary)
    return summary


def _write_jsonl(path: Path, rows: list[dict[str, Any]]) -> None:
    with path.open("w", encoding="utf-8") as handle:
        for row in rows:
            handle.write(json.dumps(row, ensure_ascii=False) + "\n")


def _write_json(path: Path, payload: Any) -> None:
    path.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")

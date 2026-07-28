"""Export user_purchase_profile.csv from Commerce COMPLETED orders (read-only)."""

from __future__ import annotations

import csv
import json
import logging
from collections import defaultdict
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from app.config import Settings, get_settings

logger = logging.getLogger(__name__)


def _parse_ts(value: Any) -> datetime | None:
    if value is None:
        return None
    if isinstance(value, datetime):
        dt = value
        if dt.tzinfo is None:
            return dt.replace(tzinfo=timezone.utc)
        return dt.astimezone(timezone.utc)
    text = str(value).strip()
    if not text:
        return None
    try:
        return datetime.fromisoformat(text.replace("Z", "+00:00")).astimezone(timezone.utc)
    except ValueError:
        return None


def _normalize_dsn(url: str) -> str:
    return url.replace("postgresql+psycopg://", "postgresql://").replace(
        "postgresql+psycopg2://", "postgresql://"
    )


def aggregate_purchase_profiles(
    order_rows: list[dict[str, Any]],
    *,
    as_of: datetime | str | None = None,
) -> list[dict[str, Any]]:
    """
    Aggregate buyer purchase history into profile rows.

    Expected row keys: buyer_id, category_id, shop_id, completed_at, order_status
    Only COMPLETED orders are kept. When as_of is set, completed_at must be <= as_of.
    """
    cutoff = _parse_ts(as_of) if not isinstance(as_of, datetime) else as_of
    if isinstance(as_of, datetime) and as_of.tzinfo is None:
        cutoff = as_of.replace(tzinfo=timezone.utc)

    cats: dict[str, set[str]] = defaultdict(set)
    shops: dict[str, set[str]] = defaultdict(set)

    for row in order_rows:
        status = str(row.get("order_status") or row.get("status") or "").upper()
        if status and status != "COMPLETED":
            continue
        completed = _parse_ts(row.get("completed_at") or row.get("order_completed_at"))
        if cutoff is not None:
            if completed is None or completed > cutoff:
                continue
        buyer = row.get("buyer_id") or row.get("user_id")
        if not buyer:
            continue
        uid = str(buyer)
        cid = row.get("category_id")
        sid = row.get("shop_id")
        if cid:
            cats[uid].add(str(cid))
        if sid:
            shops[uid].add(str(sid))

    users = sorted(set(cats) | set(shops))
    return [
        {
            "user_id": uid,
            "category_ids": sorted(cats.get(uid, set())),
            "shop_ids": sorted(shops.get(uid, set())),
        }
        for uid in users
    ]


def fetch_completed_order_lines(settings: Settings) -> list[dict[str, Any]]:
    import psycopg
    from psycopg.rows import dict_row

    if not settings.commerce_postgres_url:
        raise ValueError("Missing required database configuration: COMMERCE_POSTGRES_URL")

    dsn = _normalize_dsn(settings.commerce_postgres_url)
    sql = """
        SELECT
            o.buyer_id,
            o.status AS order_status,
            o.completed_at,
            p.category_id,
            p.shop_id
        FROM orders o
        JOIN order_items oi ON oi.order_id = o.id
        JOIN products p ON p.id = oi.product_id
        WHERE o.status = 'COMPLETED'
    """
    with psycopg.connect(dsn, row_factory=dict_row) as conn:
        with conn.cursor() as cur:
            cur.execute(sql)
            return list(cur.fetchall())


def write_purchase_profile_csv(path: Path, profiles: list[dict[str, Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=["user_id", "category_ids", "shop_ids"])
        writer.writeheader()
        for row in profiles:
            writer.writerow(
                {
                    "user_id": row["user_id"],
                    "category_ids": json.dumps(row["category_ids"], ensure_ascii=False),
                    "shop_ids": json.dumps(row["shop_ids"], ensure_ascii=False),
                }
            )


def run_export_purchase_profile(
    settings: Settings | None = None,
    *,
    as_of: datetime | str | None = None,
    order_rows: list[dict[str, Any]] | None = None,
) -> dict[str, Any]:
    settings = settings or get_settings()
    cutoff = _parse_ts(as_of) if not isinstance(as_of, datetime) else as_of
    if isinstance(as_of, datetime) and as_of.tzinfo is None:
        cutoff = as_of.replace(tzinfo=timezone.utc)

    if order_rows is None:
        order_rows = fetch_completed_order_lines(settings)

    profiles = aggregate_purchase_profiles(order_rows, as_of=cutoff)
    out_dir = Path(settings.recsys_dataset_output_dir)
    out_path = out_dir / "user_purchase_profile.csv"
    write_purchase_profile_csv(out_path, profiles)

    summary = {
        "users": len(profiles),
        "as_of": cutoff.isoformat().replace("+00:00", "Z") if cutoff else None,
        "output_path": str(out_path.resolve()),
        "provisional": cutoff is None,
        "finished_at": datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
    }
    logger.info("Purchase profile export finished: %s", summary)
    return summary

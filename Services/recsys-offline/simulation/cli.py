"""CLI entrypoints for seed / simulate / kpi (dev-only)."""

from __future__ import annotations

import argparse
import json
import sys
from datetime import datetime, timedelta
from pathlib import Path

from app.config import get_settings
from pipelines.build_dataset import build_rows
from pipelines.export_purchase_profile import aggregate_purchase_profiles, write_purchase_profile_csv
from simulation.engine import result_to_cleaned_sources, run_simulation
from simulation.kpi import evaluate_kpis
from simulation.repair_post_timestamps import (
    DEFAULT_CAPTION_PREFIX,
    DEFAULT_WINDOW_DAYS,
    repair_mongo_post_timestamps,
)
from simulation.skeleton import build_skeleton, summarize_skeleton
from simulation.timestamps import parse_utc_datetime
from simulation.writers import (
    resolve_post_clock,
    write_auth_users,
    write_commerce_skeleton,
    write_simulation_interactions,
    write_social_posts_mongo,
    write_social_user_projections_mongo,
)


def _parse_end_at(value: str | None) -> datetime | None:
    if not value:
        return None
    parsed = parse_utc_datetime(value)
    if parsed is None:
        raise SystemExit(f"Invalid --sim-end-at value: {value}")
    return parsed


def cmd_dry_run(args: argparse.Namespace) -> int:
    skeleton = build_skeleton(scale=args.scale, config_path=args.config)
    summary = summarize_skeleton(skeleton)
    print(json.dumps(summary, indent=2))
    expected = summary["expected_raw_impressions"]
    if args.scale == "full" and expected < 20_000:
        print("WARNING: expected raw impressions < 20000", file=sys.stderr)
        return 1
    return 0


def cmd_simulate_memory(args: argparse.Namespace) -> int:
    skeleton = build_skeleton(scale=args.scale, config_path=args.config)
    result = run_simulation(skeleton, seed=args.seed)
    sources = result_to_cleaned_sources(result)
    rows, ds_summary = build_rows(sources)
    kpi = evaluate_kpis(
        rows,
        buyer_rate=result.meta.get("buyer_rate"),
        scale=args.scale,
        profile_as_of_ok=True,
    )
    out = {
        "skeleton": summarize_skeleton(skeleton),
        "sim_meta": result.meta,
        "dataset": ds_summary,
        "kpi": kpi,
    }
    if args.out:
        Path(args.out).write_text(json.dumps(out, indent=2), encoding="utf-8")
    print(json.dumps(out, indent=2))
    return 0 if kpi["ok"] else 2


def cmd_seed_db(args: argparse.Namespace) -> int:
    settings = get_settings()
    settings.require_sim_allow()
    skeleton = build_skeleton(scale=args.scale, config_path=args.config)
    end_at = _parse_end_at(args.sim_end_at)
    sim_days = args.sim_days
    written: dict[str, object] = {"skeleton": summarize_skeleton(skeleton)}
    if settings.auth_postgres_url:
        written["auth_users"] = write_auth_users(settings.auth_postgres_url, skeleton)
    if settings.commerce_postgres_url:
        written["commerce"] = write_commerce_skeleton(settings.commerce_postgres_url, skeleton)
    if settings.social_mongo_url:
        written["mongo_user_projections"] = write_social_user_projections_mongo(
            settings.social_mongo_url,
            settings.social_mongo_db,
            skeleton,
        )
        clock_end, clock_days = resolve_post_clock(
            skeleton, end_at=end_at, sim_days=sim_days
        )
        written["mongo_posts"] = write_social_posts_mongo(
            settings.social_mongo_url,
            settings.social_mongo_db,
            skeleton,
            end_at=end_at,
            sim_days=sim_days,
        )
        written["post_clock"] = {
            "end_at": clock_end.isoformat(),
            "sim_days": clock_days,
        }
    if args.simulate:
        clock_end, clock_days = resolve_post_clock(
            skeleton, end_at=end_at, sim_days=sim_days
        )
        sim_start: datetime | None = None
        if end_at is not None or sim_days is not None:
            sim_start = clock_end - timedelta(days=clock_days)
        result = run_simulation(skeleton, seed=args.seed, start_at=sim_start)
        if settings.social_postgres_url:
            written["interactions"] = write_simulation_interactions(
                settings.social_postgres_url,
                settings.commerce_postgres_url,
                result,
            )
        written["sim_meta"] = result.meta
    print(json.dumps(written, indent=2, default=str))
    return 0


def cmd_repair_post_timestamps(args: argparse.Namespace) -> int:
    settings = get_settings()
    settings.require_sim_allow()
    if not settings.social_mongo_url:
        raise SystemExit("SOCIAL_MONGO_URL is required for repair-post-timestamps")
    caption_prefix: str | None
    if args.all_posts:
        caption_prefix = None
    else:
        caption_prefix = args.caption_prefix
    summary = repair_mongo_post_timestamps(
        settings.social_mongo_url,
        settings.social_mongo_db,
        window_days=args.window_days,
        dry_run=not args.apply,
        caption_prefix=caption_prefix,
    )
    print(json.dumps(summary.to_dict(), indent=2))
    return 0


def cmd_export_profile_from_sim(args: argparse.Namespace) -> int:
    """Helper: rebuild profile CSV from in-memory sim orders with optional as_of."""
    skeleton = build_skeleton(scale=args.scale, config_path=args.config)
    result = run_simulation(skeleton, seed=args.seed)
    order_rows = [
        {
            "buyer_id": o["buyer_id"],
            "category_id": o.get("category_id"),
            "shop_id": o.get("shop_id"),
            "completed_at": o.get("completed_at"),
            "order_status": o.get("status"),
        }
        for o in result.orders
    ]
    profiles = aggregate_purchase_profiles(order_rows, as_of=args.as_of)
    out = Path(args.out or "data/cleaned/user_purchase_profile.csv")
    write_purchase_profile_csv(out, profiles)
    print(json.dumps({"users": len(profiles), "output": str(out)}, indent=2))
    return 0


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Recsys fashion simulation (dev-only)")
    parser.add_argument("--config", default=None, help="Path to personas.yaml")
    sub = parser.add_subparsers(dest="cmd", required=True)

    p_dry = sub.add_parser("dry-run", help="Validate skeleton volumes without DB")
    p_dry.add_argument("--scale", choices=["full", "smoke"], default="smoke")
    p_dry.set_defaults(func=cmd_dry_run)

    p_sim = sub.add_parser("simulate-memory", help="In-memory sim + build_rows + KPI")
    p_sim.add_argument("--scale", choices=["full", "smoke"], default="smoke")
    p_sim.add_argument("--seed", type=int, default=42)
    p_sim.add_argument("--out", default=None)
    p_sim.set_defaults(func=cmd_simulate_memory)

    p_seed = sub.add_parser("seed-db", help="Write skeleton (+optional sim) to DBs")
    p_seed.add_argument("--scale", choices=["full", "smoke"], default="smoke")
    p_seed.add_argument("--seed", type=int, default=42)
    p_seed.add_argument("--simulate", action="store_true")
    p_seed.add_argument(
        "--sim-end-at",
        default=None,
        help="UTC end of post clock window (ISO-8601). Default: now UTC",
    )
    p_seed.add_argument(
        "--sim-days",
        type=int,
        default=None,
        help="Post timestamp span in days (default: skeleton volumes.sim_days)",
    )
    p_seed.set_defaults(func=cmd_seed_db)

    p_repair = sub.add_parser(
        "repair-post-timestamps",
        help="Convert string/stale Mongo post created_at to BSON Date in recent window",
    )
    p_repair.add_argument(
        "--window-days",
        type=int,
        default=DEFAULT_WINDOW_DAYS,
        help=f"Target recent window (default {DEFAULT_WINDOW_DAYS})",
    )
    p_repair.add_argument(
        "--apply",
        action="store_true",
        help="Persist updates (default is dry-run)",
    )
    p_repair.add_argument(
        "--caption-prefix",
        default=DEFAULT_CAPTION_PREFIX,
        help=f"Only posts whose caption starts with this prefix (default '{DEFAULT_CAPTION_PREFIX}')",
    )
    p_repair.add_argument(
        "--all-posts",
        action="store_true",
        help="Escape hatch: ignore caption filter (known-dev DBs only)",
    )
    p_repair.set_defaults(func=cmd_repair_post_timestamps)

    p_prof = sub.add_parser("export-profile-from-sim", help="CSV profile from memory sim")
    p_prof.add_argument("--scale", choices=["full", "smoke"], default="smoke")
    p_prof.add_argument("--seed", type=int, default=42)
    p_prof.add_argument("--as-of", dest="as_of", default=None)
    p_prof.add_argument("--out", default=None)
    p_prof.set_defaults(func=cmd_export_profile_from_sim)

    args = parser.parse_args(argv)
    return int(args.func(args))


if __name__ == "__main__":
    raise SystemExit(main())

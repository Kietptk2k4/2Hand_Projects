"""Smoke tests for fashion simulation (in-memory, no DB)."""

from datetime import datetime, timezone

from pipelines.build_dataset import build_rows
from pipelines.export_purchase_profile import aggregate_purchase_profiles
from pipelines.normalize import clean_posts
from simulation.engine import result_to_cleaned_sources, run_simulation
from simulation.kpi import evaluate_kpis
from simulation.personas import load_persona_config
from simulation.skeleton import build_skeleton, summarize_skeleton


def test_persona_config_is_fashion_only():
    cfg = load_persona_config()
    assert cfg["vertical"] == "fashion-secondhand"
    for niche in cfg["niches"].values():
        for cat in niche["category_ids"]:
            assert cat.startswith("f1000000-")


def test_dry_run_smoke_volumes():
    skeleton = build_skeleton(scale="smoke")
    summary = summarize_skeleton(skeleton)
    assert summary["users"] == 10
    assert summary["posts"] == 40
    assert summary["products"] == 24
    assert 0.55 <= summary["product_tag_rate"] <= 0.65
    assert summary["expected_raw_impressions"] == 10 * 5 * 8


def test_dry_run_full_volume_target():
    skeleton = build_skeleton(scale="full")
    summary = summarize_skeleton(skeleton)
    assert summary["users"] == 120
    assert summary["posts"] == 600
    assert summary["expected_raw_impressions"] >= 20_000


def test_simulation_no_reimpress_and_label_window():
    skeleton = build_skeleton(scale="smoke")
    result = run_simulation(skeleton, seed=7)
    assert len(result.seen) == len(result.impressions)
    assert len(result.impressions) == len({(i["user_id"], i["post_id"]) for i in result.impressions})
    for like in result.likes[:20]:
        matching = [
            i
            for i in result.impressions
            if i["user_id"] == like["user_id"] and i["post_id"] == like["post_id"]
        ]
        assert matching
        shown = datetime.fromisoformat(matching[0]["shown_at"].replace("Z", "+00:00"))
        created = datetime.fromisoformat(like["created_at"].replace("Z", "+00:00"))
        assert shown <= created
        assert (created - shown).total_seconds() < 24 * 3600


def test_simulation_search_keywords_are_hashtags():
    cfg = load_persona_config()
    vocab = {t for n in cfg["niches"].values() for t in n["hashtags"]}
    skeleton = build_skeleton(scale="smoke", config=cfg)
    result = run_simulation(skeleton, seed=1)
    for row in result.searches:
        assert row["keyword"] in vocab


def test_smoke_sim_builds_dataset_and_cross_domain():
    skeleton = build_skeleton(scale="smoke")
    result = run_simulation(skeleton, seed=42)
    sources = result_to_cleaned_sources(result)
    # product tags survive clean
    kept, _ = clean_posts(
        [
            {
                "_id": p["post_id"],
                "author_id": p["author_id"],
                "created_at": p["created_at"],
                "status": "ACTIVE",
                "productTags": p["product_tags"],
                "hashtags": p["hashtags"],
            }
            for p in sources["posts"]
        ]
    )
    assert any(p["product_tags"] for p in kept)

    rows, summary = build_rows(sources)
    assert summary["rows"] == len(result.impressions)
    assert summary["rows"] >= 50
    kpi = evaluate_kpis(rows, buyer_rate=result.meta["buyer_rate"], scale="smoke")
    assert kpi["metrics"]["rows"] >= 50
    # Prefer cross-domain signal when purchases exist
    if result.orders:
        assert kpi["metrics"]["cross_domain_share"] > 0


def test_purchase_profile_as_of_cutoff_from_sim_orders():
    skeleton = build_skeleton(scale="smoke")
    result = run_simulation(skeleton, seed=3)
    order_rows = [
        {
            "buyer_id": o["buyer_id"],
            "category_id": o["category_id"],
            "shop_id": o["shop_id"],
            "completed_at": o["completed_at"],
            "order_status": o["status"],
        }
        for o in result.orders
    ]
    if not order_rows:
        return
    # Pick a mid cutoff: exclude last half by sorting
    times = sorted(o["completed_at"] for o in order_rows)
    mid = times[len(times) // 2]
    filtered = aggregate_purchase_profiles(order_rows, as_of=mid)
    full = aggregate_purchase_profiles(order_rows, as_of=None)
    # Filtered category universe should be subset of full
    full_cats = {c for p in full for c in p["category_ids"]}
    filt_cats = {c for p in filtered for c in p["category_ids"]}
    assert filt_cats.issubset(full_cats)

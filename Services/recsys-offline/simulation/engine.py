"""Sim-clock bot engine: impressions → engage → cart → purchase (in-memory)."""

from __future__ import annotations

import random
import uuid
from dataclasses import dataclass, field
from datetime import datetime, timedelta, timezone
from typing import Any

from simulation.skeleton import Skeleton, post_affinity


@dataclass
class SimulationResult:
    impressions: list[dict[str, Any]] = field(default_factory=list)
    likes: list[dict[str, Any]] = field(default_factory=list)
    saves: list[dict[str, Any]] = field(default_factory=list)
    comments: list[dict[str, Any]] = field(default_factory=list)
    follows: list[dict[str, Any]] = field(default_factory=list)
    searches: list[dict[str, Any]] = field(default_factory=list)
    carts: list[dict[str, Any]] = field(default_factory=list)
    orders: list[dict[str, Any]] = field(default_factory=list)
    seen: set[tuple[str, str]] = field(default_factory=set)
    posts: list[dict[str, Any]] = field(default_factory=list)
    users: list[dict[str, Any]] = field(default_factory=list)
    products: list[dict[str, Any]] = field(default_factory=list)
    meta: dict[str, Any] = field(default_factory=dict)


def _p_like(engage: dict[str, Any], a: float) -> float:
    return float(engage.get("like_base", 0.03)) + float(engage.get("like_scale", 0.35)) * (a * a)


def _sample_feed(
    rng: random.Random,
    posts: list[dict[str, Any]],
    persona: str,
    skeleton: Skeleton,
    seen: set[tuple[str, str]],
    user_id: str,
    feed_size: int,
) -> list[dict[str, Any]]:
    candidates = [p for p in posts if (user_id, p["post_id"]) not in seen]
    if not candidates:
        return []
    weights = []
    for post in candidates:
        a = post_affinity(skeleton, persona, post)
        weights.append(max(0.02, a) ** 2)
    # Intentional mismatch: flatten 15% toward uniform
    uniform = 1.0 / len(candidates)
    mixed = [0.85 * w + 0.15 * uniform * sum(weights) for w in weights]
    chosen: list[dict[str, Any]] = []
    pool = list(candidates)
    pool_w = list(mixed)
    k = min(feed_size, len(pool))
    for _ in range(k):
        total = sum(pool_w)
        pick = rng.random() * total
        acc = 0.0
        idx = 0
        for i, w in enumerate(pool_w):
            acc += w
            if acc >= pick:
                idx = i
                break
        chosen.append(pool.pop(idx))
        pool_w.pop(idx)
    # Order by affinity desc for rank_position
    chosen.sort(key=lambda p: post_affinity(skeleton, persona, p), reverse=True)
    return chosen


def run_simulation(
    skeleton: Skeleton,
    *,
    seed: int = 42,
    start_at: datetime | None = None,
) -> SimulationResult:
    rng = random.Random(seed)
    cfg = skeleton.config
    engage = cfg.get("engage") or {}
    vol = skeleton.volumes
    sessions = int(vol["sessions_per_user"])
    feed_size = int(vol["feed_size"])
    sim_days = int(vol.get("sim_days", 21))
    start = start_at or datetime(2026, 1, 1, tzinfo=timezone.utc)

    result = SimulationResult(
        posts=[dict(p) for p in skeleton.posts],
        users=[dict(u) for u in skeleton.users],
        products=[dict(p) for p in skeleton.products],
    )
    stock = {p["product_id"]: int(p.get("stock_quantity", 1)) for p in skeleton.products}
    follow_keys: set[tuple[str, str]] = set()
    order_seq = 0
    search_seq = 0

    for user in skeleton.users:
        user_id = user["user_id"]
        persona = user["persona"]
        for session_idx in range(sessions):
            # Spread sessions across sim_days; early sessions still create impressions
            day_offset = (session_idx / max(sessions - 1, 1)) * (sim_days - 1)
            shown_at = start + timedelta(days=day_offset, hours=rng.uniform(8, 22))
            request_id = str(uuid.UUID(int=rng.getrandbits(128)))

            feed = _sample_feed(
                rng,
                result.posts,
                persona,
                skeleton,
                result.seen,
                user_id,
                feed_size,
            )
            for rank, post in enumerate(feed, start=1):
                post_id = post["post_id"]
                key = (user_id, post_id)
                if key in result.seen:
                    continue
                result.seen.add(key)
                result.impressions.append(
                    {
                        "user_id": user_id,
                        "post_id": post_id,
                        "shown_at": shown_at.isoformat().replace("+00:00", "Z"),
                        "rank_position": rank,
                        "model_version": None,
                        "model_name": None,
                        "request_id": request_id,
                    }
                )

                a = post_affinity(skeleton, persona, post)
                p_like = _p_like(engage, a)
                engaged = False
                delta_h = rng.uniform(0.02, 12.0)
                engage_at = shown_at + timedelta(hours=delta_h)
                engage_iso = engage_at.isoformat().replace("+00:00", "Z")

                if rng.random() < p_like:
                    result.likes.append(
                        {"user_id": user_id, "post_id": post_id, "created_at": engage_iso}
                    )
                    post["like_count"] = int(post.get("like_count") or 0) + 1
                    engaged = True
                    if rng.random() < float(engage.get("save_of_like", 0.4)):
                        result.saves.append(
                            {
                                "user_id": user_id,
                                "post_id": post_id,
                                "created_at": engage_iso,
                            }
                        )
                    if rng.random() < float(engage.get("comment_of_like", 0.15)):
                        result.comments.append(
                            {
                                "comment_id": f"simcmt{len(result.comments)+1:016d}",
                                "post_id": post_id,
                                "author_id": user_id,
                                "created_at": engage_iso,
                                "status": "ACTIVE",
                            }
                        )
                        post["reply_count"] = int(post.get("reply_count") or 0) + 1

                if rng.random() < float(engage.get("follow_scale", 0.1)) * a:
                    author = post["author_id"]
                    fk = (user_id, author)
                    if author != user_id and fk not in follow_keys:
                        follow_keys.add(fk)
                        result.follows.append(
                            {
                                "follower_id": user_id,
                                "followee_id": author,
                                "status": "ACCEPTED",
                                "created_at": engage_iso,
                            }
                        )

                if post.get("product_tags"):
                    cart_p = float(engage.get("cart_after_engage", 0.55))
                    if engaged:
                        pass
                    elif a >= 0.55:
                        # Affinity-driven browse-to-cart without like (still minority path)
                        cart_p *= 0.35
                    else:
                        cart_p = 0.0
                    if a >= 0.5:
                        cart_p = min(0.95, cart_p + 0.10)
                    if cart_p > 0 and rng.random() < cart_p:
                        tag = post["product_tags"][0]
                        product_id = tag.get("productId") or tag.get("product_id")
                        if product_id and stock.get(product_id, 0) > 0:
                            cart_at = engage_at + timedelta(minutes=rng.uniform(5, 90))
                            result.carts.append(
                                {
                                    "user_id": user_id,
                                    "product_id": product_id,
                                    "shop_id": tag.get("shopId") or tag.get("shop_id"),
                                    "category_id": tag.get("categoryId")
                                    or tag.get("category_id"),
                                    "created_at": cart_at.isoformat().replace("+00:00", "Z"),
                                }
                            )
                            purchase_p = float(engage.get("purchase_after_cart", 0.8))
                            if rng.random() < purchase_p and stock.get(product_id, 0) > 0:
                                stock[product_id] -= 1
                                order_seq += 1
                                completed = cart_at + timedelta(
                                    hours=rng.uniform(1, 36)
                                )
                                result.orders.append(
                                    {
                                        "order_id": f"e5000000-0000-4000-8000-{order_seq:012d}",
                                        "buyer_id": user_id,
                                        "product_id": product_id,
                                        "shop_id": tag.get("shopId")
                                        or tag.get("shop_id"),
                                        "category_id": tag.get("categoryId")
                                        or tag.get("category_id"),
                                        "status": "COMPLETED",
                                        "completed_at": completed.isoformat().replace(
                                            "+00:00", "Z"
                                        ),
                                        "created_at": cart_at.isoformat().replace(
                                            "+00:00", "Z"
                                        ),
                                    }
                                )

            # Search once per session with hashtag vocab
            if rng.random() < float(engage.get("search_per_session", 0.55)):
                niche = rng.choice(list((cfg.get("niches") or {}).keys()))
                tags = list(cfg["niches"][niche]["hashtags"])
                keyword = rng.choice(tags)
                search_seq += 1
                result.searches.append(
                    {
                        "id": f"f6000000-0000-4000-8000-{search_seq:012d}",
                        "user_id": user_id,
                        "keyword": keyword,
                        "created_at": shown_at.isoformat().replace("+00:00", "Z"),
                    }
                )

        # Ensure most bots purchase at least once (DoD buyer_rate) using in-stock niche goods
        user_orders = [o for o in result.orders if o["buyer_id"] == user_id]
        if not user_orders and rng.random() < 0.85:
            persona = user["persona"]
            primary = None
            for p in cfg.get("personas") or []:
                if p.get("id") == persona:
                    primary = p.get("primary_niche")
                    break
            candidates = [
                p
                for p in skeleton.products
                if stock.get(p["product_id"], 0) > 0
                and (primary is None or p.get("niche") == primary)
            ]
            if not candidates:
                candidates = [p for p in skeleton.products if stock.get(p["product_id"], 0) > 0]
            if candidates:
                product = rng.choice(candidates)
                stock[product["product_id"]] -= 1
                order_seq += 1
                completed = start + timedelta(days=sim_days - 1, hours=rng.uniform(10, 20))
                created = completed - timedelta(hours=2)
                result.orders.append(
                    {
                        "order_id": f"e5000000-0000-4000-8000-{order_seq:012d}",
                        "buyer_id": user_id,
                        "product_id": product["product_id"],
                        "shop_id": product["shop_id"],
                        "category_id": product["category_id"],
                        "status": "COMPLETED",
                        "completed_at": completed.isoformat().replace("+00:00", "Z"),
                        "created_at": created.isoformat().replace("+00:00", "Z"),
                    }
                )
                result.carts.append(
                    {
                        "user_id": user_id,
                        "product_id": product["product_id"],
                        "shop_id": product["shop_id"],
                        "category_id": product["category_id"],
                        "created_at": created.isoformat().replace("+00:00", "Z"),
                    }
                )

    buyers = {o["buyer_id"] for o in result.orders}
    result.meta = {
        "impressions": len(result.impressions),
        "unique_pairs": len(result.seen),
        "likes": len(result.likes),
        "saves": len(result.saves),
        "comments": len(result.comments),
        "follows": len(result.follows),
        "searches": len(result.searches),
        "carts": len(result.carts),
        "orders": len(result.orders),
        "buyer_rate": (len(buyers) / len(skeleton.users)) if skeleton.users else 0.0,
        "seed": seed,
        "start_at": start.isoformat().replace("+00:00", "Z"),
    }
    return result


def result_to_cleaned_sources(result: SimulationResult) -> dict[str, list[dict[str, Any]]]:
    """Shape compatible with build_dataset.build_rows / clean CSV consumers."""
    posts = []
    for p in result.posts:
        posts.append(
            {
                "post_id": p["post_id"],
                "author_id": p["author_id"],
                "hashtags": p.get("hashtags") or [],
                "like_count": p.get("like_count") or 0,
                "reply_count": p.get("reply_count") or 0,
                "created_at": "2025-12-20T10:00:00Z",
                "product_tags": p.get("product_tags") or [],
                "status": "ACTIVE",
                "visibility": "PUBLIC",
            }
        )
    profiles_map: dict[str, dict[str, set[str]]] = {}
    for o in result.orders:
        uid = o["buyer_id"]
        bucket = profiles_map.setdefault(uid, {"category_ids": set(), "shop_ids": set()})
        if o.get("category_id"):
            bucket["category_ids"].add(str(o["category_id"]))
        if o.get("shop_id"):
            bucket["shop_ids"].add(str(o["shop_id"]))
    profiles = [
        {
            "user_id": uid,
            "category_ids": sorted(v["category_ids"]),
            "shop_ids": sorted(v["shop_ids"]),
        }
        for uid, v in profiles_map.items()
    ]
    return {
        "posts": posts,
        "comments": result.comments,
        "post_likes": result.likes,
        "post_saves": result.saves,
        "follows": result.follows,
        "search_history": result.searches,
        "post_impression_log": result.impressions,
        "user_purchase_profile": profiles,
    }

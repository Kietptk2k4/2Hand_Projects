"""Build deterministic skeleton entities (users, shops, products, posts)."""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

from simulation import ids
from simulation.personas import affinity, load_persona_config, niche_names, volumes_for


@dataclass
class Skeleton:
    scale: str
    users: list[dict[str, Any]] = field(default_factory=list)
    shops: list[dict[str, Any]] = field(default_factory=list)
    products: list[dict[str, Any]] = field(default_factory=list)
    posts: list[dict[str, Any]] = field(default_factory=list)
    volumes: dict[str, int] = field(default_factory=dict)
    config: dict[str, Any] = field(default_factory=dict)


def build_skeleton(
    *,
    scale: str = "full",
    config: dict[str, Any] | None = None,
    config_path: str | None = None,
) -> Skeleton:
    cfg = config or load_persona_config(config_path)
    vol = volumes_for(cfg, scale)
    personas = [p["id"] for p in cfg.get("personas") or []]
    niches = niche_names(cfg)
    engage = cfg.get("engage") or {}
    tag_rate = float(engage.get("product_tag_rate", 0.5))

    users: list[dict[str, Any]] = []
    for i in range(vol["users"]):
        persona = personas[i % len(personas)]
        is_seller = i < vol["sellers"]
        users.append(
            {
                "user_id": ids.user_uuid(i + 1),
                "email": f"sim.bot{i+1:03d}@2hands.local",
                "persona": persona,
                "is_seller": is_seller,
                "index": i + 1,
            }
        )

    shops: list[dict[str, Any]] = []
    for i in range(vol["shops"]):
        seller = users[i]
        shops.append(
            {
                "shop_id": ids.shop_uuid(i + 1),
                "seller_id": seller["user_id"],
                "shop_name": f"Sim Closet {i+1}",
                "status": "ACTIVE",
            }
        )

    products: list[dict[str, Any]] = []
    for i in range(vol["products"]):
        shop = shops[i % len(shops)]
        niche = niches[i % len(niches)]
        niche_cfg = cfg["niches"][niche]
        category_id = niche_cfg["category_ids"][i % len(niche_cfg["category_ids"])]
        products.append(
            {
                "product_id": ids.product_uuid(i + 1),
                "shop_id": shop["shop_id"],
                "seller_id": shop["seller_id"],
                "category_id": category_id,
                "niche": niche,
                "title": f"{niche} item {i+1}",
                "status": "ACTIVE",
                "stock_quantity": 1,
            }
        )

    posts: list[dict[str, Any]] = []
    authors = [u for u in users if u["is_seller"]] or users[: vol["sellers"]]
    # Prefer unique product per tagged post so stock=1 purchases can cover many buyers
    products_by_niche: dict[str, list[dict[str, Any]]] = {}
    for product in products:
        products_by_niche.setdefault(product["niche"], []).append(product)
    niche_cursors: dict[str, int] = {n: 0 for n in niches}
    global_product_cursor = 0

    for i in range(vol["posts"]):
        author = authors[i % len(authors)]
        niche = niches[i % len(niches)]
        niche_cfg = cfg["niches"][niche]
        hashtags = list(niche_cfg["hashtags"][:3])
        tagged = (i / max(vol["posts"], 1)) < tag_rate
        product_tags: list[dict[str, Any]] = []
        if tagged:
            niche_list = products_by_niche.get(niche) or products
            idx = niche_cursors.get(niche, 0)
            if idx < len(niche_list):
                product = niche_list[idx]
                niche_cursors[niche] = idx + 1
            else:
                product = products[global_product_cursor % len(products)]
                global_product_cursor += 1
            product_tags.append(
                {
                    "productId": product["product_id"],
                    "categoryId": product["category_id"],
                    "shopId": product["shop_id"],
                    "name": product["title"],
                    "available": True,
                }
            )
        posts.append(
            {
                "post_id": ids.post_id(i + 1),
                "author_id": author["user_id"],
                "niche": niche,
                "hashtags": hashtags,
                "product_tags": product_tags,
                "status": "ACTIVE",
                "visibility": "PUBLIC",
                "like_count": 0,
                "reply_count": 0,
            }
        )

    return Skeleton(
        scale=scale,
        users=users,
        shops=shops,
        products=products,
        posts=posts,
        volumes=vol,
        config=cfg,
    )


def summarize_skeleton(skeleton: Skeleton) -> dict[str, Any]:
    tagged = sum(1 for p in skeleton.posts if p.get("product_tags"))
    return {
        "scale": skeleton.scale,
        "users": len(skeleton.users),
        "sellers": sum(1 for u in skeleton.users if u["is_seller"]),
        "shops": len(skeleton.shops),
        "products": len(skeleton.products),
        "posts": len(skeleton.posts),
        "posts_with_product_tags": tagged,
        "product_tag_rate": tagged / len(skeleton.posts) if skeleton.posts else 0.0,
        "sessions_per_user": skeleton.volumes.get("sessions_per_user"),
        "feed_size": skeleton.volumes.get("feed_size"),
        "expected_raw_impressions": (
            skeleton.volumes.get("users", 0)
            * skeleton.volumes.get("sessions_per_user", 0)
            * skeleton.volumes.get("feed_size", 0)
        ),
    }


def post_affinity(skeleton: Skeleton, persona_id: str, post: dict[str, Any]) -> float:
    return affinity(skeleton.config, persona_id, str(post.get("niche") or "minimal"))

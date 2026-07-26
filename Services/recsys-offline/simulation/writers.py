"""Optional direct DB writers for simulation (dev-only, guarded by RECSYS_SIM_ALLOW)."""

from __future__ import annotations

import logging
from datetime import datetime, timezone
from typing import Any

from simulation.engine import SimulationResult
from simulation.skeleton import Skeleton
from simulation.timestamps import ensure_utc, spread_timestamps

logger = logging.getLogger(__name__)

# bcrypt hash for "password" — bots are not for interactive login in MVP sim
_PASSWORD_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"


def _dsn(url: str) -> str:
    return url.replace("postgresql+psycopg://", "postgresql://").replace(
        "postgresql+psycopg2://", "postgresql://"
    )


def write_auth_users(auth_url: str, skeleton: Skeleton) -> int:
    import psycopg

    sql = """
        INSERT INTO users (
            id, email, email_normalized, password_hash, status,
            email_verified, phone_verified, created_at, updated_at
        ) VALUES (
            %(id)s::uuid, %(email)s, %(email)s, %(password_hash)s, 'ACTIVE',
            TRUE, FALSE, NOW(), NOW()
        )
        ON CONFLICT (id) DO UPDATE SET
            email = EXCLUDED.email,
            email_normalized = EXCLUDED.email_normalized,
            updated_at = NOW()
    """
    rows = [
        {
            "id": u["user_id"],
            "email": u["email"],
            "password_hash": _PASSWORD_HASH,
        }
        for u in skeleton.users
    ]
    with psycopg.connect(_dsn(auth_url)) as conn:
        with conn.cursor() as cur:
            for row in rows:
                cur.execute(sql, row)
        conn.commit()
    return len(rows)


def write_commerce_skeleton(commerce_url: str, skeleton: Skeleton) -> dict[str, int]:
    import psycopg

    with psycopg.connect(_dsn(commerce_url)) as conn:
        with conn.cursor() as cur:
            for shop in skeleton.shops:
                cur.execute(
                    """
                    INSERT INTO seller_shops (
                        id, seller_id, shop_name, description, status,
                        rating_avg, rating_count, created_at, updated_at
                    ) VALUES (
                        %(shop_id)s::uuid, %(seller_id)s::uuid, %(shop_name)s, 'sim', 'ACTIVE',
                        0, 0, NOW(), NOW()
                    )
                    ON CONFLICT (id) DO UPDATE SET shop_name = EXCLUDED.shop_name, updated_at = NOW()
                    """,
                    shop,
                )
            for product in skeleton.products:
                cur.execute(
                    """
                    INSERT INTO products (
                        id, seller_id, shop_id, product_type, category_id, condition,
                        title, description, weight_gram, status, created_at, updated_at
                    ) VALUES (
                        %(product_id)s::uuid, %(seller_id)s::uuid, %(shop_id)s::uuid,
                        'PHYSICAL', %(category_id)s::uuid, 'GOOD',
                        %(title)s::varchar, %(description)s::text, 500, 'ACTIVE', NOW(), NOW()
                    )
                    ON CONFLICT (id) DO UPDATE SET
                        title = EXCLUDED.title,
                        description = EXCLUDED.description,
                        updated_at = NOW()
                    """,
                    {
                        **product,
                        "description": product.get("title") or "sim product",
                    },
                )
                cur.execute(
                    """
                    INSERT INTO product_inventories (
                        product_id, stock_quantity, low_stock_threshold, reserved_quantity,
                        created_at, updated_at
                    ) VALUES (
                        %(product_id)s::uuid, 1, 0, 0, NOW(), NOW()
                    )
                    ON CONFLICT (product_id) DO UPDATE SET
                        stock_quantity = EXCLUDED.stock_quantity,
                        updated_at = NOW()
                    """,
                    product,
                )
                cur.execute(
                    """
                    INSERT INTO product_prices (
                        id, product_id, price, sale_price, start_at, end_at, created_at
                    ) VALUES (
                        gen_random_uuid(), %(product_id)s::uuid, 199000, NULL, NOW(), NULL, NOW()
                    )
                    """,
                    product,
                )
        conn.commit()
    return {
        "shops": len(skeleton.shops),
        "products": len(skeleton.products),
    }


def resolve_post_clock(
    skeleton: Skeleton,
    *,
    end_at: datetime | None = None,
    sim_days: int | None = None,
) -> tuple[datetime, int]:
    end = ensure_utc(end_at or datetime.now(timezone.utc))
    days = int(
        sim_days
        if sim_days is not None
        else skeleton.volumes.get("sim_days", 21)
    )
    return end, max(days, 1)


def build_social_post_documents(
    skeleton: Skeleton,
    *,
    end_at: datetime | None = None,
    sim_days: int | None = None,
) -> list[dict[str, Any]]:
    """Build Mongo post docs with BSON-Date-ready UTC datetimes on the sim clock."""
    end, days = resolve_post_clock(skeleton, end_at=end_at, sim_days=sim_days)
    stamps = spread_timestamps(len(skeleton.posts), end, days)
    docs: list[dict[str, Any]] = []
    for post, ts in zip(skeleton.posts, stamps, strict=True):
        docs.append(
            {
                "_id": post["post_id"],
                "author_id": post["author_id"],
                "caption": f"Sim {post['niche']} look",
                "media": [],
                "productTags": post.get("product_tags") or [],
                "status": "ACTIVE",
                "visibility": "PUBLIC",
                "like_count": 0,
                "reply_count": 0,
                "hashtags": post.get("hashtags") or [],
                "allow_comments": True,
                "created_at": ts,
                "updated_at": ts,
            }
        )
    return docs


def build_social_user_projection_documents(
    skeleton: Skeleton,
) -> list[dict[str, Any]]:
    """Build Social Mongo user_projections for all sim users (needed for feed author cards)."""
    docs: list[dict[str, Any]] = []
    for user in skeleton.users:
        user_id = str(user["user_id"])
        docs.append(
            {
                "_id": user_id,
                "user_id": user_id,
                "status": "ACTIVE",
                "display_name": f"Sim Bot {user.get('index', '')}".strip(),
                "avatar_url": None,
                "cover_url": None,
                "is_private": False,
            }
        )
    return docs


def write_social_user_projections_mongo(
    mongo_url: str,
    mongo_db: str,
    skeleton: Skeleton,
) -> int:
    from pymongo import MongoClient

    docs = build_social_user_projection_documents(skeleton)
    client = MongoClient(mongo_url)
    try:
        col = client[mongo_db]["user_projections"]
        for doc in docs:
            col.replace_one({"user_id": doc["user_id"]}, doc, upsert=True)
        return len(docs)
    finally:
        client.close()


def write_social_posts_mongo(
    mongo_url: str,
    mongo_db: str,
    skeleton: Skeleton,
    *,
    end_at: datetime | None = None,
    sim_days: int | None = None,
) -> int:
    from pymongo import MongoClient

    docs = build_social_post_documents(
        skeleton, end_at=end_at, sim_days=sim_days
    )
    client = MongoClient(mongo_url)
    try:
        col = client[mongo_db]["posts"]
        for doc in docs:
            col.replace_one({"_id": doc["_id"]}, doc, upsert=True)
        return len(docs)
    finally:
        client.close()


def write_simulation_interactions(
    social_postgres_url: str,
    commerce_url: str | None,
    result: SimulationResult,
) -> dict[str, int]:
    """Persist impressions/engagements; optionally orders into Commerce."""
    import psycopg

    counts = {
        "impressions": 0,
        "likes": 0,
        "saves": 0,
        "follows": 0,
        "searches": 0,
        "orders": 0,
    }
    with psycopg.connect(_dsn(social_postgres_url)) as conn:
        with conn.cursor() as cur:
            for row in result.impressions:
                cur.execute(
                    """
                    INSERT INTO post_impression_log (
                        id, user_id, post_id, shown_at, rank_position,
                        model_version, model_name, request_id
                    ) VALUES (
                        gen_random_uuid(), %(user_id)s::uuid, %(post_id)s,
                        %(shown_at)s::timestamptz, %(rank_position)s,
                        NULL, NULL, %(request_id)s
                    )
                    """,
                    row,
                )
                cur.execute(
                    """
                    INSERT INTO user_seen_posts (user_id, post_id, seen_at)
                    VALUES (%(user_id)s::uuid, %(post_id)s, %(shown_at)s::timestamptz)
                    ON CONFLICT (user_id, post_id) DO NOTHING
                    """,
                    row,
                )
                counts["impressions"] += 1
            for row in result.likes:
                cur.execute(
                    """
                    INSERT INTO post_likes (post_id, user_id, created_at)
                    VALUES (%(post_id)s, %(user_id)s::uuid, %(created_at)s::timestamptz)
                    ON CONFLICT (post_id, user_id) DO NOTHING
                    """,
                    row,
                )
                counts["likes"] += 1
            for row in result.saves:
                cur.execute(
                    """
                    INSERT INTO post_saves (post_id, user_id, created_at)
                    VALUES (%(post_id)s, %(user_id)s::uuid, %(created_at)s::timestamptz)
                    ON CONFLICT (post_id, user_id) DO NOTHING
                    """,
                    row,
                )
                counts["saves"] += 1
            for row in result.follows:
                cur.execute(
                    """
                    INSERT INTO follows (follower_id, followee_id, status, created_at)
                    VALUES (
                        %(follower_id)s::uuid, %(followee_id)s::uuid,
                        'ACCEPTED', %(created_at)s::timestamptz
                    )
                    ON CONFLICT (follower_id, followee_id) DO NOTHING
                    """,
                    row,
                )
                counts["follows"] += 1
            for row in result.searches:
                cur.execute(
                    """
                    INSERT INTO search_history (id, user_id, keyword, created_at, updated_at)
                    VALUES (
                        %(id)s::uuid, %(user_id)s::uuid, %(keyword)s,
                        %(created_at)s::timestamptz, %(created_at)s::timestamptz
                    )
                    ON CONFLICT (id) DO NOTHING
                    """,
                    row,
                )
                counts["searches"] += 1
        conn.commit()

    if commerce_url:
        with psycopg.connect(_dsn(commerce_url)) as conn:
            with conn.cursor() as cur:
                for order in result.orders:
                    cur.execute(
                        """
                        INSERT INTO orders (
                            id, buyer_id, total_amount, final_amount, payment_method,
                            status, payment_status, created_at, updated_at, completed_at
                        ) VALUES (
                            %(order_id)s::uuid, %(buyer_id)s::uuid, 199000, 199000, 'COD',
                            'COMPLETED', 'PAID', %(created_at)s::timestamptz,
                            %(completed_at)s::timestamptz, %(completed_at)s::timestamptz
                        )
                        ON CONFLICT (id) DO NOTHING
                        """,
                        order,
                    )
                    cur.execute(
                        """
                        INSERT INTO order_items (
                            id, order_id, product_id, seller_id, quantity,
                            unit_price_snapshot, final_price, product_name_snapshot,
                            shipping_fee_allocated, shop_name_snapshot, status,
                            created_at, updated_at, completed_at
                        )
                        SELECT
                            gen_random_uuid(), %(order_id)s::uuid, %(product_id)s::uuid,
                            p.seller_id, 1, 199000, 199000, p.title, 0, s.shop_name,
                            'COMPLETED', %(created_at)s::timestamptz,
                            %(completed_at)s::timestamptz, %(completed_at)s::timestamptz
                        FROM products p
                        JOIN seller_shops s ON s.id = p.shop_id
                        WHERE p.id = %(product_id)s::uuid
                        """,
                        order,
                    )
                    cur.execute(
                        """
                        UPDATE product_inventories
                        SET stock_quantity = 0, updated_at = NOW()
                        WHERE product_id = %(product_id)s::uuid
                        """,
                        order,
                    )
                    counts["orders"] += 1
            conn.commit()
    return counts

"""Read-only extractors for Social Mongo + Postgres."""

from __future__ import annotations

import logging
from typing import Any

from app.config import Settings

logger = logging.getLogger(__name__)


def extract_mongo_collection(settings: Settings, collection_name: str) -> list[dict[str, Any]]:
    from pymongo import MongoClient

    client = MongoClient(settings.social_mongo_url)
    try:
        db = client[settings.social_mongo_db]
        docs = list(db[collection_name].find({}))
        for doc in docs:
            if "_id" in doc:
                doc["_id"] = str(doc["_id"])
        return docs
    finally:
        client.close()


def extract_postgres_table(
    settings: Settings, table: str, columns: list[str]
) -> list[dict[str, Any]]:
    import psycopg
    from psycopg.rows import dict_row

    url = settings.social_postgres_url
    if url is None:
        raise ValueError("SOCIAL_POSTGRES_URL is required")

    # Accept SQLAlchemy-style prefix
    dsn = url.replace("postgresql+psycopg://", "postgresql://").replace(
        "postgresql+psycopg2://", "postgresql://"
    )

    col_sql = ", ".join(columns)
    sql = f"SELECT {col_sql} FROM {table}"  # noqa: S608 — table/columns are fixed callers

    try:
        with psycopg.connect(dsn, row_factory=dict_row) as conn:
            with conn.cursor() as cur:
                cur.execute(sql)
                return list(cur.fetchall())
    except psycopg.errors.UndefinedTable:
        logger.warning("Table %s does not exist yet; returning empty extract", table)
        return []
    except Exception:
        # Graceful for missing impression table / permissions in early envs
        if table == "post_impression_log":
            logger.warning(
                "Could not read post_impression_log; returning empty extract",
                exc_info=True,
            )
            return []
        raise


def extract_all(settings: Settings) -> dict[str, list[dict[str, Any]]]:
    settings.require_db_urls()

    posts = extract_mongo_collection(settings, "posts")
    comments = extract_mongo_collection(settings, "comments")

    likes = extract_postgres_table(
        settings, "post_likes", ["user_id", "post_id", "created_at"]
    )
    saves = extract_postgres_table(
        settings, "post_saves", ["user_id", "post_id", "created_at"]
    )
    follows = extract_postgres_table(
        settings,
        "follows",
        ["follower_id", "followee_id", "status", "created_at"],
    )
    search = extract_postgres_table(
        settings, "search_history", ["id", "user_id", "keyword", "created_at"]
    )
    impressions = extract_postgres_table(
        settings,
        "post_impression_log",
        [
            "id",
            "user_id",
            "post_id",
            "shown_at",
            "rank_position",
            "model_version",
            "request_id",
        ],
    )

    if not impressions:
        logger.warning(
            "post_impression_log is empty or unavailable; label construction will be incomplete"
        )

    return {
        "posts": posts,
        "comments": comments,
        "post_likes": likes,
        "post_saves": saves,
        "follows": follows,
        "search_history": search,
        "post_impression_log": impressions,
    }

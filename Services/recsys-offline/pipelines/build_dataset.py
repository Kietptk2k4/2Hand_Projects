"""Build labeled dataset.parquet from cleaned extracts (no negative sampling)."""

from __future__ import annotations

import csv
import json
import logging
from collections import defaultdict
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from app.config import Settings, get_settings
from pipelines.features import (
    cross_domain_score,
    engagement_raw,
    filter_before,
    min_max_normalize,
    parse_product_tag_ids,
    recency_score,
    _parse_hashtags,
)
from pipelines.labels import assign_label

logger = logging.getLogger(__name__)


def _read_csv(path: Path) -> list[dict[str, Any]]:
    if not path.exists():
        return []
    with path.open("r", encoding="utf-8-sig", newline="") as handle:
        return list(csv.DictReader(handle))


def _load_cleaned(input_dir: Path) -> dict[str, list[dict[str, Any]]]:
    names = [
        "posts",
        "comments",
        "post_likes",
        "post_saves",
        "follows",
        "search_history",
        "post_impression_log",
        "user_purchase_profile",
    ]
    data = {name: _read_csv(input_dir / f"{name}.csv") for name in names}
    return data


def _index_by_user(rows: list[dict[str, Any]], key: str = "user_id") -> dict[str, list[dict[str, Any]]]:
    out: dict[str, list[dict[str, Any]]] = defaultdict(list)
    for row in rows:
        uid = row.get(key) or row.get("author_id")
        if uid is not None:
            out[str(uid)].append(row)
    return out


def build_rows(raw: dict[str, list[dict[str, Any]]]) -> tuple[list[dict[str, Any]], dict[str, Any]]:
    posts = {str(p.get("post_id") or p.get("_id") or p.get("id")): p for p in raw.get("posts", [])}
    impressions = raw.get("post_impression_log", [])
    likes = raw.get("post_likes", [])
    saves = raw.get("post_saves", [])
    comments = raw.get("comments", [])
    follows = raw.get("follows", [])
    searches = raw.get("search_history", [])
    profiles = {
        str(r.get("user_id")): r for r in raw.get("user_purchase_profile", []) if r.get("user_id")
    }

    likes_by_user = _index_by_user(likes)
    saves_by_user = _index_by_user(saves)
    comments_by_user = _index_by_user(comments, key="author_id")
    # also index comments by user_id if present
    for row in comments:
        if row.get("user_id"):
            comments_by_user[str(row["user_id"])].append(row)
    follows_by_follower = _index_by_user(follows, key="follower_id")
    search_by_user = _index_by_user(searches)

    warnings: list[str] = []
    if not impressions:
        warnings.append("no_impressions")

    # First pass: compute raw features grouped by request_id for min-max
    grouped: dict[str, list[dict[str, Any]]] = defaultdict(list)
    dropped_missing_post = 0

    for imp in impressions:
        user_id = str(imp.get("user_id"))
        post_id = str(imp.get("post_id"))
        shown_at = imp.get("shown_at") or imp.get("created_at")
        post = posts.get(post_id)
        if post is None:
            dropped_missing_post += 1
            continue

        label = assign_label(
            user_id=user_id,
            post_id=post_id,
            shown_at=shown_at,
            likes=likes_by_user.get(user_id, []),
            saves=saves_by_user.get(user_id, []),
            comments=comments_by_user.get(user_id, []),
        )

        user_likes = filter_before(likes_by_user.get(user_id, []), shown_at)
        user_saves = filter_before(saves_by_user.get(user_id, []), shown_at)
        user_searches = filter_before(search_by_user.get(user_id, []), shown_at)
        user_follows = filter_before(follows_by_follower.get(user_id, []), shown_at)

        liked_post_ids = [str(r["post_id"]) for r in user_likes if r.get("post_id")][:50]
        saved_post_ids = [str(r["post_id"]) for r in user_saves if r.get("post_id")][:50]

        liked_tags: set[str] = set()
        liked_author_counts: dict[str, int] = defaultdict(int)
        for pid in liked_post_ids:
            lp = posts.get(pid)
            if not lp:
                continue
            liked_tags.update(_parse_hashtags(lp.get("hashtags")))
            liked_author_counts[str(lp.get("author_id"))] += 1

        saved_tags: set[str] = set()
        saved_author_counts: dict[str, int] = defaultdict(int)
        for pid in saved_post_ids:
            sp = posts.get(pid)
            if not sp:
                continue
            saved_tags.update(_parse_hashtags(sp.get("hashtags")))
            saved_author_counts[str(sp.get("author_id"))] += 1

        search_terms = {
            str(r.get("keyword", "")).strip().lower()
            for r in user_searches[-20:]
            if r.get("keyword")
        }

        followee_ids = {
            str(r.get("followee_id"))
            for r in user_follows
            if str(r.get("status", "ACCEPTED")).upper() == "ACCEPTED" and r.get("followee_id")
        }

        author_id = str(post.get("author_id"))
        cand_tags = _parse_hashtags(post.get("hashtags"))
        hashtag_raw = 0.0
        for tag in cand_tags:
            if tag in search_terms:
                hashtag_raw += 1.0
            if tag in saved_tags:
                hashtag_raw += 0.8
            if tag in liked_tags:
                hashtag_raw += 0.4

        follows_author = author_id in followee_ids
        author_aff_raw = (
            (1.0 if follows_author else 0.0)
            + liked_author_counts.get(author_id, 0) * 0.5
            + saved_author_counts.get(author_id, 0) * 0.6
        )

        if follows_author:
            mutual = 1.0
        else:
            # approximate Jaccard using only follower->followee edges we have for author
            author_followees = {
                str(r.get("followee_id"))
                for r in filter_before(follows_by_follower.get(author_id, []), shown_at)
                if str(r.get("status", "ACCEPTED")).upper() == "ACCEPTED" and r.get("followee_id")
            }
            inter = followee_ids.intersection(author_followees)
            union = followee_ids.union(author_followees)
            mutual = (len(inter) / len(union)) if union else 0.0

        profile = profiles.get(user_id, {})
        user_cats = set()
        user_shops = set()
        for field, bucket in (("category_ids", user_cats), ("shop_ids", user_shops)):
            raw_val = profile.get(field)
            if isinstance(raw_val, str):
                try:
                    raw_val = json.loads(raw_val)
                except json.JSONDecodeError:
                    raw_val = [x for x in raw_val.split("|") if x]
            if isinstance(raw_val, list):
                bucket.update(str(x) for x in raw_val if x)

        post_cats, post_shops = parse_product_tag_ids(post.get("product_tags") or post.get("productTags"))
        cross = cross_domain_score(user_cats, user_shops, post_cats, post_shops)

        request_id = str(imp.get("request_id") or f"{user_id}:{shown_at}")
        sample = {
            "user_id": user_id,
            "post_id": post_id,
            "shown_at": shown_at,
            "request_id": request_id,
            "model_version": imp.get("model_version"),
            "rank_position": imp.get("rank_position"),
            "recency_score": recency_score(post.get("created_at"), shown_at),
            "engagement_raw": engagement_raw(post.get("like_count"), post.get("reply_count")),
            "hashtag_raw": hashtag_raw,
            "affinity_raw": author_aff_raw,
            "mutual_follow_score": mutual,
            "cross_domain_product_score": cross,
            "label": label,
        }
        grouped[request_id].append(sample)

    rows: list[dict[str, Any]] = []
    for request_id, group in grouped.items():
        eng = min_max_normalize([g["engagement_raw"] for g in group])
        hash_n = min_max_normalize([g["hashtag_raw"] for g in group])
        aff_n = min_max_normalize([g["affinity_raw"] for g in group])
        for i, g in enumerate(group):
            rows.append(
                {
                    "user_id": g["user_id"],
                    "post_id": g["post_id"],
                    "shown_at": g["shown_at"],
                    "request_id": g["request_id"],
                    "model_version": g["model_version"],
                    "rank_position": g["rank_position"],
                    "recency_score": g["recency_score"],
                    "engagement_score": eng[i],
                    "hashtag_match_score": hash_n[i],
                    "author_affinity_score": aff_n[i],
                    "mutual_follow_score": g["mutual_follow_score"],
                    "cross_domain_product_score": g["cross_domain_product_score"],
                    "label": g["label"],
                }
            )

    positives = sum(1 for r in rows if int(r["label"]) == 1)
    summary = {
        "rows": len(rows),
        "positives": positives,
        "positive_rate": (positives / len(rows)) if rows else 0.0,
        "dropped_missing_post": dropped_missing_post,
        "warnings": warnings,
        "finished_at": datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
    }
    return rows, summary


def _write_parquet(path: Path, rows: list[dict[str, Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    try:
        import pyarrow as pa
        import pyarrow.parquet as pq
    except ImportError as exc:
        raise RuntimeError(
            "pyarrow is required to export dataset.parquet. Install with: pip install pyarrow"
        ) from exc

    if not rows:
        table = pa.table(
            {
                "user_id": pa.array([], type=pa.string()),
                "post_id": pa.array([], type=pa.string()),
                "label": pa.array([], type=pa.int8()),
            }
        )
        pq.write_table(table, path)
        return

    table = pa.Table.from_pylist(rows)
    pq.write_table(table, path)


def run_build_dataset(settings: Settings | None = None, require_rows: bool = False) -> dict[str, Any]:
    settings = settings or get_settings()
    input_dir = Path(settings.recsys_dataset_output_dir)
    if not input_dir.exists():
        raise ValueError(f"Cleaned dataset directory not found: {input_dir}")

    required = ["posts.csv", "post_impression_log.csv"]
    missing = [name for name in required if not (input_dir / name).exists()]
    if missing:
        raise ValueError(f"Missing cleaned inputs: {', '.join(missing)}")

    raw = _load_cleaned(input_dir)
    rows, summary = build_rows(raw)
    if require_rows and not rows:
        raise ValueError("Build dataset produced zero rows")

    out_path = input_dir / "dataset.parquet"
    _write_parquet(out_path, rows)
    summary["output_path"] = str(out_path.resolve())
    summary_path = input_dir / "dataset_meta.json"
    summary_path.write_text(json.dumps(summary, indent=2, ensure_ascii=False), encoding="utf-8")
    logger.info("Build dataset finished: %s rows -> %s", summary["rows"], out_path)
    return summary

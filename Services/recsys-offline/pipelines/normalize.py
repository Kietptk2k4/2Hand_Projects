"""Pure clean/normalize helpers — no DB I/O."""

from __future__ import annotations

import re
from collections import Counter
from datetime import datetime, timezone
from typing import Any
from uuid import UUID

UUID_RE = re.compile(
    r"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
)


def normalize_hashtag(tag: Any) -> str | None:
    if tag is None:
        return None
    value = str(tag).strip()
    if not value:
        return None
    if value.startswith("#"):
        value = value[1:].strip()
    value = value.lower()
    return value or None


def normalize_hashtags(tags: Any) -> list[str]:
    if not tags:
        return []
    if not isinstance(tags, (list, tuple)):
        tags = [tags]
    seen: set[str] = set()
    out: list[str] = []
    for tag in tags:
        normalized = normalize_hashtag(tag)
        if normalized and normalized not in seen:
            seen.add(normalized)
            out.append(normalized)
    return out


def to_utc_iso(value: Any) -> str | None:
    if value is None:
        return None
    if isinstance(value, datetime):
        dt = value
        if dt.tzinfo is None:
            dt = dt.replace(tzinfo=timezone.utc)
        else:
            dt = dt.astimezone(timezone.utc)
        return dt.isoformat().replace("+00:00", "Z")
    if isinstance(value, str):
        text = value.strip()
        if not text:
            return None
        # Already ISO-like — normalize trailing Z
        if text.endswith("Z"):
            return text
        try:
            parsed = datetime.fromisoformat(text.replace("Z", "+00:00"))
            return to_utc_iso(parsed)
        except ValueError:
            return text
    return str(value)


def is_valid_uuid(value: Any) -> bool:
    if value is None:
        return False
    text = str(value).strip()
    if not UUID_RE.match(text):
        return False
    try:
        UUID(text)
        return True
    except ValueError:
        return False


def clean_posts(rows: list[dict[str, Any]]) -> tuple[list[dict[str, Any]], Counter]:
    drops: Counter = Counter()
    kept: list[dict[str, Any]] = []
    seen_ids: set[str] = set()

    for row in rows:
        post_id = row.get("_id") or row.get("id") or row.get("post_id")
        if post_id is None:
            drops["null_post_id"] += 1
            continue
        post_id = str(post_id)

        author_id = row.get("author_id")
        if author_id is None or str(author_id).strip() == "":
            drops["null_author"] += 1
            continue
        if not is_valid_uuid(author_id):
            drops["invalid_author_uuid"] += 1
            continue

        created_at = to_utc_iso(row.get("created_at"))
        if created_at is None:
            drops["null_created_at"] += 1
            continue

        status = str(row.get("status") or "").upper()
        if status in {"DRAFT", "DELETED"}:
            drops[f"status_{status.lower()}"] += 1
            continue

        if post_id in seen_ids:
            drops["duplicate_post_id"] += 1
            continue
        seen_ids.add(post_id)

        kept.append(
            {
                "post_id": post_id,
                "author_id": str(author_id),
                "status": status or "ACTIVE",
                "visibility": str(row.get("visibility") or "PUBLIC").upper(),
                "hashtags": normalize_hashtags(row.get("hashtags")),
                "like_count": int(row.get("like_count") or 0),
                "reply_count": int(row.get("reply_count") or row.get("comment_count") or 0),
                "created_at": created_at,
                "updated_at": to_utc_iso(row.get("updated_at")),
            }
        )

    return kept, drops


def clean_comments(rows: list[dict[str, Any]]) -> tuple[list[dict[str, Any]], Counter]:
    drops: Counter = Counter()
    kept: list[dict[str, Any]] = []
    seen_ids: set[str] = set()

    for row in rows:
        comment_id = row.get("_id") or row.get("id") or row.get("comment_id")
        post_id = row.get("post_id")
        author_id = row.get("author_id")
        if comment_id is None:
            drops["null_comment_id"] += 1
            continue
        if post_id is None:
            drops["null_post_id"] += 1
            continue
        if author_id is None or not is_valid_uuid(author_id):
            drops["invalid_author"] += 1
            continue
        created_at = to_utc_iso(row.get("created_at"))
        if created_at is None:
            drops["null_created_at"] += 1
            continue
        status = str(row.get("status") or "ACTIVE").upper()
        if status == "DELETED":
            drops["status_deleted"] += 1
            continue
        cid = str(comment_id)
        if cid in seen_ids:
            drops["duplicate_comment_id"] += 1
            continue
        seen_ids.add(cid)
        kept.append(
            {
                "comment_id": cid,
                "post_id": str(post_id),
                "author_id": str(author_id),
                "status": status,
                "created_at": created_at,
            }
        )
    return kept, drops


def clean_user_post_events(
    rows: list[dict[str, Any]], *, entity: str
) -> tuple[list[dict[str, Any]], Counter]:
    drops: Counter = Counter()
    kept: list[dict[str, Any]] = []
    seen: set[tuple[str, str]] = set()

    for row in rows:
        user_id = row.get("user_id")
        post_id = row.get("post_id")
        if user_id is None or not is_valid_uuid(user_id):
            drops["invalid_user_id"] += 1
            continue
        if post_id is None or str(post_id).strip() == "":
            drops["null_post_id"] += 1
            continue
        created_at = to_utc_iso(row.get("created_at") or row.get("shown_at"))
        if created_at is None:
            drops["null_timestamp"] += 1
            continue
        key = (str(user_id), str(post_id))
        if key in seen:
            drops[f"duplicate_{entity}"] += 1
            continue
        seen.add(key)
        out = {
            "user_id": str(user_id),
            "post_id": str(post_id),
            "created_at": created_at,
        }
        if "shown_at" in row or entity == "impressions":
            out["shown_at"] = to_utc_iso(row.get("shown_at") or row.get("created_at"))
            out["rank_position"] = row.get("rank_position")
            out["model_version"] = row.get("model_version")
            out["request_id"] = row.get("request_id")
        kept.append(out)
    return kept, drops


def clean_follows(rows: list[dict[str, Any]]) -> tuple[list[dict[str, Any]], Counter]:
    drops: Counter = Counter()
    kept: list[dict[str, Any]] = []
    seen: set[tuple[str, str]] = set()

    for row in rows:
        follower = row.get("follower_id")
        followee = row.get("followee_id")
        if follower is None or not is_valid_uuid(follower):
            drops["invalid_follower_id"] += 1
            continue
        if followee is None or not is_valid_uuid(followee):
            drops["invalid_followee_id"] += 1
            continue
        if str(follower) == str(followee):
            drops["self_follow"] += 1
            continue
        created_at = to_utc_iso(row.get("created_at"))
        if created_at is None:
            drops["null_created_at"] += 1
            continue
        key = (str(follower), str(followee))
        if key in seen:
            drops["duplicate_follow"] += 1
            continue
        seen.add(key)
        status = str(row.get("status") or "ACCEPTED").upper()
        kept.append(
            {
                "follower_id": str(follower),
                "followee_id": str(followee),
                "status": status,
                "created_at": created_at,
            }
        )
    return kept, drops


def clean_search_history(rows: list[dict[str, Any]]) -> tuple[list[dict[str, Any]], Counter]:
    drops: Counter = Counter()
    kept: list[dict[str, Any]] = []
    seen: set[str] = set()

    for row in rows:
        row_id = row.get("id")
        user_id = row.get("user_id")
        keyword = row.get("keyword")
        if user_id is None or not is_valid_uuid(user_id):
            drops["invalid_user_id"] += 1
            continue
        if keyword is None or str(keyword).strip() == "":
            drops["empty_keyword"] += 1
            continue
        created_at = to_utc_iso(row.get("created_at"))
        if created_at is None:
            drops["null_created_at"] += 1
            continue
        rid = str(row_id) if row_id is not None else f"{user_id}:{keyword}:{created_at}"
        if rid in seen:
            drops["duplicate_search"] += 1
            continue
        seen.add(rid)
        kept.append(
            {
                "id": rid,
                "user_id": str(user_id),
                "keyword": str(keyword).strip().lower(),
                "created_at": created_at,
            }
        )
    return kept, drops

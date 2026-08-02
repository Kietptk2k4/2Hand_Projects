"""Upsert CF/AR/export file rows into Commerce Postgres (no event pollution)."""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any, Iterable

import psycopg


ALLOWED_TABLES = {
    "entity_cooccur",
    "user_social_interest_export",
    "social_tag_category_ar",
}


def read_jsonl(path: Path) -> list[dict[str, Any]]:
    if not path.exists():
        return []
    rows = []
    with path.open("r", encoding="utf-8") as handle:
        for line in handle:
            line = line.strip()
            if line:
                rows.append(json.loads(line))
    return rows


def load_home_artifacts(
    commerce_dsn: str,
    artifact_dir: Path,
    *,
    tables: Iterable[str] | None = None,
) -> dict[str, int]:
    selected = set(tables or ALLOWED_TABLES)
    unknown = selected - ALLOWED_TABLES
    if unknown:
        raise ValueError(f"Refusing non-artifact tables: {sorted(unknown)}")

    counts: dict[str, int] = {}
    with psycopg.connect(commerce_dsn) as conn:
        with conn.cursor() as cur:
            if "entity_cooccur" in selected:
                rows = read_jsonl(artifact_dir / "entity_cooccur.jsonl")
                for row in rows:
                    cur.execute(
                        """
                        INSERT INTO entity_cooccur (
                            entity_type, entity_id, neighbor_type, neighbor_id, score, updated_at
                        ) VALUES (
                            %(entity_type)s, %(entity_id)s::uuid, %(neighbor_type)s,
                            %(neighbor_id)s::uuid, %(score)s, NOW()
                        )
                        ON CONFLICT (entity_type, entity_id, neighbor_type, neighbor_id)
                        DO UPDATE SET score = EXCLUDED.score, updated_at = NOW()
                        """,
                        row,
                    )
                counts["entity_cooccur"] = len(rows)

            if "user_social_interest_export" in selected:
                rows = read_jsonl(artifact_dir / "user_social_interest_export.jsonl")
                for row in rows:
                    cur.execute(
                        """
                        INSERT INTO user_social_interest_export (
                            user_id, tag_type, tag, score, window_days, computed_at, as_of
                        ) VALUES (
                            %(user_id)s::uuid, %(tag_type)s, %(tag)s, %(score)s,
                            %(window_days)s, %(computed_at)s::timestamptz, %(as_of)s::timestamptz
                        )
                        ON CONFLICT (user_id, tag_type, tag)
                        DO UPDATE SET score = EXCLUDED.score,
                            window_days = EXCLUDED.window_days,
                            computed_at = EXCLUDED.computed_at,
                            as_of = EXCLUDED.as_of
                        """,
                        row,
                    )
                counts["user_social_interest_export"] = len(rows)

            if "social_tag_category_ar" in selected:
                rows = read_jsonl(artifact_dir / "social_tag_category_ar.jsonl")
                for row in rows:
                    cur.execute(
                        """
                        INSERT INTO social_tag_category_ar (
                            tag_type, tag, category_id, support, confidence, updated_at
                        ) VALUES (
                            %(tag_type)s, %(tag)s, %(category_id)s::uuid,
                            %(support)s, %(confidence)s, NOW()
                        )
                        ON CONFLICT (tag_type, tag, category_id)
                        DO UPDATE SET support = EXCLUDED.support,
                            confidence = EXCLUDED.confidence,
                            updated_at = NOW()
                        """,
                        row,
                    )
                counts["social_tag_category_ar"] = len(rows)
        conn.commit()
    return counts

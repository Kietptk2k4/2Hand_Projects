"""Verify one-active partial unique index semantics (SQL documentation smoke).

Run against a migrated Social Postgres if available:
  psql $SOCIAL_POSTGRES_URL -f scripts/verify_one_active.sql

This Python helper only validates the intended SQL text is present in Flyway.
"""

from pathlib import Path

MIGRATION = (
    Path(__file__).resolve().parents[2]
    / "social-service"
    / "src"
    / "main"
    / "resources"
    / "db"
    / "migration"
    / "V2__create_recsys_training_tables.sql"
)


def main() -> None:
    text = MIGRATION.read_text(encoding="utf-8")
    assert "uk_model_artifacts_one_active" in text
    assert "WHERE is_active = TRUE" in text
    assert "model_version INT" in text
    assert "user_seen_posts" in text
    print("OK: V2 migration contains one-active index and INT model_version")


if __name__ == "__main__":
    main()

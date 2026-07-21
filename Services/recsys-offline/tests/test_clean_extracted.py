from pipelines.clean_data import clean_extracted


def test_clean_extracted_summary_and_warning_for_empty_impressions():
    raw = {
        "posts": [
            {
                "_id": "p1",
                "author_id": "11111111-1111-1111-1111-111111111111",
                "created_at": "2026-01-01T00:00:00Z",
                "status": "ACTIVE",
                "hashtags": ["#x"],
            }
        ],
        "comments": [],
        "post_likes": [],
        "post_saves": [],
        "follows": [],
        "search_history": [],
        "post_impression_log": [],
    }
    cleaned, summary = clean_extracted(raw)
    assert len(cleaned["posts"]) == 1
    assert summary["sources"]["post_impression_log"]["warning"] == "empty_or_unavailable"
    assert "total_dropped_by_reason" in summary

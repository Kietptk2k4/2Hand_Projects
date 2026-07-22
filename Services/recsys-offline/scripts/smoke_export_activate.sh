#!/usr/bin/env bash
# Smoke notes for export-activate (manual / CI-ish).
# Prerequisites: recsys-offline on :8095, Social Postgres reachable via SOCIAL_POSTGRES_URL,
# train + evaluate already produced model.txt + evaluate_report.json + dataset_test.parquet.
set -euo pipefail

BASE="${RECSYS_OFFLINE_URL:-http://localhost:8095}"
SOCIAL="${SOCIAL_API_URL:-http://localhost:8082}"

echo "== health =="
curl -sf "$BASE/health" | tee /tmp/recsys_health.json

echo
echo "== export-activate =="
curl -sf -X POST "$BASE/jobs/export-activate" | tee /tmp/recsys_export.json
echo

STATUS=$(python -c "import json; print(json.load(open('/tmp/recsys_export.json'))['status'])")
echo "job status: $STATUS"
test "$STATUS" = "activated" -o "$STATUS" = "exported_not_activated"

echo
echo "== Social list (needs ADMIN bearer) =="
echo "curl -H \"Authorization: Bearer \$TOKEN\" \"$SOCIAL/api/v1/social/admin/recommendation-model-artifacts\""
echo "curl -H \"Authorization: Bearer \$TOKEN\" \"$SOCIAL/api/v1/social/admin/recommendation-model-status\""
echo
echo "Expect list to include new version; status mode lightgbm after ModelLoader cron if activated."

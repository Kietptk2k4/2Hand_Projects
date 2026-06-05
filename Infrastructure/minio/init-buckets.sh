#!/bin/sh
set -eu

MINIO_ENDPOINT="${MINIO_ENDPOINT:-http://minio:9000}"
MINIO_ROOT_USER="${MINIO_ROOT_USER:-admin}"
MINIO_ROOT_PASSWORD="${MINIO_ROOT_PASSWORD:-password123}"
ALIAS="local"

BUCKETS="
2hands-avatar
2hands-social-post
2hands-commerce-product
2hands-commerce-review
2hands-commerce-shop
"

echo "Waiting for MinIO at ${MINIO_ENDPOINT}..."
until mc alias set "${ALIAS}" "${MINIO_ENDPOINT}" "${MINIO_ROOT_USER}" "${MINIO_ROOT_PASSWORD}" >/dev/null 2>&1; do
  sleep 2
done

echo "MinIO is ready. Creating buckets and applying policies..."

for bucket in ${BUCKETS}; do
  mc mb "${ALIAS}/${bucket}" --ignore-existing
  mc anonymous set download "${ALIAS}/${bucket}"
  echo "Configured bucket: ${bucket}"
done

echo "MinIO init complete."

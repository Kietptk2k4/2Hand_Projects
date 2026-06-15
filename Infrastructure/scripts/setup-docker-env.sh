#!/usr/bin/env sh
# Creates gitignored .env.docker from committed .env.docker.example templates.
#
# Usage:
#   cd Infrastructure
#   sh scripts/setup-docker-env.sh
#   sh scripts/setup-docker-env.sh --force

set -eu

FORCE=0
if [ "${1:-}" = "--force" ]; then
  FORCE=1
fi

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
INFRA_DIR="$(dirname -- "$SCRIPT_DIR")"
REPO_ROOT="$(dirname -- "$INFRA_DIR")"

copy_template() {
  src_rel="$1"
  dst_rel="$2"
  src="$REPO_ROOT/$src_rel"
  dst="$REPO_ROOT/$dst_rel"

  if [ ! -f "$src" ]; then
    echo "WARN missing template: $src_rel" >&2
    return
  fi

  if [ -f "$dst" ] && [ "$FORCE" -eq 0 ]; then
    echo "  skip  $dst_rel (exists; use --force to overwrite)"
    return
  fi

  cp "$src" "$dst"
  echo "  copy  $dst_rel"
}

echo "2Hands - setup Docker env files"
echo "Repo: $REPO_ROOT"
echo

copy_template "Services/auth-service/.env.docker.example" "Services/auth-service/.env.docker"
copy_template "Services/social-service/.env.docker.example" "Services/social-service/.env.docker"
copy_template "Services/commerce-service/.env.docker.example" "Services/commerce-service/.env.docker"
copy_template "Services/admin-service/.env.docker.example" "Services/admin-service/.env.docker"
copy_template "Services/notification-service/.env.docker.example" "Services/notification-service/.env.docker"
copy_template "frontend/.env.docker.example" "frontend/.env.docker"

echo
echo "Optional secrets: Services/<service>/.env.docker.local (gitignored)"
echo
echo "Next:"
echo "  cd Infrastructure"
echo "  docker compose up -d"
echo "  docker compose -f docker-compose.yml -f docker-compose.apps.yml --profile apps up -d --build"
#!/usr/bin/env sh
# Prints the ngrok gateway URL and env checklist for remote demo (profile ngrok).

set -eu

NGROK_API="http://127.0.0.1:4040/api/tunnels"
PAYOS_WEBHOOK_PATH="/commerce/api/v1/payments/webhooks/payos"

if ! response="$(curl -fsS "$NGROK_API" 2>/dev/null)"; then
  echo "Cannot reach ngrok API at $NGROK_API" >&2
  echo "Start ngrok first:" >&2
  echo "  cd Infrastructure" >&2
  echo "  docker compose -f docker-compose.yml -f docker-compose.dev.yml -f docker-compose.ngrok.yml --profile dev --profile ngrok up -d" >&2
  exit 1
fi

gateway="$(printf '%s' "$response" | python -c "import json,sys; d=json.load(sys.stdin); t=[x for x in d.get('tunnels',[]) if str(x.get('public_url','')).startswith('https://')]; print(t[0]['public_url'].rstrip('/') if t else '')")"

if [ -z "$gateway" ]; then
  echo "No HTTPS tunnel found. Is dev-gateway running?" >&2
  exit 1
fi

host_only="$(printf '%s' "$gateway" | sed -E 's#^https?://([^/]+).*#\1#')"

echo ""
echo "Gateway HTTPS URL:"
echo "$gateway"
echo ""
echo "PayOS Webhook URL:"
echo "${gateway}${PAYOS_WEBHOOK_PATH}"
echo ""
echo "Env checklist:"
echo "  GATEWAY_HTTPS_URL=$gateway"
echo "  CORS_ALLOWED_ORIGINS=$gateway"
echo "  AUTH_OAUTH2_SUCCESS_REDIRECT_URL=$gateway/oauth/success"
echo "  AUTH_OAUTH2_FAILURE_REDIRECT_URL=$gateway/oauth/failure"
echo "  AUTH_OAUTH2_COOKIE_SECURE=true"
echo "  Google OAuth redirect URI: ${gateway}/login/oauth2/code/google"
echo "  Facebook OAuth redirect URI: ${gateway}/login/oauth2/code/facebook"
echo "  AUTH_MINIO_PRESIGNED_ENDPOINT=$gateway"
echo "  AUTH_MINIO_PUBLIC_URL=$gateway/2hands-avatar"
echo "  SOCIAL_MINIO_PRESIGNED_ENDPOINT=$gateway"
echo "  SOCIAL_MINIO_PUBLIC_URL=$gateway/2hands-social-post"
echo "  COMMERCE_MINIO_PRESIGNED_ENDPOINT=$gateway"
echo "  COMMERCE_MINIO_PUBLIC_URL=$gateway"
echo "  COMMERCE_PAYOS_RETURN_URL=$gateway/commerce/checkout/payment-result"
echo "  COMMERCE_PAYOS_CANCEL_URL=$gateway/commerce/checkout/payment-result"
echo "  VITE_*_SERVICE_BASE_URL=$gateway"
echo "  EXPO_PUBLIC_DEV_HOST=$host_only"
echo "  EXPO_PUBLIC_*_SERVICE_BASE_URL=$gateway"
echo ""
echo "Rebuild frontend: ./scripts/build-frontend-ngrok.ps1"
echo "ngrok dashboard: http://127.0.0.1:4040"

# Prints the ngrok gateway URL and env checklist for remote demo (profile ngrok).
# Requires ngrok-gateway container running.

$ErrorActionPreference = "Stop"
$NgrokApi = "http://127.0.0.1:4040/api/tunnels"
$PayosWebhookPath = "/commerce/api/v1/payments/webhooks/payos"

function Get-GatewayTunnel {
    try {
        $response = Invoke-RestMethod -Uri $NgrokApi -Method Get
    } catch {
        Write-Host "Cannot reach ngrok API at $NgrokApi" -ForegroundColor Red
        Write-Host "Start ngrok first:"
        Write-Host "  cd Infrastructure"
        Write-Host "  docker compose -f docker-compose.yml -f docker-compose.dev.yml -f docker-compose.ngrok.yml --profile dev --profile ngrok up -d"
        exit 1
    }

    $tunnel = $response.tunnels | Where-Object { $_.public_url -like "https://*" } | Select-Object -First 1
    if (-not $tunnel) {
        Write-Host "No HTTPS tunnel found. Is dev-gateway running?" -ForegroundColor Yellow
        exit 1
    }

    return $tunnel.public_url.TrimEnd("/")
}

$gateway = Get-GatewayTunnel
$hostOnly = ([Uri]$gateway).Host

Write-Host ""
Write-Host "Gateway HTTPS URL:" -ForegroundColor Green
Write-Host $gateway
Write-Host ""
Write-Host "PayOS Webhook URL:" -ForegroundColor Green
Write-Host "$gateway$PayosWebhookPath"
Write-Host ""
Write-Host "Env checklist (copy to .env.docker.local / mobile .env / FE build):" -ForegroundColor Cyan
Write-Host "  GATEWAY_HTTPS_URL=$gateway"
Write-Host "  CORS_ALLOWED_ORIGINS=$gateway"
Write-Host "  AUTH_OAUTH2_SUCCESS_REDIRECT_URL=$gateway/oauth/success"
Write-Host "  AUTH_OAUTH2_FAILURE_REDIRECT_URL=$gateway/oauth/failure"
Write-Host "  AUTH_OAUTH2_COOKIE_SECURE=true"
Write-Host "  Google OAuth redirect URI: $gateway/login/oauth2/code/google"
Write-Host "  Facebook OAuth redirect URI: $gateway/login/oauth2/code/facebook"
Write-Host "  AUTH_MINIO_PRESIGNED_ENDPOINT=$gateway"
Write-Host "  AUTH_MINIO_PUBLIC_URL=$gateway/2hands-avatar"
Write-Host "  SOCIAL_MINIO_PRESIGNED_ENDPOINT=$gateway"
Write-Host "  SOCIAL_MINIO_PUBLIC_URL=$gateway/2hands-social-post"
Write-Host "  COMMERCE_MINIO_PRESIGNED_ENDPOINT=$gateway"
Write-Host "  COMMERCE_MINIO_PUBLIC_URL=$gateway"
Write-Host "  COMMERCE_PAYOS_RETURN_URL=$gateway/commerce/checkout/payment-result"
Write-Host "  COMMERCE_PAYOS_CANCEL_URL=$gateway/commerce/checkout/payment-result"
Write-Host "  VITE_*_SERVICE_BASE_URL=$gateway  (all five)"
Write-Host "  EXPO_PUBLIC_DEV_HOST=$hostOnly"
Write-Host "  EXPO_PUBLIC_*_SERVICE_BASE_URL=$gateway  (auth, social, commerce, notification)"
Write-Host ""
Write-Host "Rebuild frontend: ./scripts/build-frontend-ngrok.ps1"
Write-Host "ngrok dashboard: http://127.0.0.1:4040"
Write-Host "Note: free ngrok URL changes when the container restarts - rerun this script and update env."

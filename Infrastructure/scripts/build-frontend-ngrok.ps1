# Build frontend production bundle with VITE_* base URLs pointing at the current ngrok gateway.
# Run from Infrastructure/ directory after ngrok tunnel is online.

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$FrontendDir = Join-Path $Root "..\frontend"

$NgrokApi = "http://127.0.0.1:4040/api/tunnels"
try {
    $response = Invoke-RestMethod -Uri $NgrokApi -Method Get
} catch {
    Write-Host "Cannot reach ngrok API. Start ngrok profile first, then rerun this script." -ForegroundColor Red
    exit 1
}

$tunnel = $response.tunnels | Where-Object { $_.public_url -like "https://*" } | Select-Object -First 1
if (-not $tunnel) {
    Write-Host "No HTTPS tunnel found." -ForegroundColor Red
    exit 1
}

$gateway = $tunnel.public_url.TrimEnd("/")
Write-Host "Building frontend with gateway base URL: $gateway" -ForegroundColor Cyan

$env:VITE_AUTH_SERVICE_BASE_URL = $gateway
$env:VITE_SOCIAL_SERVICE_BASE_URL = $gateway
$env:VITE_COMMERCE_SERVICE_BASE_URL = $gateway
$env:VITE_ADMIN_SERVICE_BASE_URL = $gateway
$env:VITE_NOTIFICATION_SERVICE_BASE_URL = $gateway
$env:VITE_USE_MOCK = "false"

Push-Location $FrontendDir
try {
    npm run build
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "Frontend built to frontend/dist. Restart dev-gateway to pick up static files:" -ForegroundColor Green
Write-Host "  docker compose -f docker-compose.yml -f docker-compose.dev.yml -f docker-compose.ngrok.yml --profile dev --profile ngrok up -d --force-recreate dev-gateway"

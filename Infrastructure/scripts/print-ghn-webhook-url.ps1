#Requires -Version 5.1
<#
.SYNOPSIS
  Prints the GHN webhook URL from local ngrok (commerce tunnel).
#>
param(
    [string]$NgrokApi = "http://127.0.0.1:4040/api/tunnels"
)

$ErrorActionPreference = "Stop"
$WebhookPath = "/commerce/api/v1/shipments/webhooks/ghn"

try {
    $tunnels = Invoke-RestMethod -Uri $NgrokApi -Method Get
} catch {
    Write-Host "Cannot reach ngrok API at $NgrokApi" -ForegroundColor Red
    Write-Host "Start ngrok:"
    Write-Host "  cd Infrastructure"
    Write-Host "  docker compose -f docker-compose.yml -f docker-compose.payos.yml --profile payos up -d ngrok-commerce"
    exit 1
}

$https = @($tunnels.tunnels) | Where-Object { $_.public_url -like "https://*" } | Select-Object -First 1
if (-not $https) {
    Write-Host "No HTTPS tunnel found. Is commerce-service running on host port 3003?" -ForegroundColor Red
    exit 1
}

$base = $https.public_url.TrimEnd("/")
$webhookUrl = "$base$WebhookPath"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$demoHtml = Join-Path $scriptDir "ghn-webhook-demo.html"

Write-Host ""
Write-Host "GHN Webhook URL (send to GHN support with Client ID, or use demo tools):"
Write-Host $webhookUrl
Write-Host ""
Write-Host "Set COMMERCE_GHN_WEBHOOK_SECRET in commerce-service .env (same value in header Token / Bearer)."
Write-Host "GHN / demo should send header Token or Authorization: Bearer <secret>."
Write-Host "ngrok dashboard: http://127.0.0.1:4040"
Write-Host ""
Write-Host "Demo (fake webhook from this PC or another machine):"
Write-Host "  1. Open: $demoHtml"
Write-Host "  2. Paste base URL: $base"
Write-Host "  3. Paste secret + ghn_order_code, click status buttons"
Write-Host "  Or CLI:"
Write-Host "  .\demo-ghn-webhook.ps1 -OrderCode <GHN_CODE> -Status delivering -Secret <secret>"
Write-Host "  Docs: docs/ghn/GHN-webhook-demo.md"
Write-Host ""

# Applies ngrok gateway HTTPS URL to auth/social/commerce .env.docker.local MinIO + CORS keys.
# Does NOT mutate database URLs — use read-time rewrite in services instead.
# Optional DB canonicalization: ./migrate-stored-media-urls.ps1 -GatewayUrl $gateway
#
# Usage (from Infrastructure/):
#   ./scripts/apply-ngrok-gateway-env.ps1
#   ./scripts/apply-ngrok-gateway-env.ps1 -GatewayUrl "https://your-host.ngrok-free.app"

param(
    [string]$GatewayUrl = ""
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$RepoRoot = Split-Path -Parent $Root

function Get-GatewayFromNgrokApi {
    $NgrokApi = "http://127.0.0.1:4040/api/tunnels"
    try {
        $response = Invoke-RestMethod -Uri $NgrokApi -Method Get
    } catch {
        Write-Host "Cannot reach ngrok API at $NgrokApi" -ForegroundColor Red
        Write-Host "Pass -GatewayUrl explicitly or start ngrok profile first."
        exit 1
    }
    $tunnel = $response.tunnels | Where-Object { $_.public_url -like "https://*" } | Select-Object -First 1
    if (-not $tunnel) {
        Write-Host "No HTTPS tunnel found." -ForegroundColor Red
        exit 1
    }
    return $tunnel.public_url.TrimEnd("/")
}

function Set-EnvLine {
    param(
        [string]$FilePath,
        [string]$Key,
        [string]$Value
    )
    if (-not (Test-Path $FilePath)) {
        Write-Host "Skip missing file: $FilePath" -ForegroundColor Yellow
        return
    }
    $lines = Get-Content -Path $FilePath -Encoding UTF8
    $pattern = "^\s*$([regex]::Escape($Key))\s*="
    $newLine = "$Key=$Value"
    $found = $false
    $updated = foreach ($line in $lines) {
        if ($line -match $pattern) {
            $found = $true
            $newLine
        } else {
            $line
        }
    }
    if (-not $found) {
        $updated = $updated + $newLine
    }
    [System.IO.File]::WriteAllText($FilePath, ($updated -join "`n") + "`n", [System.Text.UTF8Encoding]::new($false))
}

if ([string]::IsNullOrWhiteSpace($GatewayUrl)) {
    $GatewayUrl = Get-GatewayFromNgrokApi
}

$gateway = $GatewayUrl.TrimEnd("/")
$localCors = "$gateway,http://localhost:5173,http://127.0.0.1:5173"

Write-Host "Applying gateway env: $gateway" -ForegroundColor Cyan

$authEnv = Join-Path $RepoRoot "Services\auth-service\.env.docker.local"
$socialEnv = Join-Path $RepoRoot "Services\social-service\.env.docker.local"
$commerceEnv = Join-Path $RepoRoot "Services\commerce-service\.env.docker.local"

Set-EnvLine $authEnv "CORS_ALLOWED_ORIGINS" $gateway
Set-EnvLine $authEnv "AUTH_OAUTH2_SUCCESS_REDIRECT_URL" "$gateway/oauth/success"
Set-EnvLine $authEnv "AUTH_OAUTH2_FAILURE_REDIRECT_URL" "$gateway/oauth/failure"
Set-EnvLine $authEnv "AUTH_OAUTH2_COOKIE_SECURE" "true"
Set-EnvLine $authEnv "AUTH_MINIO_PRESIGNED_ENDPOINT" $gateway
Set-EnvLine $authEnv "AUTH_MINIO_PUBLIC_URL" "$gateway/2hands-avatar"

Set-EnvLine $socialEnv "CORS_ALLOWED_ORIGINS" $localCors
Set-EnvLine $socialEnv "SOCIAL_MINIO_PRESIGNED_ENDPOINT" $gateway
Set-EnvLine $socialEnv "SOCIAL_MINIO_PUBLIC_URL" "$gateway/2hands-social-post"

Set-EnvLine $commerceEnv "CORS_ALLOWED_ORIGINS" $localCors
Set-EnvLine $commerceEnv "COMMERCE_MINIO_PRESIGNED_ENDPOINT" $gateway
Set-EnvLine $commerceEnv "COMMERCE_MINIO_PUBLIC_URL" $gateway
Set-EnvLine $commerceEnv "COMMERCE_PAYOS_RETURN_URL" "$gateway/commerce/checkout/payment-result"
Set-EnvLine $commerceEnv "COMMERCE_PAYOS_CANCEL_URL" "$gateway/commerce/checkout/payment-result"

Write-Host ""
Write-Host "Updated .env.docker.local for auth, social, commerce." -ForegroundColor Green
Write-Host "Restart services: docker compose -f docker-compose.yml -f docker-compose.dev.yml --profile dev restart auth-service social-service commerce-service"
Write-Host "Rebuild FE: ./scripts/build-frontend-ngrok.ps1"

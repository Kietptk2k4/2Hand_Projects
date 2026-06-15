# Creates gitignored .env.docker from committed .env.docker.example templates.
# Run from Infrastructure folder.
#
# Usage:
#   cd Infrastructure
#   ./scripts/setup-docker-env.ps1
#   ./scripts/setup-docker-env.ps1 -Force

param(
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$infraDir = Split-Path -Parent $PSScriptRoot
$repoRoot = Split-Path -Parent $infraDir

$templates = @(
    @{ Source = "Services/auth-service/.env.docker.example"; Target = "Services/auth-service/.env.docker" },
    @{ Source = "Services/social-service/.env.docker.example"; Target = "Services/social-service/.env.docker" },
    @{ Source = "Services/commerce-service/.env.docker.example"; Target = "Services/commerce-service/.env.docker" },
    @{ Source = "Services/admin-service/.env.docker.example"; Target = "Services/admin-service/.env.docker" },
    @{ Source = "Services/notification-service/.env.docker.example"; Target = "Services/notification-service/.env.docker" },
    @{ Source = "frontend/.env.docker.example"; Target = "frontend/.env.docker" }
)

Write-Host "2Hands - setup Docker env files"
Write-Host "Repo: $repoRoot"
Write-Host ""

$copied = 0
$skipped = 0

foreach ($item in $templates) {
    $sourcePath = Join-Path $repoRoot $item.Source
    $targetPath = Join-Path $repoRoot $item.Target

    if (-not (Test-Path $sourcePath)) {
        Write-Warning "Missing template: $($item.Source)"
        continue
    }

    if ((Test-Path $targetPath) -and -not $Force) {
        Write-Host "  skip  $($item.Target) (exists; use -Force to overwrite)"
        $skipped++
        continue
    }

    Copy-Item -Path $sourcePath -Destination $targetPath -Force
    Write-Host "  copy  $($item.Target)"
    $copied++
}

Write-Host ""
Write-Host "Done. Copied: $copied, skipped: $skipped."
Write-Host ""
Write-Host "Optional secrets: Services/<service>/.env.docker.local (gitignored)"
Write-Host ""
Write-Host "Next:"
Write-Host "  cd Infrastructure"
Write-Host "  docker compose up -d"
Write-Host "  docker compose -f docker-compose.yml -f docker-compose.apps.yml --profile apps up -d --build"
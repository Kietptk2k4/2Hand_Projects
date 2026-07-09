# OPTIONAL: canonicalize stored media URLs in dev databases (replaces dev origin prefix only).
# Prefer read-time rewrite (ResponseBodyStoredMediaUrlAdvice) — run this only if you need DB values updated.
#
# Usage (from Infrastructure/):
#   ./scripts/migrate-stored-media-urls.ps1 -GatewayUrl "https://your-host.ngrok-free.app"
#   ./scripts/migrate-stored-media-urls.ps1 -GatewayUrl "https://your-host.ngrok-free.app" -FromOrigin "http://localhost:9000"
#
# Requires postgres containers from docker-compose dev profile.

param(
    [Parameter(Mandatory = $true)]
    [string]$GatewayUrl,
    [string]$FromOrigin = "http://localhost:9000"
)

$ErrorActionPreference = "Stop"

$gateway = $GatewayUrl.TrimEnd("/")
$from = $FromOrigin.TrimEnd("/")

Write-Host "Rewriting stored media URLs in DB:" -ForegroundColor Cyan
Write-Host "  from: $from"
Write-Host "  to:   $gateway"
Write-Host ""

function Invoke-PostgresReplace {
    param(
        [string]$Container,
        [string]$Database,
        [string]$Sql
    )
    Write-Host "[$Database] running update..." -ForegroundColor DarkCyan
    docker exec -i $Container psql -U postgres -d $Database -v ON_ERROR_STOP=1 -c $Sql
}

$replaceExpr = "regexp_replace(%COLUMN%, '^$([regex]::Escape($from))', '$gateway', 'g')"

# auth_db — user_profiles
$authSql = @"
UPDATE user_profiles
SET avatar_url = regexp_replace(avatar_url, '^$([regex]::Escape($from))', '$gateway', 'g')
WHERE avatar_url LIKE '$from%';
UPDATE user_profiles
SET cover_url = regexp_replace(cover_url, '^$([regex]::Escape($from))', '$gateway', 'g')
WHERE cover_url LIKE '$from%';
"@
Invoke-PostgresReplace -Container "postgres-auth" -Database "auth_db" -Sql $authSql

# commerce_db — shops, product_media, review_media (column names may vary)
$commerceSql = @"
UPDATE shops
SET avatar_url = regexp_replace(avatar_url, '^$([regex]::Escape($from))', '$gateway', 'g')
WHERE avatar_url LIKE '$from%';
UPDATE shops
SET cover_url = regexp_replace(cover_url, '^$([regex]::Escape($from))', '$gateway', 'g')
WHERE cover_url LIKE '$from%';
UPDATE product_media
SET media_url = regexp_replace(media_url, '^$([regex]::Escape($from))', '$gateway', 'g')
WHERE media_url LIKE '$from%';
UPDATE review_media
SET media_url = regexp_replace(media_url, '^$([regex]::Escape($from))', '$gateway', 'g')
WHERE media_url LIKE '$from%';
"@
Invoke-PostgresReplace -Container "postgres-commerce" -Database "commerce_db" -Sql $commerceSql

Write-Host ""
Write-Host "Note: social post media URLs live in MongoDB (posts collection)." -ForegroundColor Yellow
Write-Host "Use API read-time rewrite or run a separate mongo script if canonical DB values are required."
Write-Host "Done." -ForegroundColor Green

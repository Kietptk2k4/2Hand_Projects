# Start remote demo stack end-to-end:

#   1. infra + dev services + ngrok gateway

#   2. print gateway URL + env checklist

#   3. build frontend against current ngrok URL

#   4. recreate dev-gateway to serve fresh dist/

#

# Run from repo root or Infrastructure/ (script cd's to Infrastructure).



$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $PSScriptRoot

Set-Location $Root



$ComposeArgs = @(

    "-f", "docker-compose.yml",

    "-f", "docker-compose.dev.yml",

    "-f", "docker-compose.ngrok.yml",

    "--profile", "dev",

    "--profile", "ngrok"

)



function Wait-ForNgrokTunnel {

    param(

        [int]$MaxAttempts = 30,

        [int]$DelaySeconds = 2

    )



    $ngrokApi = "http://127.0.0.1:4040/api/tunnels"



    for ($attempt = 1; $attempt -le $MaxAttempts; $attempt++) {

        try {

            $response = Invoke-RestMethod -Uri $ngrokApi -Method Get

            $tunnel = $response.tunnels | Where-Object { $_.public_url -like "https://*" } | Select-Object -First 1

            if ($tunnel) {

                return $tunnel.public_url.TrimEnd("/")

            }

        } catch {

            # ngrok inspect API not ready yet

        }



        Write-Host "Waiting for ngrok HTTPS tunnel ($attempt/$MaxAttempts)..." -ForegroundColor DarkYellow

        Start-Sleep -Seconds $DelaySeconds

    }



    Write-Host "Timed out waiting for ngrok HTTPS tunnel at $ngrokApi" -ForegroundColor Red

    exit 1

}



if (-not (Test-Path ".env")) {

    Write-Host "Missing Infrastructure/.env - copy .env.example and set NGROK_AUTHTOKEN" -ForegroundColor Red

    exit 1

}



Write-Host "Step 1/4: Starting infra + dev services + ngrok gateway..." -ForegroundColor Cyan

docker compose @ComposeArgs up -d

if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }



Write-Host "Step 2/4: Waiting for ngrok tunnel..." -ForegroundColor Cyan

$null = Wait-ForNgrokTunnel



Write-Host "Step 2/4: Gateway URL + env checklist" -ForegroundColor Cyan

& "$PSScriptRoot\print-ngrok-urls.ps1"

if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }



Write-Host ""

Write-Host "Patch Services/*/.env.docker.local from the checklist above if this is the first run or URL changed." -ForegroundColor Yellow

Write-Host "Step 3/4: Building frontend for current ngrok URL..." -ForegroundColor Cyan

& "$PSScriptRoot\build-frontend-ngrok.ps1"

if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }



$distPath = Join-Path $Root "..\frontend\dist\index.html"

if (-not (Test-Path $distPath)) {

    Write-Host "Frontend build failed: $distPath not found" -ForegroundColor Red

    exit 1

}



Write-Host "Step 4/4: Recreating dev-gateway with fresh frontend/dist..." -ForegroundColor Cyan

docker compose @ComposeArgs up -d --force-recreate dev-gateway

if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }



Write-Host ""

Write-Host "Remote demo ready. Open the Gateway HTTPS URL from step 2 in your browser." -ForegroundColor Green


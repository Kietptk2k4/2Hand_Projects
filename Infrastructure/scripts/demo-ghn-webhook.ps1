#Requires -Version 5.1
<#
.SYNOPSIS
  POST a fake GHN webhook to commerce-service (via ngrok or local URL).

.EXAMPLE
  .\demo-ghn-webhook.ps1 -OrderCode "5ENLKKHD" -Status delivered -Secret "my-secret"

.EXAMPLE
  .\demo-ghn-webhook.ps1 -WebhookBaseUrl "https://xxxx.ngrok-free.dev" -OrderCode "5ENLKKHD" -Status delivering
#>
param(
    [string]$WebhookBaseUrl = "",
    [Parameter(Mandatory = $true)]
    [string]$OrderCode,
    [ValidateSet("ready_to_pick", "picking", "delivering", "delivered", "cancel", "return")]
    [string]$Status = "delivering",
    [string]$Secret = "",
    [string]$ClientOrderCode = "",
    [string]$NgrokApi = "http://127.0.0.1:4040/api/tunnels"
)

$ErrorActionPreference = "Stop"
$WebhookPath = "/commerce/api/v1/shipments/webhooks/ghn"

function Get-NgrokHttpsBaseUrl {
    param([string]$ApiUrl)
    try {
        $tunnels = Invoke-RestMethod -Uri $ApiUrl -Method Get
    } catch {
        throw "Cannot reach ngrok API at $ApiUrl. Start ngrok-commerce or pass -WebhookBaseUrl."
    }
    $https = @($tunnels.tunnels) | Where-Object { $_.public_url -like "https://*" } | Select-Object -First 1
    if (-not $https) {
        throw "No HTTPS tunnel found from ngrok API."
    }
    return $https.public_url.TrimEnd("/")
}

if ([string]::IsNullOrWhiteSpace($WebhookBaseUrl)) {
    $WebhookBaseUrl = Get-NgrokHttpsBaseUrl -ApiUrl $NgrokApi
}

$base = $WebhookBaseUrl.TrimEnd("/")
$url = "$base$WebhookPath"

$bodyObj = [ordered]@{
    CODAmount         = 0
    CODTransferDate   = $null
    ClientOrderCode   = $ClientOrderCode
    ConvertedWeight   = 200
    Description       = "2Hands GHN webhook demo: $Status"
    Height            = 10
    IsPartialReturn   = $false
    Length            = 10
    OrderCode         = $OrderCode
    PartialReturnCode = ""
    PaymentType       = 1
    Reason            = ""
    ReasonCode        = ""
    ShopID            = 0
    Status            = $Status
    Time              = (Get-Date).ToUniversalTime().ToString("o")
    TotalFee          = 0
    Type              = "switch_status"
    Warehouse         = "Demo"
    Weight            = 200
    Width             = 10
}

$json = $bodyObj | ConvertTo-Json -Depth 5 -Compress
$headers = @{
    "Content-Type"                 = "application/json"
    "ngrok-skip-browser-warning"   = "true"
}
if (-not [string]::IsNullOrWhiteSpace($Secret)) {
    $headers["Token"] = $Secret
}

Write-Host ""
Write-Host "POST $url"
Write-Host "Status: $Status | OrderCode: $OrderCode"
Write-Host ""

try {
    $response = Invoke-WebRequest -Uri $url -Method Post -Headers $headers -Body $json -UseBasicParsing
    Write-Host "HTTP $($response.StatusCode)"
    Write-Host $response.Content
} catch {
    $resp = $_.Exception.Response
    if ($resp) {
        $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
        $content = $reader.ReadToEnd()
        Write-Host "HTTP $([int]$resp.StatusCode)" -ForegroundColor Red
        Write-Host $content
    } else {
        throw
    }
    exit 1
}

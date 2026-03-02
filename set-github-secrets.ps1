# =============================================================
#  set-github-secrets.ps1
#  Sets GitHub Actions secrets using the GitHub CLI (gh)
#  Run: .\set-github-secrets.ps1
# =============================================================

# ── CONFIG: Fill in these values ──────────────────────────────────────────
$GITHUB_TOKEN         = "<YOUR_GITHUB_TOKEN>"   # needs repo/secrets scope
$REPO                 = "naidk/ecommerce-microservices-backend"

$AWS_ACCESS_KEY_ID     = "<YOUR_AWS_ACCESS_KEY_ID>"
$AWS_SECRET_ACCESS_KEY = "<YOUR_AWS_SECRET_ACCESS_KEY>"
$EB_URL                = "<YOUR_EB_URL>"   # e.g. http://ecommerce-api-prod.us-east-1.elasticbeanstalk.com
# ──────────────────────────────────────────────────────────────────────────

function Log($msg)  { Write-Host "`n>>> $msg" -ForegroundColor Cyan }
function Ok($msg)   { Write-Host "    OK: $msg" -ForegroundColor Green }
function Err($msg)  { Write-Host "    ERR: $msg" -ForegroundColor Red; exit 1 }

# Validate no placeholders
foreach ($var in @($GITHUB_TOKEN, $AWS_ACCESS_KEY_ID, $AWS_SECRET_ACCESS_KEY, $EB_URL)) {
    if ($var -match "<.+>") {
        Err "Please fill in all values before running this script."
    }
}

Log "Verifying GitHub CLI (gh)..."
$ghInstalled = Get-Command gh -ErrorAction SilentlyContinue
if (-not $ghInstalled) {
    Log "Installing GitHub CLI via winget..."
    winget install --id GitHub.cli --silent --accept-package-agreements --accept-source-agreements
    $env:PATH = [System.Environment]::GetEnvironmentVariable("PATH", "Machine") + ";" + [System.Environment]::GetEnvironmentVariable("PATH", "User")
}

# Auth gh with token
Log "Authenticating GitHub CLI..."
$env:GH_TOKEN = $GITHUB_TOKEN

# Set secrets using gh CLI
Log "Setting GitHub Actions secrets for repository: $REPO"
$secrets = @{
    AWS_ACCESS_KEY_ID     = $AWS_ACCESS_KEY_ID
    AWS_SECRET_ACCESS_KEY = $AWS_SECRET_ACCESS_KEY
    EB_URL                = $EB_URL
}

foreach ($secret in $secrets.GetEnumerator()) {
    $secret.Value | gh secret set $secret.Key --repo $REPO
    if ($LASTEXITCODE -eq 0) {
        Ok "Secret '$($secret.Key)' successfully set"
    } else {
        Write-Host "    WARN: Failed to set '$($secret.Key)'" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  GitHub Secrets configured!" -ForegroundColor Green
Write-Host "  Repo: https://github.com/$REPO/settings/secrets/actions" -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Now push your changes to 'main' and GitHub Actions will deploy" -ForegroundColor Cyan
Write-Host "your Docker image to Elastic Beanstalk automatically!" -ForegroundColor Cyan

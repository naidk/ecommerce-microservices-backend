# test-live-api.ps1
# This script runs the automated Postman collection against your LIVE AWS environment.

$baseUrl = "http://ecommerce-api-prod.eba-jshpsgpi.us-east-1.elasticbeanstalk.com"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "🚀 TESTING LIVE API" -ForegroundColor Cyan
Write-Host "URL: $baseUrl" -ForegroundColor White
Write-Host "==================================================" -ForegroundColor Cyan

# Check for Python
if (!(Get-Command python -ErrorAction SilentlyContinue)) {
    Write-Error "Python is not installed or not in PATH. Please install Python to run automated tests."
    exit 1
}

# Run the python script with the live URL
python test_postman_live.py "$baseUrl"

Write-Host "`nTests Finished!" -ForegroundColor Green

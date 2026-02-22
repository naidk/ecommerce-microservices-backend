# Check if newman is installed
try {
    $newmanVer = newman -v
    Write-Host "Newman version $newmanVer detected." -ForegroundColor Green
}
catch {
    Write-Host "Newman not found. Attempting to install via npm..." -ForegroundColor Cyan
    npm install -g newman
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to install Newman. Please run 'npm install -g newman' manually."
        exit 1
    }
}

Write-Host "Starting Automated API Tests..." -ForegroundColor Yellow
Write-Host "Collection: automated_test_collection.json"

# Run newman
# Note: --delay-request 500 is added to prevent hitting rate limits if any
newman run automated_test_collection.json --delay-request 500

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nTests completed successfully!" -ForegroundColor Green
}
else {
    Write-Host "`nSome tests failed. Check the output above." -ForegroundColor Red
}

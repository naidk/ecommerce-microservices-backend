$dockerExe = "C:\Program Files\Docker\Docker\Docker Desktop.exe"
$swaggerUrl = "http://localhost:8081/swagger-ui.html"

Write-Host "Checking Docker status..." -ForegroundColor Cyan

# Check if Docker is running
$dockerRunning = $false
try {
    docker info | Out-Null
    if ($LASTEXITCODE -eq 0) {
        $dockerRunning = $true
    }
} catch {
    $dockerRunning = $false
}

if (-not $dockerRunning) {
    Write-Host "Docker is not running. Attempting to start Docker Desktop..." -ForegroundColor Yellow
    if (Test-Path $dockerExe) {
        Start-Process $dockerExe
        Write-Host "Waiting for Docker to start (this may take a minute or two)..." -ForegroundColor Yellow
        
        # Loop until Docker is responsive
        $retryCount = 0
        $maxRetries = 60 # wait up to 2-3 minutes
        while ($retryCount -lt $maxRetries) {
            Start-Sleep -Seconds 3
            try {
                docker info | Out-Null
                if ($LASTEXITCODE -eq 0) {
                    $dockerRunning = $true
                    break
                }
            } catch {}
            Write-Host "." -NoNewline
            $retryCount++
        }
        Write-Host ""
    } else {
        Write-Error "Docker Desktop executable not found at expected path: $dockerExe"
        exit 1
    }
}

if ($dockerRunning) {
    Write-Host "Docker is up and running!" -ForegroundColor Green
    
    Write-Host "Starting application services..." -ForegroundColor Cyan
    docker compose up -d
    
    Write-Host "Waiting for services to be ready..." -ForegroundColor Cyan
    Start-Sleep -Seconds 15 
    
    Write-Host "Opening Swagger UI..." -ForegroundColor Green
    Start-Process $swaggerUrl
} else {
    Write-Error "Failed to start Docker. Please start Docker Desktop manually."
    exit 1
}

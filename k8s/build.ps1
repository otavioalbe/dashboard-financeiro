# Build all microservice images inside Minikube's Docker daemon
# Run from the project root: .\k8s\build.ps1

Write-Host "Configuring Docker to use Minikube's daemon..." -ForegroundColor Cyan
& minikube -p minikube docker-env --shell powershell | Invoke-Expression

$services = @(
    "auth-service",
    "account-service",
    "transaction-service",
    "analytics-service",
    "api-gateway"
)

$root = Split-Path $PSScriptRoot -Parent

foreach ($svc in $services) {
    Write-Host "`nBuilding $svc..." -ForegroundColor Yellow
    docker build -t "${svc}:latest" "$root\$svc"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "FAILED: $svc" -ForegroundColor Red
        exit 1
    }
    Write-Host "OK: $svc" -ForegroundColor Green
}

Write-Host "`nAll images built successfully." -ForegroundColor Cyan
Write-Host "Run .\k8s\deploy.ps1 to deploy to Minikube." -ForegroundColor Cyan

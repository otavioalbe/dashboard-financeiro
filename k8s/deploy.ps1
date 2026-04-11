# Deploy all resources to Minikube
# Run from the project root: .\k8s\deploy.ps1

$k8sDir = $PSScriptRoot
$root = Split-Path $k8sDir -Parent
$envFile = "$root\.env"

# ── 1. Namespace ─────────────────────────────────────────────────────────────
Write-Host "Creating namespace..." -ForegroundColor Cyan
kubectl apply -f "$k8sDir\00-namespace.yaml"

# ── 2. Secret from .env ───────────────────────────────────────────────────────
Write-Host "`nCreating secret from .env..." -ForegroundColor Cyan

if (-not (Test-Path $envFile)) {
    Write-Host "ERROR: .env file not found at $envFile" -ForegroundColor Red
    exit 1
}

$env = @{}
Get-Content $envFile | Where-Object { $_ -match "^\s*[^#].*=.*" } | ForEach-Object {
    $parts = $_ -split "=", 2
    $env[$parts[0].Trim()] = $parts[1].Trim()
}

kubectl create secret generic app-secrets `
    --from-literal=JWT_SECRET=$($env["JWT_SECRET"]) `
    --from-literal=DB_USERNAME=$($env["DB_USERNAME"]) `
    --from-literal=DB_PASSWORD=$($env["DB_PASSWORD"]) `
    --from-literal=MONGO_USERNAME=$($env["MONGO_USERNAME"]) `
    --from-literal=MONGO_PASSWORD=$($env["MONGO_PASSWORD"]) `
    --from-literal=INTERNAL_API_KEY=$($env["INTERNAL_API_KEY"]) `
    --namespace financial-dashboard `
    --dry-run=client -o yaml | kubectl apply -f -

# ── 3. Infrastructure ─────────────────────────────────────────────────────────
Write-Host "`nDeploying infrastructure..." -ForegroundColor Cyan
kubectl apply -f "$k8sDir\infrastructure"

# ── 4. Wait for infrastructure to be ready ───────────────────────────────────
Write-Host "`nWaiting for databases to be ready..." -ForegroundColor Yellow
kubectl rollout status deployment/postgres-auth      -n financial-dashboard --timeout=120s
kubectl rollout status deployment/postgres-account   -n financial-dashboard --timeout=120s
kubectl rollout status deployment/postgres-transaction -n financial-dashboard --timeout=120s
kubectl rollout status deployment/mongodb-analytics  -n financial-dashboard --timeout=120s
kubectl rollout status deployment/kafka              -n financial-dashboard --timeout=180s

# ── 5. Microservices ──────────────────────────────────────────────────────────
Write-Host "`nDeploying microservices..." -ForegroundColor Cyan
kubectl apply -f "$k8sDir\apps"

# ── 6. Summary ────────────────────────────────────────────────────────────────
Write-Host "`nDeploy complete. Checking pods..." -ForegroundColor Green
kubectl get pods -n financial-dashboard

Write-Host "`nAPI Gateway URL:" -ForegroundColor Cyan
minikube service api-gateway -n financial-dashboard --url

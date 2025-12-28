#!/usr/bin/env pwsh
# WeatherSpring - Windows Minikube Deployment
# This script deploys the application to Minikube and runs tests

param(
    [string]$ApiKey = $env:WEATHER_API_KEY,
    [switch]$SkipTests
)

$ErrorActionPreference = "Stop"

# Output functions
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Warning { Write-Host $args -ForegroundColor Yellow }
function Write-Error { Write-Host $args -ForegroundColor Red }

Write-Host @"

WeatherSpring - Minikube Deployment for Windows
================================================

"@ -ForegroundColor Cyan

# Check prerequisites
Write-Info "Checking prerequisites..."

$tools = @{
    "docker" = "Docker"
    "minikube" = "Minikube"
    "kubectl" = "kubectl"
    "helm" = "Helm"
}

$missing = @()
foreach ($cmd in $tools.Keys) {
    if (Get-Command $cmd -ErrorAction SilentlyContinue) {
        Write-Success "  $($tools[$cmd]) found"
    } else {
        $missing += $tools[$cmd]
        Write-Error "  $($tools[$cmd]) not found"
    }
}

if ($missing.Count -gt 0) {
    Write-Error "`nMissing tools: $($missing -join ', ')"
    Write-Info "Install with: choco install docker-desktop minikube kubernetes-cli kubernetes-helm"
    exit 1
}

# Check/Start Minikube
Write-Info "`nChecking Minikube..."

$minikubeStatus = minikube status --format='{{.Host}}' 2>&1
if ($minikubeStatus -ne "Running") {
    Write-Warning "  Minikube not running. Starting it now..."
    minikube start --cpus=4 --memory=8192
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to start Minikube"
        exit 1
    }
    Write-Success "  Minikube started"
} else {
    Write-Success "  Minikube is running"
}

# Enable addons
Write-Info "`nEnabling Minikube addons..."

$addons = @("ingress", "metrics-server")
foreach ($addon in $addons) {
    $status = minikube addons list | Select-String $addon
    if ($status -match "disabled") {
        Write-Info "  Enabling $addon..."
        minikube addons enable $addon | Out-Null
    } else {
        Write-Success "  $addon already enabled"
    }
}

# Get API Key
if (-not $ApiKey) {
    Write-Warning "`nWeather API Key not provided"
    Write-Info "Get a free API key from: https://www.weatherapi.com/"
    $ApiKey = Read-Host "Enter your Weather API Key"
}

if (-not $ApiKey) {
    Write-Error "API Key is required"
    exit 1
}

Write-Success "  API Key provided"

# Build Docker image
Write-Info "`nBuilding Docker image..."

docker build -t weatherspring/weather-service:1.0.0 .
if ($LASTEXITCODE -ne 0) {
    Write-Error "Docker build failed"
    exit 1
}
Write-Success "  Image built"

# Load image into Minikube
Write-Info "`nLoading image into Minikube..."

minikube image load weatherspring/weather-service:1.0.0
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to load image"
    exit 1
}
Write-Success "  Image loaded"

# Deploy with Helm
Write-Info "`nDeploying with Helm..."

$valuesFile = "./helm/weatherspring/values-minikube-windows.yaml"

$existing = helm list -q | Select-String "^weatherspring$"
if ($existing) {
    Write-Warning "  Existing release found. Upgrading..."
    helm upgrade weatherspring ./helm/weatherspring `
        -f $valuesFile `
        --set secrets.weatherApiKey=$ApiKey `
        --wait
} else {
    Write-Info "  Installing new release..."
    helm install weatherspring ./helm/weatherspring `
        -f $valuesFile `
        --set secrets.weatherApiKey=$ApiKey `
        --wait
}

if ($LASTEXITCODE -ne 0) {
    Write-Error "Helm deployment failed"
    exit 1
}
Write-Success "  Helm deployment complete"

# Wait for pod to be ready
Write-Info "`nWaiting for pod to be ready..."

try {
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=weatherspring --timeout=180s 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Success "  Pod is ready"
    } else {
        Write-Error "  Timeout waiting for pod"
        Write-Info "`nPod status:"
        kubectl get pods -l app.kubernetes.io/name=weatherspring
        kubectl describe pods -l app.kubernetes.io/name=weatherspring
        exit 1
    }
} catch {
    Write-Error "  Error waiting for pod: $($_.Exception.Message)"
    Write-Info "`nPod status:"
    kubectl get pods -l app.kubernetes.io/name=weatherspring
    kubectl describe pods -l app.kubernetes.io/name=weatherspring
    exit 1
}

# Start minikube tunnel
Write-Host "`n" + ("=" * 60) -ForegroundColor Cyan
Write-Info "Starting minikube tunnel..."
Write-Host ("=" * 60) -ForegroundColor Cyan

Write-Warning "`nIMPORTANT: This requires administrator privileges!"
Write-Info "Starting tunnel in background...`n"

try {
    $tunnelJob = Start-Job -ScriptBlock {
        minikube tunnel
    }

    Write-Success "  Tunnel started (Job ID: $($tunnelJob.Id))"
    Write-Info "  Waiting for LoadBalancer IP..."
    Start-Sleep -Seconds 10

    # Wait for LoadBalancer IP
    $attempts = 0
    $maxAttempts = 30
    $externalIP = $null

    while ($attempts -lt $maxAttempts) {
        $externalIP = kubectl get svc weatherspring -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>$null
        if ($externalIP) {
            break
        }
        Start-Sleep -Seconds 2
        $attempts++
    }

    if (-not $externalIP) {
        Write-Error "  Failed to get LoadBalancer IP"
        Stop-Job -Id $tunnelJob.Id
        Remove-Job -Id $tunnelJob.Id
        Write-Info "`nManual setup required:"
        Write-Host "  1. Open PowerShell as Administrator"
        Write-Host "  2. Run: minikube tunnel"
        Write-Host "  3. Check IP: kubectl get svc weatherspring"
        exit 1
    }

    $port = kubectl get svc weatherspring -o jsonpath='{.spec.ports[0].port}'

    # Run tests
    if (-not $SkipTests) {
        Write-Host "`n" + ("=" * 60) -ForegroundColor Cyan
        Write-Info "Running tests..."
        Write-Host ("=" * 60) -ForegroundColor Cyan

        $baseUrl = "http://${externalIP}:${port}"
        $testsPassed = 0
        $testsFailed = 0

        # Test 1: Health check
        Write-Info "`nTest 1: Health check"
        try {
            $response = Invoke-WebRequest -Uri "$baseUrl/actuator/health" -TimeoutSec 10 -UseBasicParsing
            if ($response.StatusCode -eq 200) {
                Write-Success "  PASS: Health endpoint responding"
                $testsPassed++
            } else {
                Write-Error "  FAIL: Health endpoint returned $($response.StatusCode)"
                $testsFailed++
            }
        } catch {
            Write-Error "  FAIL: Health endpoint not accessible"
            $testsFailed++
        }

        # Test 2: Prometheus metrics
        Write-Info "`nTest 2: Prometheus metrics"
        try {
            $response = Invoke-WebRequest -Uri "$baseUrl/actuator/prometheus" -TimeoutSec 10 -UseBasicParsing
            if ($response.StatusCode -eq 200 -and $response.Content -match "jvm_") {
                Write-Success "  PASS: Metrics endpoint working"
                $testsPassed++
            } else {
                Write-Error "  FAIL: Metrics endpoint not working correctly"
                $testsFailed++
            }
        } catch {
            Write-Error "  FAIL: Metrics endpoint not accessible"
            $testsFailed++
        }

        # Test 3: Add location
        Write-Info "`nTest 3: Add location (London)"
        try {
            $body = @{
                name = "London"
                country = "UK"
                latitude = 51.5074
                longitude = -0.1278
            } | ConvertTo-Json

            $response = Invoke-RestMethod -Uri "$baseUrl/api/locations" `
                -Method POST `
                -ContentType "application/json" `
                -Body $body `
                -TimeoutSec 10

            if ($response.name -eq "London") {
                Write-Success "  PASS: Location added successfully"
                $testsPassed++
            } else {
                Write-Error "  FAIL: Location not added correctly"
                $testsFailed++
            }
        } catch {
            Write-Error "  FAIL: Could not add location - $_"
            $testsFailed++
        }

        # Test 4: Get weather
        Write-Info "`nTest 4: Get current weather for London"
        try {
            $response = Invoke-RestMethod -Uri "$baseUrl/api/weather/current?location=London" `
                -TimeoutSec 10

            if ($response.temperature -and $response.condition) {
                Write-Success "  PASS: Weather data retrieved"
                Write-Info "    Temperature: $($response.temperature)C"
                Write-Info "    Condition: $($response.condition)"
                $testsPassed++
            } else {
                Write-Error "  FAIL: Weather data incomplete"
                $testsFailed++
            }
        } catch {
            Write-Error "  FAIL: Could not get weather - $_"
            $testsFailed++
        }

        # Test 5: Get all locations
        Write-Info "`nTest 5: Get all locations"
        try {
            $response = Invoke-RestMethod -Uri "$baseUrl/api/locations" -TimeoutSec 10

            if ($response -is [Array] -and $response.Count -gt 0) {
                Write-Success "  PASS: Locations retrieved ($($response.Count) locations)"
                $testsPassed++
            } else {
                Write-Error "  FAIL: No locations found"
                $testsFailed++
            }
        } catch {
            Write-Error "  FAIL: Could not get locations - $_"
            $testsFailed++
        }

        # Test summary
        Write-Host "`n" + ("=" * 60) -ForegroundColor Cyan
        Write-Info "Test Summary"
        Write-Host ("=" * 60) -ForegroundColor Cyan
        Write-Host "  Total tests: $($testsPassed + $testsFailed)"
        Write-Success "  Passed: $testsPassed"
        if ($testsFailed -gt 0) {
            Write-Error "  Failed: $testsFailed"
        } else {
            Write-Success "  Failed: 0"
        }
        Write-Host ""
    }

    # Display access information
    Write-Host ("=" * 60) -ForegroundColor Green
    Write-Success "Deployment Complete!"
    Write-Host ("=" * 60) -ForegroundColor Green
    Write-Host ""
    Write-Host "  Application URL:   " -NoNewline
    Write-Host "http://${externalIP}:${port}" -ForegroundColor Yellow
    Write-Host "  Swagger UI:        " -NoNewline
    Write-Host "http://${externalIP}:${port}/swagger-ui.html" -ForegroundColor Yellow
    Write-Host "  Health Check:      " -NoNewline
    Write-Host "http://${externalIP}:${port}/actuator/health" -ForegroundColor Yellow
    Write-Host "  Metrics:           " -NoNewline
    Write-Host "http://${externalIP}:${port}/actuator/prometheus" -ForegroundColor Yellow
    Write-Host ""

    # Open browser
    Write-Info "Opening Swagger UI in browser..."
    Start-Sleep -Seconds 2
    Start-Process "http://${externalIP}:${port}/swagger-ui.html"

    Write-Host ("=" * 60) -ForegroundColor Yellow
    Write-Warning "KEEP THIS WINDOW OPEN!"
    Write-Host "The tunnel must stay running for the application to work." -ForegroundColor Yellow
    Write-Host "Press Ctrl+C to stop." -ForegroundColor Yellow
    Write-Host ("=" * 60) -ForegroundColor Yellow
    Write-Host ""

    Write-Info "Useful commands:"
    Write-Host "  View logs:     kubectl logs -l app.kubernetes.io/name=weatherspring --tail=50 -f"
    Write-Host "  View pods:     kubectl get pods"
    Write-Host "  View service:  kubectl get svc weatherspring"
    Write-Host "  Uninstall:     helm uninstall weatherspring"
    Write-Host ""

    # Keep tunnel alive
    Write-Info "Tunnel running..."
    Wait-Job -Id $tunnelJob.Id

} catch {
    Write-Error "Failed to start tunnel: $_"
    Write-Warning "`nYou may need to run this script as Administrator"
    exit 1
}

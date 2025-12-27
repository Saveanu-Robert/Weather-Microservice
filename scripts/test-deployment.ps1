#!/usr/bin/env pwsh
# WeatherSpring - Deployment Test Script
# Tests application deployment on different environments

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("local", "docker", "minikube")]
    [string]$Environment = "local",

    [Parameter(Mandatory=$false)]
    [string]$BaseUrl = ""
)

$ErrorActionPreference = "Stop"

# Output functions
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Warning { Write-Host $args -ForegroundColor Yellow }
function Write-Error { Write-Host $args -ForegroundColor Red }

# Test results tracking
$TestsPassed = 0
$TestsFailed = 0
$TestResults = @()

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET",
        [hashtable]$Body = $null,
        [string]$ExpectedContent = $null
    )

    Write-Info "`n[$($TestsPassed + $TestsFailed + 1)] Testing: $Name"
    Write-Host "  URL: $Method $Url" -ForegroundColor Gray

    try {
        $params = @{
            Uri = $Url
            Method = $Method
            TimeoutSec = 10
            UseBasicParsing = $true
        }

        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json)
            $params.ContentType = "application/json"
        }

        $response = Invoke-WebRequest @params

        # Convert response content to string to ensure proper matching
        $content = if ($response.Content -is [byte[]]) {
            [System.Text.Encoding]::UTF8.GetString($response.Content)
        } else {
            $response.Content.ToString()
        }

        if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 300) {
            if ($ExpectedContent -and $content -notmatch [regex]::Escape($ExpectedContent)) {
                Write-Error "  FAIL: Response doesn't contain expected content '$ExpectedContent'"
                $script:TestsFailed++
                $script:TestResults += @{
                    Name = $Name
                    Status = "FAIL"
                    Reason = "Expected content not found"
                }
            } else {
                Write-Success "  PASS: $($response.StatusCode) $($response.StatusDescription)"
                $script:TestsPassed++
                $script:TestResults += @{
                    Name = $Name
                    Status = "PASS"
                    Reason = ""
                }
            }
        } else {
            Write-Error "  FAIL: Unexpected status code $($response.StatusCode)"
            $script:TestsFailed++
            $script:TestResults += @{
                Name = $Name
                Status = "FAIL"
                Reason = "Status $($response.StatusCode)"
            }
        }
    } catch {
        Write-Error "  FAIL: $($_.Exception.Message)"
        $script:TestsFailed++
        $script:TestResults += @{
            Name = $Name
            Status = "FAIL"
            Reason = $_.Exception.Message
        }
    }
}

Write-Host @"

WeatherSpring - Deployment Test
================================
Environment: $Environment
Base URL: $BaseUrl

"@ -ForegroundColor Cyan

# Determine base URL if not provided
if (-not $BaseUrl) {
    switch ($Environment) {
        "local" {
            $BaseUrl = "http://localhost:8080"
            Write-Info "Testing local deployment at $BaseUrl"
        }
        "docker" {
            $BaseUrl = "http://localhost:8080"
            Write-Info "Testing Docker deployment at $BaseUrl"
        }
        "minikube" {
            # Try to get service URL
            try {
                $svcOutput = kubectl get svc weatherspring -o jsonpath='{.status.loadBalancer.ingress[0].ip}:{.spec.ports[0].port}' 2>$null
                if ($svcOutput) {
                    $BaseUrl = "http://$svcOutput"
                } else {
                    $BaseUrl = "http://127.0.0.1:8080"
                }
                Write-Info "Testing Minikube deployment at $BaseUrl"
            } catch {
                Write-Warning "Could not determine Minikube service URL, using default"
                $BaseUrl = "http://127.0.0.1:8080"
            }
        }
    }
}

Write-Host ""
Write-Info "Starting test suite..."
Write-Host ("=" * 60) -ForegroundColor Cyan

# Health and Monitoring Tests
Write-Host "`n=== Health & Monitoring ===" -ForegroundColor Yellow
Test-Endpoint -Name "Health Check" -Url "$BaseUrl/actuator/health" -ExpectedContent "UP"
Test-Endpoint -Name "Application Info" -Url "$BaseUrl/actuator/info"
Test-Endpoint -Name "Prometheus Metrics" -Url "$BaseUrl/actuator/prometheus" -ExpectedContent "jvm_"

# API Documentation Tests
Write-Host "`n=== API Documentation ===" -ForegroundColor Yellow
Test-Endpoint -Name "Swagger UI" -Url "$BaseUrl/swagger-ui.html" -ExpectedContent "Swagger"
Test-Endpoint -Name "OpenAPI Spec" -Url "$BaseUrl/api-docs" -ExpectedContent "openapi"

# Location API Tests
Write-Host "`n=== Location API ===" -ForegroundColor Yellow
Test-Endpoint -Name "Get All Locations" -Url "$BaseUrl/api/locations"

# Check if London already exists
Write-Info "`n[7] Checking for existing London location"
$script:locationId = $null
$script:createdNewLocation = $false
try {
    $searchResponse = Invoke-RestMethod -Uri "$BaseUrl/api/locations/search?name=London" -TimeoutSec 10
    if ($searchResponse -and $searchResponse.Count -gt 0) {
        $script:locationId = $searchResponse[0].id
        Write-Warning "  Location 'London' already exists (ID: $($script:locationId)), using existing"
    }
} catch {
    # Location doesn't exist, will create it
}

# Create location if it doesn't exist
if (-not $locationId) {
    $londonLocation = @{
        name = "London"
        country = "UK"
        latitude = 51.5074
        longitude = -0.1278
        region = "England"
    }

    Write-Info "[7] Testing: Create Location (London)"
    Write-Host "  URL: POST $BaseUrl/api/locations" -ForegroundColor Gray

    try {
        $createResponse = Invoke-RestMethod -Uri "$BaseUrl/api/locations" `
            -Method POST `
            -ContentType "application/json" `
            -Body ($londonLocation | ConvertTo-Json) `
            -TimeoutSec 10

        if ($createResponse.id) {
            $script:locationId = $createResponse.id
            $script:createdNewLocation = $true
            Write-Success "  PASS: 201 Created (Location ID: $($script:locationId))"
            $script:TestsPassed++
            $script:TestResults += @{
                Name = "Create Location (London)"
                Status = "PASS"
                Reason = ""
            }
        } else {
            Write-Error "  FAIL: Location created but no ID returned"
            $script:TestsFailed++
            $script:TestResults += @{
                Name = "Create Location (London)"
                Status = "FAIL"
                Reason = "No ID in response"
            }
        }
    } catch {
        Write-Error "  FAIL: $($_.Exception.Message)"
        $script:TestsFailed++
        $script:TestResults += @{
            Name = "Create Location (London)"
            Status = "FAIL"
            Reason = $_.Exception.Message
        }
    }
} else {
    # Using existing location, count as passed
    Write-Success "  PASS: Using existing location (ID: $($script:locationId))"
    $script:TestsPassed++
    $script:TestResults += @{
        Name = "Create Location (London)"
        Status = "PASS"
        Reason = "Used existing"
    }
}

# Get all locations again
Test-Endpoint -Name "Get All Locations (After Create)" -Url "$BaseUrl/api/locations" -ExpectedContent "London"

# Search locations
Test-Endpoint -Name "Search Locations by Name" -Url "$BaseUrl/api/locations/search?name=London" -ExpectedContent "London"

# Weather API Tests
Write-Host "`n=== Weather API ===" -ForegroundColor Yellow
Test-Endpoint -Name "Get Current Weather (by name)" -Url "$BaseUrl/api/weather/current?location=London&save=true" -ExpectedContent "temperature"

# Wait a moment for location to be processed
Start-Sleep -Seconds 2

# Try to get weather by location ID (use captured ID)
if ($locationId) {
    $weatherByIdUrl = "$BaseUrl/api/weather/current/location/$locationId" + "?save=false"
    Test-Endpoint -Name "Get Current Weather (by ID)" -Url $weatherByIdUrl
} else {
    Write-Warning "  Skipping weather by ID test (no location ID available)"
}

# Forecast API Tests
Write-Host "`n=== Forecast API ===" -ForegroundColor Yellow
Test-Endpoint -Name "Get Forecast (3 days)" -Url "$BaseUrl/api/forecast?location=London&days=3&save=true"

# Environment-Specific Tests
if ($Environment -eq "minikube") {
    Write-Host "`n=== Kubernetes-Specific Tests ===" -ForegroundColor Yellow

    # Check pod status
    Write-Info "`nChecking pod status..."
    try {
        $pods = kubectl get pods -l app.kubernetes.io/name=weatherspring -o jsonpath='{.items[*].metadata.name}' 2>$null
        if ($pods) {
            Write-Success "  PASS: Pod(s) running: $pods"
            $TestsPassed++
            $TestResults += @{
                Name = "Pod Status"
                Status = "PASS"
                Reason = ""
            }
        } else {
            Write-Error "  FAIL: No pods found"
            $TestsFailed++
            $TestResults += @{
                Name = "Pod Status"
                Status = "FAIL"
                Reason = "No pods found"
            }
        }
    } catch {
        Write-Error "  FAIL: $($_.Exception.Message)"
        $TestsFailed++
        $TestResults += @{
            Name = "Pod Status"
            Status = "FAIL"
            Reason = $_.Exception.Message
        }
    }

    # Check service
    Write-Info "`nChecking service..."
    try {
        $svc = kubectl get svc weatherspring -o jsonpath='{.metadata.name}' 2>$null
        if ($svc) {
            Write-Success "  PASS: Service exists: $svc"
            $TestsPassed++
            $TestResults += @{
                Name = "Service Status"
                Status = "PASS"
                Reason = ""
            }
        } else {
            Write-Error "  FAIL: Service not found"
            $TestsFailed++
            $TestResults += @{
                Name = "Service Status"
                Status = "FAIL"
                Reason = "Service not found"
            }
        }
    } catch {
        Write-Error "  FAIL: $($_.Exception.Message)"
        $TestsFailed++
        $TestResults += @{
            Name = "Service Status"
            Status = "FAIL"
            Reason = $_.Exception.Message
        }
    }

    # Check Helm release
    Write-Info "`nChecking Helm release..."
    try {
        $release = helm list -q | Select-String "^weatherspring$"
        if ($release) {
            Write-Success "  PASS: Helm release found"
            $TestsPassed++
            $TestResults += @{
                Name = "Helm Release"
                Status = "PASS"
                Reason = ""
            }
        } else {
            Write-Error "  FAIL: Helm release not found"
            $TestsFailed++
            $TestResults += @{
                Name = "Helm Release"
                Status = "FAIL"
                Reason = "Release not found"
            }
        }
    } catch {
        Write-Error "  FAIL: $($_.Exception.Message)"
        $TestsFailed++
        $TestResults += @{
            Name = "Helm Release"
            Status = "FAIL"
            Reason = $_.Exception.Message
        }
    }
}

if ($Environment -eq "docker") {
    Write-Host "`n=== Docker-Specific Tests ===" -ForegroundColor Yellow

    # Check container status
    Write-Info "`nChecking Docker container..."
    try {
        $container = docker ps --filter "name=weather-microservice" --format "{{.Names}}"
        if ($container) {
            Write-Success "  PASS: Container running: $container"
            $TestsPassed++
            $TestResults += @{
                Name = "Container Status"
                Status = "PASS"
                Reason = ""
            }
        } else {
            Write-Error "  FAIL: No container running"
            $TestsFailed++
            $TestResults += @{
                Name = "Container Status"
                Status = "FAIL"
                Reason = "No container found"
            }
        }
    } catch {
        Write-Error "  FAIL: $($_.Exception.Message)"
        $TestsFailed++
        $TestResults += @{
            Name = "Container Status"
            Status = "FAIL"
            Reason = $_.Exception.Message
        }
    }
}

# Cleanup test data
Write-Host "`n=== Cleanup ===" -ForegroundColor Yellow
Write-Info "Cleaning up test data..."
if ($locationId -and $createdNewLocation) {
    try {
        Invoke-WebRequest -Uri "$BaseUrl/api/locations/$locationId" -Method DELETE -UseBasicParsing -ErrorAction SilentlyContinue | Out-Null
        Write-Success "  Test data cleaned up (Location ID: $locationId)"
    } catch {
        Write-Warning "  Could not cleanup test data (Location ID: $locationId)"
    }
} elseif ($locationId -and -not $createdNewLocation) {
    Write-Info "  Skipping cleanup (using existing location)"
} else {
    Write-Warning "  No location ID to cleanup"
}

# Summary
Write-Host "`n" + ("=" * 60) -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host ("=" * 60) -ForegroundColor Cyan
Write-Host "  Environment: $Environment"
Write-Host "  Base URL: $BaseUrl"
Write-Host "  Total tests: $($TestsPassed + $TestsFailed)"
Write-Success "  Passed: $TestsPassed"
if ($TestsFailed -gt 0) {
    Write-Error "  Failed: $TestsFailed"
} else {
    Write-Success "  Failed: 0"
}
Write-Host ""

# Show failed tests if any
if ($TestsFailed -gt 0) {
    Write-Host "Failed Tests:" -ForegroundColor Red
    foreach ($result in $TestResults) {
        if ($result.Status -eq "FAIL") {
            Write-Host "  - $($result.Name): $($result.Reason)" -ForegroundColor Red
        }
    }
    Write-Host ""
}

# Exit with appropriate code
if ($TestsFailed -gt 0) {
    Write-Host "Some tests failed!" -ForegroundColor Red
    exit 1
} else {
    Write-Host "All tests passed!" -ForegroundColor Green
    exit 0
}

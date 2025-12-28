# Deployment Scripts

PowerShell scripts for deploying and testing WeatherSpring.

## deploy-minikube.ps1

Deploys the application to Minikube with full automation.

**Usage:**
```powershell
.\scripts\deploy-minikube.ps1
```

**Parameters:**
- `-ApiKey` - Weather API key (optional, will prompt if not set)
- `-SkipTests` - Skip the test suite after deployment

**What it does:**
1. Checks prerequisites (Docker, Minikube, kubectl, Helm)
2. Starts Minikube if needed
3. Builds Docker image
4. Loads image into Minikube
5. Deploys with Helm
6. Starts minikube tunnel (requires administrator)
7. Runs basic tests
8. Opens Swagger UI

**Examples:**
```powershell
# Quick start (will prompt for API key)
.\scripts\deploy-minikube.ps1

# With API key
.\scripts\deploy-minikube.ps1 -ApiKey YOUR_API_KEY

# Skip tests
.\scripts\deploy-minikube.ps1 -SkipTests

# From environment variable
$env:WEATHER_API_KEY="YOUR_API_KEY"
.\scripts\deploy-minikube.ps1
```

## test-deployment.ps1

Tests the application on different environments.

**Usage:**
```powershell
.\scripts\test-deployment.ps1 -Environment <local|docker|minikube>
```

**Parameters:**
- `-Environment` - Environment to test (local, docker, or minikube)
- `-BaseUrl` - Custom base URL (optional, auto-detected if not set)

**What it tests:**
- Health and monitoring endpoints
- API documentation (Swagger, OpenAPI)
- Location API (CRUD operations)
- Weather API (current weather)
- Forecast API
- Environment-specific checks:
  - Minikube: Pod status, service status, Helm release
  - Docker: Container status
  - Local: Application availability

**Examples:**
```powershell
# Test local development server
mvn spring-boot:run
.\scripts\test-deployment.ps1 -Environment local

# Test Docker deployment
docker-compose up -d
.\scripts\test-deployment.ps1 -Environment docker

# Test Minikube deployment
.\scripts\deploy-minikube.ps1
.\scripts\test-deployment.ps1 -Environment minikube

# Custom URL
.\scripts\test-deployment.ps1 -Environment local -BaseUrl http://localhost:8081
```

## Requirements

- PowerShell 5.1 or later
- For Minikube: Docker Desktop, Minikube, kubectl, Helm
- For Docker: Docker Desktop
- For Local: Java 25, Maven
- Weather API key from https://www.weatherapi.com/

## Notes

- The minikube tunnel requires administrator privileges
- Keep the tunnel window open while using Minikube
- Test scripts will create and delete test data (location named "London")

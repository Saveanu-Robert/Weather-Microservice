# Weather Microservice

[![Java](https://img.shields.io/badge/Java-25%20LTS-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A production-ready RESTful microservice for weather data management built with Spring Boot 3.5.7 and Java 25. Integrates with WeatherAPI.com to fetch current weather and forecasts, with intelligent caching, persistence, and fault tolerance.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Running Modes](#running-modes)
  - [Local Development](#local-development)
  - [Docker](#docker)
  - [Kubernetes (Minikube)](#kubernetes-minikube)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Monitoring & Observability](#monitoring--observability)
- [CI/CD & Automation](#cicd--automation)
- [Project Structure](#project-structure)
- [Development Guide](#development-guide)
- [Troubleshooting](#troubleshooting)
- [License](#license)

---

## Features

### Core Functionality
- **Location Management** - Full CRUD operations with search, pagination, and validation
- **Current Weather** - Real-time weather data with automatic caching (5min TTL)
- **Weather Forecasts** - 1-14 day forecasts with historical storage (1hr TTL)
- **Weather History** - Query historical weather data with date range filters
- **Async Bulk Operations** - Process multiple locations concurrently using CompletableFuture

### Architecture & Patterns
- **Virtual Threads** - Handles 10,000+ concurrent requests (Java 25 Project Loom)
- **Circuit Breaker** - Resilience4j fault tolerance, automatic recovery (15s wait)
- **Rate Limiting** - 30 requests/minute protection for external API calls
- **Retry Logic** - Exponential backoff (500ms → 1s → 2s, max 3 attempts)
- **Distributed Tracing** - Zipkin integration for request flow visualization
- **Caching** - Caffeine in-memory cache (90% reduction in API calls)

### Quality & Security
- **Authentication** - Spring Security with role-based access control
- **Validation** - Bean validation with custom constraints
- **Error Handling** - Global exception handling with detailed error responses
- **Code Quality** - Checkstyle + Spotless enforcement (0 violations)
- **Test Coverage** - 183 tests, 80%+ coverage (JaCoCo verified)
- **Metrics** - Prometheus-compatible metrics with custom business metrics

---

## Technology Stack

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **Language** | Java | 25 LTS | Virtual threads, records, pattern matching |
| **Framework** | Spring Boot | 3.5.7 | Application framework |
| **Database** | H2 / PostgreSQL | Latest | Development / Production |
| **Cache** | Caffeine | 3.2.3 | High-performance in-memory cache |
| **Resilience** | Resilience4j | 2.2.0 | Circuit breaker, retry, rate limiter |
| **API Docs** | SpringDoc OpenAPI | 2.8.14 | Interactive Swagger UI |
| **Metrics** | Micrometer + Prometheus | - | Application monitoring |
| **Tracing** | Zipkin | Latest | Distributed tracing |
| **Testing** | JUnit 5 + Mockito | Latest | Unit & integration tests |
| **Build** | Maven | 3.8+ | Dependency management & build |
| **Container** | Docker + Kubernetes | Latest | Containerization & orchestration |

---

## Prerequisites

### Required
- **Java 25 LTS** (or Java 21 LTS minimum)
- **Maven 3.8+**
- **WeatherAPI.com API Key** - Free at https://www.weatherapi.com/signup.aspx

### Optional (for container deployment)
- **Docker Desktop** - For Docker and Kubernetes
- **Minikube** - For local Kubernetes cluster
- **kubectl** - Kubernetes CLI
- **Helm 3.0+** - Kubernetes package manager

---

## Quick Start

### 1. Get Your API Key

Sign up at https://www.weatherapi.com/signup.aspx and copy your API key.

### 2. Set Environment Variable

**Windows (PowerShell):**
```powershell
$env:WEATHER_API_KEY="your-api-key-here"
```

**Windows (Command Prompt):**
```cmd
set WEATHER_API_KEY=your-api-key-here
```

**Linux/Mac:**
```bash
export WEATHER_API_KEY=your-api-key-here
```

### 3. Build & Run

```bash
# Build (runs 183 tests, ~15-20 seconds)
mvn clean install

# Run
mvn spring-boot:run
```

### 4. Access the Application

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **H2 Console:** http://localhost:8080/h2-console
- **Health Check:** http://localhost:8080/actuator/health (requires auth: `actuator:actuator123`)

### 5. Try the API

```bash
# Get current weather (public)
curl "http://localhost:8080/api/weather/current?location=London"

# Create a location (requires user auth)
curl -X POST http://localhost:8080/api/locations \
  -u user:user123 \
  -H "Content-Type: application/json" \
  -d '{"name":"London","country":"UK","latitude":51.5074,"longitude":-0.1278}'

# Get 5-day forecast (public)
curl "http://localhost:8080/api/forecast?location=London&days=5&save=true"
```

### 6. Stop the Application

**Graceful shutdown via Ctrl+C** (recommended):
```bash
# Press Ctrl+C in the terminal running mvn spring-boot:run
```

**Or via shutdown endpoint:**
```bash
curl -X POST -u actuator:actuator123 http://localhost:8080/actuator/shutdown
```

Both methods ensure:
- Active requests complete (30s grace period)
- Database connections close properly
- No port conflicts on restart

---

## Running Modes

### Local Development

Best for: Active development, debugging, hot reload

**Start:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Features enabled:**
- DEBUG logging
- H2 console at `/h2-console`
- SQL query logging
- Hot reload with spring-boot-devtools
- In-memory H2 database

**Configuration:** `src/main/resources/application-dev.yml`

---

### Docker

Best for: Testing production build, container validation

#### Option 1: Docker Compose (Recommended)

**Setup:**
```bash
# Create .env file
cat > .env << EOF
WEATHER_API_KEY=your-api-key-here
EOF
```

**Start:**
```bash
docker-compose up --build
```

**Access:**
- Application: http://localhost:8080/swagger-ui.html
- Zipkin Tracing: http://localhost:9411

**Stop:**
```bash
docker-compose down
```

**Includes:**
- Weather service with persistent storage
- Zipkin for distributed tracing
- Automatic health checks
- Network isolation

#### Option 2: Docker CLI

**Build:**
```bash
docker build -t weather-service:latest .
```

**Run:**
```bash
docker run -d \
  -p 8080:8080 \
  -e WEATHER_API_KEY=your-api-key-here \
  -e SPRING_PROFILES_ACTIVE=prod \
  --name weather-service \
  weather-service:latest
```

**Logs:**
```bash
docker logs -f weather-service
```

**Stop:**
```bash
docker stop weather-service
docker rm weather-service
```

---

### Kubernetes (Minikube)

Best for: Production-like environment, autoscaling, high availability

#### Prerequisites

**Windows (PowerShell with Chocolatey):**
```powershell
choco install docker-desktop minikube kubernetes-cli kubernetes-helm
```

**Mac (Homebrew):**
```bash
brew install docker minikube kubectl helm
```

**Linux:**
```bash
# Docker
curl -fsSL https://get.docker.com -o get-docker.sh && sh get-docker.sh

# kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install kubectl /usr/local/bin/kubectl

# Minikube
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# Helm
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
```

#### Quick Deploy (Automated)

**Windows PowerShell:**
```powershell
# Set API key
$env:WEATHER_API_KEY="your-api-key-here"

# Run deployment script
.\scripts\deploy-minikube.ps1
```

**What it does:**
1. Checks prerequisites (Docker, Minikube, kubectl, Helm)
2. Starts Minikube if needed
3. Builds and loads Docker image
4. Deploys with Helm
5. Starts minikube tunnel (requires admin)
6. Runs automated tests
7. Opens Swagger UI in browser

#### Manual Deploy

**1. Start Minikube:**
```bash
minikube start --cpus=4 --memory=8192
minikube addons enable metrics-server
```

**2. Configure Docker to use Minikube:**
```bash
# Windows PowerShell
& minikube -p minikube docker-env --shell powershell | Invoke-Expression

# Linux/Mac
eval $(minikube docker-env)
```

**3. Build image:**
```bash
docker build -t weather-service:latest .
```

**4. Deploy with Helm:**
```bash
helm install weatherspring helm/weatherspring \
  --set image.tag=latest \
  --set image.pullPolicy=Never \
  --set secrets.weatherApiKey=your-api-key-here \
  --set service.type=LoadBalancer
```

**5. Start tunnel (separate terminal, requires admin/sudo):**
```bash
# Windows: Run PowerShell as Administrator
minikube tunnel

# Linux/Mac
sudo minikube tunnel
```

**6. Access:**
```bash
# Check status
kubectl get pods
kubectl get svc weatherspring

# Access application
# http://localhost:8080/swagger-ui.html
```

#### Helm Chart Features

The Helm chart includes:

| Feature | Description |
|---------|-------------|
| **Autoscaling (HPA)** | 2-5 replicas based on 80% CPU usage |
| **Health Probes** | Startup, liveness, readiness checks |
| **Persistent Storage** | Optional 1Gi PVC for H2 database |
| **Network Policy** | Restricts ingress/egress traffic |
| **Pod Disruption Budget** | Ensures high availability during updates |
| **Security Context** | Non-root user, read-only filesystem |
| **Prometheus Metrics** | ServiceMonitor for automatic scraping |
| **Resource Limits** | Memory: 512Mi-1Gi, CPU: 250m-1000m |

**Configuration:**

```bash
# Custom resource limits
helm install weatherspring helm/weatherspring \
  --set resources.requests.memory=1Gi \
  --set resources.limits.memory=2Gi

# Enable persistence
helm install weatherspring helm/weatherspring \
  --set persistence.enabled=true \
  --set persistence.size=5Gi

# Enable autoscaling
helm install weatherspring helm/weatherspring \
  --set autoscaling.enabled=true \
  --set autoscaling.minReplicas=3 \
  --set autoscaling.maxReplicas=10
```

See `helm/weatherspring/values.yaml` for all configuration options.

#### Testing Deployment

**Automated tests:**
```powershell
# Basic test suite (12 tests, ~15 seconds)
.\scripts\test-deployment.ps1 -Environment minikube

# Custom base URL
.\scripts\test-deployment.ps1 -Environment minikube -BaseUrl http://localhost:8080
```

**Manual verification:**
```bash
# Check pod status
kubectl get pods -l app.kubernetes.io/name=weatherspring

# View logs
kubectl logs -l app.kubernetes.io/name=weatherspring --tail=50 -f

# Check HPA
kubectl get hpa weatherspring

# Port forward (alternative to tunnel)
kubectl port-forward svc/weatherspring 8080:8080
```

#### Cleanup

```bash
# Uninstall application
helm uninstall weatherspring

# Stop Minikube
minikube stop

# Delete cluster
minikube delete
```

---

## Configuration

### Environment Variables

#### Required

| Variable | Description | Example |
|----------|-------------|---------|
| `WEATHER_API_KEY` | WeatherAPI.com API key | `abc123def456` |

#### Optional

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | HTTP server port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active profile (dev/test/prod) | `dev` |
| `DATABASE_URL` | Database JDBC URL | `jdbc:h2:file:./data/weatherdb` |
| `DATABASE_USERNAME` | Database username | `sa` |
| `DATABASE_PASSWORD` | Database password | (empty) |
| `CACHE_CURRENT_WEATHER_TTL` | Weather cache TTL (seconds) | `300` |
| `CACHE_FORECAST_TTL` | Forecast cache TTL (seconds) | `3600` |
| `TRACING_SAMPLE_RATE` | Zipkin sampling rate (0.0-1.0) | `0.1` |
| `ZIPKIN_URL` | Zipkin endpoint URL | `http://localhost:9411/api/v2/spans` |

### Spring Profiles

| Profile | Database | Logging | H2 Console | Use Case |
|---------|----------|---------|------------|----------|
| `dev` | H2 (in-memory) | DEBUG | Enabled | Local development |
| `test` | H2 (in-memory) | INFO | Disabled | Automated testing |
| `prod` | H2 (file) or PostgreSQL | INFO | Disabled | Production |

**Activate profile:**
```bash
# Via Maven
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Via JAR
java -jar target/weather-service-1.0.0.jar --spring.profiles.active=prod

# Via environment variable
export SPRING_PROFILES_ACTIVE=prod
```

### Production Database (PostgreSQL)

**1. Start PostgreSQL:**
```bash
docker run -d \
  -e POSTGRES_DB=weatherspring \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=your_password \
  -p 5432:5432 \
  postgres:15-alpine
```

**2. Configure environment:**
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/weatherspring
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=your_password
export SPRING_PROFILES_ACTIVE=prod
```

**3. Run:**
```bash
mvn spring-boot:run
```

Flyway migrations will automatically create the schema.

### Cache Configuration

Uses Caffeine for high-performance caching:

| Cache Name | TTL | Max Size | Purpose |
|------------|-----|----------|---------|
| `currentWeather` | 5 minutes | 1000 | Current weather responses |
| `forecasts` | 1 hour | 500 | Forecast responses |
| `locations` | 15 minutes | 1000 | Location data |

Reduces external API calls by ~90%, staying within free tier limits.

### Resilience Configuration

#### Circuit Breaker
- **Failure threshold:** 50% (last 10 calls)
- **Wait duration:** 15 seconds
- **Permitted calls (half-open):** 5
- **Slow call threshold:** 5 seconds

#### Rate Limiter
- **Limit:** 30 requests/minute per instance
- **Timeout:** 5 seconds
- **Scope:** External API calls only

#### Retry
- **Max attempts:** 3
- **Wait duration:** 500ms (exponential backoff)
- **Retry on:** IOException, TimeoutException

---

## API Documentation

### Interactive Documentation

**Swagger UI:**
```
http://localhost:8080/swagger-ui.html
```

**OpenAPI Specification:**
```
http://localhost:8080/v3/api-docs
```

### Authentication

**Default Credentials (development only):**

| User | Password | Roles | Permissions |
|------|----------|-------|-------------|
| `user` | `user123` | USER | Create/Update operations |
| `admin` | `admin123` | ADMIN | Delete operations |
| `actuator` | `actuator123` | ACTUATOR_ADMIN | Monitoring endpoints |

**Security Model:**
- `GET /api/**` - Public (no authentication required)
- `POST/PUT /api/**` - Requires USER role
- `DELETE /api/**` - Requires ADMIN role
- `/actuator/**` - Requires ACTUATOR_ADMIN role

### Main API Endpoints

#### Location Management

```bash
# Create location (requires auth)
curl -X POST http://localhost:8080/api/locations \
  -u user:user123 \
  -H "Content-Type: application/json" \
  -d '{"name":"London","country":"UK","latitude":51.5074,"longitude":-0.1278}'

# Get all locations (public)
curl "http://localhost:8080/api/locations?page=0&size=20"

# Search locations (public)
curl "http://localhost:8080/api/locations/search?name=London"

# Get location by ID (public)
curl "http://localhost:8080/api/locations/1"

# Update location (requires auth)
curl -X PUT http://localhost:8080/api/locations/1 \
  -u user:user123 \
  -H "Content-Type: application/json" \
  -d '{"name":"London","country":"United Kingdom","latitude":51.5074,"longitude":-0.1278}'

# Delete location (requires admin)
curl -X DELETE http://localhost:8080/api/locations/1 -u admin:admin123
```

#### Current Weather

```bash
# By location name (public)
curl "http://localhost:8080/api/weather/current?location=London&save=true"

# By location ID (public)
curl "http://localhost:8080/api/weather/current/location/1?save=true"

# Get weather history (public)
curl "http://localhost:8080/api/weather/history?locationId=1&startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59"
```

#### Forecasts

```bash
# By location name (public)
curl "http://localhost:8080/api/forecast?location=London&days=3&save=true"

# By location ID (public)
curl "http://localhost:8080/api/forecast/location/1?days=5&save=true"

# Get saved forecasts (public)
curl "http://localhost:8080/api/forecast/location/1/forecasts?page=0&size=10"
```

#### Async Bulk Operations

```bash
# Bulk weather refresh (requires auth)
curl -X POST http://localhost:8080/api/async/bulk/weather/refresh \
  -u user:user123 \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3]'

# Bulk forecast refresh (requires auth)
curl -X POST http://localhost:8080/api/async/bulk/forecast/refresh \
  -u user:user123 \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3]'
```

#### Composite Queries

```bash
# Get combined weather and forecast (public)
curl "http://localhost:8080/api/composite/location/1?forecastDays=7"
```

---

## Testing

### Unit & Integration Tests

**Run all tests:**
```bash
mvn test
```

**Output:** 183 tests, 100% pass rate, 80%+ code coverage

**Run specific test:**
```bash
mvn test -Dtest=LocationServiceTest
```

**Run integration tests only:**
```bash
mvn test -Dtest=*IntegrationTest
```

### Code Coverage

**Generate coverage report:**
```bash
mvn clean test jacoco:report
```

**View report:**
```
target/site/jacoco/index.html
```

**Coverage breakdown:**
- Overall: 80%+
- Service layer: 85%+
- Controller layer: 90%+
- Exception handling: 95%+

### Deployment Testing

Test running deployments with automated API tests:

**Basic test suite (12 tests, ~15 seconds):**
```powershell
# Local
.\scripts\test-deployment.ps1 -Environment local

# Docker
.\scripts\test-deployment.ps1 -Environment docker

# Minikube
.\scripts\test-deployment.ps1 -Environment minikube
```

**Tests cover:**
- Health and actuator endpoints
- API documentation availability
- Location CRUD operations
- Weather and forecast retrieval
- Environment-specific validation (pods, containers, services)

### Code Quality

**Run Checkstyle:**
```bash
mvn checkstyle:check
```

**Run Spotless:**
```bash
# Check formatting
mvn spotless:check

# Apply formatting
mvn spotless:apply
```

**Current status:** 0 violations

---

## Monitoring & Observability

### Actuator Endpoints

All actuator endpoints require authentication (`actuator:actuator123`):

```bash
# Health check
curl -u actuator:actuator123 http://localhost:8080/actuator/health

# Application info
curl -u actuator:actuator123 http://localhost:8080/actuator/info

# All metrics
curl -u actuator:actuator123 http://localhost:8080/actuator/metrics

# Prometheus metrics
curl -u actuator:actuator123 http://localhost:8080/actuator/prometheus

# Circuit breaker state
curl -u actuator:actuator123 http://localhost:8080/actuator/circuitbreakers

# Graceful shutdown
curl -X POST -u actuator:actuator123 http://localhost:8080/actuator/shutdown
```

### Custom Metrics

Available via Micrometer:

| Metric | Type | Description |
|--------|------|-------------|
| `location.created` | Counter | Location creation count |
| `location.updated` | Counter | Location update count |
| `location.deleted` | Counter | Location deletion count |
| `weather.fetch.success` | Counter | Successful weather fetches |
| `weather.fetch.failure` | Counter | Failed weather fetches |
| `forecast.fetch.success` | Counter | Successful forecast fetches |
| `forecast.fetch.failure` | Counter | Failed forecast fetches |
| `cache.hit` | Counter | Cache hit count |
| `cache.miss` | Counter | Cache miss count |

### Distributed Tracing (Zipkin)

**Start Zipkin:**
```bash
docker run -d -p 9411:9411 openzipkin/zipkin:latest
```

**Enable tracing:**
```bash
export TRACING_SAMPLE_RATE=1.0  # 100% of requests
mvn spring-boot:run
```

**View traces:**
```
http://localhost:9411
```

**Trace data includes:**
- API call latencies
- External API calls to WeatherAPI.com
- Circuit breaker operations
- Cache hits/misses
- Database queries

**Note:** Default sampling rate is 10% (`TRACING_SAMPLE_RATE=0.1`) to reduce overhead. Docker Compose and Kubernetes deployments include Zipkin automatically.

### Logging

**Log locations:**
- Console output (all profiles)
- `logs/application.log` - Rolling file, 10MB max, 30 days retention
- `logs/error.log` - Errors only, 90 days retention

**Log levels by profile:**
- `dev`: DEBUG for application, INFO for frameworks
- `test`: INFO
- `prod`: INFO with structured JSON format (Logstash)

**Example log output:**
```json
{
  "timestamp": "2024-12-29T10:30:00.123Z",
  "level": "INFO",
  "logger": "c.w.service.WeatherService",
  "message": "Fetching weather for London",
  "correlation_id": "abc-123-def",
  "thread": "virtual-1234"
}
```

---

## CI/CD & Automation

### GitHub Actions Workflows

#### 1. CI Pipeline (`.github/workflows/ci.yml`)

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main`

**Actions:**
- Build with Maven
- Run 183 tests
- Generate JaCoCo coverage report (80%+ required)
- Run Checkstyle and Spotless checks
- Upload artifacts (JAR, test results, coverage)
- Upload to Codecov

**Status:** All checks must pass before merge

#### 2. Dependency Updates (`.github/workflows/dependency-updates.yml`)

**Triggers:**
- Daily at 9:00 AM UTC
- Manual trigger via GitHub Actions UI

**Actions:**
- Check for Maven dependency and plugin updates
- Update to latest stable versions (no major version bumps)
- Verify build still works
- Create Pull Request with changes

**Features:**
- Uses `maven-version-rules.xml` to filter alpha/beta/RC versions
- Only suggests stable releases
- Auto-labels PRs: `dependencies`, `automated`

#### 3. Dependabot (`.github/dependabot.yml`)

**Schedule:** Weekly on Mondays at 9:00 AM UTC

**Manages:**
- Maven dependencies (grouped by category)
- GitHub Actions versions
- Docker base images

**Groups:**
- Spring Boot & Spring Framework
- Testing libraries (JUnit, Mockito, AssertJ)
- Resilience4j
- Lombok & MapStruct

**Auto-merge:** Patch and minor updates via `dependabot-auto-merge.yml` workflow

### Manual Dependency Management

**Check for updates:**
```bash
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
```

**Update specific dependency:**
```bash
mvn versions:use-latest-versions -Dincludes=groupId:artifactId
```

**Update all dependencies:**
```bash
mvn versions:use-latest-releases
```

**Version properties:** All versions centralized in `pom.xml` `<properties>` section for easy management.

---

## Project Structure

```
weather-service/
├── .github/
│   ├── workflows/              # CI/CD workflows
│   │   ├── ci.yml             # Build, test, coverage
│   │   ├── dependency-updates.yml  # Daily dependency checks
│   │   └── dependabot-auto-merge.yml  # Auto-merge Dependabot PRs
│   └── dependabot.yml         # Dependabot configuration
├── helm/weatherspring/        # Kubernetes Helm chart
│   ├── Chart.yaml
│   ├── values.yaml            # Default configuration
│   ├── values-minikube-windows.yaml  # Minikube settings
│   └── templates/             # K8s resource templates
│       ├── deployment.yaml
│       ├── service.yaml
│       ├── hpa.yaml           # Autoscaling
│       ├── ingress.yaml
│       ├── networkpolicy.yaml
│       ├── pvc.yaml           # Persistent storage
│       └── servicemonitor.yaml  # Prometheus metrics
├── scripts/
│   ├── deploy-minikube.ps1    # Automated Minikube deployment
│   └── test-deployment.ps1    # API testing script
├── src/
│   ├── main/
│   │   ├── java/com/weatherspring/
│   │   │   ├── annotation/    # Custom annotations (@Auditable, etc.)
│   │   │   ├── client/        # External API clients
│   │   │   │   └── WeatherApiClient.java
│   │   │   ├── config/        # Spring configuration
│   │   │   │   ├── AsyncConfig.java  # Virtual threads
│   │   │   │   ├── CacheConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── ...
│   │   │   ├── controller/    # REST endpoints
│   │   │   │   ├── LocationController.java
│   │   │   │   ├── WeatherController.java
│   │   │   │   ├── ForecastController.java
│   │   │   │   ├── AsyncBulkController.java
│   │   │   │   └── CompositeWeatherController.java
│   │   │   ├── dto/           # Data transfer objects
│   │   │   │   ├── LocationDto.java
│   │   │   │   ├── WeatherDto.java
│   │   │   │   ├── ForecastDto.java
│   │   │   │   └── external/  # External API DTOs
│   │   │   ├── exception/     # Exception handling
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── ...
│   │   │   ├── listener/      # JPA entity listeners
│   │   │   ├── mapper/        # Entity-DTO mappers
│   │   │   ├── model/         # JPA entities
│   │   │   │   ├── Location.java
│   │   │   │   ├── WeatherRecord.java
│   │   │   │   └── ForecastRecord.java
│   │   │   ├── repository/    # Spring Data JPA
│   │   │   ├── service/       # Business logic
│   │   │   │   ├── LocationService.java
│   │   │   │   ├── WeatherService.java
│   │   │   │   ├── ForecastService.java
│   │   │   │   ├── AsyncBulkWeatherService.java
│   │   │   │   └── ...
│   │   │   ├── util/          # Utilities
│   │   │   ├── validation/    # Custom validators
│   │   │   └── WeatherApplication.java  # Main class
│   │   └── resources/
│   │       ├── db/migration/  # Flyway migrations
│   │       │   └── V1__Initial_Schema.sql
│   │       ├── application.yml  # Main config
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       ├── application-prod.yml
│   │       └── logback-spring.xml
│   └── test/                  # Unit & integration tests
│       └── java/com/weatherspring/
│           ├── TestDataFactory.java
│           ├── architecture/  # ArchUnit tests
│           ├── client/
│           ├── controller/    # Integration tests
│           ├── service/       # Unit tests
│           └── ...
├── .env.example               # Environment variables template
├── .dockerignore
├── .gitignore
├── Dockerfile                 # Multi-stage build
├── docker-compose.yml         # Docker Compose config
├── pom.xml                    # Maven dependencies
├── maven-version-rules.xml    # Version filtering rules
├── checkstyle-suppressions.xml  # Checkstyle config
└── README.md                  # This file
```

---

## Development Guide

### Setting Up Development Environment

**1. Clone repository:**
```bash
git clone https://github.com/Saveanu-Robert/Weather-Microservice.git
cd Weather-Microservice
```

**2. Install dependencies:**
```bash
mvn dependency:go-offline
```

**3. Set up API key:**
```bash
export WEATHER_API_KEY=your-api-key-here
```

**4. Run in dev mode:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Code Style & Quality

**Before committing:**
```bash
# Format code
mvn spotless:apply

# Check style
mvn checkstyle:check

# Run tests
mvn test

# Full verification
mvn clean verify
```

**IDE Setup:**
- Import `checkstyle.xml` for style checking
- Enable Spotless plugin for auto-formatting
- Configure Lombok annotation processing

### Building Production JAR

```bash
# Build with all checks
mvn clean package

# Skip tests (not recommended)
mvn clean package -DskipTests
```

**Output:** `target/weather-service-1.0.0.jar`

**Run JAR:**
```bash
java -jar target/weather-service-1.0.0.jar
```

### Database Schema Changes

**1. Create new migration:**
```
src/main/resources/db/migration/V2__Description.sql
```

**2. Write SQL:**
```sql
ALTER TABLE locations ADD COLUMN timezone VARCHAR(50);
```

**3. Test:**
```bash
# Clean database
rm -rf data/

# Run application (Flyway applies migrations)
mvn spring-boot:run
```

### Adding New Dependencies

**1. Add to `pom.xml`:**
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>library</artifactId>
    <version>${library.version}</version>
</dependency>
```

**2. Add version property:**
```xml
<properties>
    <library.version>1.0.0</library.version>
</properties>
```

**3. Update dependency rules if needed:**
Edit `maven-version-rules.xml` to add filtering rules.

---

## Troubleshooting

### Common Issues

#### "Could not resolve placeholder 'WEATHER_API_KEY'"

**Cause:** Environment variable not set

**Solution:**
```bash
# Windows PowerShell
$env:WEATHER_API_KEY="your-api-key-here"

# Linux/Mac
export WEATHER_API_KEY=your-api-key-here
```

#### "Port 8080 already in use"

**Cause:** Another application using port 8080

**Solution 1:** Change port
```bash
export SERVER_PORT=8081
mvn spring-boot:run
```

**Solution 2:** Stop conflicting service
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

#### "Flyway migration error"

**Cause:** Database in inconsistent state

**Solution:** Delete database and restart
```bash
# Windows
rmdir /s /q data

# Linux/Mac
rm -rf data/
```

#### Docker build fails

**Cause:** Insufficient resources or disk space

**Solution:**
```bash
# Check Docker status
docker info

# Clean up
docker system prune -a

# Increase Docker resources (Docker Desktop → Settings → Resources)
```

#### Minikube tunnel requires admin/sudo

**Cause:** Tunnel needs elevated privileges to modify routing

**Solution:**
```bash
# Windows: Run PowerShell as Administrator
minikube tunnel

# Linux/Mac
sudo minikube tunnel
```

#### Tests failing with "Connection refused"

**Cause:** H2 database lock or port conflict

**Solution:**
```bash
# Clean database
rm -rf data/

# Kill any Java processes
# Windows
taskkill /F /IM java.exe

# Linux/Mac
pkill -9 java
```

#### Circuit breaker always OPEN

**Cause:** External API key invalid or rate limit exceeded

**Solution:**
1. Verify API key is correct
2. Check rate limits at https://www.weatherapi.com/my/
3. Wait 15 seconds for circuit breaker to attempt recovery
4. Check circuit breaker state:
   ```bash
   curl -u actuator:actuator123 http://localhost:8080/actuator/circuitbreakers
   ```

### Getting Help

- **Application logs:** `logs/application.log`
- **Error logs:** `logs/error.log`
- **Health check:** http://localhost:8080/actuator/health
- **GitHub Issues:** https://github.com/Saveanu-Robert/Weather-Microservice/issues

---

## License

This project is licensed under the MIT License.

---

## Author

**Robert Saveanu**
- GitHub: [@Saveanu-Robert](https://github.com/Saveanu-Robert)
- Repository: [Weather-Microservice](https://github.com/Saveanu-Robert/Weather-Microservice)

---

**Built with ❤️ using Spring Boot 3.5.7 and Java 25**

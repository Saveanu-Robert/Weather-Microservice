# Weather Microservice

[![Java](https://img.shields.io/badge/Java-25%20LTS-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A RESTful microservice for weather data management. Integrates with WeatherAPI.com to fetch current weather and forecasts, with local caching and persistence.

## Features

- **Location Management**: Full CRUD operations with search and pagination
- **Current Weather**: Real-time weather data with automatic caching
- **Weather Forecasts**: 1-14 day forecasts with historical storage
- **Async Bulk Operations**: Process multiple requests concurrently
- **Circuit Breaker**: Resilience4j fault tolerance for external APIs
- **Virtual Threads**: Massive concurrency with Java 25
- **Metrics & Monitoring**: Prometheus-compatible metrics
- **API Documentation**: Interactive Swagger UI

## Technology Stack

- **Java 25 LTS** - Latest LTS with virtual threads, records, pattern matching
- **Spring Boot 3.5.7** - Application framework
- **H2 Database** - Development (PostgreSQL support for production)
- **Caffeine** - High-performance caching
- **Resilience4j** - Circuit breaker, retry, rate limiting
- **Docker & Kubernetes** - Containerization and orchestration

---

## Prerequisites

- **Java 25 LTS** (or Java 21 LTS)
- **Maven 3.8+**
- **WeatherAPI.com API Key** (free at https://www.weatherapi.com/signup.aspx)
- **Docker** (optional, for containers)
- **Minikube** (optional, for Kubernetes)

---

## Quick Start - Running Locally

### 1. Get Your API Key

1. Sign up at https://www.weatherapi.com/signup.aspx
2. Copy your API key from the dashboard

### 2. Set Environment Variable

**Windows (Command Prompt):**
```cmd
set WEATHER_API_KEY=your-api-key-here
```

**Windows (PowerShell):**
```powershell
$env:WEATHER_API_KEY="your-api-key-here"
```

**Linux/Mac:**
```bash
export WEATHER_API_KEY=your-api-key-here
```

### 3. Build

```bash
mvn clean install
```

This compiles the code and runs 184 tests (takes ~15-20 seconds).

### 4. Run

```bash
mvn spring-boot:run
```

The application starts at http://localhost:8080

### 5. Test It

Open Swagger UI in your browser:
```
http://localhost:8080/swagger-ui.html
```

Try the API:
```bash
curl "http://localhost:8080/api/weather/current?location=London"
```

### 6. Stop the Application

To gracefully stop the running application:

**Option 1: Use Ctrl+C in the terminal**
```bash
# Press Ctrl+C in the terminal running mvn spring-boot:run
# This triggers graceful shutdown
```

**Option 2: Use the shutdown endpoint**
```bash
curl -X POST -u actuator:actuator123 http://localhost:8080/actuator/shutdown
```

Both methods ensure:
- All active requests complete (up to 30s grace period)
- Database connections close properly
- Resources are released cleanly
- No port conflicts or database locks on restart

---

## Running Tests

### Run All Tests
```bash
mvn test
```

**Output:** 184 tests, 100% pass rate, 80%+ code coverage

### Generate Coverage Report
```bash
mvn clean test jacoco:report
```

View report at: `target/site/jacoco/index.html`

### Test Specific Class
```bash
mvn test -Dtest=LocationServiceTest
```

---

## Docker Deployment

### Option 1: Docker Compose (Recommended)

1. Create `.env` file in project root:
```bash
WEATHER_API_KEY=your-api-key-here
```

2. Start with Docker Compose:
```bash
docker-compose up --build
```

3. Access at http://localhost:8080/swagger-ui.html

4. Stop:
```bash
docker-compose down
```

### Option 2: Docker Commands

Build image:
```bash
docker build -t weather-service:latest .
```

Run container:
```bash
docker run -d \
  -p 8080:8080 \
  -e WEATHER_API_KEY=your-api-key-here \
  --name weather-service \
  weather-service:latest
```

View logs:
```bash
docker logs -f weather-service
```

Stop container:
```bash
docker stop weather-service
docker rm weather-service
```

---

## Kubernetes (Minikube) Deployment

### Prerequisites

Install required tools:

**Windows (using Chocolatey):**
```powershell
choco install docker-desktop minikube kubernetes-cli kubernetes-helm
```

**Mac (using Homebrew):**
```bash
brew install docker minikube kubectl helm
```

**Linux:**
```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Install kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# Install minikube
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# Install Helm
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
```

### Deploy to Minikube

#### Quick Deploy (Windows PowerShell)

```powershell
# Set your API key
$env:WEATHER_API_KEY="your-api-key-here"

# Run deployment script
.\scripts\deploy-minikube.ps1
```

This script automatically:
1. Checks prerequisites
2. Starts Minikube if needed
3. Builds and loads Docker image
4. Deploys with Helm
5. Starts tunnel (requires admin)
6. Runs verification tests
7. Opens Swagger UI

#### Manual Deploy

1. **Start Minikube:**
```bash
minikube start
```

2. **Set Docker environment:**
```bash
# Windows PowerShell
& minikube -p minikube docker-env --shell powershell | Invoke-Expression

# Linux/Mac
eval $(minikube docker-env)
```

3. **Build image:**
```bash
docker build -t weather-service:latest .
```

4. **Create secret with API key:**
```bash
kubectl create secret generic weather-api-secret \
  --from-literal=WEATHER_API_KEY=your-api-key-here
```

5. **Deploy with Helm:**
```bash
helm install weatherspring helm/weatherspring \
  --set image.tag=latest \
  --set image.pullPolicy=Never
```

6. **Start tunnel (in separate terminal - requires admin/sudo):**
```bash
minikube tunnel
```

7. **Get service URL:**
```bash
kubectl get svc weatherspring
```

Access at http://localhost:8080/swagger-ui.html

### Verify Deployment

**Check pods:**
```bash
kubectl get pods
```

**Check logs:**
```bash
kubectl logs -l app=weatherspring -f
```

**Check services:**
```bash
kubectl get svc
```

**Run automated tests:**
```powershell
.\scripts\test-deployment.ps1 -Environment minikube
```

### Helm Chart Configuration

The Helm chart includes:
- **Autoscaling**: 2-5 replicas based on CPU usage
- **Health Probes**: Startup, liveness, and readiness checks
- **Persistent Storage**: For H2 database
- **Resource Limits**: Memory and CPU constraints
- **Security**: Non-root user, read-only filesystem
- **Metrics**: Prometheus ServiceMonitor

Configure values in `helm/weatherspring/values.yaml` or override:
```bash
helm install weatherspring helm/weatherspring \
  --set replicaCount=3 \
  --set resources.requests.memory=512Mi
```

See `helm/weatherspring/README.md` for full configuration options.

### Cleanup Minikube

```bash
# Uninstall Helm release
helm uninstall weatherspring

# Delete secret
kubectl delete secret weather-api-secret

# Stop Minikube
minikube stop

# Delete Minikube cluster
minikube delete
```

---

## API Documentation

### Swagger UI (Interactive)
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI Specification
```
http://localhost:8080/v3/api-docs
```

### Main Endpoints

**Default Credentials (development):**
- Regular User: `user:user123` (POST/PUT operations)
- Admin User: `admin:admin123` (DELETE operations)
- Actuator: `actuator:actuator123` (monitoring endpoints)

**Security:**
- GET `/api/**` - Public (no auth required)
- POST/PUT `/api/**` - Requires USER role
- DELETE `/api/**` - Requires ADMIN role
- `/actuator/**` - Requires ACTUATOR_ADMIN role

#### Location Management
```bash
# Create location (requires authentication)
curl -X POST http://localhost:8080/api/locations \
  -u user:user123 \
  -H "Content-Type: application/json" \
  -d '{"name":"London","country":"UK","latitude":51.5074,"longitude":-0.1278}'

# Get all locations (public)
curl http://localhost:8080/api/locations

# Search locations (public)
curl "http://localhost:8080/api/locations/search?name=London"

# Delete location (requires admin)
curl -X DELETE http://localhost:8080/api/locations/1 -u admin:admin123
```

#### Weather Data
```bash
# Get current weather (public)
curl "http://localhost:8080/api/weather/current?location=London&save=true"

# Get weather by location ID (public)
curl "http://localhost:8080/api/weather/current/location/1?save=true"
```

#### Forecasts
```bash
# Get 3-day forecast (public)
curl "http://localhost:8080/api/forecast?location=London&days=3&save=true"

# Get forecast by location ID (public)
curl "http://localhost:8080/api/forecast/location/1?days=5&save=true"
```

#### Health & Metrics
```bash
# Health check (requires actuator credentials)
curl -u actuator:actuator123 http://localhost:8080/actuator/health

# Prometheus metrics (requires actuator credentials)
curl -u actuator:actuator123 http://localhost:8080/actuator/prometheus

# Application info (requires actuator credentials)
curl -u actuator:actuator123 http://localhost:8080/actuator/info
```

---

## Configuration

### Environment Variables

#### Required
| Variable | Description | Example |
|----------|-------------|---------|
| `WEATHER_API_KEY` | API key for WeatherAPI.com | `abc123def456` |

#### Optional
| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | HTTP server port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active profile (dev/test/prod) | `dev` |
| `DATABASE_URL` | Database JDBC URL | `jdbc:h2:file:./data/weatherdb` |
| `CACHE_CURRENT_WEATHER_TTL` | Weather cache TTL (seconds) | `300` |
| `CACHE_FORECAST_TTL` | Forecast cache TTL (seconds) | `3600` |

### Spring Profiles

| Profile | Database | Logging | Use Case |
|---------|----------|---------|----------|
| `dev` | H2 (in-memory) | DEBUG | Local development |
| `test` | H2 (in-memory) | INFO | Automated testing |
| `prod` | H2 (file) or PostgreSQL | INFO | Production |

Activate profile:
```bash
# Via Maven
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Via JAR
java -jar target/weather-service-1.0.0.jar --spring.profiles.active=prod

# Via environment variable
export SPRING_PROFILES_ACTIVE=prod
```

### Production Database (PostgreSQL)

The application includes built-in PostgreSQL support.

1. **Start PostgreSQL:**
```bash
docker run -d \
  -e POSTGRES_DB=weatherspring \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=your_password \
  -p 5432:5432 \
  postgres:15-alpine
```

2. **Set environment variables:**
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/weatherspring
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=your_password
export SPRING_PROFILES_ACTIVE=prod
```

3. **Run application:**
```bash
mvn spring-boot:run
```

Flyway will automatically create the schema and run migrations.

---

## Database

### H2 Console (Development)

Access at: http://localhost:8080/h2-console

**Connection Settings:**
- JDBC URL: `jdbc:h2:file:./data/weatherdb` (prod) or `jdbc:h2:mem:weatherdb` (dev)
- Username: `sa`
- Password: (empty)

### Schema

Three main tables:
- `locations` - Geographical locations
- `weather_records` - Historical weather data
- `forecast_records` - Weather forecasts

Managed by Flyway migrations in `src/main/resources/db/migration/`

---

## Caching

Uses Caffeine for in-memory caching:

| Cache | TTL | Purpose |
|-------|-----|---------|
| `currentWeather` | 5 min | Current weather responses |
| `forecasts` | 1 hour | Forecast responses |
| `locations` | 15 min | Location data |

This reduces external API calls by ~90%, staying within the free tier limit.

---

## Resilience & Fault Tolerance

### Circuit Breaker

Prevents cascading failures:
- Opens at 50% failure rate (last 10 calls)
- Waits 15 seconds before retry
- Protects against API outages

### Retry Mechanism

Automatic retry with exponential backoff:
- Max 3 attempts
- Initial wait: 500ms
- Backoff: 500ms → 1s → 2s

### Rate Limiting

Protects API quotas:
- 50 calls per minute per instance
- With caching, easily stays within limits

---

## Monitoring & Observability

### Actuator Endpoints

All actuator endpoints require authentication with the actuator user (`actuator:actuator123`):

```bash
# Health check
curl -u actuator:actuator123 http://localhost:8080/actuator/health

# All metrics
curl -u actuator:actuator123 http://localhost:8080/actuator/metrics

# Prometheus metrics
curl -u actuator:actuator123 http://localhost:8080/actuator/prometheus

# Circuit breaker state
curl -u actuator:actuator123 http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state

# Graceful shutdown (stops the application)
curl -X POST -u actuator:actuator123 http://localhost:8080/actuator/shutdown
```

**Note:** The shutdown endpoint triggers graceful shutdown, which:
- Waits for active requests to complete (up to 30 seconds)
- Closes database connections properly
- Releases all resources cleanly
- Prevents database locking and port conflicts on restart

### Custom Metrics

Available via Micrometer:
- Location operations (create, update, delete)
- Weather fetches (success, failure counts)
- Forecast operations
- Cache hit/miss rates

### Logging

Logs written to:
- Console (all profiles)
- `logs/application.log` (rolling, 10MB max, 30 days)
- `logs/error.log` (errors only, 90 days)

### Distributed Tracing with Zipkin

The application supports distributed tracing with Zipkin for request flow visualization and performance analysis.

**Running Zipkin Locally:**

```bash
# Using Docker
docker run -d -p 9411:9411 --name zipkin openzipkin/zipkin:latest

# Verify Zipkin is running
curl http://localhost:9411/health
```

**Enable Tracing:**

```bash
# Set sampling rate (0.0 = disabled, 1.0 = 100% of requests)
export TRACING_SAMPLE_RATE=1.0

# Run the application
mvn spring-boot:run
```

**View Traces:**

Open Zipkin UI at http://localhost:9411 to visualize request traces, including:
- API call latencies
- Circuit breaker operations
- External API calls to WeatherAPI.com
- Cache operations

**Note:** Tracing is disabled by default (`TRACING_SAMPLE_RATE=0.0`) to avoid connection errors when Zipkin is not running. The Docker Compose and Kubernetes deployments automatically include Zipkin.

---

## Project Structure

```
weather-service/
├── src/
│   ├── main/
│   │   ├── java/com/weatherspring/
│   │   │   ├── client/         # External API clients
│   │   │   ├── config/         # Configuration
│   │   │   ├── controller/     # REST endpoints
│   │   │   ├── dto/            # Data transfer objects
│   │   │   ├── exception/      # Exception handling
│   │   │   ├── mapper/         # Entity-DTO mappers
│   │   │   ├── model/          # JPA entities
│   │   │   ├── repository/     # Data access
│   │   │   ├── service/        # Business logic
│   │   │   └── util/           # Utilities
│   │   └── resources/
│   │       ├── db/migration/   # Flyway migrations
│   │       ├── application.yml
│   │       └── application-*.yml
│   └── test/                   # Unit & integration tests
├── helm/weatherspring/         # Kubernetes Helm chart
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

---

## Troubleshooting

### "Could not resolve placeholder 'WEATHER_API_KEY'"

**Solution:** Set the `WEATHER_API_KEY` environment variable before starting the application.

```bash
# Windows
set WEATHER_API_KEY=your-api-key-here

# Linux/Mac
export WEATHER_API_KEY=your-api-key-here
```

### "Port 8080 already in use"

**Solution:** Change the port or stop the conflicting service.

```bash
# Change port (in application.yml or via environment)
export SERVER_PORT=8081
mvn spring-boot:run
```

### "Flyway migration error"

**Solution:** Delete the H2 database and let it recreate:

```bash
# Windows
rmdir /s /q data

# Linux/Mac
rm -rf data/
```

### Docker build fails

**Solution:** Ensure Docker is running and you have enough disk space:

```bash
# Check Docker status
docker info

# Clean up old images
docker system prune -a
```

### Minikube tunnel requires admin

**Solution:** Run the tunnel command as administrator/sudo:

```bash
# Windows PowerShell (run as Administrator)
minikube tunnel

# Linux/Mac
sudo minikube tunnel
```

---

## Development

### Run in Development Mode

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Features enabled:
- DEBUG logging
- H2 console at `/h2-console`
- SQL logging
- Hot reload (with spring-boot-devtools)

### Build Production JAR

```bash
mvn clean package -DskipTests
```

JAR location: `target/weather-service-1.0.0.jar`

Run JAR:
```bash
java -jar target/weather-service-1.0.0.jar
```

---

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test
```bash
mvn test -Dtest=LocationServiceTest
```

### Run Integration Tests Only
```bash
mvn test -Dtest=*IntegrationTest
```

### Coverage Report
```bash
mvn clean test jacoco:report
```

View at: `target/site/jacoco/index.html`

### Deployment Tests

Test the running application with automated API tests:

**Basic Deployment Test (12 tests, ~15 seconds):**
```powershell
.\scripts\test-deployment.ps1
```

**Comprehensive Test Suite (50+ tests, ~60 seconds):**
```powershell
.\scripts\test-deployment-comprehensive.ps1
```

The comprehensive test suite validates:
- All REST API endpoints (30+ endpoints across 5 controllers)
- Async and bulk operations with CompletableFuture
- Composite queries with parallel execution (virtual threads)
- Weather history and advanced forecast features
- Caching behavior and performance
- Circuit breaker monitoring
- Error handling and validation
- Security and authentication (401/403 responses)
- Correlation ID tracking

**Test Coverage:** 95% of available endpoints

**Skip slow tests:**
```powershell
.\scripts\test-deployment-comprehensive.ps1 -SkipSlowTests
```

**Test different environments:**
```powershell
# Docker
.\scripts\test-deployment-comprehensive.ps1 -Environment docker

# Kubernetes
.\scripts\test-deployment-comprehensive.ps1 -Environment minikube
```

See [scripts/TEST_COVERAGE.md](scripts/TEST_COVERAGE.md) for detailed test coverage information.

---

## CI/CD

GitHub Actions workflows:
- **CI Pipeline** (`.github/workflows/ci.yml`) - Build, test, coverage
- **Dependency Scanning** (`.github/workflows/dependency-check.yml`) - Security vulnerabilities
- **Dependabot** - Automated dependency updates

---

## Support

- **Issues:** [GitHub Issues](https://github.com/Saveanu-Robert/Weather-Microservice/issues)
- **API Docs:** http://localhost:8080/swagger-ui.html
- **Logs:** `logs/application.log`
- **Health:** http://localhost:8080/actuator/health

---

## Author

**Robert Saveanu**
- GitHub: [@Saveanu-Robert](https://github.com/Saveanu-Robert)

---

## License

This project is licensed under the MIT License.

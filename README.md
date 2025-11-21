# Weather Microservice

[![Build Status](https://github.com/Saveanu-Robert/Weather-Microservice/workflows/CI/badge.svg)](https://github.com/Saveanu-Robert/Weather-Microservice/actions)
[![codecov](https://codecov.io/gh/Saveanu-Robert/Weather-Microservice/graph/badge.svg)](https://codecov.io/gh/Saveanu-Robert/Weather-Microservice)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A production-ready RESTful microservice for weather data management with external API integration, intelligent caching, comprehensive testing, and adherence to enterprise Java best practices.

## Features

- **Complete CRUD Operations** for location management
- **Real-time Weather Data** via WeatherAPI.com integration
- **Multi-day Forecasts** (1-14 days) with persistent storage
- **Intelligent Caching** with Caffeine (separate TTLs per cache type)
- **Historical Weather Data** with pagination support
- **Production-ready Logging** with Logback and profile-specific configs
- **Database Migrations** using Flyway for schema versioning
- **Startup Validation** with automatic API key verification
- **OpenAPI Documentation** with Swagger UI
- **Health Checks** via Spring Boot Actuator
- **Comprehensive Testing** - 72 unit tests, 24 endpoint tests, high code coverage
- **Docker Support** with docker-compose
- **Immutable Entities & DTOs** following SOLID principles
- **CI/CD Ready** with GitHub Actions workflow

## Architecture

```
com.weatherspring
├── client/          # External API clients (WeatherAPI.com)
├── config/          # Application configuration (Cache, OpenAPI, REST client, Validation)
├── controller/      # REST controllers (Location, Weather, Forecast)
├── dto/             # Data Transfer Objects (immutable with @Value)
│   └── external/    # External API response DTOs
├── exception/       # Custom exceptions and global handler
├── mapper/          # Entity ↔ DTO mappers
├── model/           # JPA entities (immutable with builders)
├── repository/      # Spring Data JPA repositories
└── service/         # Business logic layer
```

## Technology Stack

### Core
- **Java 25** - Latest Java version with modern language features
- **Spring Boot 3.5.7** - Latest Spring Boot framework
- **Spring Data JPA** - Database access with Hibernate
- **H2 Database** - In-memory and file-based relational database
- **Flyway** - Database migration and schema versioning

### Caching & Performance
- **Caffeine** - High-performance caching library
  - Current Weather: 5 minutes TTL (300s)
  - Forecasts: 1 hour TTL (3600s)
  - Locations: 15 minutes TTL (900s)

### Documentation & Monitoring
- **SpringDoc OpenAPI** - API documentation (v2.8.14)
- **Swagger UI** - Interactive API explorer
- **Spring Boot Actuator** - Production-ready monitoring
- **Logback** - Logging framework with profile-specific configs
  - Separate error logs
  - Rolling file policy (10MB max, 30 days retention)
  - Async appenders for performance

### Testing
- **JUnit 5** (5.11.4) - Unit testing framework
- **Mockito** (5.17.0) - Mocking framework
- **AssertJ** (3.27.3) - Fluent assertions
- **JaCoCo** (0.8.14) - Code coverage analysis
- **Bash** - Comprehensive endpoint testing script

### External APIs
- **WeatherAPI.com** - Weather data provider (1M free calls/month)

### Build & DevOps
- **Maven** - Build automation
- **Docker** - Containerization
- **GitHub Actions** - CI/CD pipeline

## Prerequisites

- **Java 25** ([Download Java 25](https://jdk.java.net/25/) or [Java 21 LTS](https://adoptium.net/))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **WeatherAPI.com API Key** ([Get Free Key](https://www.weatherapi.com/signup.aspx))
- **Git** (optional, for version control)
- **Docker** (optional, for containerized deployment)

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/Saveanu-Robert/Weather-Microservice.git
cd Weather-Microservice
```

### 2. Configure API Key

Create `src/main/resources/application-local.yml`:

```yaml
weather:
  api:
    key: YOUR_API_KEY_HERE  # Get from https://www.weatherapi.com/
    base-url: https://api.weatherapi.com/v1
```

**Important:** Never commit your API key! The `application-local.yml` file is in `.gitignore`.

Alternatively, set an environment variable:

```bash
export WEATHER_API_KEY=your_api_key_here
```

### 3. Build the Project

```bash
mvn clean install
```

This will:
- Compile the application
- Run 72 unit and integration tests (100% passing)
- Generate JaCoCo coverage report
- Verify 80%+ code coverage requirement is met
- Package the application as an executable JAR

### 4. Run the Application

```bash
# Using Maven
mvn spring-boot:run

# Or using the JAR
java -jar target/weather-service-1.0.0.jar
```

The application will start on `http://localhost:8080`.

**Startup Process:**
1. Flyway migrations applied automatically
2. API key validated (application fails fast if invalid)
3. Caches initialized
4. Health checks active

## Running with Docker

### Using Docker Compose (Recommended)

```bash
# 1. Set your API key in .env file
echo "WEATHER_API_KEY=your_key_here" > .env

# 2. Build and start
docker-compose up --build

# 3. Access application
open http://localhost:8080/swagger-ui.html
```

### Manual Docker Build

```bash
# Build Docker image
docker build -t weather-microservice:latest .

# Run container
docker run -p 8080:8080 \
  -e WEATHER_API_KEY=your-api-key \
  -e SPRING_PROFILES_ACTIVE=prod \
  weather-microservice:latest
```

## Configuration

### Profiles

The application supports multiple profiles:

| Profile | Purpose | Database | Logging | DDL Mode |
|---------|---------|----------|---------|----------|
| **dev** | Development | In-memory H2 | DEBUG | validate |
| **test** | Testing | In-memory H2 | INFO (errors suppressed) | validate |
| **prod** | Production | File-based H2 | INFO (+ error file) | validate |

Activate a profile:

```bash
# Via Maven
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Via JAR
java -jar target/weather-service-1.0.0.jar --spring.profiles.active=prod

# Via environment variable
export SPRING_PROFILES_ACTIVE=prod
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `WEATHER_API_KEY` | WeatherAPI.com API key (required) | - |
| `WEATHER_API_BASE_URL` | Weather API base URL | `https://api.weatherapi.com/v1` |
| `CACHE_CURRENT_WEATHER_TTL` | Current weather cache TTL (seconds) | `300` |
| `CACHE_FORECAST_TTL` | Forecast cache TTL (seconds) | `3600` |
| `CACHE_LOCATION_TTL` | Location cache TTL (seconds) | `900` |
| `SERVER_PORT` | Application port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active profile (dev, test, prod) | `dev` |

## API Documentation

### Swagger UI

Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON

The raw OpenAPI specification is available at:
```
http://localhost:8080/v3/api-docs
```

## API Endpoints

### Location Management

```bash
# Create a location
curl -X POST http://localhost:8080/api/locations \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Paris",
    "country": "France",
    "latitude": 48.8566,
    "longitude": 2.3522,
    "region": "Île-de-France"
  }'

# Get all locations
curl http://localhost:8080/api/locations

# Get location by ID
curl http://localhost:8080/api/locations/1

# Search locations
curl "http://localhost:8080/api/locations/search?name=Paris"

# Update location
curl -X PUT http://localhost:8080/api/locations/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Paris",
    "country": "France",
    "latitude": 48.8566,
    "longitude": 2.3522,
    "region": "Updated Region"
  }'

# Delete location
curl -X DELETE http://localhost:8080/api/locations/1
```

### Weather Data

```bash
# Get current weather (by location name)
curl "http://localhost:8080/api/weather/current?location=Paris&save=true"

# Get current weather (by location ID)
curl "http://localhost:8080/api/weather/current/location/1?save=true"

# Get weather history (paginated)
curl "http://localhost:8080/api/weather/history/location/1?page=0&size=10"

# Get weather history by date range
curl "http://localhost:8080/api/weather/history/location/1/range?startDate=2025-01-01T00:00:00&endDate=2025-01-31T23:59:59"
```

### Forecasts

```bash
# Get forecast (by location name)
curl "http://localhost:8080/api/forecast?location=Paris&days=3&save=true"

# Get forecast (by location ID)
curl "http://localhost:8080/api/forecast/location/1?days=5&save=true"

# Get stored forecasts
curl "http://localhost:8080/api/forecast/stored/location/1"

# Get future forecasts
curl "http://localhost:8080/api/forecast/future/location/1"

# Get forecasts by date range
curl "http://localhost:8080/api/forecast/range/location/1?startDate=2025-01-15&endDate=2025-01-20"
```

### Health & Monitoring

```bash
# Health check
curl http://localhost:8080/actuator/health

# Application info
curl http://localhost:8080/actuator/info

# Metrics
curl http://localhost:8080/actuator/metrics

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

## Testing

### Run All Tests

```bash
mvn test
```

**Output:**
- 72 tests executed
- 100% pass rate
- High code coverage (see Codecov badge)

### Run Specific Test Class

```bash
mvn test -Dtest=LocationServiceTest
```

### Generate Coverage Report

```bash
mvn clean test jacoco:report
```

View the coverage report at `target/site/jacoco/index.html`.

### Endpoint Testing Script

A comprehensive bash script tests all 24 API endpoints:

```bash
# Make executable (Linux/Mac/WSL)
chmod +x test-endpoints.sh

# Run all endpoint tests
./test-endpoints.sh
```

**Test Coverage:**
- ✅ 8 Location endpoint tests
- ✅ 4 Weather endpoint tests
- ✅ 6 Forecast endpoint tests
- ✅ 3 Actuator endpoint tests
- ✅ 3 Delete operation tests
- **Total: 24 tests** (100% passing)

The script includes:
- Automatic cleanup after execution
- Color-coded pass/fail output
- Response validation
- Error handling verification

### Test Structure

```
src/test/java/com/weatherspring/
├── client/           # WeatherApiClient tests (9 tests)
├── controller/       # Integration tests (13 tests)
├── exception/        # Exception handler tests (6 tests)
├── mapper/           # Mapper tests (21 tests)
└── service/          # Service tests (23 tests)
```

**Coverage Details:**

See the [Codecov report](https://codecov.io/gh/Saveanu-Robert/Weather-Microservice) for detailed coverage breakdown by package and file.

## Database

### H2 Console (Development)

Access the H2 console at:
```
http://localhost:8080/h2-console
```

**Connection Settings:**
- **JDBC URL**: `jdbc:h2:file:./data/weatherdb` (prod) or `jdbc:h2:mem:weatherdb` (dev)
- **Username**: `sa`
- **Password**: (empty)

### Flyway Migrations

Database schema is managed with Flyway migrations:

```
src/main/resources/db/migration/
└── V1__Initial_Schema.sql  # Creates locations, weather_records, forecast_records
```

**Features:**
- Automatic migration on startup
- Versioned schema changes
- Rollback support
- Validation mode in production (ddl-auto: validate)

**Migration Details:**
- `locations` table with indexes on name, country
- `weather_records` table with composite index on location_id + timestamp
- `forecast_records` table with composite index on location_id + forecast_date
- Foreign key constraints with cascading deletes

### Schema Overview

| Table | Purpose | Key Indexes |
|-------|---------|-------------|
| `locations` | Store geographical locations | name, country, lat/lon |
| `weather_records` | Historical weather data | location_id, timestamp |
| `forecast_records` | Weather forecasts | location_id, forecast_date |

## Logging

Logs are written to:
- **Console** - All profiles (colored output in dev)
- **logs/application.log** - Rolling file (10MB max, 30 days retention, 1GB total cap)
- **logs/error.log** - Error-only logs (90 days retention, 500MB total cap)

### Log Levels by Profile

| Logger | Dev | Test | Prod |
|--------|-----|------|------|
| com.weatherspring | DEBUG | INFO | INFO |
| org.springframework.web | DEBUG | WARN | WARN |
| org.hibernate.SQL | DEBUG | WARN | WARN |
| org.hibernate.type | TRACE | WARN | WARN |

### Async Logging

The application uses async appenders for better performance:
- Queue size: 512
- Discarding threshold: 0 (no messages discarded)

### Example Log Output

```
2025-11-20 19:39:25.132 [main] INFO  c.w.WeatherApplication - Starting WeatherApplication
2025-11-20 19:39:25.456 [main] INFO  c.w.config.StartupValidator - Validating application configuration...
2025-11-20 19:39:25.789 [main] INFO  c.w.config.StartupValidator - Application configuration validation completed successfully
2025-11-20 19:39:26.123 [main] INFO  c.w.WeatherApplication - Started WeatherApplication in 2.456 seconds
```

## Caching Strategy

The application uses **Caffeine** for high-performance in-memory caching:

| Cache | TTL | Max Entries | Purpose |
|-------|-----|-------------|---------|
| `currentWeather` | 5 min | 500 | Real-time weather data |
| `forecasts` | 1 hour | 500 | Forecast predictions |
| `locations` | 15 min | 500 | Location metadata |

### Cache Benefits

- **Reduced API Calls**: ~90% reduction in external API requests
- **Faster Response Times**: <10ms for cached data vs ~200ms for API calls
- **Cost Optimization**: Stay within free tier limits (1M calls/month)
- **Automatic Eviction**: Cache invalidated on write operations

### Cache Configuration

See `com.weatherspring.config.CacheConfig` for implementation details.

## Error Handling

All errors return a consistent JSON structure:

```json
{
  "code": "ERROR_CODE",
  "message": "Human-readable error message",
  "timestamp": "2025-11-20T18:00:00Z"
}
```

### Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `LOCATION_NOT_FOUND` | 404 | Location doesn't exist |
| `WEATHER_DATA_NOT_FOUND` | 404 | No weather data available |
| `WEATHER_API_ERROR` | 503 | External API failure |
| `VALIDATION_ERROR` | 400 | Invalid request data |
| `INVALID_ARGUMENT` | 400 | Invalid parameter values |
| `TYPE_MISMATCH` | 400 | Parameter type error |
| `INTERNAL_SERVER_ERROR` | 500 | Unexpected server error |

### Global Exception Handler

See `com.weatherspring.exception.GlobalExceptionHandler` for implementation.

## Best Practices Implemented

This project follows enterprise Java development best practices:

### Code Quality

| Practice | Description |
|----------|-------------|
| ✅ SOLID Principles | Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion |
| ✅ Immutability | Entities and DTOs are immutable (no setters, use builders) |
| ✅ Clean Code | Small methods, meaningful names, guard clauses |
| ✅ Design Patterns | Factory, Builder, Strategy, Template Method |

### Architecture

| Practice | Description |
|----------|-------------|
| ✅ Layered Architecture | Clear separation: Controller → Service → Repository |
| ✅ Dependency Injection | Constructor injection throughout |
| ✅ Transaction Management | `@Transactional(readOnly=true)` optimization |
| ✅ Caching | Intelligent caching with separate TTLs |

### Documentation

| Practice | Description |
|----------|-------------|
| ✅ Javadoc | All classes and public methods documented |
| ✅ OpenAPI | Swagger annotations on all controllers |
| ✅ README | Comprehensive project documentation |
| ✅ Comments | Explain "why", not "what" |

### Testing

| Practice | Description |
|----------|-------------|
| ✅ High Coverage | Code coverage exceeds 80% requirement |
| ✅ Unit Tests | Service, mapper, client, exception tests |
| ✅ Integration Tests | Controller tests with MockMvc |
| ✅ Test Organization | AAA pattern (Arrange-Act-Assert) |

### Configuration

| Practice | Description |
|----------|-------------|
| ✅ Externalized Config | Profile-specific YAML files |
| ✅ Environment Variables | Secrets never committed |
| ✅ Profile Support | dev, test, prod environments |
| ✅ Startup Validation | Fail-fast on misconfiguration |

### Database

| Practice | Description |
|----------|-------------|
| ✅ Flyway Migrations | Versioned schema management |
| ✅ Proper Indexing | Composite indexes on frequently queried fields |
| ✅ Validation Mode | `ddl-auto: validate` in production |
| ✅ Transaction Annotations | Repository delete methods |

### Logging

| Practice | Description |
|----------|-------------|
| ✅ SLF4J + Logback | Structured logging throughout |
| ✅ Profile-Specific | Different log levels per environment |
| ✅ Async Appenders | Performance optimization |
| ✅ Separate Error Logs | Easy troubleshooting |

### Security

| Practice | Description |
|----------|-------------|
| ✅ Input Validation | `@Valid` on DTOs, defensive checks in services |
| ✅ No Secrets in Code | API keys via environment variables |
| ✅ Proper Exception Handling | No stack traces leaked to clients |
| ✅ CORS Configuration | Ready for frontend integration |

## Performance Optimization

- **Caching**: Reduces external API calls by ~90%
- **Connection Pooling**: HikariCP with optimized settings
- **Async Logging**: Non-blocking I/O for log writes
- **Database Indexing**: Composite indexes on frequently queried fields
- **Read-Only Transactions**: @Transactional(readOnly=true) optimization
- **Lazy Loading**: JPA relationships configured optimally

## Production Deployment

### Environment Variables

```bash
export SPRING_PROFILES_ACTIVE=prod
export WEATHER_API_KEY=your-production-api-key
export DATABASE_URL=jdbc:h2:file:/var/lib/weatherdb/data
export LOG_PATH=/var/log/weather-microservice
```

### Build Production JAR

```bash
mvn clean package -DskipTests
```

### Run in Production

```bash
java -jar target/weather-service-1.0.0.jar \
  --spring.profiles.active=prod \
  --server.port=8080
```

### Security Checklist

- ✅ API keys stored in environment variables
- ✅ HTTPS enabled (configure SSL certificates)
- ✅ CORS configured for your domain
- ✅ Health checks enabled
- ✅ Actuator endpoints secured (if public)
- ✅ Database password secured
- ✅ Log files protected (proper file permissions)
- ✅ Regular dependency updates

## CI/CD with GitHub Actions

The project includes a GitHub Actions workflow that:

1. **Builds** the application
2. **Runs** all 72 unit tests
3. **Verifies** code coverage meets 80% threshold
4. **Generates** test reports
5. **Caches** Maven dependencies

See `.github/workflows/ci.yml` for workflow details.

### Build Status

[![CI](https://github.com/Saveanu-Robert/Weather-Microservice/workflows/CI/badge.svg)](https://github.com/Saveanu-Robert/Weather-Microservice/actions)

## Troubleshooting

### Common Issues

**1. API Key Error**
```
Error: Weather API server error or Invalid API key
```
**Solution**: Verify `WEATHER_API_KEY` is set correctly. Get a free key at https://www.weatherapi.com/

**2. Port Already in Use**
```
Error: Web server failed to start. Port 8080 was already in use
```
**Solution**: Change port in `application.yml` (`server.port: 8081`) or stop conflicting service

**3. Flyway Migration Error**
```
Error: Validate failed: Migrations have failed validation
```
**Solution**: Delete database file (`rm -rf data/`) or run with `spring.flyway.clean-on-validation-error=true` (dev only)

**4. Tests Show Expected Errors**
```
ERROR logs during mvn test
```
**Solution**: Expected! Tests verify error handling. Errors are suppressed via `logback-test.xml`

**5. Coverage Below 80%**
```
WARNING: lines covered ratio is 0.XX, but expected minimum is 0.80
```
**Solution**: Run `mvn clean install` - coverage should exceed 80%

## Project Structure

```
WeatherSpring/
├── .github/
│   └── workflows/
│       └── ci.yml              # GitHub Actions CI/CD pipeline
├── src/
│   ├── main/
│   │   ├── java/com/weatherspring/
│   │   │   ├── client/         # External API clients
│   │   │   ├── config/         # Configuration (Cache, OpenAPI, Validation)
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── dto/            # DTOs (immutable)
│   │   │   │   └── external/   # External API DTOs
│   │   │   ├── exception/      # Custom exceptions + global handler
│   │   │   ├── mapper/         # Entity ↔ DTO mappers
│   │   │   ├── model/          # JPA entities (immutable)
│   │   │   ├── repository/     # Spring Data repositories
│   │   │   ├── service/        # Business logic
│   │   │   └── WeatherApplication.java
│   │   └── resources/
│   │       ├── db/migration/
│   │       │   └── V1__Initial_Schema.sql
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       ├── application-prod.yml
│   │       └── logback-spring.xml
│   └── test/
│       ├── java/com/weatherspring/
│       │   ├── client/         # Client unit tests
│       │   ├── controller/     # Integration tests
│       │   ├── exception/      # Exception handler tests
│       │   ├── mapper/         # Mapper tests
│       │   └── service/        # Service unit tests
│       └── resources/
│           └── logback-test.xml
├── docker-compose.yml          # Docker Compose configuration
├── Dockerfile                  # Docker image definition
├── test-endpoints.sh           # Comprehensive endpoint testing script
├── pom.xml                     # Maven build configuration
├── .gitignore                  # Git ignore patterns
└── README.md                   # This file
```

## API Rate Limits

**WeatherAPI.com Free Tier:**
- 1,000,000 calls/month
- ~32,000 calls/day
- No credit card required

**With Caching:**
- Effective rate: ~3,200 calls/day (90% reduction)
- Can handle ~10,000 unique location requests/day

Monitor usage: https://www.weatherapi.com/my/

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Follow the best practices outlined in this README
4. Write tests (maintain 80%+ coverage)
5. Update documentation
6. Commit changes (`git commit -m 'Add amazing feature'`)
7. Push to branch (`git push origin feature/amazing-feature`)
8. Create a Pull Request

### Development Guidelines

- Follow SOLID principles
- Maintain immutability (entities, DTOs)
- Write comprehensive Javadoc
- Add unit + integration tests
- Keep code coverage above 80%
- Use conventional commit messages
- Update README for new features

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [WeatherAPI.com](https://www.weatherapi.com/) for free weather data API
- [Spring Boot](https://spring.io/projects/spring-boot) for the excellent framework
- [Caffeine](https://github.com/ben-manes/caffeine) for high-performance caching
- [Flyway](https://flywaydb.org/) for database migration management

## Author

**Robert Saveanu**
- GitHub: [@Saveanu-Robert](https://github.com/Saveanu-Robert)
- Repository: [Weather-Microservice](https://github.com/Saveanu-Robert/Weather-Microservice)

## Support

For issues and questions:
- [Create an issue](https://github.com/Saveanu-Robert/Weather-Microservice/issues)
- Check API documentation at `/swagger-ui.html`
- Review logs in `logs/application.log`
- Check health status at `/actuator/health`

---

**Built with ☕ using Spring Boot 3.5.7 and Java 25**

**Production Ready ✅ | High Test Coverage ✅ | All Tests Passing ✅ | Docker Ready ✅**

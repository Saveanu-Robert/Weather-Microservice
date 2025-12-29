# Multi-stage build for optimal image size
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

# Copy pom.xml and configuration files
COPY pom.xml .
COPY checkstyle-suppressions.xml .
COPY maven-version-rules.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:25-jre-alpine

# Application port (can be overridden at build time)
ARG APP_PORT=8080

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create logs and data directories with proper ownership
RUN mkdir -p /app/logs /data && chown -R spring:spring /app/logs /data

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE ${APP_PORT}

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -q -O - http://localhost:${APP_PORT}/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

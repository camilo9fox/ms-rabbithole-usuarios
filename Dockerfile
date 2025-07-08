# ---------------------------------------------------------------------------
# Build stage: compile the Spring Boot application with Maven
# ---------------------------------------------------------------------------
FROM maven:3.9-amazoncorretto-17 AS build
WORKDIR /app

# Copy Maven descriptor first and resolve dependencies (layer cache)
COPY pom.xml .
RUN mvn -q -B dependency:resolve dependency:resolve-plugins

# Copy sources and build fat-jar (skip tests for CI speed)
COPY src ./src
RUN mvn -q -B package -DskipTests

# ---------------------------------------------------------------------------
# Runtime stage: lightweight JRE image
# ---------------------------------------------------------------------------
FROM amazoncorretto:17-alpine
LABEL maintainer="Rabbit Hole DevOps"
WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy compiled jar
ARG JAR_FILE=/app/target/usuarios-0.0.1-SNAPSHOT.jar
COPY --from=build ${JAR_FILE} app.jar

# Copy Oracle wallet directory required by JDBC URL
COPY Wallet_C3CQ5Y7AELCSQGMU /app/Wallet_C3CQ5Y7AELCSQGMU

# Default JVM options (override via JVM_OPTS env)
ENV JVM_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

# Expose service port 8082 (overridable)
EXPOSE 8082

# Healthcheck (adjust path if actuator disabled)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s CMD curl -f http://localhost:${SERVER_PORT:-8082}/actuator/health || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar /app/app.jar --server.port=${SERVER_PORT:-8082}"]

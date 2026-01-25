# Stage 1: Build Frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend

# Use production mode and leverage cache
COPY frontend/package*.json ./
RUN npm ci || npm install

COPY frontend/ .
RUN npm run build

# Stage 2: Build Backend
FROM eclipse-temurin:21-jdk-jammy AS backend-builder
WORKDIR /app

# Optimize Maven cache
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

COPY src ./src
# Embed frontend in backend
COPY --from=frontend-builder /app/frontend/dist ./src/main/resources/static

RUN ./mvnw clean package -DskipTests -B

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-jammy AS runtime

# Metadata
LABEL maintainer="Docker Sentinel" \
      org.opencontainers.image.title="Aegis Patrimônio" \
      org.opencontainers.image.description="Sistema de Gestão de Ativos Patrimoniais" \
      org.opencontainers.image.vendor="Aegis" \
      org.opencontainers.image.version="1.0.0"

# Install necessary runtime tools (curl for healthcheck)
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring -m -s /bin/false spring

# Copy JAR from builder
COPY --from=backend-builder --chown=spring:spring /app/target/*.jar app.jar

# JVM Performance & Container optimizations
ENV JAVA_OPTS="-XX:+UseParallelGC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom" \
    SPRING_PROFILES_ACTIVE=prod

# Change to non-root user
USER spring

# Healthcheck using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

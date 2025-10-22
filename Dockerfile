# Multi-stage Dockerfile: build jar with Maven, then copy to a lightweight JRE image

# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace/app
COPY pom.xml mvnw .mvn/ ./
COPY src ./src
# Enable Maven wrapper execution permissions
RUN mvn -B -DskipTests package -DskipITs

# Run stage
FROM openjdk:21-slim
WORKDIR /app
# copy jar from build stage
COPY --from=build /workspace/app/target/aegispatrimonio-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
# Multi-stage Dockerfile: build jar with Maven, then copy to a lightweight JRE image

# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace/app
COPY pom.xml mvnw .mvn/ ./
COPY src ./src
# Enable Maven wrapper execution permissions
RUN mvn -B -DskipTests package -DskipITs

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
# Set safe JVM defaults for containers; can be overridden at runtime
ENV JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=25 -XX:MaxRAMPercentage=75 -XX:MaxMetaspaceSize=256m -XX:+UseStringDeduplication -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp -Dfile.encoding=UTF-8"
# copy jar from build stage
COPY --from=build /workspace/app/target/aegispatrimonio-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
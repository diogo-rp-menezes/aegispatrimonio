# Stage 1: Build Frontend
FROM node:20-alpine as frontend-build
WORKDIR /app/frontend

# Copy dependency definitions
COPY frontend/package*.json ./

# Install dependencies
RUN npm install

# Copy source code
COPY frontend/ .

# Build the application
RUN npm run build

# Stage 2: Build Backend
FROM openjdk:21-jdk-slim as backend-build
WORKDIR /app

# Copy Maven wrapper and configuration
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make wrapper executable (just in case) and download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Copy backend source code
COPY src ./src

# Copy built frontend assets to Spring Boot static resources
# This ensures the frontend is embedded in the JAR
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

# Build the application
RUN ./mvnw clean package -DskipTests

# Stage 3: Runtime Image
FROM openjdk:21-jdk-slim
WORKDIR /app

# Copy the built JAR from the backend stage
COPY --from=backend-build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

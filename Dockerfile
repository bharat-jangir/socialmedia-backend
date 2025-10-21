# Multi-stage build for better optimization
FROM openjdk:17-jdk-slim AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Production stage
FROM openjdk:17-jre-slim

# Set working directory
WORKDIR /app

# Copy the JAR file from builder stage
COPY --from=builder /app/target/bharat-social-media-backend-1.0.0.jar app.jar

# Expose port
EXPOSE 5000

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=production

# Run the application
CMD ["java", "-jar", "app.jar"]

# Use official OpenJDK 17 base image
FROM openjdk:17-jdk-slim

# Set work directory
WORKDIR /app

# Copy the jar file (built by Maven/Gradle) into the image
COPY target/*.jar app.jar

# Expose port (adjust to match your Spring Boot `server.port`)
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]

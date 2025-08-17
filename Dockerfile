# ====== Build stage ======
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy all sources (so Maven can see parent + modules)
COPY . .

# Build everything, skip tests
RUN mvn clean package -DskipTests

# ====== Runtime stage ======
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy only the final JAR from booking-service
COPY --from=builder /app/booking-service/target/booking-service-*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]

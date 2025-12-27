# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app

# Copy root pom
COPY pom.xml ./pom.xml

# Copy all modules
COPY movie-booking-commons ./movie-booking-commons
COPY booking-service ./booking-service
COPY seat-inventory-service ./seat-inventory-service
COPY payment-service ./payment-service

# Build only booking-service along with dependencies
RUN mvn clean package -pl payment-service -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY --from=build /app/payment-service/target/payment-service-0.0.1-SNAPSHOT.jar ./payment-service.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "payment-service.jar"]

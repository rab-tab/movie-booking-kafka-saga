# Stage 1: Build
FROM maven:3.9.3-eclipse-temurin-21 AS build
WORKDIR /app

# Copy root pom and required modules
COPY pom.xml .
COPY movie-booking-commons ./movie-booking-commons
COPY payment-service ./payment-service

# Build payment-service along with commons
RUN mvn clean package -pl movie-booking-commons,payment-service -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY --from=build /app/payment-service/target/payment-service-0.0.1-SNAPSHOT.jar ./payment-service.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","booking-service.jar"]

# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app

# Copy root pom and required modules
COPY pom.xml .
COPY movie-booking-commons ./movie-booking-commons
COPY booking-service ./booking-service

# Build booking-service along with commons
RUN mvn clean package -pl movie-booking-commons,booking-service -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY --from=build /app/booking-service/target/booking-service-0.0.1-SNAPSHOT.jar ./booking-service.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","booking-service.jar"]

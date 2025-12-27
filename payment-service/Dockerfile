# ---------- Stage 1: Build ----------
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app

# Copy commons and payment-service only
COPY movie-booking-commons ./movie-booking-commons
COPY payment-service ./payment-service

# 1️⃣ Install commons into local Maven repo
RUN mvn -f movie-booking-commons/pom.xml clean install -DskipTests

# 2️⃣ Build payment-service (will now resolve commons)
RUN mvn -f payment-service/pom.xml clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY --from=build \
  /app/payment-service/target/payment-service-0.0.1-SNAPSHOT.jar \
  app.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY target/seat-inventory-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]

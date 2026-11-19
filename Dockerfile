# Multi-stage Dockerfile optimized for OCI Always Free (AMD64 & ARM64 Ampere A1)
FROM maven:3.9.12-eclipse-temurin-21 AS builder
WORKDIR /build

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application
COPY src src
RUN mvn clean package -DskipTests -B

# Runtime Image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Run as non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

COPY --from=builder --chown=spring:spring /build/target/bus-reservation-0.0.1-SNAPSHOT.jar /app/app.jar

USER spring:spring

EXPOSE 8080 10000

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-XX:+UseSerialGC", "-Xss512k", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]

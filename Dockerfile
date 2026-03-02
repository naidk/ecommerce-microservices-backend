# Use Amazon Corretto 17 for build
FROM maven:3.9.6-amazoncorretto-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Use Eclipse Temurin for runtime
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install curl for health check
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

# Create a Java Truststore for Aiven CA
COPY aiven-ca.crt /app/aiven-ca.crt
RUN keytool -import -trustcacerts -alias aiven -file /app/aiven-ca.crt -keystore /app/client.truststore.jks -storepass secret -noprompt

EXPOSE 8080

# Health check so Elastic Beanstalk can detect when the app is ready
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Optimize memory for t3.micro / t3.small free-tier instances
ENTRYPOINT ["java", "-Xmx380m", "-Xms380m", "-XX:+UseSerialGC", "-Dspring.profiles.active=aws", "-jar", "app.jar"]

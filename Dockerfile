# Use Eclipse Temurin for runtime (lightweight)
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install curl for health check
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Copy the PRE-BUILT jar from the GitHub Actions build
# Note: GitHub Actions zip will include the target/ directory
COPY target/*.jar app.jar
COPY aiven-ca.crt /app/aiven-ca.crt

# Create a Java Truststore for Aiven CA
RUN keytool -import -trustcacerts -alias aiven -file /app/aiven-ca.crt -keystore /app/client.truststore.jks -storepass secret -noprompt

EXPOSE 8080

# Health check so Elastic Beanstalk can detect when the app is ready
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Optimize memory for t3.micro free-tier instances
ENTRYPOINT ["java", "-Xmx380m", "-Xms380m", "-XX:+UseSerialGC", "-Dspring.profiles.active=aws", "-jar", "app.jar"]

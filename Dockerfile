# Use Amazon Corretto 17 for build
FROM maven:3.9.6-amazoncorretto-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Use Amazon Corretto 17 for runtime
FROM amazoncorretto:17
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Copy and trust the provided Aiven CA certificate
COPY aiven-ca.crt /etc/pki/ca-trust/source/anchors/aiven-ca.crt

# Install ca-certificates and update trust store
RUN yum install -y ca-certificates && update-ca-trust

EXPOSE 8080
# Optimize memory: 380MB Heap + SerialGC (lower overhead)
ENTRYPOINT ["java", "-Xmx380m", "-Xms380m", "-XX:+UseSerialGC", "-jar", "app.jar"]

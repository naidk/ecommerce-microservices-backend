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
COPY --from=build /app/target/*.jar app.jar

# Explicitly trust Aiven CA
COPY aiven-ca.crt /usr/local/share/ca-certificates/aiven-ca.crt
RUN update-ca-certificates

EXPOSE 8080
# Optimize memory: 380MB Heap (Returning to safer limit)
ENTRYPOINT ["java", "-Xmx380m", "-Xms380m", "-XX:+UseSerialGC", "-jar", "app.jar"]

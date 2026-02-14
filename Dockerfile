# Use Amazon Corretto 17 for build
FROM maven:3.9.6-amazoncorretto-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Use Eclipse Temurin for runtime (Contains standard Root CAs for Aiven)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
# Optimize memory: 340MB Heap to leave room for Native Memory (512MB Total Limit)
ENTRYPOINT ["java", "-Xmx340m", "-Xms340m", "-XX:+UseSerialGC", "-jar", "app.jar"]

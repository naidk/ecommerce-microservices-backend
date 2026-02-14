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

# Create a Java Truststore for Aiven CA
# This explicitly creates a .jks file that Java understands perfectly.
COPY aiven-ca.crt /app/aiven-ca.crt
RUN keytool -import -trustcacerts -alias aiven -file /app/aiven-ca.crt -keystore /app/client.truststore.jks -storepass secret -noprompt

EXPOSE 8080
# Optimize memory: 380MB Heap (Returning to safer limit)
ENTRYPOINT ["java", "-Xmx380m", "-Xms380m", "-XX:+UseSerialGC", "-jar", "app.jar"]

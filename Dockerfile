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

# Install CA certificates (crucial for connecting to secure services like Aiven)
RUN yum install -y ca-certificates && update-ca-trust

EXPOSE 8080
ENTRYPOINT ["java", "-Xmx350m", "-Xms350m", "-jar", "app.jar"]

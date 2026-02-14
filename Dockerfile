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

# Install openssl and ca-certificates
RUN yum install -y openssl ca-certificates

# Manually trust Aiven Kafka certificate (fixes PKIX path building failed)
RUN openssl s_client -showcerts -connect kafka-3805ed0c-naidugudivada768-bb80.b.aivencloud.com:10560 -servername kafka-3805ed0c-naidugudivada768-bb80.b.aivencloud.com </dev/null 2>/dev/null | openssl x509 -outform PEM > /etc/pki/ca-trust/source/anchors/aiven.pem && update-ca-trust

EXPOSE 8080
# Optimize memory: 380MB Heap + SerialGC (lower overhead)
ENTRYPOINT ["java", "-Xmx380m", "-Xms380m", "-XX:+UseSerialGC", "-jar", "app.jar"]

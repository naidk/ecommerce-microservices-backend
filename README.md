# 🛒 E-commerce API Extension

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-green)
![Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Streaming-black)
![Postgres](https://img.shields.io/badge/PostgreSQL-Persistence-blue)
![Redis](https://img.shields.io/badge/Redis-Caching-red)
![Docker](https://img.shields.io/badge/Docker-Containerization-blue)

A robust, event-driven E-commerce backend built with **Spring Boot 3**, designed to handle high-concurrency scenarios, distributed data consistency, and fault tolerance.

**Extended and maintained by Naidu Gudivada.**

## 🚀 Project Overview

This project goes beyond standard CRUD operations to address real-world distributed system challenges. It implements a Layered Architecture to manage Products, Customers, Orders, and Payments, leveraging **Kafka** for asynchronous messaging and **Redis** for high-performance caching.

### 🌟 Key Architectural Highlights

*   **Clean Architecture & Separation of Concerns**: Enforced strict layer isolation (Controller, Service, Repository) to ensure maintainability and testability. Leveraged **Java Records** for immutable DTOs to prevent domain entity leakage, **MapStruct** for type-safe object mapping, and **Global Exception Handling** for consistent API error responses.
*   **Idempotency & Fault Tolerance**: Implemented a custom **Idempotency Key** mechanism for the Checkout API. This prevents duplicate charges and order creation if a client retries a request due to network timeouts, caching the JSON response for subsequent calls.
*   **Performance Optimization**: Strategic use of **Redis** via `@Cacheable` and `@CacheEvict` to reduce database load on high-read endpoints (e.g., Product Catalog).
*   **Concurrency Control & Inventory Safety**: Solved "Lost Update" race conditions in inventory management using **Pessimistic Locking** within the database transactions, ensuring stock is never oversold during high-traffic spikes.
*   **Resilient Payment Processing**: Engineered a robust integration with external payment gateways using `WebClient`. Implemented blocking logic to ensure payment confirmation before order finalization, with proper error handling for 4xx (Client) vs 5xx (Server) errors to prevent "poison pill" retry loops in Kafka consumers.

## 🔍 Observability & Logging

This project implements a multi-layered observability strategy to ensure production readiness:

*   **Spring Actuator**: Exposes operational information about the running application (health checks, metrics, info) to ensure the system is monitorable.
*   **AOP Controller Logging**: Utilizes **Aspect-Oriented Programming (AOP)** via a custom `LoggingAspect`. This automatically logs entry/exit points and execution time for all Controller methods, providing performance tracing without cluttering business code.
*   **Structured Business Logging**: Implements detailed `Slf4j` logging with strict level separation:
    *   `INFO`: High-level business events (e.g., "Order Placed", "Payment Confirmed").
    *   `WARN`: Business logic rejections (e.g., "Insufficient Stock", "Empty Cart").
    *   `ERROR`: System failures requiring intervention (e.g., External API downtime).

## 🛠 Tech Stack

*   **Language:** Java 17
*   **Framework:** Spring Boot 3.4.1 (Web, Data JPA, Validation, Actuator)
*   **Messaging:** Spring Kafka 3.3.1
*   **Database:** PostgreSQL 14 (with Flyway for migrations)
*   **Caching:** Redis 3.4.1
*   **Documentation:** SpringDoc OpenAPI (Swagger)
*   **Tooling:** Docker Compose, Lombok, MapStruct

## ⚡ Getting Started

### Prerequisites
*   Docker & Docker Compose installed

### Run the Application
The entire environment (API, Database, Kafka, Zookeeper, Redis) is containerized.

1.  Build and Start the services:
    ```bash
    docker compose up -d --build
    ```

3.  The API will be available at `http://localhost:8081`.

## 📚 API Documentation

Interactive API documentation is available via Swagger UI. You can test endpoints, generate payloads, and view response schemas directly in the browser.

👉 **[Access Swagger UI](http://localhost:8081/swagger-ui.html)**

## 🏗 System Flow

The system supports a complete E-commerce lifecycle with data consistency checks at every step:

1.  **Onboarding & Catalog**:
    *   **Register Customer**: `POST /api/customer` - Creates a user profile and initializes an empty shopping cart.
    *   **Register Product**: `POST /api/product` - Adds inventory to the catalog (cached in Redis).
2.  **Shopping Experience**:
    *   **Add to Cart**: `POST /api/shopping-cart/{customerId}` - Validates stock availability in real-time.
    *   Inventory is tentatively reserved or validated depending on the strategy.
3.  **Checkout (Idempotent)**:
    *   `POST /api/order/{customerId}` with header `Idempotency-Key`.
    *   System checks if key exists. If yes, returns cached response.
    *   If no, **Locks Inventory** -> **Creates Order** -> **Commits DB** -> **Sends "Clear Cart" Event** to Kafka.
4.  **Payment Processing (Async)**:
    *   Kafka Consumer picks up the `Payment` event.
    *   Service calls External Payment API (Simulated) with blocking confirmation.
    *   On success, updates Order Status to `PAID`.

## 🧪 Testing & Quality

*   **Unit Tests:** JUnit and Mockito for isolated logic testing.
*   **MVC Tests:** Comprehensive testing of controllers.
*   **Code Quality:** Strictly typed DTOs (Java Records), Custom Exception Handling (`@ControllerAdvice`), and MapStruct for clean Entity-DTO mapping.

## 🚀 CI/CD Pipeline

The project includes a robust **GitHub Actions** workflow (`.github/workflows/ci-cd.yml`) that automates:
1.  **Build Verification**: Ensures the project compiles and packages correctly using Maven.
2.  **Automated Testing**: Runs unit and integration tests in an ephemeral environment with Docker services (Postgres, Redis).
3.  **Docker Build**: Verifies that the Docker image builds successfully on every push and pull request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

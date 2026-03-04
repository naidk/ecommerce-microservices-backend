# 🏗️ E-Commerce API Backend: Comprehensive Technical Documentation

This document serves as the ultimate technical reference manual for the E-Commerce Spring Boot Extension project. It covers the architectural patterns, component interactions, deployment pipeline, and testing strategies.

---

## 1. System Architecture Overview

The system is built on a **Layered (n-tier) Architecture** designed for high modularity and separation of concerns.

### 1.1 Core Components
*   **Web Layer (Controllers)**: Handles HTTP requests, input validation, and HTTP responses.
*   **Business Layer (Services)**: Contains the core business rules and orchestration logic.
*   **Data Access Layer (Repositories)**: Manages interactions with the PostgreSQL database using Spring Data JPA.
*   **Event Layer (Kafka Producers/Consumers)**: Handles asynchronous communication between domains.

### 1.2 Technology Stack
*   **Platform**: Java 17
*   **Framework**: Spring Boot 3.4.1
*   **Database**: PostgreSQL 14 (Relational data, ACID compliance)
*   **Messaging**: Apache Kafka (Event streaming, pub/sub)
*   **Caching**: Redis (High-performance in-memory caching)
*   **Security**: Spring Security + JWT (JSON Web Tokens)
*   **Testing**: JUnit, Mockito, Newman (Postman CLI)
*   **Deployment**: AWS Elastic Beanstalk (Dockerized)

---

## 2. Domain Models & Use Cases

### 2.1 Auth & Customer Domain (`/api/auth`, `/api/customer`)
*   **Responsibilities**: User registration, authentication, role management, and customer profile management.
*   **Security**: Uses BCrypt for password hashing and JWT for stateless authentication.
*   **Key Flow**:
    1.  User registers (`POST /api/auth/register`).
    2.  `AuthService` creates the user with `ROLE_ADMIN` (for full system access during testing).
    3.  Throws `CustomerRegisteredEvent` to Kafka.
    4.  Initializes an empty `ShoppingCart` for the new user.

### 2.2 Product Catalog Domain (`/api/product`)
*   **Responsibilities**: Managing inventory, product details, and category queries.
*   **Optimization**: Heavily utilizes Redis caching (`@Cacheable`) because the catalog is read-heavy.
*   **Concurrency**: Uses Pessimistic Locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) during stock deduction to prevent "lost update" anomalies during high-traffic checkout scenarios.

### 2.3 Shopping Cart Domain (`/api/shopping-cart`)
*   **Responsibilities**: Managing temporary user selections before checkout.
*   **Logic**: Recalculates total prices dynamically.

### 2.4 Order & Checkout Domain (`/api/order`)
*   **Responsibilities**: Transforming a cart into a finalized order.
*   **Idempotency**: The `checkout` method requires an `Idempotency-Key` header. This ensures that if a user double-clicks the "Pay" button or experiences a network timeout, they aren't charged twice and duplicate orders aren't created.
*   **Transaction**:
    1.  Validates and locks inventory (Pessimistically).
    2.  Creates the Order record.
    3.  Publishes `OrderCreatedEvent` to Kafka.

### 2.5 Payment Processing Domain (`/api/payment`)
*   **Responsibilities**: Simulating interactions with an external Payment Gateway (e.g., Stripe, PayPal).
*   **Resilience**: Operates asynchronously. The `PaymentProcessor` consumes the `OrderCreatedEvent`, attempts a payment lock, and updates the order status to `PAID` or `FAILED`.

---

## 3. DevOps & CI/CD Pipeline

The project uses a robust, automated pipeline via **GitHub Actions** (`.github/workflows/deploy.yml`).

### 3.1 Pipeline Stages
1.  **Checkout**: Pulls the latest code from the `main` branch.
2.  **Package Setup**: Configures Java 17 and Maven.
3.  **Build**: Compiles the code and packages it into an executable JAR (`mvn clean package -DskipTests`).
4.  **Zip Generation**: Bundles the JAR, `Dockerfile`, `Dockerrun.aws.json`, and `.ebextensions/.platform` scripts into `deploy.zip`.
5.  **Deploy to AWS**: Uses the `einaregilsson/beanstalk-deploy` action to push the zip file to Elastic Beanstalk (`ecommerce-api-prod`).
6.  **URL Discovery**: A "Smart" shell step dynamically queries the AWS CLI to find the live CNAME of the newly deployed environment.
7.  **Automated Testing (Newman)**: Runs the Postman `automated_test_collection.json` against the live dynamically-discovered URL to verify the deployment didn't break core flows.

### 3.2 Elastic Beanstalk Configuration
*   **Platform**: Docker running on 64bit Amazon Linux 2023.
*   **Configuration Files**:
    *   `.platform/nginx/conf.d/elasticbeanstalk/00_application.conf`: Configures Nginx with extended timeouts (3600s) to prevent 504 Gateway Timeouts during cold starts or long requests.
    *   `.ebextensions/deploy_timeout.config`: Increases the AWS deployment timeout to 15 minutes (900s) to ensure Docker has enough time to build and start.
    *   `.ebignore`: Excludes everything except the compiled JAR and necessary config files, keeping the deployment payload extremely lean.

---

## 4. Testing Strategy

The system enforces quality through multiple layers of testing:

### 4.1 Automated API Tests (Newman)
*   **File**: `automated_test_collection.json`
*   **Execution**: Run automatically by GitHub Actions post-deployment, or manually using `.\test-live-api.ps1`.
*   **Coverage**:
    *   Dynamic user generation (creates a new user for every run to avoid conflicts).
    *   Extracts the `accessToken` and automatically applies it as a Bearer token for subsequent requests.
    *   Verifies Product Creation (Vendor/Admin).
    *   Verifies Add to Cart.
    *   Verifies Idempotent Checkout.
    *   Verifies Order status.

### 4.2 Manual Swagger Testing
*   **URL**: `http://<EB_URL>/swagger-ui/index.html`
*   Provides an interactive interface for developers and QA to test individual endpoints.

---

## 5. Environment Variables & Setup

To run locally using Docker Compose, the following variables must be present (or defaults will be used from `.env`/`docker-compose.yml`):

*   `SPRING_DATASOURCE_URL` (PostgreSQL URL)
*   `SPRING_DATASOURCE_USERNAME` / `PASSWORD`
*   `SPRING_KAFKA_BOOTSTRAP_SERVERS`
*   `SPRING_DATA_REDIS_HOST` / `PORT`
*   `JWT_SECRET` (Must be at least 256 bits)
*   `JWT_EXPIRATION`

*Generated by Architecture Assistant on 2026-03-04*

# Fare & Payment Service

**IT3130 – Application Development: Group Assignment**  
**Component:** Fare & Payment Service Microservice  
**Primary Owner:** Sanjeewa H.D.U.S (Student ID: IT24101590)  
**Status:** `FARE ESTIMATES IMPLEMENTED / PAYMENT FLOW NOT IMPLEMENTED`

---

## 1. Responsibilities
- Upfront fare estimation based on simulated distance, estimated duration, and base rules
- Final fare calculation upon trip completion using documented formula
- Simulated payment execution and transaction recording
- Payment status lifecycle (`PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`)
- Immutable receipt generation, invoice breakdown, and receipt retrieval

The fare rule is Rs. 150 base fare plus Rs. 100 per kilometer. Distances must be
positive, and calculated fares are rounded to two decimal places.

### Fare Estimate API

`POST /api/fares/estimate`

Request:
```json
{"distanceKilometers": 8}
```

Response:
```json
{"distanceKilometers": 8, "estimatedFare": 950.00, "currency": "LKR"}
```

## 2. Technology & Architecture
- **Language:** Java 17
- **Framework:** Spring Boot 3.3.4
- **Persistence:** PostgreSQL (`payment_db`) - guarantees ACID transactions and ledger-style immutability for receipts
- **API Documentation:** Springdoc OpenAPI / Swagger UI
- **Port:** `8084` (configurable via `PAYMENT_SERVICE_PORT`)
- **Swagger URL:** `http://localhost:8084/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8084/v3/api-docs`

## 3. Package Structure
```
src/main/java/com/ridelink/payment/
├── FarePaymentServiceApplication.java
├── config/          # OpenAPI and app configuration
├── controller/      # REST API endpoints for estimates, payments, receipts
├── dto/             # Request & Response Data Transfer Objects
├── domain/          # Core domain models, enums (PaymentStatus, Currency)
├── fare/            # Fare calculation logic, rates, estimation rules
├── payment/         # Payment simulation entities and records
├── receipt/         # Receipt generation and formatting models
├── entity/          # JPA entities mapped to payment_db
├── repository/      # Spring Data JPA repositories
├── service/         # Business service interfaces
├── validation/      # Input validations
├── exception/       # Custom exceptions & global handler
├── mapper/          # Entity-DTO mapping
└── integration/     # Outbound notification/event adapters
```

## 4. Database Isolation
- Dedicated database: `payment_db` (PostgreSQL)
- **Constraint:** Strictly isolated. No other microservice can query or modify `payment_db`. Trip completion and payment triggers are initiated via REST API calls from `Ride Management Service`.

## 5. Build and Run
```bash
# Build
mvn clean package -DskipTests

# Run independently
mvn spring-boot:run
```
*(Payment processing, persistence, and receipt retrieval remain to be implemented.)*

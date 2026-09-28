# Fare & Payment Service

**IT3130 – Application Development: Group Assignment**  
**Component:** Fare & Payment Service Microservice  
**Primary Owner:** Sanjeewa H.D.U.S (Student ID: IT24101590)  
**Status:** `FARE ESTIMATES, FINAL FARES, AND SIMULATED PAYMENTS IMPLEMENTED`

---

## 1. Responsibilities
- Upfront fare estimation based on simulated distance, estimated duration, and base rules
- Final fare calculation upon trip completion using documented formula
- Simulated payment execution and transaction recording
- Payment status (`SUCCESS` or `FAILED`)
- Transaction reference and payment retrieval

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

### Final Fare API

`POST /api/fares/final` calculates and stores one final fare per ride. The request
uses the ride service's stable ride ID; no cross-service database access is used.

```json
{"rideId": "<ride-uuid>", "distanceKilometers": 8}
```

`GET /api/fares/final/{rideId}` retrieves the persisted fare breakdown.

### Simulated Payment API

`POST /api/payments` records a payment attempt. Set `simulateFailure` to `true`
to persist a `FAILED` attempt; otherwise the simulated result is `SUCCESS`.

```json
{
	"rideId": "<ride-uuid>",
	"passengerId": "<passenger-uuid>",
	"amount": 950.00,
	"paymentMethod": "CARD",
	"simulateFailure": false
}
```

`GET /api/payments/{paymentId}` retrieves the stored result. Payments use
`payment_db`; Flyway creates the fare and payment tables at startup.

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
- Dedicated database: `payment_db` (PostgreSQL), migrated by Flyway.
- `fare_records` stores a final fare breakdown and unique ride ID.
- `payments` stores every simulated payment attempt, its status, transaction reference, and timestamps.
- Other services may provide stable identifiers through APIs but cannot query or modify this database.

The final-fare endpoint accepts a ride ID and distance after ride completion. The payment endpoint
stores `SUCCESS` by default or `FAILED` when `simulateFailure` is true. Failed attempts have no
`paidAt` timestamp; both outcomes remain retrievable by payment ID.

## 5. Build and Run
```bash
# Build
mvn clean package -DskipTests

# Run independently
mvn spring-boot:run
```
*(Receipt generation/retrieval and payment retries remain to be implemented.)*

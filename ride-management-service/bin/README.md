# Ride Management Service

**IT3130 – Application Development: Group Assignment**  
**Component:** Ride Management Service Microservice  
**Primary Owner:** Herath H M S R (Student ID: IT24103280)  
**Status:** `STRUCTURE READY / NOT IMPLEMENTED`

---

## 1. Responsibilities
- Ride request creation (passenger, pickup location, destination coordinates/name)
- Driver assignment reference handling
- Ride status state machine enforcement:
  - `REQUESTED`
  - `ASSIGNED`
  - `ACCEPTED`
  - `IN_PROGRESS`
  - `COMPLETED`
  - `CANCELLED`
- Ride history and status retrieval

## 2. Technology & Architecture
- **Language:** Java 17
- **Framework:** Spring Boot 3.3.4
- **Persistence:** PostgreSQL (`ride_db`) - ACID transactional guarantees for state transitions
- **API Documentation:** Springdoc OpenAPI / Swagger UI
- **Port:** `8083` (configurable via `RIDE_SERVICE_PORT`)
- **Swagger URL:** `http://localhost:8083/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8083/v3/api-docs`

## 3. Package Structure
```
src/main/java/com/ridelink/ride/
├── RideManagementServiceApplication.java
├── config/          # OpenAPI and HTTP Client configuration
├── controller/      # REST API endpoints for ride creation & transitions
├── dto/             # Request & Response Data Transfer Objects
├── domain/          # Ride domain models & RideStatus enum
├── entity/          # JPA entities mapped to ride_db
├── repository/      # Spring Data JPA repositories
├── service/         # Core ride coordination service
├── lifecycle/       # Ride state machine validator & transition rules
├── validation/      # Input validations
├── exception/       # Domain exceptions & error responses
├── mapper/          # Entity-DTO mapping
└── integration/     # Downstream REST adapters
    ├── account/     # Passenger verification client
    ├── driver/      # Eligible driver query & status notification client
    └── fare/        # Fare estimate & completion calculation client
```

## 4. Interservice Interactions & Database Isolation
- **Dedicated database:** `ride_db` (PostgreSQL)
- **Downstream HTTP integrations:**
  - `Driver & Vehicle Service` (`http://localhost:8082`): to fetch eligible drivers and mark drivers as busy/available.
  - `Fare & Payment Service` (`http://localhost:8084`): to trigger fare estimates and final payment processing upon completion.
  - `Account Service` (`http://localhost:8081`): to validate passenger account existence.
- **Constraint:** Zero direct cross-service database access. Cross-service entity relationships are strictly modeled using scalar IDs (`passengerId`, `driverId`, `fareId`).

## 5. Build and Run
```bash
# Build
mvn clean package -DskipTests

# Run independently
mvn spring-boot:run
```
*(Business logic not yet implemented; structure is prepared for subsequent phases)*

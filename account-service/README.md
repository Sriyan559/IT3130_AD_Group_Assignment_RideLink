# Account Service

**IT3130 – Application Development: Group Assignment**  
**Component:** Account Service Microservice  
**Primary Owner:** Fernando B S C (Student ID: IT24103775)  
**Status:** `STRUCTURE READY / NOT IMPLEMENTED`

---

## 1. Responsibilities
- Passenger and driver account registration
- Login and authentication token (JWT) issuance
- Role management (`PASSENGER`, `DRIVER`, `ADMIN`)
- User profile viewing and updating
- Account lifecycle and status management (`ACTIVE`, `SUSPENDED`, `DEACTIVATED`)

## 2. Technology & Architecture
- **Language:** Java 17
- **Framework:** Spring Boot 3.3.4
- **Persistence:** PostgreSQL (`account_db`)
- **API Documentation:** Springdoc OpenAPI / Swagger UI
- **Port:** `8081` (configurable via `ACCOUNT_SERVICE_PORT`)
- **Swagger URL:** `http://localhost:8081/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8081/v3/api-docs`

## 3. Package Structure
```
src/main/java/com/ridelink/account/
├── AccountServiceApplication.java
├── config/          # OpenAPI, Security, and App configurations
├── controller/      # REST API endpoints
├── dto/             # Request & Response Data Transfer Objects
├── domain/          # Core domain models and enums
├── entity/          # JPA entities mapped to account_db
├── repository/      # Spring Data JPA repositories
├── service/         # Service layer interfaces & implementations
├── security/        # JWT utilities, filters, password encoders
├── validation/      # Input validation constraints
├── exception/       # Custom exceptions & global exception handler
├── mapper/          # Entity-DTO mapping
└── integration/     # Downstream client contracts
```

## 4. Database Isolation
- Dedicated database: `account_db`
- **Constraint:** Cross-service database queries or joins are strictly forbidden. Other services must interact solely via REST APIs or stable identifiers.

## 5. Build and Run
```bash
# Build
mvn clean package -DskipTests

# Run independently
mvn spring-boot:run
```
*(Business logic not yet implemented; structure is prepared for subsequent phases)*

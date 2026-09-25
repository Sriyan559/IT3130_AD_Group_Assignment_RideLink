# Driver & Vehicle Service

**IT3130 – Application Development: Group Assignment**  
**Component:** Driver & Vehicle Service Microservice  
**Primary Owner:** Rathnakoon D A (Student ID: IT24300246)  
**Status:** `IN PROGRESS / DRIVER PROFILE CREATION IMPLEMENTED`

The first increment implements `POST /api/drivers`, MongoDB persistence, request validation,
duplicate account/licence protection and automated tests. See [the first API guide](docs/driver-profile-create.md).
The responsibilities below describe the full planned service; other endpoints and authentication integration remain pending.

---

## 1. Responsibilities
- Driver operational profile management (licensing, active status, contact details)
- Vehicle registration and attributes (make, model, license plate, vehicle class, capacity)
- Driver availability state management (`AVAILABLE`, `ON_TRIP`, `OFFLINE`)
- Operating service area definition
- Simulated current location updates (geospatial coordinates: latitude, longitude)
- Retrieval of eligible available drivers within a designated service zone or radius

## 2. Technology & Architecture
- **Language:** Java 17
- **Framework:** Spring Boot 3.3.4
- **Persistence:** MongoDB (`driver_db`) - leverages document flexibility for varying vehicle features and geospatial location coordinates
- **API Documentation:** Springdoc OpenAPI / Swagger UI
- **Port:** `8082` (configurable via `DRIVER_SERVICE_PORT`)
- **Swagger URL:** `http://localhost:8082/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8082/v3/api-docs`

## 3. Package Structure
```
src/main/java/com/ridelink/driver/
├── DriverVehicleServiceApplication.java
├── config/          # OpenAPI and MongoDB configuration
├── controller/      # REST endpoints for drivers, vehicles, and availability
├── dto/             # Request & Response Data Transfer Objects
├── domain/          # Core domain models and enums
├── document/        # Spring Data MongoDB document entities (driver_db)
├── entity/          # Model mapping layer
├── repository/      # Spring Data MongoDB repositories
├── service/         # Operational business logic interfaces
├── validation/      # Input validations (plate numbers, lat/lng limits)
├── exception/       # Custom exceptions & global handler
├── mapper/          # Document-to-DTO conversion
└── integration/     # Downstream client contracts
```

## 4. Database Isolation
- Dedicated database: `driver_db` (MongoDB)
- **Constraint:** Strictly isolated. Other services (such as Ride Management Service) must obtain driver availability via REST API calls, never by accessing `driver_db` directly.

## 5. Build and Run
```bash
# Build
mvn clean package -DskipTests

# Run independently
mvn spring-boot:run
```
*Driver profile creation is implemented. See the first API guide for verification commands and remaining work.*

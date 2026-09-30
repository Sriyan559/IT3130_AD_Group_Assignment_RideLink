# Ride Management Service API Specification

**Owner:** Herath H M S R (IT24103280)
**Base URL:** http://localhost:8083
**Swagger UI:** http://localhost:8083/swagger-ui/index.html
**OpenAPI Spec:** http://localhost:8083/v3/api-docs

### Planned Endpoints:
- POST /api/v1/rides (Create Ride Request)
- GET /api/v1/rides/{rideId} (Retrieve Ride Details)
- PATCH /api/v1/rides/{rideId}/status (Transition Lifecycle State)
- GET /api/v1/rides/passenger/{passengerId} (Passenger Ride History)

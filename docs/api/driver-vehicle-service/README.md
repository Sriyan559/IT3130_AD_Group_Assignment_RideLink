# Driver & Vehicle Service API Specification

**Owner:** Rathnakoon D A (IT24300246)
**Base URL:** http://localhost:8082
**Swagger UI:** http://localhost:8082/swagger-ui/index.html
**OpenAPI Spec:** http://localhost:8082/v3/api-docs

### Planned Endpoints:
- POST /api/v1/drivers (Create Operational Profile)
- POST /api/v1/drivers/{driverId}/vehicles (Register Vehicle)
- PUT /api/v1/drivers/{driverId}/availability (Toggle AVAILABLE/OFFLINE)
- PUT /api/v1/drivers/{driverId}/location (Update Simulated Coordinates)
- GET /api/v1/drivers/eligible (Query Available Drivers in Radius)

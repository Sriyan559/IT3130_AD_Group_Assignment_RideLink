# Driver & Vehicle Service API Specification

**Owner:** Rathnakoon D A (IT24300246)
**Base URL:** http://localhost:8082
**Swagger UI:** http://localhost:8082/swagger-ui/index.html
**OpenAPI Spec:** http://localhost:8082/v3/api-docs

### Implemented local endpoints

- POST /api/drivers (Create Operational Profile)
- GET /api/drivers/{driverId} (Retrieve Operational Profile)
- POST /api/drivers/{driverId}/vehicles (Register Vehicle)
- PUT /api/drivers/{driverId}/availability (Set AVAILABLE/OFFLINE; ON_TRIP drivers return 409)

- PUT /api/drivers/{driverId}/location (Update Simulated Coordinates)

See the [location contract and verification guide](../../../driver-vehicle-service/docs/driver-location.md).
See the [availability contract and verification guide](../../../driver-vehicle-service/docs/driver-availability.md).
Implemented endpoints consistently use `/api/drivers`. The earlier `/api/v1/drivers`
proposal is not implemented; a coordinated versioning decision remains pending before integration.
ON_TRIP is reserved for future ride operations, not a driver-selected availability value.

### Planned endpoints (routes subject to integration agreement)

- GET /api/v1/drivers/eligible (Query Available Drivers in Radius)

# First increment: create driver profile

Owner: Rathnakoon D A (IT24300246).

Implemented: `POST /api/drivers`, MongoDB persistence, validation, unique account/licence constraints, error responses and automated unit/HTTP contract tests.

This is an incremental local development endpoint. Authentication, role/ownership checks and validation of the referenced account against Account Service are not implemented yet. Do not treat it as a completed secured service. Vehicle, location, availability-update and driver-search APIs are subsequent increments.

## Run and test

Prerequisites: Java 17 or compatible JDK, Maven, MongoDB at `localhost:27017`.
From the repository root:

```powershell
mvn -pl driver-vehicle-service -am verify
java -jar driver-vehicle-service/target/driver-vehicle-service-1.0.0-SNAPSHOT.jar --server.address=127.0.0.1
```

Swagger: http://localhost:8082/swagger-ui/index.html

Tests use mocks and do not require MongoDB. Running the application requires MongoDB; startup ensures unique indexes on `drivers.accountId` and `drivers.licenseNumber`. Existing duplicate records will prevent successful index creation rather than silently weakening uniqueness.

## Try the endpoint

Send a JSON request to `http://localhost:8082/api/drivers`:

```json
{
  "accountId": "demo-account-001",
  "licenseNumber": "DEMO-LICENCE-001",
  "serviceArea": "Malabe"
}
```

Expect `201 Created` with a MongoDB-generated `id`, the submitted fields and `availabilityStatus: "OFFLINE"`. The example account is simulated, not a verified Account Service user.

All three fields are required. Maximum lengths: account ID 100, licence 50, area 100. Surrounding whitespace is removed; licence numbers are stored uppercase. The account ID remains case-sensitive and is an opaque identifier until the team agrees its contract.

Each account and normalized licence can belong to only one driver. Database unique indexes prevent concurrent duplicate requests from creating extra profiles. Repeating an account or licence returns `409 DRIVER_ALREADY_EXISTS`. Blank/overlong fields return `400 VALIDATION_ERROR`; missing or malformed JSON returns `400 INVALID_REQUEST`; database access failures return `503 DATABASE_UNAVAILABLE` without exposing database details.

No vehicle exists yet, so a new driver is always OFFLINE. The client cannot select availability through this creation request.

## Understand the code

`DriverController` accepts JSON and triggers DTO validation. `DriverService` normalizes data and sets OFFLINE. `DriverRepository` inserts a `Driver` document into `driver_db.drivers`. `DriverResponse` defines the public response independently of the stored document. `ApiExceptionHandler` supplies consistent error JSON.

Before integration, agree with the Account and Ride owners on account identifiers, token validation, role/ownership checks and eligibility rules.

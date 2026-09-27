# Update driver availability

Implemented: `PUT /api/drivers/{driverId}/availability`.
Use the driver ID returned by profile creation. Current local routes use `/api/drivers`;
versioned routes remain a future integration decision.

## Contract

```json
{"availabilityStatus":"AVAILABLE"}
```

- Accepts exactly `AVAILABLE` or `OFFLINE` (uppercase, no surrounding whitespace).
- Returns `200` with the full updated driver profile, including `availabilityStatus`.
- Repeating the same request returns `200` and leaves the requested status in place.
- Changes only availability; account ID, licence, service area and vehicles are preserved.
- Missing, null, blank, unknown or `ON_TRIP` targets return `400 VALIDATION_ERROR`.
  Malformed JSON or a missing body returns `400 INVALID_REQUEST`.
- Missing driver: `404 DRIVER_NOT_FOUND`. The update never creates a driver.
- Driver currently `ON_TRIP`: `409 DRIVER_ON_TRIP`. Trip transitions are reserved for
  future ride integration and cannot be made through this endpoint.
- Database failure: `503 DATABASE_UNAVAILABLE`, without internal database details.

Authentication, ownership checks and vehicle/eligibility requirements remain pending.
This increment allows an existing OFFLINE driver to become AVAILABLE without a vehicle.

## Try it locally

Start MongoDB and the service using the [creation guide](driver-profile-create.md),
then use an existing driver ID:

```powershell
$driverId = 'replace-with-created-driver-id'
$url = "http://localhost:8082/api/drivers/$driverId/availability"
Invoke-RestMethod -Method Put -Uri $url -ContentType 'application/json' `
    -Body '{"availabilityStatus":"AVAILABLE"}'
Invoke-RestMethod -Method Get -Uri "http://localhost:8082/api/drivers/$driverId"
Invoke-RestMethod -Method Put -Uri $url -ContentType 'application/json' `
    -Body '{"availabilityStatus":"OFFLINE"}'
```

Swagger UI: `http://localhost:8082/swagger-ui/index.html`.

## Implementation and verification

`DriverController` validates `UpdateAvailabilityRequest`, then delegates to
`DriverAvailabilityService`. MongoDB `findAndModify` matches the driver ID and a current
AVAILABLE/OFFLINE state, sets only `availabilityStatus`, and returns the updated document.
The state check is part of the atomic write. Concurrent toggle requests use last-write-wins;
any future trip assignment must also use a conditional write to coordinate state changes.
If nothing matches, an existence check distinguishes a missing profile from a state conflict.

Run the mock-based unit and HTTP contract tests from the repository root:

```powershell
mvn -B -pl driver-vehicle-service -am verify
```

For repeatable live HTTP/MongoDB tests, start MongoDB at `localhost:27017` and run:

```powershell
$env:RIDELINK_MONGO_TESTS = 'true'
try {
    mvn -B -pl driver-vehicle-service -am verify
} finally {
    Remove-Item Env:RIDELINK_MONGO_TESTS
}
```

The live tests start the application on a random port, use a new UUID-named
`driver_availability_test_...` database, and drop that database after the tests.
They cover both transitions, repeated requests, profile preservation, read-back persistence,
invalid ON_TRIP input, existing ON_TRIP protection, missing drivers and Swagger availability.
They are skipped unless the environment flag is set. Concurrent requests and real database
outages are not exercised by these tests.

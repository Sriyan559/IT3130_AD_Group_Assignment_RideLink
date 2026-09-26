# Third increment: vehicle registration

`POST /api/drivers/{driverId}/vehicles` registers a vehicle for an existing driver.
Use the driver ID returned by profile creation, not the account ID.
The route follows the existing local `/api/drivers` convention; versioned routes
and authentication/ownership checks remain pending integration.

## Request and response

With MongoDB and the service running as described in the [creation guide](driver-profile-create.md),
run this PowerShell example after substituting a real driver ID:

```powershell
$driverId = 'replace-with-created-driver-id'
$vehicleBody = @{
    plateNumber = 'ABC-1234'
    make = 'Toyota'
    model = 'Prius'
    vehicleClass = 'CAR'
    capacity = 4
} | ConvertTo-Json
Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8082/api/drivers/$driverId/vehicles" `
    -ContentType 'application/json' -Body $vehicleBody
```

Expected `201 Created` response:

```json
{
  "id": "generated-vehicle-id",
  "driverId": "the-existing-driver-id",
  "plateNumber": "ABC1234",
  "make": "Toyota",
  "model": "Prius",
  "vehicleClass": "CAR",
  "capacity": 4
}
```

- All five request fields are required. Capacity is a positive passenger seat count,
  excluding the driver. Make, model and vehicle class have a 50-character input limit.
- Plate input is limited to 20 characters: ASCII letters/digits with single spaces
  or hyphens between groups and optional surrounding spaces. This is a local format
  rule, not verification against an official vehicle registry.
- Plates are stored uppercase with spaces and hyphens removed: `abc-1234`, `ABC1234`
  and `abc 1234` share the same stored plate and cannot be registered twice.
- Make/model are trimmed; vehicle class is trimmed and uppercased. Vehicle class is
  currently free text; agreed ride eligibility categories are a future integration decision.
- One driver may register multiple different vehicles. Selecting an active vehicle
  for a trip is not part of this increment. Registration does not change availability.

## Errors and persistence

| HTTP status | Error code | Meaning |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | Missing, blank, overlong or invalid fields |
| 400 | `INVALID_REQUEST` | Missing/malformed JSON |
| 404 | `DRIVER_NOT_FOUND` | Driver ID does not exist |
| 409 | `VEHICLE_ALREADY_EXISTS` | Normalized plate is already registered, even for another driver |
| 503 | `DATABASE_UNAVAILABLE` | Database access failed |

Vehicles are stored in `driver_db.vehicles` with a scalar `driverId` reference.
`VehicleIndexes` creates the unique `vehicle_plate_unique` MongoDB index at startup,
so concurrent inserts cannot both register the same normalized plate. Existing duplicate
stored plates prevent startup rather than silently disabling the constraint.
Driver deletion is not implemented; any future deletion workflow must preserve vehicle ownership consistency.

`VehicleController` validates the DTO; `VehicleService` verifies driver existence,
normalizes fields and inserts the document through `VehicleRepository`.
`VehicleResponse` defines the response. Duplicate-key failures are translated into a
vehicle-specific exception so they do not return the driver-profile duplicate error.

## Verification

```powershell
mvn -B -pl driver-vehicle-service -am verify
```

On 2026-09-26, all 51 service tests passed (17 existing and 34 new vehicle tests).
Tests cover normalized values, driver existence, validation and HTTP error responses.
They use mocks; live MongoDB index enforcement/concurrency and end-to-end registration
still need manual or integration testing.

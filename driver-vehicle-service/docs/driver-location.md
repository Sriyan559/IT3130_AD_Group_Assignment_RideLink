# Update simulated driver location

`PUT /api/drivers/{driverId}/location` updates an existing driver's latest coordinates.
Use the ID returned by `POST /api/drivers`. The current base URL is
`http://localhost:8082`; route versioning remains pending integration agreement.

## Request and response

In Swagger UI (`/swagger-ui/index.html`) or Postman, send a PUT request with
`Content-Type: application/json` and this body:

```json
{
  "latitude": 6.9271,
  "longitude": 79.8612
}
```

Both coordinates are required. Latitude must be between -90 and 90, and longitude
between -180 and 180, inclusive. Zero and negative coordinates are valid.
Malformed values, including NaN and infinity, are rejected.

Success returns HTTP 200 with the driver profile and a nested `location`:

```json
{
  "id": "<driverId>",
  "accountId": "account-1",
  "licenseNumber": "B1234567",
  "serviceArea": "Colombo",
  "availabilityStatus": "OFFLINE",
  "location": {
    "latitude": 6.9271,
    "longitude": 79.8612,
    "updatedAt": "2026-09-28T10:00:00Z"
  }
}
```

The timestamp above is illustrative. The service generates `updatedAt` in UTC;
MongoDB stores it at millisecond precision. Clients do not supply the timestamp.
`GET /api/drivers/{driverId}` also returns the latest location. New drivers and
older documents without coordinates have `location: null`.

## Persistence and errors

- A single atomic update replaces only the location object. Profile fields and
  availability are preserved, including during concurrent availability updates.
- OFFLINE, AVAILABLE and ON_TRIP drivers can all report coordinates.
- Repeated requests succeed and refresh the timestamp. Only the latest location is
  retained; concurrent location writes use last-write-wins behavior. The timestamp
  describes server processing time, not GPS observation time or a sequence number.
- Missing/null/out-of-range coordinates return `400 VALIDATION_ERROR`; unreadable
  JSON or incompatible values return `400 INVALID_REQUEST`.
- Missing drivers return `404 DRIVER_NOT_FOUND`; no profile is created implicitly.
- Database access failures return the existing safe `503 DATABASE_UNAVAILABLE`.

Authentication, ownership checks, automatic GPS simulation and location history remain pending.
[Eligible-driver search](eligible-drivers.md) uses a configurable freshness policy (default five minutes). This endpoint accepts
manually simulated coordinates and does not change driver eligibility.

## Verification

From the repository root:

```powershell
mvn -B -pl driver-vehicle-service -am verify

# Include real HTTP/MongoDB tests with MongoDB running on localhost:27017:
$env:RIDELINK_MONGO_TESTS = 'true'
mvn -B -pl driver-vehicle-service -am verify
Remove-Item Env:RIDELINK_MONGO_TESTS
```

The live tests use random isolated database names and drop those databases afterward.
They cover all availability states, old documents, boundaries, repeated updates,
profile read-back, invalid input preservation, missing drivers, Swagger/OpenAPI and
concurrent location/availability writes. Unit and HTTP tests also cover safe database
error responses; an actual database outage is not simulated.

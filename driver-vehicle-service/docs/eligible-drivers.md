# Find eligible nearby drivers

`GET /api/drivers/eligible` discovers drivers near a pickup point. This is a read-only
query, not a reservation or assignment. Driver status can change after the response;
Ride Management must eventually reserve a driver atomically through an agreed API.

## Request

In Postman select GET and use:

```text
http://localhost:8082/api/drivers/eligible?lat=6.9271&lng=79.8612&radius=5
```

No request body is needed. All three query parameters are required:

| Parameter | Meaning | Allowed values |
| --- | --- | --- |
| lat | Pickup latitude in degrees | -90 to 90 inclusive |
| lng | Pickup longitude in degrees | -180 to 180 inclusive |
| radius | Search distance in kilometres | Greater than 0, at most 50 |

PowerShell equivalent:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8082/api/drivers/eligible?lat=6.9271&lng=79.8612&radius=5"
```

## Eligibility and response

The defaults were confirmed by the user on September 29; agreement with the other
service owners remains pending. A driver must:

- Have AVAILABLE status (OFFLINE and ON_TRIP are excluded).
- Have valid stored coordinates and a timestamp no more than 300 seconds old.
  Exactly 300 seconds is accepted; missing and future timestamps are excluded.
- Have at least one registered vehicle.
- Be within the requested radius, including the boundary.

HTTP 200 returns an array, nearest first by great-circle distance. Equal distances
are ordered by driverId. Each driver appears once, with all registered vehicles
ordered by vehicle ID. No account ID or licence number is included.

```json
[
  {
    "driverId": "<driverId>",
    "location": {
      "latitude": 6.9271,
      "longitude": 79.8612,
      "updatedAt": "2026-09-29T17:00:00Z"
    },
    "distanceKm": 0.0,
    "vehicles": [
      {
        "id": "<vehicleId>",
        "driverId": "<driverId>",
        "plateNumber": "ABC1234",
        "make": "Toyota",
        "model": "Aqua",
        "vehicleClass": "CAR",
        "capacity": 4
      }
    ]
  }
]
```

The timestamp and IDs above are examples. No matches returns `200 []`, not 404.
Invalid/missing parameters return `400 VALIDATION_ERROR`. Database failures return
the existing safe `503 DATABASE_UNAVAILABLE` response, including vehicle-query failures.

## Configuration and implementation limits

`DRIVER_LOCATION_MAX_AGE_SECONDS` controls freshness, default 300. Set to 0 only for
an intentional demo that permits old locations; timestamps must still be present
and not in the future. Negative values prevent startup. Restart after changing it.

MongoDB filters status, coordinate bounds and timestamp; Java calculates Haversine
distance using a mean Earth radius of 6371.0088 km. Vehicles are loaded in one bulk
query for nearby drivers. Status/timestamp and vehicle-driver indexes support these
queries. No existing document migration is needed.

This implementation suits the small assignment dataset: it loads all candidates
that pass the database filter and returns all eligible matches without pagination.
For a larger deployment, introduce GeoJSON/2dsphere indexed distance queries and
pagination. Distances are geographic, not road distances or travel times. Vehicle
class/capacity matching, service-area rules, identity/ownership verification and
atomic reservation are not implemented. Existing routes remain unversioned.

## Test and demo

Run the Maven suite with local MongoDB:

```powershell
$env:RIDELINK_MONGO_TESTS = 'true'
mvn -B -pl driver-vehicle-service -am verify
Remove-Item Env:RIDELINK_MONGO_TESTS
```

Live tests use isolated databases and drop them afterwards. They check exclusions,
nearest-first ordering, multiple vehicles, empty results, unchanged stored data,
validation, static-route resolution, OpenAPI and indexes. Unit tests cover freshness
and radius boundaries, date-line/pole distances and database failures.

Re-import the updated Driver-Vehicle Postman collection and run in order. Requests
05a/05b test search and invalid radius; 10a verifies the OFFLINE driver is excluded.
For manual testing, send 04 Set AVAILABLE and 05 Update location immediately before
05a. Old test profiles may be OFFLINE or have stale coordinates, yielding an empty
array until those two updates are sent.

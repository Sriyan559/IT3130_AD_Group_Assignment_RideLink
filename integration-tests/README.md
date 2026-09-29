# Account, Driver and Ride integration

The `integration` branch combines the component branches and the String external-ID
fix. Account, Driver and Ride now communicate over authenticated HTTP. Fare/payment
remains unfinished; this is not a completed four-service platform.

## Start the local Postman demo

Requires local MongoDB on port 27017, Java 17+ and Maven. From this checkout:

```powershell
.\integration-tests\start-local.ps1 -Build
```

Import `postman/collections/RideLink-Integration.postman_collection.json` into Postman.
Run requests in numbered order. It registers real users, logs in, creates a driver
and vehicle, updates location, searches, assigns a ride, completes it and verifies
release. Tokens and IDs are captured automatically. Use this collection for secured
integration; the earlier synthetic-account Driver collection cannot authenticate real accounts.

Ports: 18081/18082/18083. Separate local databases: `ridelink_demo_account`,
`ridelink_demo_driver`, `ridelink_demo_ride`. Existing Postman data and the old service
on 8082 are preserved. Secrets are generated in memory per launch. Restarting
invalidates earlier JWTs; rerun login. No service secret belongs in Postman.

```powershell
.\integration-tests\stop-local.ps1
```

## Repeatable automated verification

Stop the demo first so ports 18081-18083 are free.

```powershell
$env:RIDELINK_MONGO_TESTS = 'true'
mvn -B -pl account-service,driver-vehicle-service,ride-management-service -am verify
python -m pip install -r integration-tests/requirements.txt
python integration-tests/run-local-e2e.py
```

The E2E script starts three real JARs with security enabled and unique local databases.
It tests registration/login, ownership rejection, eligibility, reservation, completion,
cancellation, concurrent assignment, delayed releases and outage/restart recovery.
It stops its processes and drops only its temporary databases in `finally`.
Logs are under ignored `tmp/e2e/`. Fare Service is not started or tested.

## Authentication and ownership

- Account `GET /api/accounts/me` validates the Bearer JWT and current ACTIVE account.
- Driver creation requires a DRIVER token matching the request account ID. Profile,
  vehicle, location and availability routes are owner-only. Eligible search accepts
  authenticated accounts. Missing/invalid credentials: 401; wrong owner: 403;
  Account failure: 503, with access denied.
- Ride creation/history belong to the PASSENGER. Only that passenger may assign or
  cancel; only the assigned DRIVER may advance status. Ride lookup accepts its
  passenger or assigned driver. ADMIN may manage existing rides and list all rides.
- Internal Driver routes require `X-Service-Token`, at least 32 characters, configured
  as `RIDELINK_SERVICE_TOKEN` in both Driver and Ride. Unconfigured tokens fail closed.
  `ACCOUNT_SERVICE_URL` and `DRIVER_SERVICE_URL` configure HTTP dependencies.
- `RIDELINK_SECURITY_ENABLED=false` is only for isolated legacy component tests.
  Authentication is enabled by default and explicitly enabled by demo/E2E launchers.

## Reservation and recovery

`PUT /internal/drivers/{driverId}/reservations/{rideId}` atomically changes an eligible
AVAILABLE driver to ON_TRIP. A vehicle and fresh location are required. Same-ride
retries succeed; conflicts return 409. A partial unique index prevents one ride
holding two drivers. Search alone never reserves a driver.

`DELETE` on that path releases only that ride to AVAILABLE. Delayed releases cannot
free a newer reservation. Released ride IDs are retained as tombstones to prevent
late reserve retries resurrecting completed reservations.

Ride persists `pendingDriverId` before reserving. Ambiguous failures retain it;
a 5-second worker retries the same ID. Definite 404/409 clears the intent. Optimistic
locking rejects concurrent Ride edits. To assign, use `/assign-driver/{driverId}`;
a direct status change to ASSIGNED is rejected.

Completion/cancellation saves terminal status with `releasePending=true` before
calling Driver. Failure returns 503 but durable state survives restarts; the worker
clears the flag after retry. Repeating terminal requests is safe. Cancelling during
pending assignment returns 409 until reconciliation completes. A create-with-driver
failure includes the already-created rideId: fetch/retry it instead of creating a
duplicate. The collection uses the simpler create-unassigned then assign flow.

## Remaining platform work

- Fare calculation, payments and receipts are not implemented by this integration.
- Legacy numeric references need explicit migration before using old Ride data.
  Verification uses new isolated records, not shared/cloud data.
- Production needs TLS, network restrictions and managed service secrets.
- Large deployments need pagination/geospatial queries and tombstone archival
  (currently retained per driver). Recovery is eventual, not a distributed transaction.

Historical `workflows/` folders are planning placeholders, not executed fare/payment
tests. Root `AGENTS.md` records actual dated verification results.

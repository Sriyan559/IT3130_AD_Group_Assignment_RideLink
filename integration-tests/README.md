# Four-service RideLink integration

The `integration` branch combines the component branches and the String external-ID
fix. Account, Driver, Ride and Fare/Payment communicate over authenticated HTTP.
The seven backend workflows in the repository architecture report are implemented.

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

Ports: 18081/18082/18083/18084. Separate local databases: `ridelink_demo_account`,
`ridelink_demo_driver`, `ridelink_demo_ride`; Fare uses a separate persistent H2 SQL
file under `tmp/local-integration/payment` for the local demo. Its default runtime
configuration is PostgreSQL. Existing Postman data and the old service
on 8082 are preserved. Secrets are generated in memory per launch. Restarting
invalidates earlier JWTs; rerun login. No service secret belongs in Postman.

```powershell
.\integration-tests\stop-local.ps1
```

## Repeatable automated verification

Stop the demo first so ports 18081-18084 are free.

```powershell
$env:RIDELINK_MONGO_TESTS = 'true'
mvn -B verify
python -m pip install -r integration-tests/requirements.txt
python integration-tests/run-local-e2e.py
```

The E2E script starts four real JARs with security enabled and unique local databases.
It tests registration/login, ownership rejection, eligibility, reservation, completion,
cancellation, concurrent assignment, delayed releases and outage/restart recovery.
It stops its processes and drops only its temporary databases in `finally`.
Logs are under ignored `tmp/e2e/`. Local E2E uses H2 SQL; setting `E2E_POSTGRES_DSN`
uses a temporary PostgreSQL database instead (the database user needs CREATE DATABASE).
CI provides PostgreSQL 16 and MongoDB. A hosted CI pass must be checked separately.

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

- Hosted CI/PR review and any deployment are separate from local verification.
- Legacy numeric references need explicit migration before using old Ride data.
  Verification uses new isolated records, not shared/cloud data.
- Production needs TLS, network restrictions and managed service secrets.
- Large deployments need pagination/geospatial queries and tombstone archival
  (currently retained per driver). Recovery is eventual, not a distributed transaction.

See [requirements and evidence](../docs/requirements-status.md). Root `AGENTS.md`
records actual dated verification results.

## Fare, payment and receipt flow

The collection now has 37 ordered requests, including fare estimation, automatic
completion payment, receipt retrieval, a failed simulation and an idempotent retry.
`POST /api/v1/rides/{rideId}/assign?radius=5` invokes nearest eligible-driver search
and reserves a candidate. No eligible candidate returns 409 NO_AVAILABLE_DRIVER.

Completion requires `distanceKm` > 0 (at most 10000) and `durationMinutes` 0-100000.
Ride saves COMPLETED with `paymentPending=true`, releases its driver, and calls Fare.
Fare reads the completed Ride through authenticated HTTP, calculates a fixed final
fare, and records a simulated CARD payment. Default simulation succeeds; completion
with `simulatePaymentFailure:true` records FAILED with no receipt. No bank is contacted.

Formula: max(LKR 200, 150 + 100 * distanceKm + 10 * durationMinutes), with each
component rounded HALF_UP to two decimals. Example: 8 km / 20 minutes = LKR 1150.
Final fares retain their original rate breakdown even if configuration later changes.

Payment outages leave a durable intent. The recovery worker retries `completion-<rideId>`;
the database transaction and idempotency key prevent a duplicate payment. SQL locks
serialize competing payment requests for the same ride. Receipts snapshot itemized
amounts and have no update endpoint. Cancellation never creates a payment.

Passengers may retry a FAILED attempt using POST `/api/v1/payments/process` with
`rideId`, a new `idempotencyKey`, `paymentMethod` CARD/CASH and `simulateFailure:false`.
Reusing a key with identical input returns the original result; changed input or a
second successful payment returns 409. Ride's payment fields describe its automatic
completion attempt; the payment history/receipt-by-ride endpoint is authoritative
after a later passenger retry. All public payment/receipt access is owner/ADMIN-only.

Default PostgreSQL configuration uses PAYMENT_DB_HOST/PORT/NAME/USERNAME/PASSWORD.
The demo profile is an explicit local convenience, not a PostgreSQL verification claim.

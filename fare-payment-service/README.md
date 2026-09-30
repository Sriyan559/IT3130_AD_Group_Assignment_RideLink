# Fare & Payment Service

Owner: Sanjeewa H.D.U.S (IT24101590). Integration implementation completed at the
user's request on the shared `integration` branch; original component history is retained.

Java 17 / Spring Boot 3.3.4. Default port 8084. PostgreSQL owns final fares, simulated
payment attempts and immutable receipt snapshots. Account/Driver/Ride databases are
accessed only through their HTTP APIs, never through this service's repositories.

## Run and demonstrate

Use the repository [four-service launcher and Postman guide](../integration-tests/README.md).
The launcher uses port 18084 and persistent H2 SQL in the explicit `demo` profile.
Default production-style configuration remains PostgreSQL with Flyway migrations:
PAYMENT_DB_HOST, PAYMENT_DB_PORT, PAYMENT_DB_NAME, PAYMENT_DB_USERNAME and
PAYMENT_DB_PASSWORD. Supply ACCOUNT_SERVICE_URL, RIDE_SERVICE_URL and the shared
RIDELINK_SERVICE_TOKEN. Public endpoints verify Account JWTs and passenger ownership.
Swagger: `/swagger-ui/index.html`; use Authorize with the passenger Bearer token.

## Fare rule

Distance must be >0 and <=10000 km; duration 0-100000 minutes.
Each component is rounded HALF_UP to two decimals:

`max(200, 150 + 100 * distanceKilometers + 10 * durationMinutes)` in LKR.

Rates are configurable using `fare.base-rate`, `fare.per-km-rate`,
`fare.per-minute-rate` and `fare.minimum-fare`. Final fare components are stored once
per completed ride and remain stable across rate changes. Example: 8 km / 20 minutes
returns LKR 1150.00. No tax/surge/refund system is claimed.

## Public API

| Method | Route | Input / behavior |
|---|---|---|
| POST | `/api/v1/fare/estimate` | distanceKilometers, durationMinutes; itemized estimate |
| POST | `/api/v1/fare/final` | rideId; metrics fetched from completed Ride |
| GET | `/api/v1/fare/final/{rideId}` | Stored final fare |
| POST | `/api/v1/payments/process` | rideId, idempotencyKey, paymentMethod CARD/CASH, simulateFailure |
| GET | `/api/v1/payments/{paymentId}` | Attempt status and transaction reference |
| GET | `/api/v1/payments/ride/{rideId}` | Payment history |
| GET | `/api/v1/receipts/{receiptId}` | Immutable itemized receipt |
| GET | `/api/v1/receipts/ride/{rideId}` | Successful receipt for ride |

Compatibility aliases: `/api/fares/estimate`, `/api/fares/final`,
`/api/fares/final/{rideId}`, `/api/payments`, `/api/payments/{paymentId}`.
Account IDs are String; ride/payment/receipt IDs are UUID. Do not submit a client
amount or passenger ID to determine a charge: these come from trusted Ride/fare data.

Ride calls the service-token protected `/internal/payments/process` after completing
and persisting a trip. The service calls `/internal/rides/{id}` for current trip data.
These endpoints are hidden from public Swagger and reject user JWTs without a service token.

## Payments and consistency

This is a simulation: no cards, banks or external payment networks are contacted.
SUCCESS creates one receipt; FAILED creates no receipt and has no paidAt timestamp.
A failed attempt can be retried with a new idempotencyKey. Identical-key retries
return the original attempt. Changed-key payload reuse or a second successful payment
returns 409. Pessimistic SQL locking on the fare serializes payment attempts; unique
constraints defend IDs and receipt cardinality. Payment, receipt and fare linkage
commit in one transaction. There is no API to edit or delete receipts.

Ride retains paymentPending across outages and retries its stable completion key.
A successful later passenger retry is visible in payment history/receipt-by-ride;
Ride's stored payment fields describe its original automatic completion attempt.

## Verification

Fare has nine tests for formula boundaries/rounding, Flyway/JPA persistence,
idempotency, failed-payment retry, immutable fare amounts and concurrent payments.
Local verification uses H2 PostgreSQL mode; four-service E2E includes ownership,
receipts and payment-outage recovery across process restarts. Hosted CI is configured
for real PostgreSQL; local H2 results are not a PostgreSQL test-pass claim.

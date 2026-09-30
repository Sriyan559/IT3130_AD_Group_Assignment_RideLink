# Requirements and verification status — 2026-09-30

Source: minimum functional workflows in `RIDELINK_SPRING_BOOT_ARCHITECTURE_REPORT.md`
and repository API/service-boundary documents. No separate original assessment PDF
was present in this checkout. This table reports implementation evidence, not marks
or an external assessor's approval.

| Required backend workflow | Implemented behavior | Evidence |
|---|---|---|
| Account and access | Passenger/driver registration, JWT login, ACTIVE account/role and ownership checks | Account tests; authenticated E2E |
| Driver preparation | Profile, vehicle, availability, service area, simulated location | Driver unit/HTTP/live Mongo tests |
| Fare estimation | Configurable base + distance + duration, minimum fare, rounded LKR breakdown | Fare calculator tests; Postman estimate |
| Ride request and assignment | Create REQUESTED; Ride queries nearby eligible drivers and atomically reserves one | `/api/v1/rides/{id}/assign`; no-match and concurrency tests |
| Ride lifecycle | Strict transitions, owner/driver access, illegal transition rejection, completion metrics | Ride tests and E2E |
| Completion and payment | Durable completion payment intent, authoritative final fare, simulated success/failure, receipt lookup | Four-service E2E, SQL transaction tests, Postman |
| Negative scenarios | No available driver, invalid transition/input, missing/wrong credentials, failed payment | E2E and component suites |

Exactly four independently executable Java/Spring Boot services are used. The
`service-support` Maven module is a shared HTTP utility library, not a service or
shared persistence layer. Backend-only scope is preserved; Postman/Swagger provide
the demo. Account, Driver and Ride each own separate Mongo databases. Fare owns SQL
(PostgreSQL default, explicit persistent H2 demo profile). No service queries another
service's database. Older architecture proposals specifying SQL for Account/Ride
are superseded by their implemented Mongo repositories.

## Verified locally

- `RIDELINK_MONGO_TESTS=true mvn -B verify`: 204 tests, zero failures/errors/skips.
- Real four-JAR E2E: 88 HTTP status checks plus business assertions; isolated Mongo
  and H2 SQL data cleaned after testing. Includes payment and driver release recovery
  across process restarts, contention, ownership, successful/failed payment and receipts.
- Newman: 37 requests and 51 assertions passed, zero failures.

## Delivery and environment boundaries

- Run `integration-tests/start-local.ps1 -Build`, then import
  `postman/collections/RideLink-Integration.postman_collection.json` and run all 37 requests.
- Hosted CI for commit `88f3fbd` passed all five jobs on 2026-09-30, including the
  full-system workflow with PostgreSQL 16 and MongoDB 8.0. This verifies PostgreSQL
  separately from the local H2 demo. [Verified run](https://github.com/Sriyan559/IT3130_AD_Group_Assignment_RideLink/actions/runs/36655685604).
- Local commits/push attempts and exact remote results are recorded in AGENTS.md.
  Peer PR review/main merge cannot be claimed merely because code is pushed.
- No shared legacy data was migrated. External numeric IDs require owner-provided
  identity mapping before migration; the demo creates new valid String references.
- Production TLS/network policy and deployment credentials depend on the hosting
  environment. This is an assignment backend demonstration, not a live payment deployment.
- Embedded cloud credentials found in tracked examples were replaced with local
  examples. Repository history still contains prior values; the credential owner must
  rotate any such credentials that remain active. Values are not copied into this report.

> Updated 2026-09-30: the gaps below describe the earlier branch review.
> String IDs, ownership, Account verification and reservations are now implemented on
> `integration`. See [the runnable integration guide](../../integration-tests/README.md).

# Driver Service integration handoff

Reviewed on 2026-09-29 after fetching origin. This is an implementation review and
proposed integration contract, not a claim that the services have been integrated.

Evidence: Driver commit `cd34269`, Ride branch commit `9b34d37`, Account branch
commit `9fd1dea`. Other owners' branches were read without merging or editing them.

Follow-up on September 29: the user authorized the ID fix. Local commit `e71492d`
on `fix/ride-external-string-ids` implements it in sibling worktree
`C:/Users/mashi/Desktop/RideLink-ride-id-fix`. All 15 Ride tests passed, including
HTTP string-ID flows and isolated local MongoDB queries. The fix has not been pushed
or merged into the Ride owner's branch. Findings below describe the original branch.

## 1. Fix cross-service identifier types first

Driver Service returns string IDs such as `6abbfa6d095b5451ceb2f20a`. AccountResponse
also declares its ID as String. Ride currently uses Long for both driverId and
passengerId in CreateRideRequest, service methods and repository queries. The driver
assignment controller path also binds driverId to Long. These contracts cannot
accept Driver Service's real ID.

Proposed Ride-owner change: use String for external driver/account IDs throughout
request/response DTOs, Ride document, controller parameters, service methods,
repository query signatures, examples and tests. Keep the ride's own UUID unchanged.
Do not hash or truncate external IDs into numbers. Existing numeric sample data
needs an explicit migration or fresh isolated integration database.

Acceptance: create a ride using a real passenger account ID, assign the exact
driverId from Driver Service and retrieve the same identifiers unchanged. Test
driver/passenger history routes and ensure no numeric conversion is attempted.

## 2. Consume the implemented driver search contract

Ride should call:

```text
GET http://localhost:8082/api/drivers/eligible?lat=6.9271&lng=79.8612&radius=5
```

Use the [eligible-driver guide](eligible-drivers.md) for the response schema.
Drivers must be AVAILABLE, have a registered vehicle and a location at most five
minutes old. Radius is in kilometres, greater than zero and at most 50.
`200 []` means no eligible drivers; it is not a database failure. Handle 400 and
503 separately and configure HTTP timeouts. Never read driver_db directly.

RideController uses `/api/v1/rides`; Driver uses `/api/drivers` and Account uses
`/api/accounts` and `/api/auth`. Each client must use the implemented path rather
than assuming a shared prefix. Coordinate route versioning separately.

The inspected RideService currently saves supplied driver IDs directly and has no
implemented Driver HTTP client. Its integration/driver package contains only a
package description. Search wiring remains Ride-owner work.

## 3. Agree on account authorization

Account endpoints implemented in the inspected branch:

- POST /api/auth/register/driver: returns an ACTIVE DRIVER account.
- POST /api/auth/register/passenger: returns an ACTIVE PASSENGER account.
- POST /api/auth/login: returns token, tokenType, expiresInMs and account.
- GET /api/accounts/{id}: requires JWT and access by that owner or ADMIN.

Driver currently accepts synthetic accountId values without Account verification.
Proposed creation integration: forward the caller's Bearer token to Account lookup,
then require matching account ID, role DRIVER and status ACTIVE before inserting a
driver profile. Define timeouts and explicit handling of 401/403/404/5xx first.
An ADMIN-authorized lookup alone must not be mistaken for proof of driver ownership;
owner and ADMIN behavior must be agreed explicitly.

Profile/vehicle/location/availability ownership and service-to-service search/trip
authorization also need a consistent policy. Do not disable Account security or
invent an ADMIN token to make integration work. Do not put tokens or shared secrets
in documentation, collection exports or Git.

## 4. Reserve and release drivers safely

Search is read-only; two rides can discover the same driver. The current availability
endpoint accepts only AVAILABLE/OFFLINE and deliberately rejects ON_TRIP as a target.
It cannot be used as the trip-assignment endpoint.

Before implementation, agree on a separate authenticated reserve/release contract
carrying rideId. It should atomically reserve only eligible AVAILABLE drivers, bind
the reservation to the ride, permit safe same-ride retries and reject competing rides.
Release must verify the owning ride and handle completion/cancellation retries.
Define compensation if Ride persistence fails after reservation. Endpoint routes,
reservation timing and post-trip availability are still proposals to agree on.

## Joint verification order

1. Align external ID types and client routes.
2. Register/login real passenger and driver accounts in isolated integration data.
3. Create the driver profile, vehicle, AVAILABLE status and fresh location.
4. Query from Ride Service and assign the returned string driver ID using reservation.
5. Verify a second ride cannot reserve the same driver concurrently.
6. Complete/cancel the ride and verify owned release and retry behavior.
7. Test no matches, expired locations, invalid identity, unauthorized ownership and
   dependency failures. Keep a dated result record distinguishing mocks from live calls.

These steps are not yet complete. Existing Driver tests (142) and Newman assertions
(23) validate the Driver component only, not this cross-service workflow.

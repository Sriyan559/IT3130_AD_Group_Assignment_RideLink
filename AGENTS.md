# RideLink project instructions and dated work log

## Purpose

Keep project context and completed work here so the next working session can continue
without relying on chat memory. This file applies throughout this repository.

## Instructions for future coding sessions

- Read this file before changing the project. Check the current branch, working tree,
  relevant source and recent Git history; the log may be older than the code.
- After every meaningful implementation, fix, documentation change or investigation,
  update the dated work log below in the same working session, before the final reply.
  Do this as part of the task without waiting for a separate reminder.
- Use the actual local date in `YYYY-MM-DD` format, timezone `Asia/Colombo` (UTC+05:30).
  Preserve older entries. Add separate entries for distinct tasks on the same day.
- Record what changed, relevant files/endpoints, verification performed and its actual
  outcome, remaining work, and a commit reference when already available.
- Separate completed work from planned work. Never invent dates, successful tests,
  authorship, commits or push results. For reconstructed history, cite the Git commit
  and label details whose verification is unavailable.
- If changes are not committed, say so. A commit cannot contain its own hash; add that
  reference in a later session when useful. Do not create extra commits just to record hashes.
- Update the current-state and next-work sections when the implementation changes.
- Keep entries concise. Do not log credentials, tokens, private environment values,
  every tool call or conversation-only acknowledgements.
- This is a session-maintained log, not a background watcher or Git hook. Manual work
  outside an agent session must be recorded by the contributor or reconciled from
  evidence at the next session. Updating the log does not itself commit or push files.

## Project context

- RideLink is a Java 17 / Spring Boot 3.3.4 Maven project with four microservices.
- The current user's component is Driver & Vehicle Service, owner Rathnakoon D A,
  student ID IT24300246, under `driver-vehicle-service/`.
- Driver data belongs to MongoDB `driver_db`; other services access it through APIs.
- Working branch: `feature/it24300246-driver-vehicle-service`. Verify before each task.
  The user chose this original student-ID branch; do not recreate `feature/driver-vehicle`.
  Do not create a new branch for each day or delete older branches without a task reason.
- Keep changes focused on the requested component and preserve unrelated user changes.
- Use meaningful commits for completed increments. Do not fabricate activity for history.

## Current state (last checked 2026-10-01)

- Authorized whole-project work is on `integration`, sibling checkout
  C:/Users/mashi/Desktop/RideLink-integration. Preserve the original student-ID branch.
- Delivered to `main` through PR #1, merge 1605e3e on 2026-09-30. All five PR CI
  jobs passed (run 36754310117), including PostgreSQL/MongoDB full-system E2E.
- Main commit 7a3237d also passed hosted CI run 36754806785. October 1 demo rehearsal
  improved collection repeatability; two consecutive runs passed 37 requests/53 assertions.
- All seven minimum backend workflows in the repository architecture report are now
  implemented across four Java/Spring Boot services. service-support is a library.
- Account/Driver/Ride use isolated Mongo databases. Fare uses PostgreSQL by default,
  Flyway SQL migrations and a separate H2 SQL demo profile; no cross-service DB access.
- Account JWT/ACTIVE status, role/ownership and internal service-token authentication
  are implemented. Driver supports profile/vehicle/location/availability/search and
  atomic ride-scoped reservation/release. Public search is read-only.
- Ride supports nearest eligible-driver matching, strict state transitions, required
  completion metrics, optimistic locking, durable release/payment intent and recovery.
- Fare calculates rounded base/distance/duration/minimum amounts from trusted completed
  Ride data; simulated payments are idempotent with SQL locking and immutable receipts.
  Failed simulations can be retried with a new key; no actual banking is performed.
- Verification: 204 Maven tests passed, zero failures/errors/skips, live Mongo enabled.
  Four-JAR E2E passed 88 HTTP checks plus business/recovery assertions using Mongo/H2.
  Newman passed 37 requests/51 assertions. Hosted CI run 36655685604 passed all five
  jobs for published commit 88f3fbd, including PostgreSQL 16/MongoDB 8.0 E2E.
- Demo launcher/start-stop supports ports 18081-18084, separate Mongo demo databases
  and persistent H2 under tmp/local-integration. See integration-tests/README.md and
  docs/requirements-status.md. Latest publication result is in the dated log below.

## Next work (external delivery / optional scale work)

- Backend implementation, publication and main merge are complete. GitHub write
  access works with explicit account selection (Dinuli2004); no permission changes.
- Owner must rotate any still-active cloud credentials formerly present in repository
  history; current tracked examples were sanitized without recording their values.
- Legacy data requires explicit identity mapping before migration; existing shared
  data was not modified. Fresh demonstration data works with String external IDs.
- Deployment TLS/network rules/credentials depend on the target environment; no live
  deployment claimed. Pagination/geospatial indexing/tombstone archival are scale work.

## Dated work log

Historical entries below were reconstructed on 2026-09-26 from the current branch's
Git history. Commit dates identify when changes were recorded, not how long they took.
Shared repository setup is not a claim of individual contribution by the current user.

### 2026-09-24 - Shared project scaffold

- Evidence: `ae109db` (`feat: initialize RideLink Java Spring Boot microservices architecture`).
- Added the parent Maven project, four service skeletons, configuration templates,
  architecture/API documentation, Postman assets, CI and build/test scripts.
- Verification: historical test execution was not verified during reconstruction.

### 2026-09-24 - Tracked driver build-directory files

- Evidence: `c2fc909` (commit title: `feature/account-service`).
- The actual diff adds five files under `driver-vehicle-service/bin/`: README,
  documentation README, POM and resource configuration files. The title alone does
  not establish Account Service implementation.
- Verification: inspected the commit's file summary; historical runtime checks unknown.

### 2026-09-25 - Driver profile creation

- Evidence: `69b1359` (`feat(driver): add validated driver profile creation with tests`).
- Implemented `POST /api/drivers`, request/response DTOs, document/repository/service,
  OFFLINE default status, input normalization and unique account/licence indexes.
- Added validation and safe error handling, unit/HTTP contract tests and
  `driver-vehicle-service/docs/driver-profile-create.md`.
- Verification: test code is present in the commit; its original execution was not
  re-established here. Creation tests also passed in the 2026-09-26 verification below.

### 2026-09-26 - Driver profile lookup

- Evidence: `7641c93` (`Add driver profile lookup with error handling and tests`).
- Added `GET /api/drivers/{driverId}` through `DriverController` and `DriverService`.
- Added `DriverNotFoundException` and `404 DRIVER_NOT_FOUND`; database failures return
  the existing safe `503 DATABASE_UNAVAILABLE` response.
- Added two service tests and three HTTP tests; updated the service README and added
  `driver-vehicle-service/docs/driver-profile-get.md`.
- Verification: Maven verify succeeded; 17 tests passed, zero failures/errors/skips.
  `git diff --check` passed. Live MongoDB testing remains pending.
- At the next local check, the branch was clean and matched the locally recorded
  `origin/feature/driver-vehicle` tracking reference; no fresh remote fetch was performed.

### 2026-09-26 - Persistent instructions and work history

- Added root `AGENTS.md` with session update instructions, project state, next work
  and evidence-based dated history for all four commits currently on this branch.
- Linked this file and the two endpoint guides from the driver documentation index.
- Verification: checked Git history and documentation links; `git diff --check` passed.
  No application tests rerun for this documentation-only change.
- Status at writing: local changes, not yet committed or pushed.

### 2026-09-26 - Consolidate work into the original student-ID branch

- At the user's request, switched to `feature/it24300246-driver-vehicle-service` and
  fast-forwarded it to include all work from `feature/driver-vehicle`.
- Pushed the original branch and verified with `git ls-remote` that both remote branches
  pointed to `7641c93` before deleting the duplicate branch locally and remotely.
- Existing commit hashes, authors and dates were preserved; no application code changed.
- Updated the working-branch instructions above. Included the previously uncommitted
  work log and documentation index in this branch consolidation task.
- Verification: fetched remote history, confirmed no divergent commits, verified the
  destination remote hash and successful duplicate deletion. Documentation whitespace
  check passed; application tests were not rerun because code was unchanged.
- Documentation status at writing: prepared for commit and push on the original branch.

### 2026-09-26 - Vehicle registration

- Added `POST /api/drivers/{driverId}/vehicles` with request/response DTOs, controller,
  service, MongoDB vehicle document/repository and a unique normalized-plate startup index.
- Missing driver returns `404`; duplicate plate returns vehicle-specific `409`;
  invalid input returns `400` and database failures use the existing safe `503` response.
- Plates are uppercase without spaces/hyphens; make/model are trimmed and vehicle class
  is trimmed/uppercased. Capacity must be positive. Registration preserves driver availability.
- Added 34 vehicle unit/HTTP tests and a usage guide; updated both documentation indexes.
- Verification: Maven verify passed all 51 tests with zero failures/errors/skips;
  `git diff --check` passed. Live MongoDB and concurrent index enforcement remain unverified.
- Previous work-log/branch consolidation was committed as `32cdfd4` and pushed.
- Git: vehicle increment committed locally on `feature/it24300246-driver-vehicle-service`.
  Push blocked at Git credential-manager authentication. A noninteractive retry failed
  with `unable to get password from user`; the user must sign in and run `git push`.
  The stalled push processes were stopped. No remote vehicle commit was confirmed.

### 2026-09-26 - Live MongoDB API verification

- Found MongoDB listening on localhost:27017. Started the built Driver Service JAR
  on localhost:8082 with isolated database `driver_db_smoke_20260926152647`.
- Verified driver creation 201, profile read 200, vehicle registration 201,
  normalized duplicate plate 409 VEHICLE_ALREADY_EXISTS, nonexistent driver 404
  DRIVER_NOT_FOUND and invalid capacity 400 VALIDATION_ERROR. A second profile read
  confirmed availability remained OFFLINE. Swagger UI returned 200.
- The initial PowerShell error-response reader failed after the successful creation
  requests; reran with HttpClient and all checks passed. This was a test harness issue.
- Test data retained: two smoke driver/vehicle pairs in the isolated database.
  Successful full-run driver ID: `6ab7972081b50c2c952779a3`;
  vehicle ID: `6ab7972081b50c2c952779a4`.
- Left the local service running (PID 27384) for Swagger inspection; output logs are
  ignored files `tmp-driver-live-out.log` and `tmp-driver-live-err.log`.
- Verification limitation: no concurrent-request test or live database-outage test.
- Vehicle commit `b8514ac` now matches the local remote-tracking branch (no fresh fetch).
  This verification/documentation update is local and uncommitted at writing.

### 2026-09-26 - End-of-day commit and push

- User requested committing and pushing all completed work for today.
- Fresh `git fetch origin` confirmed vehicle commit `b8514ac` is on the remote
  student-ID branch and there are no divergent commits.
- Reviewed and prepared the remaining live-verification documentation and work log
  for the end-of-day commit. `git diff --check` passed; no application code changed
  since the 51 passing tests and successful live checks.
- Remaining documentation was committed locally. Push failed because Git credential
  manager requires interactive sign-in (`unable to get password from user`). The user
  needs to run `git push` in their terminal and complete GitHub authentication.
- Next feature remains availability updates; no availability implementation was added today.

### 2026-09-27 - Review next development increment

- Reviewed branch, working tree, recent history and availability references in source/docs.
- Confirmed the student-ID branch was clean and matched its local remote-tracking
  reference at `8fe3e59`; no fresh remote fetch was performed.
- Recommended availability updates as today's increment, followed by validation,
  tests, usage documentation and a live smoke check. Implementation remains planned.
- Route versioning and the documented ON_TRIP status need reconciliation with the
  AVAILABLE/OFFLINE toggle contract before implementation.
- Verification: inspection only; application tests not rerun. This log update is local
  and uncommitted.

### 2026-09-27 - Driver availability updates

- Added `PUT /api/drivers/{driverId}/availability`, validated AVAILABLE/OFFLINE input,
  atomic status-only MongoDB updates and the updated profile response. Repeated requests
  succeed; missing drivers return 404, ON_TRIP conflicts 409 and database failures safe 503.
- Added `UpdateAvailabilityRequest`, `DriverAvailabilityService`, `DriverOnTripException`,
  controller/advice handling, 25 unit/HTTP cases and three opt-in live MongoDB tests.
- Added the availability guide and updated documentation indexes/API specification to
  distinguish implemented unversioned routes from the future versioning proposal.
  Authorization, vehicle eligibility and trip assignment integration remain pending.
- Verification: initial mock-only Maven verify passed 76 tests. Final Maven verify with
  `RIDELINK_MONGO_TESTS=true` passed all 79 tests, zero failures/errors/skips. Live tests
  verified both transitions, idempotence, profile preservation/read-back, invalid target,
  ON_TRIP protection, missing driver and Swagger 200. The isolated test database was
  dropped. Concurrent requests and live database outages remain untested.
- `git diff --check` and the staged whitespace check passed.
- Git: committed locally on the original student-ID branch. Push failed because Git
  could not obtain the GitHub username with interactive prompts disabled. No remote
  availability commit was confirmed. Run `git push` in a terminal and complete sign-in.

### 2026-09-27 - Check visibility of recent work

- Inspected branch/file history after the user reported only older work was visible.
  September 26 lookup, vehicle and verification commits are present in branch history.
- Fresh `git ls-remote` confirmed the remote student-ID branch now points to `a56de46`,
  matching local HEAD and including yesterday's commits; the earlier push blockage is
  resolved. The intervening push was not performed in this investigation.
- `integration-tests/README.md` last changed in scaffold commit `ae109db` on September 24.
  Driver implementation/tests live under `driver-vehicle-service/`, so the integration
  README's older file-history date does not indicate missing driver work.
- Verification: Git history and live remote hash inspection only; no code changes or
  application tests. This investigation log update is local and uncommitted.

### 2026-09-28 - Plan next Driver & Vehicle increment

- Reviewed branch, recent history, driver controller/document and location/eligibility
  references. Availability is implemented; simulated location updates remain planned.
- Recommended today's scope: location update endpoint, coordinate validation, atomic
  location-only persistence, tests and usage documentation; eligible-driver queries follow.
- Verification: source/Git inspection only; no application tests rerun or code changed.
  HEAD is `a56de46` on the student-ID branch and matches the local remote-tracking
  reference; no fresh remote check performed. Preserved the existing work-log change.
- Git: this planning log update is local and uncommitted; implementation has not started.

### 2026-09-28 - Simulated driver location updates

- Added `PUT /api/drivers/{driverId}/location`, required coordinate/range validation,
  atomic location-only MongoDB updates and a server UTC timestamp. All availability
  states are supported; missing drivers return 404 and database failures safe 503.
- Added DriverLocation, request/service/controller classes and nullable location in
  profile responses. Older documents without location remain compatible; repeated
  requests refresh the timestamp without changing availability or profile fields.
- Added 29 unit/HTTP/live test cases and the location usage guide; updated service
  and API documentation, guide index and stale creation-guide future-work wording.
- Verification: `mvn -B -pl driver-vehicle-service -am verify` with
  `RIDELINK_MONGO_TESTS=true` passed 108 tests, zero failures/errors/skips. Six live
  tests used isolated databases and dropped them afterward. New live coverage includes
  legacy documents, all states, boundaries, repeated writes, read-back, invalid-input
  preservation, missing drivers, Swagger/OpenAPI and ten concurrent location/status
  update pairs. `git diff --check` passed.
- Remaining: eligible-driver queries, identity/ownership integration and location
  freshness policy. Actual database outages and concurrent vehicle registration remain
  untested; database-error HTTP handling is covered with mocks.
- Git: implementation and documentation are local and uncommitted; preserved the
  existing work-log changes. No commit or push performed in this task.

### 2026-09-28 - Prepare location increment for commit and push

- User requested committing and pushing the completed location increment. Reviewed
  the changes and preserved the earlier investigation/planning entries in this commit.
- Fresh `git fetch origin` succeeded. Verification remains the 108 passing tests
  recorded above; no application code changed since that run. `git diff --check` passed.
- Git: committed the location increment on the original student-ID branch. Push
  failed because Git could not obtain the GitHub username with terminal prompts
  disabled. Run `git push` in a terminal and complete sign-in. This outcome is
  included by amending the local commit; no remote location commit was confirmed.

### 2026-09-28 - Review eligible-driver query as next increment

- Reviewed branch/history, API plans and vehicle fields. Location commit `03f0b06`
  matches the remote student-ID branch, confirmed with fresh `git ls-remote`; the
  earlier push blockage is resolved. The working tree was clean on inspection.
- Proposed next scope: `GET /api/drivers/eligible` with pickup coordinates and radius
  in kilometres, AVAILABLE drivers with stored locations and registered vehicles,
  nearest-first results and an empty list when no drivers match. These eligibility
  rules are proposed, not implemented or agreed with other service owners.
- Verification: source and Git inspection only; application tests not rerun.
- Remaining: agree on radius limits and location freshness, implement query validation,
  tests and usage documentation. This review log is local and uncommitted.

### 2026-09-29 - Install Postman and prepare Driver API testing

- Installed Postman 12.30.0 using winget and added the standalone
  `postman/collections/Driver-Vehicle.postman_collection.json` with ten ordered
  requests covering all five implemented endpoints, read-back and negative cases.
  Creation generates unique sample values and saves driverId automatically.
- Added `driver-vehicle-service/docs/postman-testing.md` and linked it from the
  guide index. Existing shared collection and prior work-log changes were preserved.
- Started the existing built service on localhost:8082 (PID 3484), with MongoDB on
  localhost:27017 and isolated demo database `driver_db_postman_20260929`. Left it
  running for manual Postman testing; synthetic driver/vehicle data is retained.
- Verification: Newman executed the collection successfully: ten requests and 17
  assertions, zero failures. Includes expected 400/409/404 responses. No Java code
  changed and Maven tests were not rerun. `git diff --check` passed.
- Desktop automation was unavailable (`Computer Use native pipe is unavailable`),
  including after retry/reset. Postman UI import/run could not be verified; the
  collection was verified through Newman, not through the desktop UI.
- Git: collection, guide and work-log changes are local and uncommitted.

### 2026-09-29 - User-operated Postman verification

- Guided the user through importing the Driver & Vehicle collection and sending
  requests 01-10 manually. Evidence is user-provided screenshots and response JSON,
  not an agent-operated desktop run or an exported collection-run report.
- Screenshots confirmed creation 201 (2/2 tests), lookup 200 (2/2), location response
  200 (2/2), duplicate vehicle 409 VEHICLE_ALREADY_EXISTS (1/1), missing driver 404
  DRIVER_NOT_FOUND (1/1), and final OFFLINE update 200 (2/2).
- Supplied JSON showed the registered vehicle linked to the same driver, AVAILABLE
  status, location read-back and invalid latitude 400 VALIDATION_ERROR. HTTP status
  and test totals were not shown for every JSON-only response.
- Final screenshot confirms OFFLINE retained latitude 6.9271, longitude 79.8612 and
  the original location timestamp. No code changes or application tests rerun.
- Remaining: eligible-driver search and integration work. Collection, guide and
  work-log changes remain local and uncommitted on the student-ID branch.

### 2026-09-29 - Compass verification and test-artifact commit preparation

- User-supplied Compass screenshots/record text confirmed both test drivers and
  vehicles in `driver_db_postman_20260929`. The manual Postman driver is OFFLINE
  with coordinates 6.9271/79.8612 and the same timestamp returned by the API.
  Its Toyota Aqua vehicle has plate PM1790697769832, capacity 4 and matching driverId.
- Reviewed collection JSON and usage guide for committing the completed testing
  increment. Prior Newman verification passed ten requests and 17 assertions; no
  application code changed or application tests rerun. `git diff --check` passed.
- Remaining: eligible-driver search and integration before the October 1 deadline.
- Git: committed collection, guide/index and accumulated work-log updates on the
  original student-ID branch. Push failed because GitHub sign-in requires interactive
  prompts. Recorded this outcome by amending the local commit. User must run
  `git push` in a terminal and complete sign-in; no remote test-artifact commit confirmed.

### 2026-09-29 - Eligible-driver search

- Implemented `GET /api/drivers/eligible?lat={lat}&lng={lng}&radius={km}` with required
  coordinate/radius validation, nearest-first results and one result per driver with
  vehicle details. No matches returns 200 []; invalid query 400 and database failure 503.
- User explicitly confirmed a 50 km maximum radius, registered vehicle requirement
  and five-minute location freshness. Only AVAILABLE drivers qualify; missing/stale/
  future locations are excluded. Configuration can change freshness (0 permits old
  locations for demos). Search does not reserve drivers or alter stored documents.
- Added service/controller/DTOs, Haversine distance utility, injectable clock and
  status/timestamp plus vehicle-driver indexes. Existing coordinate schema is preserved.
  Candidate distance filtering is in Java; larger deployments need geospatial queries
  and pagination. Authentication and interservice reservation remain pending.
- Added 34 tests. Initial verify passed all 142 tests but packaging failed because
  the running demo held the JAR open. Verified and stopped that demo process (3484),
  then `mvn -B -pl driver-vehicle-service -am clean verify` with RIDELINK_MONGO_TESTS=true
  succeeded: 142 tests, zero failures/errors/skips. Eight live tests used isolated
  databases which were dropped. `git diff --check` passed.
- Restarted updated demo on localhost:8082, PID 26912, using retained database
  driver_db_postman_20260929. Newman passed all 13 requests/23 assertions, including
  eligible match, invalid radius and OFFLINE exclusion. One additional synthetic
  driver/vehicle pair was retained. Service left running for user testing.
- Updated Postman collection, service/API docs and eligibility guide. Re-import the
  collection for new requests 05a/05b/10a; refresh availability and location before
  a manual search after a pause. Prior test-artifact commit is bf153b5; local tracking
  reference matches it (no fresh remote check in this increment).
- Git: eligible-driver implementation and documentation are local and uncommitted.
  No commit or push performed for this increment.

### 2026-09-29 - Verify manual demo driver and prepare search commit

- At the user's request, set their existing synthetic Postman driver to AVAILABLE,
  refreshed coordinates 6.9271/79.8612 and queried eligible drivers via HTTP. All
  returned 200; search included the same driver at 0 km with plate PM1790697769832.
  Left this demo driver AVAILABLE; its location naturally expires after five minutes.
- Postman desktop import remains unperformed because Computer Use native pipe is
  unavailable. The updated collection's prior Newman run passed 13 requests/23
  assertions; the unchanged application suite previously passed 142 tests.
- Fresh fetch succeeded and whitespace checks passed. Committed eligible-search
  code, tests, collection and documentation on the student-ID branch. Push failed
  because GitHub username/sign-in requires interactive prompts. Amended the local
  commit to record the outcome; user must run `git push` and sign in. Integration
  and reservation work remain pending.

### 2026-09-29 - Investigate empty Postman search response

- User screenshots showed successful location update but an empty displayed search
  response. Read-only live HTTP checks at 17:58 UTC returned the user's new AVAILABLE
  driver with location timestamp 17:56:41.846Z, and search returned that driver at
  0 km with its Toyota Aqua. Both requests returned 200; no data was modified.
- The displayed empty response may predate the update; advised sending the search
  request again rather than only selecting its tab. No application defect reproduced.
- Verification: live profile/search GETs only; no code changes or tests rerun.
- Git: investigation log is local and uncommitted.

### 2026-09-29 - User-operated eligible-search verification

- User screenshots confirmed updated location followed by 05a search returning 200
  with the same driver, 0 km distance and registered Toyota Aqua. The earlier empty
  displayed response was resolved after resending location and search requests.
- 05b returned 400 VALIDATION_ERROR for radius 51 (2/2 tests); 10 returned 200 with
  OFFLINE and unchanged location (2/2); 10a returned 200 [] (2/2). These are manual
  Postman screenshots, not a new exported collection run. No application changes or
  automated tests rerun; existing 142-test and 23-assertion results remain applicable.
- Remaining: interservice identity/ownership and reservation integration. Local HEAD
  is cd34269 and matches the local remote-tracking reference; no fresh fetch performed.
- Git: preserved prior investigation entry; these work-log updates are uncommitted.

### 2026-09-29 - Review integration compatibility across service branches

- Fresh fetch succeeded. Read Account branch 9fd1dea and Ride branch 9b34d37 without
  merging or changing those components. Driver remains cd34269 on the student-ID branch.
- Found a blocking contract mismatch: Ride uses Long passengerId/driverId in DTOs,
  controller/service/repository; Account and Driver IDs are strings. Ride currently
  assigns supplied IDs without an implemented Driver HTTP client/reservation call.
- Account lookup requires owner/ADMIN JWT. Driver currently has no ownership/account
  verification; the availability toggle is not an ON_TRIP reservation API.
- Created docs/integration-handoff.md under Driver Service and linked the guide index:
  evidence, required ID alignment, actual routes, account-auth proposals, reservation
  semantics to agree and joint acceptance checks. No messages sent to other owners.
- Verification: source inspection and documentation whitespace check only; no live
  cross-service test or application test rerun. Integration is not complete.
- Git: guide/index and accumulated verification log changes are local and uncommitted.

### 2026-09-29 - Fix Ride external ID compatibility at user's request

- User explicitly authorized fixing Ride Service after the compatibility explanation.
  Created sibling worktree C:/Users/mashi/Desktop/RideLink-ride-id-fix on task branch
  fix/ride-external-string-ids from freshly fetched Ride commit 9b34d37. Preserved
  the original Driver branch and all its existing documentation/log changes.
- Changed passengerId/driverId types throughout Ride DTOs, document/accessors,
  controller, service and repository to String. Updated validation, OpenAPI examples,
  README and Ride Postman samples. Ride/fare UUIDs and numeric duration are unchanged.
- Verification: Maven verify with RIDELINK_MONGO_TESTS=true passed 15 tests, zero
  failures/errors/skips: six existing lifecycle, eight new HTTP and one new local
  MongoDB persistence/query test. Random isolated database was dropped. No cloud
  database connected or existing records migrated. Whitespace checks passed.
- Git: committed fix locally as e71492d on fix/ride-external-string-ids. Not pushed
  or merged into the Ride owner's branch; that integration step remains. Main Driver
  workspace documentation/log updates remain uncommitted.
- Remaining: review/merge fix, deliberate handling of legacy numeric Ride references,
  account/driver clients and reservation integration. Full integration is not complete.

### 2026-09-30 - Authenticated Account/Driver/Ride integration

- User authorized completing remaining integration. Preserved the student-ID branch;
  merged component branches in the integration worktree. Earlier handoff docs were
  committed as 3d01634; Ride String-ID fix e71492d is included via merge d05100d.
- Added Account /me verification, shared HTTP authentication support, default-on
  Driver/Ride role and ownership checks, safe dependency errors and service-token
  authentication for internal Driver reserve/release/profile endpoints.
- Added atomic AVAILABLE -> ON_TRIP reservations, unique active-ride index, fresh
  location/vehicle checks, same-ride retries and tombstones preventing stale releases
  or late reservation resurrection. Added Ride optimistic locking and persisted
  assignment/release recovery; completion/cancellation releases only its own driver.
- Added unit/live concurrency tests, isolated three-service Python E2E, authenticated
  Postman collection, local start/stop scripts and usage/recovery documentation.
  Removed embedded cloud URI defaults from Account/Ride runtime configuration.
- Verification: initial Ride fixture failed because creation now persists before
  assignment; updated its repository mock to read the saved record. Final Maven
  verify passed 191 tests (24/146/21), zero failures/errors/skips, live Mongo enabled.
  E2E passed 60 HTTP checks and business assertions including two-ride contention,
  delayed release and persisted recovery across Driver/Ride restarts; Account outage
  denied access. Only temporary test databases were dropped.
- Newman first attempt found services stopped after an interrupted tool run. Restarted
  using start-local.ps1; final collection run passed 19 requests/26 assertions. Demo
  services left on 18081-18083, demo data retained; prior 8082 data was untouched.
- Remaining: fare/payment implementation, shared-data migration and production setup.
  This is verified three-service integration, not a finished four-service platform.
- Git: committed locally on integration. Atomic push of integration and the String-ID
  fix branch failed because GitHub requires interactive username/sign-in. No remote
  update occurred. Run git push origin integration fix/ride-external-string-ids after
  signing in. This outcome is included by amending the local integration commit.

### 2026-09-30 - Retry publishing verified integration

- User requested pushing completed integration. Confirmed clean integration checkout
  at 96f968d and inspected current branches/history before attempting publication.
- Atomic push of integration, fix/ride-external-string-ids and the original student-ID
  branch failed: GitHub username/sign-in requires interactive prompts unavailable to
  this command. No remote update confirmed; no force push or application changes.
- Remaining: run the same push from a user terminal and complete GitHub sign-in.
  Application tests were not rerun because code is unchanged. This log is uncommitted.
### 2026-09-30 - Confirm publication and save next-session handoff

- User supplied GitHub screenshot showing all three branches pushed, then requested
  pausing remaining work until the next session and saving the handoff in AGENTS.md.
- Fresh git ls-remote confirmed integration at 96f968d, fix/ride-external-string-ids
  at e71492d and feature/it24300246-driver-vehicle-service at ad3b3e2. Earlier push
  authentication failure is resolved by the user's subsequent push. No merge into
  main was performed in this session.
- Completed: Account/Driver/Ride integration; prior verification remains 191 passing
  Maven tests, 60 E2E HTTP checks and 19 Postman requests / 26 assertions. No tests
  rerun for this documentation-only handoff. Application code is unchanged.
- Next session: use RideLink-integration; check whether demo ports 18081-18083 are
  running, start integration-tests/start-local.ps1 if needed, then import/run the
  authenticated RideLink-Integration Postman collection in order (01-19).
- Check the Fare/Payment teammate's latest branch/work before deciding remaining
  integration scope. Fare/payment is not complete. PR/review/main merge is separate
  future work, not implied by successful branch publication.
- Updated both checkouts' work logs, preserving older entries. This documentation
  update is local and uncommitted; no new commit or push performed.

### 2026-09-30 - Finish documented four-service backend workflows

- User explicitly authorized all remaining project work, including Fare/Payment,
  according to the project document. Fresh fetch found Fare branch still at 7978efd:
  README claimed implementation but source had only scaffolding. Used repository
  architecture report/API documents; no original assessment PDF was available here.
- Implemented fare estimation/finalization, JPA/Flyway SQL schema, idempotent simulated
  payments, per-ride locking, failed attempts and immutable receipt snapshots. Public
  endpoints enforce Account token/ownership; final amounts use Ride HTTP data, not
  caller-supplied amounts. PostgreSQL default retained; explicit local H2 demo added.
- Added Ride -> Driver nearest matching, no-driver handling, internal Ride lookup,
  completion metrics validation and durable automatic payment/recovery with stable key.
  Added Swagger bearer authorization and hid internal service-token endpoints.
- Expanded scripts/collection/E2E to all four services. Launcher restores caller env
  and fails early for exited processes. CI now builds shared modules, triggers on
  integration, and provides Mongo/PostgreSQL for full workflow verification.
- Reconciled runtime/API/architecture docs and added requirements-status.md. Replaced
  embedded cloud credential examples in .env.example and Ride README with local URIs;
  historical values are not removed from Git history and may require owner rotation.
- Verification: root Maven verify with RIDELINK_MONGO_TESTS=true passed 204 tests,
  zero failures/errors/skips. Added nine Fare formula/SQL/concurrency tests and four
  Ride matching/payment tests. Local E2E passed 88 HTTP status checks plus assertions,
  including ownership, no-match, illegal transitions, payment failure/retry, receipt
  persistence, concurrent reservation, delayed release and payment/release recovery
  after restarts. Temporary Mongo and H2 data removed; no shared database migrated.
- Newman passed all 37 requests and 51 assertions. Four demo services left running
  on 18081-18084 with retained demo data. Local verification used H2, not PostgreSQL;
  hosted PostgreSQL CI execution and peer review remain external verification steps.
- Git: committed implementation/docs on integration, preserving prior handoff entries.
  Push failed because GitHub username/sign-in requires unavailable interactive prompts.
  No remote update confirmed. Run git push origin integration from a signed-in terminal.
  This outcome is recorded by amending the local implementation commit.

### 2026-09-30 - Verify published four-service implementation and PostgreSQL CI

- Fresh remote inspection confirmed integration at 88f3fbd and main at f04a7c6.
  The earlier push blockage was resolved outside the agent's failed push attempt.
- GitHub Actions API confirmed run 36655685604 completed successfully for 88f3fbd:
  all four component jobs and full-system aggregation passed. The latter runs the
  real four-service E2E with PostgreSQL 16 and MongoDB 8.0, resolving the previous
  PostgreSQL verification gap. Updated requirements-status.md and current state.
- No open PR was returned. Git credential helper has no usable GitHub credential;
  browser automation inventory was empty and Chrome unavailable. Requested user
  sign-in so the already-authorized PR/merge can proceed. No merge performed.
- Verification: remote hash and hosted job/step results; application code unchanged,
  so local suites were not rerun. Documentation whitespace checks passed. Committed
  this evidence update locally; publication awaits GitHub sign-in. PR text is prepared
  in ignored tmp/local-integration/pr-body.txt. No peer approval or main merge is claimed.

### 2026-09-30 - Resolve GitHub account selection and prepare main integration

- Git Credential Manager had two accounts; unqualified lookup failed. Explicit account
  lookup confirmed Dinuli2004 has repository write access. Published documentation
  commit 50a4170 successfully without exposing credentials or changing permissions.
- GitHub rejected PR creation because main and integration have unrelated histories.
  Merged main's original f04a7c6 history into integration without rewriting either
  history. Resolved the sole README add/add conflict using the comprehensive current
  README, which already covers main's original project description. No application
  code changed. Existing branches are retained.
- Fresh fetch also found Fare owner's branch advanced to 56ec753 with domain/service
  scaffolding. Preserved it without replacing the verified four-service implementation;
  that branch is not claimed as merged or as this user's individual contribution.
- Verification: remote access and diff inspection; local application suites not rerun
  for README/history-only changes. PR and resulting hosted checks are next.

### 2026-09-30 - Deliver verified backend to main

- Created PR #1 and waited for all five checks in run 36754310117 to pass on exact
  head 5631d67. Full-system verification used PostgreSQL 16 and MongoDB 8.0.
- Merged PR #1 using GitHub's merge API with the tested head SHA guard. GitHub
  confirmed merge 1605e3e; fresh fetch confirmed main and local integration was
  fast-forwarded to it. Existing component branches were retained. No independent
  peer approval is claimed; delivery was authorized by the user.
- Local demo services remain listening on 18081-18084. Prior local verification:
  204 Maven tests, 88 E2E HTTP checks plus assertions, 37 Newman requests/51 assertions.
- Updated delivery documentation; no application changes since the passing CI.
  Final documentation is committed/published with normal fast-forward pushes.
- Assignment backend delivery is complete. Production deployment, shared legacy-data
  migration and rotation of any historical live cloud credentials remain owner-specific
  operations, not prerequisites for the isolated assignment demo.

### 2026-10-01 - Rehearse repeatable final demo

- Verified remote main at 7a3237d and successful main CI run 36754806785. Four local
  service ports remain listening. No Java implementation changes were needed.
- An initial npm-based rehearsal was interrupted during startup/request execution;
  its synthetic driver remained AVAILABLE. The next run exposed equal-distance
  matching to that older driver, causing expected ownership rejection downstream.
- Changed the integration collection to derive distinct nearby pickup coordinates
  from its run UUID, use them consistently for driver location and ride pickup, and
  assert the matched driver ID in both scenarios. This preserves automatic matching
  while preventing previous demo records at the fixed pickup from winning a tie.
- Verification: cached Newman completed two consecutive runs with all 37 requests
  and 53 assertions passing each, zero failures. Updated the demo guide; whitespace
  checks passed. Java suites were not rerun for this collection-only change.
- Synthetic rehearsal data is retained in demo databases. Full Newman reports are
  ignored local files under tmp/local-integration and may contain demo tokens;
  they are not published. Changes prepared for normal commit/push to main/integration.

### 2026-10-01 - Save automated demo evidence

- User requested running the collection and saving results. UI inventory returned
  no apps/browsers and no Postman connector was available, so desktop import/run
  and a genuine Postman screenshot could not be performed.
- Ran the published 81ccb2a collection with cached Newman against the four local
  services: 37 requests, 53 assertions, zero failures. Generated sanitized HTML/text
  reports in tmp/local-integration/RideLink-test-evidence.{html,txt}, with request
  names/status/assertion totals only. Raw reports containing tokens stay ignored.
- Validated all 37 evidence rows against the actual Newman JSON. Reports explicitly
  identify Newman execution, not desktop screenshots. No application code changed.
- This evidence log update is local/uncommitted; desktop import remains user-operated.

### 2026-10-01 - Commit final evidence and work logs

- User requested committing and pushing all remaining work, including AGENTS.md.
  Preserved the pending evidence log and copied sanitized HTML/text reports into
  docs/evidence/ for version control. Raw reports, tokens, runtime databases and
  generated build files remain ignored; no credentials are included in evidence.
- Confirmed main CI run 36759345753 passed for 81ccb2a. Latest Newman evidence is
  37 requests/53 assertions with zero failures. Documentation/evidence-only update;
  no application tests rerun. Checked whitespace and report contents before commit.
- Reconciled the original student-ID checkout's pending handoff documentation too.
  Integration evidence/logs and original component logs are committed separately;
  publishing uses normal fast-forward pushes and retains all component branches.

## Entry template

```markdown
### YYYY-MM-DD - Short task title

- Changes: what was completed and why; relevant files/endpoints.
- Verification: exact check and outcome, or not run with reason.
- Remaining: relevant unfinished work, if any.
- Git: existing commit reference, or local/uncommitted status at writing.
```

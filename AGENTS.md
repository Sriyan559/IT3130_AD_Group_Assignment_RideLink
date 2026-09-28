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

## Current state (last checked 2026-09-28)

- Implemented: `POST /api/drivers`, `GET /api/drivers/{driverId}` and
  `POST /api/drivers/{driverId}/vehicles`, `PUT /api/drivers/{driverId}/availability`
  and `PUT /api/drivers/{driverId}/location`.
- Location accepts valid latitude/longitude in every availability state, atomically
  replaces only the latest location and adds a server UTC timestamp. Profile responses
  include nullable location; older documents remain readable.
- Availability accepts AVAILABLE/OFFLINE, atomically updates only the status, supports
  repeated requests and rejects toggles for ON_TRIP drivers with 409 DRIVER_ON_TRIP.
- Vehicle registration checks driver existence, validates attributes, normalizes plates
  and uses a unique MongoDB plate index. It does not change driver availability.
- Creation includes validation, normalization, initial OFFLINE status, MongoDB persistence
  and unique account/licence indexes. Lookup includes a structured missing-driver error.
- Error responses cover invalid requests, duplicate profiles, missing profiles and
  database access failures.
- Latest verification: `mvn -B -pl driver-vehicle-service -am verify` with
  `RIDELINK_MONGO_TESTS=true` passed 108 tests with zero failures/errors/skips on
  2026-09-28, using local JDK 21 and Java 17 compilation target. Includes six live
  HTTP/MongoDB tests against isolated databases that were dropped after testing,
  including concurrent location/availability updates.
- Earlier live smoke checks on 2026-09-26 passed against local MongoDB in the
  isolated database `driver_db_smoke_20260926152647`: driver create/read, vehicle
  registration, normalized duplicate rejection, missing driver and invalid capacity.
  Swagger returned 200; registration preserved OFFLINE status. Concurrent vehicle
  registration remains untested.
- Authentication, ownership checks and Account Service verification are pending.
- API documentation now distinguishes implemented `/api/drivers` endpoints from proposed
  `/api/v1/drivers` routes. Agree on versioning before integration.
- Guides: [creation](driver-vehicle-service/docs/driver-profile-create.md),
  [lookup](driver-vehicle-service/docs/driver-profile-get.md),
  [vehicle registration](driver-vehicle-service/docs/vehicle-registration.md),
  [availability](driver-vehicle-service/docs/driver-availability.md),
  [location](driver-vehicle-service/docs/driver-location.md).

## Next work (planned, not implemented)

- Next Driver & Vehicle increment: eligible-driver queries using stored locations;
  agree on radius, vehicle requirements and location freshness before implementation.
- Add appropriate validation, error handling, tests and usage documentation for each increment.
- Add repeatable integration/concurrency tests for MongoDB constraints.
- Agree on versioned routes, identity/security and eligibility rules with the other service owners.

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

## Entry template

```markdown
### YYYY-MM-DD - Short task title

- Changes: what was completed and why; relevant files/endpoints.
- Verification: exact check and outcome, or not run with reason.
- Remaining: relevant unfinished work, if any.
- Git: existing commit reference, or local/uncommitted status at writing.
```

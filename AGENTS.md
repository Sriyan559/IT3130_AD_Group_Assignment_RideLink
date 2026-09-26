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

## Current state (last checked 2026-09-26)

- Implemented: `POST /api/drivers` and `GET /api/drivers/{driverId}`.
- Creation includes validation, normalization, initial OFFLINE status, MongoDB persistence
  and unique account/licence indexes. Lookup includes a structured missing-driver error.
- Error responses cover invalid requests, duplicate profiles, missing profiles and
  database access failures.
- Latest verification: `mvn -B -pl driver-vehicle-service -am verify` passed 17 tests
  with zero failures/errors/skips on 2026-09-26, using local JDK 21 and Java 17 compilation target.
  Tests use mocks; live MongoDB behavior was not verified in that run.
- Authentication, ownership checks and Account Service verification are pending.
- Planned documentation uses `/api/v1/drivers`, while implemented local endpoints use
  `/api/drivers`. Reconcile the route contract before integration.
- Guides: [creation](driver-vehicle-service/docs/driver-profile-create.md),
  [lookup](driver-vehicle-service/docs/driver-profile-get.md).

## Next work (planned, not implemented)

- Choose the next small Driver & Vehicle increment: vehicle registration, availability
  updates, simulated location updates or eligible-driver queries.
- Add appropriate validation, error handling, tests and usage documentation for each increment.
- Verify the implemented endpoints against a running local MongoDB instance.
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

## Entry template

```markdown
### YYYY-MM-DD - Short task title

- Changes: what was completed and why; relevant files/endpoints.
- Verification: exact check and outcome, or not run with reason.
- Remaining: relevant unfinished work, if any.
- Git: existing commit reference, or local/uncommitted status at writing.
```

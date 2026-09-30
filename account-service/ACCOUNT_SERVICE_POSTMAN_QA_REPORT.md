# RideLink Account Service Postman QA Report

Owner: Fernando B S C (IT24103775)  
Verification date: 29 September 2026

## 1. Scope

This report covers Account Service only: its six implemented API operations,
Swagger/OpenAPI, validation, MongoDB persistence, authentication, JWT handling,
owner authorization, role authorization, safe profile updates, status controls,
error responses, and automated regression tests. Driver & Vehicle, Ride
Management, and Fare/Payment services were not modified or tested.

## 2. Environment

- Windows PowerShell
- Java 17.0.12
- Maven Wrapper using Maven 3.9.9
- Spring Boot 3.3.4
- MongoDB Atlas connection supplied locally through `MONGODB_URI`
- JWT key supplied locally through `JWT_SECRET`
- No secrets or returned JWTs were written to repository files or this report

Security note: a MongoDB credential was exposed outside the repository during
troubleshooting. It must be rotated in Atlas. Repository scans found no MongoDB
URI or compact JWT token in the generated Postman artifacts.

## 3. Actual Base URL

`http://localhost:8081`

The configured default port and live HTTP verification both confirmed 8081.

## 4. Actual Endpoint Inventory

| Method | Actual URL | Controller | Authentication | Authorization |
|---|---|---|---|---|
| POST | `/api/auth/register/passenger` | `AuthController` | None | Public |
| POST | `/api/auth/register/driver` | `AuthController` | None | Public |
| POST | `/api/auth/login` | `AuthController` | None | Public |
| GET | `/api/accounts/{id}` | `AccountController` | Bearer JWT | Owner or ADMIN |
| PUT | `/api/accounts/{id}` | `AccountController` | Bearer JWT | Owner or ADMIN |
| PATCH | `/api/accounts/{id}/status` | `AccountController` | Bearer JWT | ADMIN only |

## 5. Test Data Strategy

Live tests used fictional `example.com` addresses containing a millisecond
timestamp. Passwords were temporary QA values. Returned tokens were held only in
process memory. The QA run created passenger and driver test documents so that
login and persistence could be verified. No other service database was queried.

## 6. Passenger Registration Tests

Valid passenger registration returned HTTP 201 with an ID, `PASSENGER` role,
and `ACTIVE` status. The response contained neither password nor passwordHash.
The earlier HTTP 500 was an environment/MongoDB-write issue and was no longer
reproducible after the connection configuration was corrected.

## 7. Driver Registration Tests

Valid driver registration returned HTTP 201 with an ID, `DRIVER` role, and
`ACTIVE` status. No vehicle, availability, service-area, or location data was
created. The response contained no credential material.

## 8. Login Tests

Passenger and driver login each returned HTTP 200, a non-empty signed JWT, and
safe account data with the correct role. Wrong-password and unknown-account
requests returned HTTP 401 without a token. Blank fields and malformed JSON
returned HTTP 400.

## 9. JWT Tests

Valid passenger and driver JWTs authenticated protected requests. Missing,
malformed, invalid, and tampered bearer values returned HTTP 401. JWT unit tests
also cover generation, claims, and expiration rejection. A live expired-token
wait was not necessary because expiration is covered by the automated test.

## 10. Profile Tests

An owner retrieved their profile with HTTP 200. A passenger attempting to read
the driver's profile received HTTP 403. Responses included safe identity/profile
fields and excluded passwordHash.

## 11. Status Tests

No-token status requests returned HTTP 401. PASSENGER and DRIVER tokens returned
HTTP 403. Invalid status JSON returned HTTP 400 before authorization-sensitive
business processing where applicable. Successful ADMIN transition, suspended
login, and disabled login remain blocked because no legitimate ADMIN bootstrap
mechanism exists. No database bypass was used.

## 12. RBAC Tests

| Operation | Public | PASSENGER | DRIVER | ADMIN |
|---|---|---|---|---|
| Register passenger | Allowed | Allowed | Allowed | Allowed |
| Register driver identity | Allowed | Allowed | Allowed | Allowed |
| Login | Allowed | Allowed | Allowed | Allowed |
| Get own profile | No | Allowed | Allowed | Allowed |
| Get another profile | No | Forbidden | Forbidden | Allowed by code/tests |
| Update own profile | No | Allowed | Allowed | Allowed |
| Change account status | No | Forbidden | Forbidden | Allowed by code/tests; live blocked |

## 13. Validation Tests

Blank/missing name, invalid/blank/missing email, blank/missing/short password,
invalid phone, and malformed JSON were rejected with HTTP 400. DTO inspection
confirmed name maximum 100, email maximum 254, password length 8-72, and the
implemented phone pattern. Errors did not expose stack traces or database data.

## 14. MongoDB Persistence

The service uses database `ridelink_account_db` by default and the `accounts`
collection. Passenger and driver inserts succeeded. A profile update was read
back through a separate GET and retained the updated name and phone. Repeating
the same normalized emails returned HTTP 409, confirming application pre-check
and unique-email enforcement behavior.

## 15. Password Security

`BCryptPasswordEncoder` hashes registration passwords before persistence.
Authentication uses `matches`, not plaintext comparison. Response DTOs have no
password fields, live response scans found no passwordHash, and unit tests verify
hashing input. No actual stored hash was copied into QA output.

## 16. Error Response Review

Validation, malformed JSON, duplicate email, invalid credentials, unauthorized,
and forbidden responses use the `ApiError` contract with timestamp, status,
stable error code, safe message, path, and field errors where relevant. The
initial database failure correctly returned a generic 500 client message without
leaking internals.

## 17. Defects Found

1. Environment defect: registration initially returned HTTP 500 during MongoDB
   write. Reads still worked, indicating connection-string/write configuration
   rather than a controller route defect.
2. Security incident: a live MongoDB credential was pasted during debugging and
   must be rotated.
3. Functional limitation: no legitimate ADMIN bootstrap mechanism exists, so
   live successful status transitions cannot be exercised.

## 18. Fixes Applied

- Maven Wrapper had already been restored for Account Service and was used for
  repeatable Java 17 builds.
- The MongoDB runtime configuration was corrected externally; registration then
  passed for both account roles. No secret was committed.
- A dedicated safe Account Service Postman collection and environment were added.
- No backend source or API-contract change was required by the verified results.

## 19. Regression Results

After live API testing, `./mvnw.cmd clean test` completed with `BUILD SUCCESS`.
All owner checks, status-role denials, validation behavior, login behavior, and
safe DTO assertions remained green.

## 20. Automated Test Results

- Tests run: 23
- Passed: 23
- Failed: 0
- Errors: 0
- Skipped: 0

## 21. Complete Test Matrix

| ID | Scenario | Result | Evidence |
|---|---|---|---|
| AS-001 | Application startup | PASS | Live HTTP service on port 8081 |
| AS-002 | Swagger | PASS | HTTP 200 |
| AS-003 | OpenAPI docs | PASS | HTTP 200 JSON |
| AS-004 | Passenger registration | PASS | 201, ID, PASSENGER/ACTIVE |
| AS-005 | Driver registration | PASS | 201, ID, DRIVER/ACTIVE |
| AS-006 | Passenger duplicate email | PASS | 409 |
| AS-007 | Driver duplicate email | PASS | 409 |
| AS-008 | Invalid email | PASS | 400 |
| AS-009 | Missing name | PASS | 400 |
| AS-010 | Missing password | PASS | 400 |
| AS-011 | Valid passenger login | PASS | 200 and JWT |
| AS-012 | Valid driver login | PASS | 200 and JWT |
| AS-013 | Wrong password | PASS | 401, no JWT |
| AS-014 | Unknown email | PASS | 401, no enumeration |
| AS-015 | JWT issued/accepted | PASS | Protected GET 200 |
| AS-016 | Protected route without JWT | PASS | 401 |
| AS-017 | Protected route invalid JWT | PASS | 401 |
| AS-018 | Get passenger profile | PASS | 200, safe DTO |
| AS-019 | Update passenger profile | PASS | 200 and read-back confirmed |
| AS-020 | Restricted-field attack | PASS | Role/status/ID unchanged |
| AS-021 | Status without auth | PASS | 401 |
| AS-022 | Passenger status attempt | PASS | 403 |
| AS-023 | Driver status attempt | PASS | 403 |
| AS-024 | Admin status change | BLOCKED | No legitimate admin bootstrap |
| AS-025 | Suspended login | BLOCKED | Depends on legitimate admin transition |
| AS-026 | Disabled login | BLOCKED | Depends on legitimate admin transition |
| AS-027 | Password hash exposure | PASS | Absent from live API responses |
| AS-028 | MongoDB persistence | PASS | Inserts and update read-back succeeded |
| AS-029 | Unique email enforcement | PASS | Repeat registrations returned 409 |
| AS-030 | Error consistency | PASS | Safe structured errors verified |

Additional passing checks covered blank fields, short password, invalid phone,
malformed login/registration JSON, cross-account access, malformed authorization,
tampered JWT, and invalid status input.

## 22. Remaining Blockers

1. Rotate the exposed Atlas database password and update only the local
   `MONGODB_URI` value.
2. ADMIN success, SUSPENDED login, and DISABLED login cannot be verified live
   until the project defines a legitimate operational admin bootstrap mechanism.

## 23. Final Status

**PARTIAL PASS**: all testable Account Service functions passed. The required
ADMIN-only success path and dependent status-login scenarios are explicitly
blocked, not failed. There are no known failing testable Account Service API
scenarios after the MongoDB environment correction.

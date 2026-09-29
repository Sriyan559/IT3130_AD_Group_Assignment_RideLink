# Account Service Implementation Report

## 1. Executive Summary

The Account Service scaffold was completed as an independently executable Java 17/Spring Boot 3.3.4 microservice. It implements the six specified APIs, MongoDB persistence, BCrypt credentials, JWT authentication, role/status authorization, validation, consistent errors, OpenAPI documentation, and 23 passing automated tests. No Driver, Ride, Fare, or Payment business logic was implemented.

## 2. Scope

Owner: Fernando B S C (IT24103775). Scope is registration, login/token issuance, roles, profiles, account status, and security for passenger/driver identities. Vehicle, availability, location, ride, fare, and payment concerns remain outside this service.

## 3. Files Reviewed

The root/module POMs, Account Service Java/resources/tests/docs, root environment example, shared Postman collection/environment, architecture/API documentation, and the complete 14-page assignment guide were reviewed. Other service source listings were inspected only to confirm boundaries and conventions.

## 4. Architecture

Thin REST controllers call `AuthService` and `AccountService`. Dedicated request/response records isolate API contracts. `AccountRepository` is the only persistence gateway. `JwtAuthenticationFilter` creates a stateless Spring Security context after cryptographic validation and a current ACTIVE-account lookup. Method security applies ownership and role rules. Central handlers produce one error shape.

## 5. MongoDB Data Ownership

The service exclusively owns `ridelink_account_db`. Other services may store an account's stable string ID but must not query or modify the Account database.

## 6. MongoDB Collection Design

Collection `accounts` stores `id`, `fullName`, normalized unique `email`, `passwordHash`, `role`, `phone`, `status`, `createdAt`, and `updatedAt`. Spring Data auditing maintains timestamps and `@Indexed(unique=true)` enforces email uniqueness in addition to the service check.

## 7. Implemented APIs

- `POST /api/auth/register/passenger`
- `POST /api/auth/register/driver`
- `POST /api/auth/login`
- `GET /api/accounts/{id}`
- `PUT /api/accounts/{id}`
- `PATCH /api/accounts/{id}/status`

## 8. Authentication Flow

Login normalizes email, performs a generic lookup/failure response, verifies the BCrypt hash, rejects non-ACTIVE status, and signs a token. Each protected request verifies the token and confirms the referenced stored account is still ACTIVE with the same role before establishing authentication.

## 9. JWT Design

JWTs contain subject/account ID, email, role, issue time, and expiry only. HMAC-SHA signing requires a secret of at least 32 bytes. Secret and lifetime come from `JWT_SECRET` and `JWT_EXPIRATION_MS`; tokens and secrets are never logged.

## 10. RBAC Rules

Registration/login/Swagger are public. A profile can be read/updated by its owner or ADMIN. Status change requires ADMIN. Public registration cannot create ADMIN. A role claim alone is insufficient: the filter also checks current database role and ACTIVE status.

## 11. Validation Rules

Names are required and at most 100 characters, emails are required/valid/at most 254 characters, passwords are 8-72 characters, and phone numbers use a bounded international-friendly pattern. Profile update accepts only full name and phone. Status is a required enum.

## 12. Error Handling

`ApiError` contains timestamp, status, stable error code, safe message, path, and field errors. Duplicate email is 409; invalid credentials/missing or invalid token is 401; suspended/disabled login and insufficient role are 403; missing accounts are 404; malformed/invalid input is 400. Stack traces and internals are not returned.

## 13. Swagger/OpenAPI

Springdoc exposes schemas and documented operations. A global HTTP bearer/JWT scheme enables Swagger authorization. Public auth operations explicitly suppress the global security requirement.

## 14. Tests Implemented

Tests cover passenger/driver registration, email normalization/duplicate rejection, valid/unknown/bad-password login, suspended/disabled rejection, profile retrieval/update/status service logic, request validation, public registration, safe responses, missing/invalid JWT, passenger/driver forbidden status changes, admin access, JWT claims, expiry, and weak-secret rejection.

## 15. Test Results

Command executed with JDK 17 and Maven 3: `mvn -pl account-service test`. Result on 2026-09-29: **BUILD SUCCESS; 23 tests run, 0 failures, 0 errors, 0 skipped**. Compilation covered 42 main source files and 5 test source files.

## 16. Security Review

The repository scan found no committed real Atlas URI or JWT secret. Example values are visibly placeholders. BCrypt is used, DTOs exclude the password hash, JWT expiry/signature checks exist, protected endpoints use Spring Security, and errors are sanitized. A race on duplicate registration is also covered by MongoDB's unique index and mapped to 409.

## 17. MongoDB Connectivity Verification

Not run: no real `MONGODB_URI` was supplied, and production Atlas must not be used by automated tests. Configuration binding and MongoDB code compiled. Live connectivity, Swagger loading, and end-to-end HTTP smoke tests remain environment-dependent and are not claimed as passed.

## 18. Requirement Traceability

See `ACCOUNT_SERVICE_REQUIREMENTS_TRACEABILITY.md`. Environment-independent requirements pass; live MongoDB and Swagger runtime checks are marked PARTIAL rather than fabricated.

## 19. Files Created

Created Account document/enums/repository, six DTOs, mapper, two services, two controllers, four exception classes plus advice, JWT/filter/authorization/security handlers, Mongo/security config, four test classes, and the two requested reports.

## 20. Files Modified

Modified Account POM, application configuration/examples, Account README, OpenAPI configuration, root safe environment example, and the shared Postman Account section/environment.

## 21. Existing Files Preserved

Existing package conventions, base package, Java/Spring versions, parent/module structure, ports, application entry point, and other services' functional code were preserved. Pre-existing working-tree deletions were not altered.

## 22. Known Limitations

No refresh/revocation flow, email verification, reset-password flow, MFA, rate limiting, audit-event store, or automated admin bootstrap is included. Integration tests mock persistence to avoid production Atlas. Those are appropriate production enhancements, not assignment scope.

## 23. Viva Notes

- **Why separate?** Identity/security change and scale independently and require a strict trust boundary.
- **Owned data:** Credentials, passenger/driver identity, role, safe profile, and account status.
- **Why its own MongoDB?** Database ownership keeps the service autonomous and prevents hidden coupling.
- **Why Driver Service cannot read it:** Cross-database reads bypass authorization/contracts and couple schemas; use stable IDs/APIs.
- **BCrypt:** A salted adaptive password hash whose work factor makes guessing expensive.
- **Why hash passwords:** A database leak must not immediately reveal reusable plaintext credentials.
- **JWT flow:** Login signs time-limited identity/role claims; the filter validates signature/expiry and current account state.
- **Authentication vs authorization:** Authentication proves identity; authorization decides permitted operations.
- **Roles:** PASSENGER and DRIVER represent platform identities; operational driver/vehicle data belongs elsewhere.
- **401 vs 403:** 401 means authentication is absent/invalid; 403 means an authenticated identity lacks permission or cannot log in due to status.
- **Duplicate prevention:** Normalized pre-check gives a friendly response; a unique MongoDB index closes concurrent races.
- **DTO purpose:** Stable, validated API contracts prevent persistence fields from leaking or being mass-assigned.
- **Why no passwordHash response:** Hashes are sensitive offline-guessing material and clients never need them.
- **Secure Atlas configuration:** URI/DB are injected from environment variables; real credentials are absent from source.
- **What tests prove:** Business rules, safe mapping, validation, JWT cryptography/expiry, and access rules pass in 23 automated checks.
- **Production limitations:** Add key rotation/asymmetric signing, token revocation/refresh, MFA, verified contacts, throttling, monitoring, and controlled admin provisioning.

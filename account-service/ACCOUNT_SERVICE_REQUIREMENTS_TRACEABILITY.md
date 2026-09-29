# Account Service Requirements Traceability

Verification date: 2026-09-29

| Requirement | Implementation | Tests | Status |
|---|---|---|---|
| Passenger registration | `AuthController`, `AuthService` with server-owned PASSENGER/ACTIVE | `registerPassenger_success`, public registration API test | PASS |
| Driver registration | Dedicated route; identity only; returns stable ID | `registerDriver_success` | PASS |
| Login | Normalized lookup, BCrypt comparison, ACTIVE check | Valid, bad password, unknown, suspended, disabled tests | PASS |
| JWT issuance | HMAC signed token with subject, email, role, issued/expiry times | `JwtServiceTest` | PASS |
| Role management | `Role` enum; registration does not accept role | Registration and RBAC API tests | PASS |
| View profile | Owner-or-ADMIN protected GET | Missing/invalid token security tests | PASS |
| Update profile | Owner-or-ADMIN PUT; only name/phone DTO | Service success and API validation tests | PASS |
| Account status | ADMIN-only PATCH with enum DTO | Service update plus passenger/driver/admin API tests | PASS |
| Password hashing | `BCryptPasswordEncoder`; only hash persisted | Registration service verification | PASS |
| Credential validation | Generic invalid-credential response | Login failure tests | PASS |
| Authentication | Stateless JWT filter verifies signature/expiry and active stored account | Missing/invalid/expired JWT tests | PASS |
| RBAC | Method security for owner/ADMIN and ADMIN-only status | Passenger, driver, admin API tests | PASS |
| Validation | Jakarta Bean Validation on request DTOs | Profile validation API test | PASS |
| Consistent errors | Advice plus security JSON handlers using `ApiError` | API assertions for validation/401/403 | PASS |
| MongoDB persistence | Spring Data MongoDB repository, collection, auditing, unique email index | Repository mocked in unit/slice tests; live Atlas not tested | PARTIAL |
| Swagger/OpenAPI | Six documented operations and HTTP bearer security scheme | Compiled successfully; live UI not smoke-tested | PARTIAL |
| Unit tests | JUnit 5, Mockito, MockMvc/security coverage | 23 run, 23 passed, 0 failed | PASS |

`PARTIAL` means implementation compiled and its local automated checks passed, but environment-dependent live verification was not possible without an Atlas URI/JWT secret and running infrastructure.

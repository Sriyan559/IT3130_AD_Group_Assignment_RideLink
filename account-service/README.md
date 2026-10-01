# Account Service

Owner: Fernando B S C - IT24103775

The Account Service owns RideLink passenger/driver identity, credentials, roles, profiles, and account status. It does not own vehicle, availability, ride, fare, or payment data.

## Technology

- Java 17 and Spring Boot 3.3.4
- Spring Security with signed JWT bearer authentication
- Spring Data MongoDB and MongoDB Atlas
- Bean Validation, Springdoc OpenAPI, JUnit 5, Mockito, Maven

## Prerequisites and configuration

Install JDK 17 and Maven 3.9+, provision a MongoDB Atlas database, and set these environment variables. Never commit their real values.

```text
MONGODB_URI=<Atlas connection string>
MONGODB_DATABASE=ridelink_account_db
JWT_SECRET=<cryptographically random value of at least 32 bytes>
JWT_EXPIRATION_MS=3600000
ACCOUNT_SERVICE_PORT=8081
```

To create the first administrator, start the service once with
`ACCOUNT_ADMIN_BOOTSTRAP_ENABLED=true`, `ACCOUNT_ADMIN_EMAIL`, and
`ACCOUNT_ADMIN_PASSWORD` (8-72 characters). Then disable bootstrap and log in through
`POST /api/auth/login`; use that returned admin JWT for account-status requests.

The service owns the `accounts` collection. A unique MongoDB index on normalized `email` is created through Spring Data mapping. Other services must retain stable account IDs and use API contracts rather than reading this database.

## Build, test, and run

From the repository root:

```powershell
mvn -pl account-service clean test
mvn -pl account-service spring-boot:run
```

Swagger UI: `http://localhost:8081/swagger-ui/index.html`

OpenAPI JSON: `http://localhost:8081/v3/api-docs`

Use Swagger's Authorize control with the JWT returned by login. The HTTP bearer scheme supplies the `Bearer` prefix.

## API

| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/register/passenger` | Public | Create an ACTIVE PASSENGER |
| POST | `/api/auth/register/driver` | Public | Create an ACTIVE DRIVER identity |
| POST | `/api/auth/register/admin` | Registration key | Create an ACTIVE ADMIN when explicitly enabled |
| POST | `/api/auth/login` | Public | Validate credentials/status and issue JWT |
| GET | `/api/accounts/{id}` | Owner or ADMIN | View safe profile |
| PUT | `/api/accounts/{id}` | Owner or ADMIN | Update full name and phone |
| PATCH | `/api/accounts/{id}/status` | ADMIN | Set ACTIVE, SUSPENDED, or DISABLED |

Registration never accepts a role or status. Profile updates cannot change email, role, status, ID, timestamps, or credentials. Passwords are BCrypt hashes and `passwordHash` is absent from every response DTO.

## Roles and statuses

Roles are `PASSENGER`, `DRIVER`, and `ADMIN`. Public registration can create only the first two. Statuses are `ACTIVE`, `SUSPENDED`, and `DISABLED`; only ACTIVE accounts can log in and authenticate requests.

Admin registration is disabled by default. Temporarily set `ACCOUNT_ADMIN_REGISTRATION_ENABLED=true`
and a random `ACCOUNT_ADMIN_REGISTRATION_KEY` of at least 16 characters, then send that key in the
`X-Admin-Registration-Key` header. Disable registration again after provisioning. The startup bootstrap
remains available as an alternative and is idempotent.

## Error contract

Errors contain `timestamp`, HTTP `status`, stable `error` code, safe `message`, request `path`, and validation `fieldErrors`. Authentication failures do not expose whether an email exists.

## Known limitations

- Atlas connectivity and live endpoint smoke tests require user-supplied credentials and network access.
- JWT revocation/refresh, email verification, password reset, login throttling, and MFA are production enhancements.
- Admin bootstrap is intentionally outside the public API.

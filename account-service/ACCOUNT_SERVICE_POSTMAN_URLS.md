# Account Service Verified URLs

Verification date: 29 September 2026

Base URL: `http://localhost:8081`

| # | Method | URL | Auth | Role | Purpose | Status |
|---:|---|---|---|---|---|---|
| 1 | GET | `http://localhost:8081/swagger-ui/index.html` | None | Public | Swagger UI | PASS - HTTP 200 |
| 2 | GET | `http://localhost:8081/swagger-ui.html` | None | Public | Configured Swagger alias | PASS - redirects/serves UI |
| 3 | GET | `http://localhost:8081/v3/api-docs` | None | Public | OpenAPI JSON | PASS - HTTP 200 |
| 4 | POST | `http://localhost:8081/api/auth/register/passenger` | None | Public | Create PASSENGER identity | PASS - HTTP 201 |
| 5 | POST | `http://localhost:8081/api/auth/register/driver` | None | Public | Create DRIVER identity | PASS - HTTP 201 |
| 6 | POST | `http://localhost:8081/api/auth/login` | None | Public | Authenticate and issue JWT | PASS - HTTP 200 |
| 7 | GET | `http://localhost:8081/api/accounts/{id}` | Bearer JWT | Owner or ADMIN | Read safe profile | PASS - HTTP 200 |
| 8 | PUT | `http://localhost:8081/api/accounts/{id}` | Bearer JWT | Owner or ADMIN | Update name and phone | PASS - HTTP 200 |
| 9 | PATCH | `http://localhost:8081/api/accounts/{id}/status` | Bearer JWT | ADMIN | Change account status | PARTIAL - RBAC verified; admin success blocked |

## Copy-ready Postman URLs

```text
{{account_service_url}}/swagger-ui/index.html
{{account_service_url}}/v3/api-docs
{{account_service_url}}/api/auth/register/passenger
{{account_service_url}}/api/auth/register/driver
{{account_service_url}}/api/auth/login
{{account_service_url}}/api/accounts/{{passenger_id}}
{{account_service_url}}/api/accounts/{{passenger_id}}/status
```

Registration and login routes require `POST`; opening them as browser links
sends `GET` and is not a valid test. The status endpoint has no legitimate ADMIN
bootstrap workflow in the current implementation, so an ADMIN success request
must not be fabricated.

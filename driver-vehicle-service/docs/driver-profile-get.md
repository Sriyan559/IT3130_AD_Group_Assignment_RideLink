# Second increment: retrieve a driver profile

Implemented: `GET /api/drivers/{driverId}`. Use the `id` returned by
`POST /api/drivers`, not the account ID or licence number.

This follows the existing local `/api/drivers` route. The planned `/api/v1/drivers`
contract still needs to be reconciled before interservice integration.
Authentication and role/ownership checks remain pending, as with profile creation.

## Try it locally

Start the service and MongoDB using the [creation guide](driver-profile-create.md).
Create a driver first, then copy the returned ID into this PowerShell example:

```powershell
$driverId = 'replace-with-created-driver-id'
Invoke-RestMethod -Method Get -Uri "http://localhost:8082/api/drivers/$driverId"
```

An existing driver returns `200 OK`:

```json
{
  "id": "the-created-driver-id",
  "accountId": "demo-account-001",
  "licenseNumber": "DEMO-LICENCE-001",
  "serviceArea": "Malabe",
  "availabilityStatus": "OFFLINE"
}
```

A missing profile returns `404` with error code `DRIVER_NOT_FOUND`.
Database access failures return `503 DATABASE_UNAVAILABLE` without database details.
Lookup never creates or updates a profile.

## Verify and understand

From the repository root:

```powershell
mvn -pl driver-vehicle-service -am verify
```

Unit tests cover lookup and missing profiles. HTTP contract tests cover the response
fields, `404` error format and safe `503` handling. These tests use mocks; they do not
verify a live MongoDB connection.

`DriverController.getById` receives the path ID. `DriverService.getById` loads the
document through `DriverRepository.findById` and maps it to `DriverResponse`.
If no document exists, `DriverNotFoundException` is converted to the standard error
JSON by `ApiExceptionHandler`.

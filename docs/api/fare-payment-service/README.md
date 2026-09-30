# Fare & Payment runtime API

The implemented canonical endpoints and request contracts are documented in
[the service README](../../../fare-payment-service/README.md). Default port: 8084;
local four-service demo: 18084. Swagger: `/swagger-ui/index.html`.

Public estimate, final fare, payment and receipt APIs require an ACTIVE Account JWT.
Payments and receipts belong to their passenger; ADMIN access is permitted.
Internal completion processing uses a service token and trusted Ride data.
See [four-service verification](../../../integration-tests/README.md) for a runnable demo.

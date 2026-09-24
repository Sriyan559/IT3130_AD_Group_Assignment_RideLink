# Fare & Payment Service API Specification

**Owner:** Sanjeewa H.D.U.S (IT24101590)
**Base URL:** http://localhost:8084
**Swagger UI:** http://localhost:8084/swagger-ui/index.html
**OpenAPI Spec:** http://localhost:8084/v3/api-docs

### Planned Endpoints:
- POST /api/v1/fare/estimate (Calculate Upfront Estimate)
- POST /api/v1/payments/process (Record Simulated Payment & Final Fare)
- GET /api/v1/payments/{paymentId} (Query Payment Status)
- GET /api/v1/receipts/{receiptId} (Retrieve Immutable Receipt)

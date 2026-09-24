# RideLink Postman Test Harness

This directory contains the shared Postman collection and local environment for testing and demonstrating the RideLink backend microservices.

## 1. Files
- `collections/RideLink.postman_collection.json`: Complete collection organized into six logical folders:
  1. `01 Account Service`
  2. `02 Driver & Vehicle Service`
  3. `03 Ride Management Service`
  4. `04 Fare & Payment Service`
  5. `05 End-to-End Workflow`
  6. `06 Negative Scenarios`
- `environments/RideLink-Local.postman_environment.json`: Local testing environment containing parameterized host ports and token storage.

## 2. Environment Variables
- `account_url`: `http://localhost:8081`
- `driver_url`: `http://localhost:8082`
- `ride_url`: `http://localhost:8083`
- `payment_url`: `http://localhost:8084`
- `auth_token`: Stored JWT authentication token dynamically populated after login.

## 3. How to Use
1. Open Postman.
2. Click **Import** and select `collections/RideLink.postman_collection.json` and `environments/RideLink-Local.postman_environment.json`.
3. Select the **RideLink - Local Environment** in the top-right environment selector.
4. Run requests individually or run entire test folders sequentially using the Postman Collection Runner.

# RideLink Integration Tests

This module establishes the architectural harness for end-to-end integration testing across the four RideLink microservices.

## 1. Scope & Objective
While each microservice contains its own unit and slice-integration tests, this directory structures cross-service functional testing, validating:
1. Complete ride-booking workflows across independently running services.
2. Interservice communication consistency and resilience.
3. Stable identifier propagation (`passengerId`, `driverId`, `rideId`, `fareId`).
4. Graceful negative scenario handling.

## 2. Workflows Covered
- `workflows/account-access/`: Registration, JWT authentication, and role validation.
- `workflows/driver-preparation/`: Vehicle onboarding, driver availability declaration, and GPS location simulation.
- `workflows/fare-estimation/`: Calculating fare estimate for pickup/destination.
- `workflows/ride-request-assignment/`: Ride creation, driver lookup, and driver assignment.
- `workflows/ride-lifecycle/`: Valid state progression: `REQUESTED` -> `ASSIGNED` -> `ACCEPTED` -> `IN_PROGRESS` -> `COMPLETED`.
- `workflows/completion-payment/`: Final fare calculation, simulated payment execution, and receipt generation.
- `workflows/negative-scenarios/`:
  - No eligible driver available.
  - Invalid state machine transition (e.g. attempting to complete an unaccepted ride).
  - Unauthorized access with missing/expired JWT token.
  - Invalid input parameters.
  - Failed simulated payment.

## 3. Contracts and Configuration
- `contracts/`: JSON schema contracts expected between services.
- `configuration/`: Environment parameters, mock wiremock mappings, and test profiles.

*Note: Execution scripts and test classes will be implemented during subsequent implementation phases.*

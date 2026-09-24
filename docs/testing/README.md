# Testing Strategy & Quality Assurance

In accordance with IT3130 criteria (LO3, I2, G4):

## 1. Unit Testing
Each microservice contains dedicated unit tests covering:
- Domain validation rules and invariants
- Service business calculations (e.g. fare formula)
- State transition validations (e.g. preventing invalid ride states)

## 2. Slice & Integration Testing
- Repository integration tests with in-memory or Testcontainers databases
- MockMvc web layer tests verifying HTTP response codes and error formats

## 3. Interservice Integration & Postman Tests
- Comprehensive Postman scenarios in postman/collections/RideLink.postman_collection.json verifying cross-service workflows and negative scenarios.

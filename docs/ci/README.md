# Continuous Integration (CI) Architecture

In accordance with IT3130 criteria (LO4, G4):

CI pipeline is configured via GitHub Actions in .github/workflows/ci.yml.

Pipeline stages:
1. Checkout repository
2. Set up JDK 17
3. Cache Maven dependencies
4. Clean and test all four microservices in parallel / matrix:
   - Account Service (mvn clean test in account-service)
   - Driver & Vehicle Service (mvn clean test in driver-vehicle-service)
   - Ride Management Service (mvn clean test in ride-management-service)
   - Fare & Payment Service (mvn clean test in fare-payment-service)
5. Verify reproducible build status

# RideLink

**IT3130 – Application Development: Group Assignment (30%)**  
**Backend Microservices for a Ride-Sharing Platform**

---

> [!IMPORTANT]
> **MANDATORY TECHNOLOGY CONSTRAINT:**  
> All backend microservices are built strictly using **Java 17** and **Spring Boot 3.3.x**.  
> **Node.js, Express.js, NestJS, and MERN backend architectures are strictly prohibited** and non-compliant with the assignment requirements. A web or mobile frontend is **not required**.

---

## 1. Core Services & Team Ownership

The RideLink platform is decomposed into **exactly four core microservices**, with individual accountability and ownership assigned as follows:

| # | Microservice | Primary Owner | Student ID | Primary Responsibilities | Current Status |
| :-: | :--- | :--- | :--- | :--- | :-: |
| **1** | **Account Service** | **Fernando B S C** | IT24103775 | Passenger & driver registration, authentication, JWT tokens, RBAC roles, profile management. | `STRUCTURE READY`<br>`NOT IMPLEMENTED` |
| **2** | **Driver & Vehicle Service** | **Rathnakoon D A** | IT24300246 | Driver operational profile, vehicle registration, availability toggle, simulated GPS location, driver querying. | `STRUCTURE READY`<br>`NOT IMPLEMENTED` |
| **3** | **Ride Management Service** | **Herath H M S R** | IT24103280 | Ride request creation, state machine lifecycle transitions (REQUESTED -> COMPLETED), driver assignment, trip tracking. | `STRUCTURE READY`<br>`NOT IMPLEMENTED` |
| **4** | **Fare & Payment Service** | **Sanjeewa H.D.U.S** | IT24101590 | Upfront fare estimates, deterministic final fare calculation, simulated payments, transaction ledger, receipts. | `STRUCTURE READY`<br>`NOT IMPLEMENTED` |

---

## 2. Technology Stack

- **Core Language:** Java 17 (LTS)
- **Backend Framework:** Spring Boot 3.3.4
- **Build System:** Apache Maven (Multi-module parent + independent service POMs)
- **Persistence Technologies:**
  - **Relational (PostgreSQL):** Account Service (`account_db`), Ride Management Service (`ride_db`), Fare & Payment Service (`payment_db`)
  - **Document (MongoDB):** Driver & Vehicle Service (`driver_db`)
- **API Documentation:** Springdoc OpenAPI 2.5.0 / Swagger UI
- **Demonstration & Testing:** Swagger UI & Postman Collection v2.1.0
- **Continuous Integration:** GitHub Actions (`.github/workflows/ci.yml`)

---

## 3. High-Level Architecture

```
                    Swagger UI / Postman Test Harness
                                   |
         +-------------------------+-------------------------+
         |                         |                         |
         v                         v                         v
+------------------+     +--------------------+     +------------------+
| Account Service  |     |  Ride Management   |     | Driver & Vehicle |
| (Port 8081)      |     |  (Port 8083)       |     | (Port 8082)      |
+--------+---------+     +---------+----------+     +--------+---------+
         |                         |                         |
         v                         +-----------+             v
   [account_db]                    |           |        [driver_db]
   (PostgreSQL)                    v           v        (MongoDB)
                             +-----------+ +-----------+
                             | ride_db   | | Fare &    |
                             | (Postgres)| | Payment   |
                             +-----------+ | (Port 8084|
                                           +-----+-----+
                                                 |
                                                 v
                                           [payment_db]
                                           (PostgreSQL)
```

---

## 4. Project Directory Structure

```
RideLink/
├── account-service/                  # Microservice 1: Port 8081 (PostgreSQL: account_db)
├── driver-vehicle-service/           # Microservice 2: Port 8082 (MongoDB: driver_db)
├── ride-management-service/          # Microservice 3: Port 8083 (PostgreSQL: ride_db)
├── fare-payment-service/             # Microservice 4: Port 8084 (PostgreSQL: payment_db)
├── integration-tests/                # End-to-end integration workflows & contracts
│   ├── workflows/                    # Workflows 1-7 (including negative test cases)
│   ├── contracts/                    # Interservice API JSON schema contracts
│   └── configuration/                # Mock & environment configs
├── postman/                          # Shared Postman test suite
│   ├── collections/                  # RideLink.postman_collection.json (6 folders)
│   ├── environments/                 # RideLink-Local.postman_environment.json
│   └── README.md
├── docs/                             # Architecture & assessment documentation
│   ├── architecture/                 # System context, boundaries, technology compliance
│   ├── diagrams/                     # Mermaid diagram sources & previews
│   ├── api/                          # Service endpoint specifications
│   ├── testing/                      # Test plan & quality assurance guidelines
│   ├── ci/                           # GitHub Actions CI documentation
│   ├── git-workflow/                 # Branching and review policies
│   └── contributions/                # Individual ownership tracking
├── scripts/                          # Local build & test automation scripts
├── .github/workflows/ci.yml          # Continuous Integration workflow
├── .env.example                      # Parameterized environment variables template
├── .gitignore                        # Standard Java/Maven/Secrets gitignore
├── pom.xml                           # Root Maven aggregator POM
└── README.md
```

---

## 5. Database Ownership & Isolation

In strict compliance with assignment guidelines, **each microservice owns its private database**:
- **Account Service:** Exclusively owns `account_db`
- **Driver & Vehicle Service:** Exclusively owns `driver_db`
- **Ride Management Service:** Exclusively owns `ride_db`
- **Fare & Payment Service:** Exclusively owns `payment_db`

**Strict Rules:**
1. Direct cross-service database querying is **strictly prohibited**.
2. No sharing of JPA entities or MongoDB document classes between services.
3. Interservice data sharing is conducted strictly through REST APIs and scalar identifiers (`passengerId`, `driverId`, `rideId`).

---

## 6. Interservice Communication

Services interact synchronously via RESTful HTTP JSON endpoints:
- `Ride Management Service` calls `Driver & Vehicle Service` (`GET /api/v1/drivers/eligible`) to locate and match drivers.
- `Ride Management Service` calls `Fare & Payment Service` (`POST /api/v1/payments/process`) to compute final fares and initiate payment upon ride completion.
- `Ride Management Service` references `Account Service` for user identity verification.

---

## 7. Configuration & Environment Variables

Never commit real credentials to version control. Configuration is parameterized using environment variables with safe defaults in `application.yml`:

| Service | Port Variable | Default Port | Database Variable | Default Database |
| :--- | :--- | :-: | :--- | :--- |
| Account Service | `ACCOUNT_SERVICE_PORT` | `8081` | `ACCOUNT_DB_NAME` | `account_db` |
| Driver & Vehicle Service | `DRIVER_SERVICE_PORT` | `8082` | `DRIVER_DB_NAME` | `driver_db` |
| Ride Management Service | `RIDE_SERVICE_PORT` | `8083` | `RIDE_DB_NAME` | `ride_db` |
| Fare & Payment Service | `PAYMENT_SERVICE_PORT` | `8084` | `PAYMENT_DB_NAME` | `payment_db` |

Copy `.env.example` to `.env` or use `application-local.yml` for local testing overrides.

---

## 8. Running the Services

### Prerequisites
- Java 17+ Development Kit (JDK 17)
- Apache Maven 3.8+
- Docker / Local PostgreSQL (Port 5432) & Local MongoDB (Port 27017)

### Compile the entire system:
```bash
mvn clean compile
```

### Run an individual microservice:
```bash
# Example: Starting Account Service
cd account-service
mvn spring-boot:run
```

---

## 9. Interactive API Documentation (Swagger UI)

When running, each service exposes its interactive OpenAPI 3 documentation:
- **Account Service:** [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- **Driver & Vehicle Service:** [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
- **Ride Management Service:** [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)
- **Fare & Payment Service:** [http://localhost:8084/swagger-ui/index.html](http://localhost:8084/swagger-ui/index.html)

---

## 10. Postman Test Suite

A complete test suite is provided in the `postman/` folder:
- **Collection:** `postman/collections/RideLink.postman_collection.json`
- **Environment:** `postman/environments/RideLink-Local.postman_environment.json`

Folders included:
1. `01 Account Service`
2. `02 Driver & Vehicle Service`
3. `03 Ride Management Service`
4. `04 Fare & Payment Service`
5. `05 End-to-End Workflow`
6. `06 Negative Scenarios`

---

## 11. Testing & Quality Assurance

Unit and slice tests are structured within each service under `src/test/java/`:
- **Account Service:** Unit, repository integration, security tests.
- **Driver & Vehicle Service:** Unit, document repository, contract tests.
- **Ride Management Service:** Unit, state machine lifecycle tests, contract tests.
- **Fare & Payment Service:** Unit, fare rule algorithm tests, payment transaction tests.

Execute all tests:
```bash
mvn test
```

---

## 12. Continuous Integration (CI)

A GitHub Actions pipeline is configured in `.github/workflows/ci.yml`. On every push and pull request to `main` or `develop`, the workflow:
1. Sets up JDK 17.
2. Builds and executes unit tests for all four microservices in parallel.
3. Verifies full aggregator compilation.

---

## 13. Git Workflow & Branching Strategy

- **`main`**: Production-ready branch. Direct commits are restricted.
- **`develop`**: Integration branch for combining feature branches.
- **Feature Branches**:
  - `feature/account-service`
  - `feature/driver-vehicle-service`
  - `feature/ride-management-service`
  - `feature/fare-payment-service`

All feature additions require peer reviews and passing CI prior to merging.

---

## 14. Academic Integrity & Responsible Tool Use

All submitted code, architectures, diagrams, and documentation are designed and maintained collaboratively by the four assigned group members in accordance with the SLIIT Academic Integrity Policy.
- Individual contributions are tracked through commit history, branch activity, and personal code ownership.
- Every group member is fully prepared to explain, demonstrate, and defend the integrated platform and their respective assigned service during the scheduled viva.

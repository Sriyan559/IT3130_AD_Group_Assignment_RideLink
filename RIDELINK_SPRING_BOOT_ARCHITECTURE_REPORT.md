# RideLink - Spring Boot Microservices Architecture Report

**Module:** IT3130 – Application Development  
**Assessment:** Group Assignment (30%)  
**Project:** RideLink – Backend Microservices for a Ride-Sharing Platform  
**Author:** Senior Java / Spring Boot Software Architect  
**Date:** September 2026  
**Document Status:** Complete Architecture & Structural Blueprint

---

## 1. Executive Summary

This report establishes the comprehensive architectural foundation and project scaffolding for **RideLink**, a scalable backend microservices solution for an urban ride-sharing platform. The architecture strictly enforces all functional criteria, engineering standards, and grading constraints set forth in the IT3130 assignment brief, combined with mandatory institutional requirements:

1. **Exclusively Java 17 + Spring Boot 3.3.x:** All backend microservices are built purely with Java and Spring Boot.
2. **Absolute Prohibition of MERN / Node.js Backends:** Node.js, Express.js, NestJS, and JavaScript/TypeScript backend replacements are strictly barred from the submission.
3. **Strict Database-per-Service Isolation:** Each microservice possesses its own completely dedicated database boundary (`account_db`, `driver_db`, `ride_db`, and `payment_db`). Cross-service database queries, shared JPA entities, and cross-database joins are prohibited.
4. **Backend-Only Assessment Scope:** Web and mobile frontends are classified as out of scope. System testing, demonstration, and viva defense are conducted using Swagger UI (OpenAPI 3) and a unified Postman test harness.
5. **Clear Individual Ownership:** Each of the four team members is the designated primary owner of one distinct microservice.
6. **Architecture-Only Scope:** No business logic, mock data, or premature endpoint implementations have been authored during this phase, ensuring clean boundaries for subsequent implementation steps.

---

## 2. Assignment Requirements Reviewed

The IT3130 assignment brief outlines five key module learning outcomes (LO1–LO4) and specific functional capabilities:

- **LO1 (Software Architecture & Framework Selection):** Architectural comparison between monolithic and microservice approaches; justification of microservice boundaries and technology stack.
- **LO2 (Inter-Application Communication):** Evaluation of synchronous vs. asynchronous communication paradigms; implementation of at least two meaningful interservice workflows with robust contracts.
- **LO3 (Software Engineering & Best Practices):** Application of SOLID principles, clean packaging, input validation, role-based security, comprehensive unit/integration testing, and technical documentation.
- **LO4 (Version Control & CI/CD):** Git branching discipline (feature branches, peer-reviewed pull requests), meaningful commit history, and automated GitHub Actions continuous integration.
- **Minimum Functional Workflows:**
  1. *Account & Access:* Passenger/driver registration, authentication, role enforcement.
  2. *Driver Preparation:* Vehicle details, availability state, simulated GPS location.
  3. *Fare Estimation:* Distance/duration estimation formula.
  4. *Ride Request & Assignment:* Ride creation and eligible driver matching.
  5. *Ride Lifecycle:* Strict state machine: `REQUESTED` -> `ASSIGNED` -> `ACCEPTED` -> `IN_PROGRESS` -> `COMPLETED` / `CANCELLED`.
  6. *Completion & Payment:* Final fare calculation, simulated payment transaction, and receipt retrieval.
  7. *Negative Scenarios:* No available driver, invalid state transition, unauthorized access, invalid input, and payment failure.

---

## 3. Mandatory Technology Requirements

| Concern | Mandatory Rule | Compliance Status |
| :--- | :--- | :--- |
| **Backend Language** | Java (JDK 17 LTS baseline) | **COMPLIANT** |
| **Backend Framework** | Spring Boot 3.3.x | **COMPLIANT** |
| **Node.js / Express** | Strictly Forbidden | **COMPLIANT (Excluded)** |
| **MERN Stack** | Strictly Forbidden | **COMPLIANT (Excluded)** |
| **Number of Services** | Exactly 4 Core Services | **COMPLIANT (Exactly 4)** |
| **Database per Service**| Dedicated database per microservice | **COMPLIANT (Isolated)** |
| **Shared Database** | Strictly Forbidden | **COMPLIANT (No shared DB)** |
| **Frontend** | Not required (Out of Scope) | **COMPLIANT (None created)** |
| **Official Demonstration** | Swagger UI + Postman Collection | **COMPLIANT (Configured)** |

---

## 4. Existing Repository Audit (Phase 0)

A comprehensive, recursive audit of the workspace (`d:\IT3130_AD_Group_Assignment_RideLink`) was conducted prior to file generation. The audit addressed all 24 required inspection dimensions:

1. **Current Programming Language(s):** None existed (directory was newly created).
2. **Current Backend Framework(s):** None existed.
3. **Node.js / Express Presence:** Verified absent (`NOT PRESENT`).
4. **Java / Spring Boot Presence:** System hosts JDK 17 (`C:\Program Files\Java\jdk-17`) and JDK 24.
5. **Maven vs. Gradle Usage:** Neither build tool was configured in the repository; Maven selected.
6. **Java Version:** Baseline set to Java 17 LTS.
7. **Spring Boot Version:** Standardized to Spring Boot 3.3.4.
8. **Package Naming Convention:** Standardized to `com.ridelink.<service>`.
9. **Configuration Convention:** Standardized to `application.yml` with environment variable overrides.
10. **Existing Databases:** None attached.
11. **MongoDB Usage:** Configured exclusively for Driver & Vehicle Service (`driver_db`).
12. **SQL Database Usage:** Configured as PostgreSQL for Account, Ride, and Payment services.
13. **Existing Services/Modules:** None existed.
14. **Tests:** None existed.
15. **Swagger / OpenAPI Setup:** None existed.
16. **Postman Files:** None existed.
17. **CI Configuration:** None existed.
18. **Environment Files:** None existed.
19. **README / Docs:** None existed.
20. **Secrets Accidentally Committed:** None found; `.gitignore` established immediately.
21. **Shared Database Usage:** None found.
22. **Cross-Service DB Access:** None found.
23. **Frontend Folders:** None found.
24. **Architectural Inconsistencies:** None found.

---

## 5. Technology Compliance Audit

| Requirement | Current State | Status | Required Action |
| :--- | :--- | :--- | :--- |
| Java Backend | Java 17 configured in root POM and all services | **COMPLIANT** | Maintained across all service modules |
| Spring Boot | Spring Boot 3.3.4 parent POM configured | **COMPLIANT** | Maintained across all service modules |
| Exactly Four Core Services | 4 microservices structured | **COMPLIANT** | Boundaries locked to required 4 services |
| No Node.js / Express Backend | Excluded completely | **COMPLIANT** | Ongoing PR reviews to prevent JS backend introduction |
| Independent DB per Service | 4 distinct databases (`account_db`, `driver_db`, `ride_db`, `payment_db`) | **COMPLIANT** | Enforce zero cross-DB joins in service layers |
| No Shared Database | No common database configured | **COMPLIANT** | Locked in configuration and documentation |
| Swagger / OpenAPI | Springdoc OpenAPI starter in all services | **COMPLIANT** | Endpoints will be documented as implemented |
| Postman Suite | Collection with 6 logical folders + environment created | **COMPLIANT** | Populate request payloads during feature coding |
| Backend-Only Scope | No web or mobile UI created | **COMPLIANT** | Maintain frontend ban |
| Tests Structure | Unit, integration, security, lifecycle, contract test dirs | **COMPLIANT** | Implement test classes in subsequent phases |
| CI Structure | GitHub Actions workflow configured (`.github/workflows/ci.yml`) | **COMPLIANT** | Validates builds on PR and push |
| Architecture Documentation | 6 core architectural documents + 3 Mermaid diagrams | **COMPLIANT** | Maintained in `docs/architecture/` |
| Service Ownership Documentation | Recorded in root README and contribution documents | **COMPLIANT** | Clear individual accountability |

---

## 6. Detected Non-Compliant Technologies

- **Node.js / Express:** None detected in the repository.
- **MERN Backend:** None detected.
- **Shared DB Architecture:** None detected.
- **Frontend Code:** None detected.
- **Risk Assessment:** Clean slate. The risk of non-compliance is entirely mitigated by establishing strict templates and architecture documentation before code implementation commences.

---

## 7. Proposed Spring Boot Architecture

The RideLink platform follows an enterprise microservices topology:
- **Build Management:** Multi-module Maven setup with an aggregator `pom.xml` at root. Each microservice contains an independent `pom.xml` referencing the parent, allowing both unified multi-module builds (`mvn clean compile` from root) and standalone service development (`mvn spring-boot:run` in service directory).
- **Layered Service Architecture:** Every microservice enforces a clean architectural layering within its package root:
  - `controller`: REST API endpoints and HTTP status translation.
  - `dto`: Decoupled `request` and `response` contract models.
  - `domain`: Rich domain logic, value objects, and business enums.
  - `entity` / `document`: Private persistence entities mapped to the service's private database.
  - `repository`: Spring Data JPA / MongoDB interfaces.
  - `service`: Core business orchestration.
  - `validation`: Custom constraint validators.
  - `exception`: Domain-specific exceptions and `@ControllerAdvice` global handlers.
  - `mapper`: Bidirectional Entity <-> DTO mappers.
  - `integration`: Downstream HTTP clients and communication adapters.
  - `config`: OpenAPI documentation and service security beans.

---

## 8. Four Microservice Boundaries

```
+---------------------------------------------------------------------------------------------------+
|                                      SWAGGER UI / POSTMAN HARNESS                                 |
+-------------------+-----------------------+-------------------------+-----------------------------+
                    |                       |                         |
                    v                       v                         v
+-----------------------+   +-----------------------+   +-------------------------+   +-------------------------+
|    ACCOUNT SERVICE    |   |  DRIVER & VEHICLE     |   |   RIDE MANAGEMENT       |   |   FARE & PAYMENT        |
|      (Port 8081)      |   |     (Port 8082)       |   |      (Port 8083)        |   |      (Port 8084)        |
+-----------------------+   +-----------------------+   +-------------------------+   +-------------------------+
| • User Registration   |   | • Driver Profiles     |   | • Ride Request Creation |   | • Upfront Fare Estimate |
| • User Authentication |   | • Vehicle Attributes  |   | • Driver Assignment     |   | • Final Fare Formula    |
| • JWT Token Issuance  |   | • Availability Status |   | • State Machine         |   | • Simulated Payment     |
| • RBAC Roles          |   | • Service Areas       |   | • Interservice Matcher  |   | • Payment Statuses      |
| • Profile Updates     |   | • Simulated GPS Coord |   | • Ride Lifecycle History|   | • Immutable Receipts    |
+-----------+-----------+   +-----------+-----------+   +------------+------------+   +------------+------------+
            |                           |                            |                             |
            v                           v                            v                             v
     [ account_db ]              [ driver_db ]                 [ ride_db ]                  [ payment_db ]
     (PostgreSQL)                  (MongoDB)                   (PostgreSQL)                  (PostgreSQL)
```

---

## 9. Account Service Structure (`account-service`)
- **Primary Owner:** Fernando B S C (Student ID: IT24103775)
- **Port:** `8081`
- **Database:** `account_db` (PostgreSQL)
- **File Structure:**
  - `pom.xml`: Spring Web, Validation, Data JPA, Security, PostgreSQL, Springdoc, Test.
  - `src/main/resources/application.yml` & `application-local.yml.example`
  - `src/main/resources/db/migration/README.md`
  - `src/main/java/com/ridelink/account/`:
    - `AccountServiceApplication.java`
    - `config/OpenApiConfig.java`
    - `controller/`, `dto/request/`, `dto/response/`, `domain/`, `entity/`, `repository/`, `service/`, `security/`, `validation/`, `exception/`, `mapper/`, `integration/`
  - `src/test/java/com/ridelink/account/`: `unit/`, `integration/`, `security/`
  - `docs/README.md` & `README.md`

---

## 10. Driver & Vehicle Service Structure (`driver-vehicle-service`)
- **Primary Owner:** Rathnakoon D A (Student ID: IT24300246)
- **Port:** `8082`
- **Database:** `driver_db` (MongoDB)
- **File Structure:**
  - `pom.xml`: Spring Web, Validation, Data MongoDB, Springdoc, Test.
  - `src/main/resources/application.yml` & `application-local.yml.example`
  - `src/main/java/com/ridelink/driver/`:
    - `DriverVehicleServiceApplication.java`
    - `config/OpenApiConfig.java`
    - `controller/`, `dto/request/`, `dto/response/`, `domain/`, `document/`, `entity/`, `repository/`, `service/`, `validation/`, `exception/`, `mapper/`, `integration/`
  - `src/test/java/com/ridelink/driver/`: `unit/`, `integration/`, `contract/`
  - `docs/README.md` & `README.md`

---

## 11. Ride Management Service Structure (`ride-management-service`)
- **Primary Owner:** Herath H M S R (Student ID: IT24103280)
- **Port:** `8083`
- **Database:** `ride_db` (PostgreSQL)
- **File Structure:**
  - `pom.xml`: Spring Web, Validation, Data JPA, PostgreSQL, Springdoc, Test.
  - `src/main/resources/application.yml` & `application-local.yml.example`
  - `src/main/resources/db/migration/README.md`
  - `src/main/java/com/ridelink/ride/`:
    - `RideManagementServiceApplication.java`
    - `config/OpenApiConfig.java`
    - `controller/`, `dto/request/`, `dto/response/`, `domain/`, `entity/`, `repository/`, `service/`, `lifecycle/`, `validation/`, `exception/`, `mapper/`
    - `integration/`: `account/`, `driver/`, `fare/`
  - `src/test/java/com/ridelink/ride/`: `unit/`, `integration/`, `lifecycle/`, `contract/`
  - `docs/README.md` & `README.md`

---

## 12. Fare & Payment Service Structure (`fare-payment-service`)
- **Primary Owner:** Sanjeewa H.D.U.S (Student ID: IT24101590)
- **Port:** `8084`
- **Database:** `payment_db` (PostgreSQL)
- **File Structure:**
  - `pom.xml`: Spring Web, Validation, Data JPA, PostgreSQL, Springdoc, Test.
  - `src/main/resources/application.yml` & `application-local.yml.example`
  - `src/main/resources/db/migration/README.md`
  - `src/main/java/com/ridelink/payment/`:
    - `FarePaymentServiceApplication.java`
    - `config/OpenApiConfig.java`
    - `controller/`, `dto/request/`, `dto/response/`, `domain/`, `fare/`, `payment/`, `receipt/`, `entity/`, `repository/`, `service/`, `validation/`, `exception/`, `mapper/`, `integration/`
  - `src/test/java/com/ridelink/payment/`: `unit/`, `integration/`, `fare/`, `payment/`
  - `docs/README.md` & `README.md`

---

## 13. Database Ownership Matrix

| Microservice | Dedicated Database | Persistence Tech | Dedicated? | Shared Access Permitted? | Compliance Status |
| :--- | :--- | :--- | :-: | :-: | :---: |
| **Account Service** | `account_db` | PostgreSQL | **YES** | **NO** | **STRUCTURE READY** |
| **Driver & Vehicle Service** | `driver_db` | MongoDB | **YES** | **NO** | **STRUCTURE READY** |
| **Ride Management Service** | `ride_db` | PostgreSQL | **YES** | **NO** | **STRUCTURE READY** |
| **Fare & Payment Service** | `payment_db` | PostgreSQL | **YES** | **NO** | **STRUCTURE READY** |

---

## 14. Database Technology Decisions

- **Account Service (`account_db` - PostgreSQL):** Relational schema is chosen to enforce strict referential integrity between user accounts, credential hashes, and assigned security roles. ACID compliance ensures user profiles cannot be left in indeterminate states.
- **Driver & Vehicle Service (`driver_db` - MongoDB):** Document store is chosen to support polymorphic vehicle attributes, unstructured vehicle equipment specifications, and rapid coordinate updates for simulated GPS locations. MongoDB's native 2D sphere indexing provides ideal foundation for proximity matching.
- **Ride Management Service (`ride_db` - PostgreSQL):** Relational schema is chosen for transactional guarantees across ride state transitions, preventing race conditions during driver acceptance and cancellation.
- **Fare & Payment Service (`payment_db` - PostgreSQL):** Relational schema is chosen to maintain an immutable financial ledger of payments, tax breakdowns, and receipts.

---

## 15. Interservice Communication Boundaries

- **Paradigm:** Synchronous RESTful JSON over HTTP.
- **Interface 1 (Ride -> Driver Matching):**
  `Ride Management Service` calls `GET /api/v1/drivers/eligible?lat={lat}&lng={lng}&radius={radius}` on `Driver & Vehicle Service` (Port 8082).
- **Interface 2 (Ride -> Fare/Payment Processing):**
  `Ride Management Service` calls `POST /api/v1/payments/process` on `Fare & Payment Service` (Port 8084) upon trip completion.
- **Interface 3 (Ride -> Account Validation):**
  `Ride Management Service` validates passenger account identity via `Account Service` (Port 8081).
- **Boundary Rule:** All cross-service references are scalar IDs (`passengerId`, `driverId`, `fareId`). Zero direct database queries.

---

## 16. Swagger / OpenAPI Architecture

Each service exposes independent interactive documentation powered by `springdoc-openapi-starter-webmvc-ui` 2.5.0:
- **Account Service:** `http://localhost:8081/swagger-ui/index.html` (JSON: `/v3/api-docs`)
- **Driver & Vehicle Service:** `http://localhost:8082/swagger-ui/index.html` (JSON: `/v3/api-docs`)
- **Ride Management Service:** `http://localhost:8083/swagger-ui/index.html` (JSON: `/v3/api-docs`)
- **Fare & Payment Service:** `http://localhost:8084/swagger-ui/index.html` (JSON: `/v3/api-docs`)

Each service has a dedicated `OpenApiConfig.java` defining OpenAPI metadata, contact information, and licensing.

---

## 17. Postman Architecture

A complete Postman workspace is established in `postman/`:
- `collections/RideLink.postman_collection.json`: Fully structured with six logical directories:
  1. `01 Account Service`
  2. `02 Driver & Vehicle Service`
  3. `03 Ride Management Service`
  4. `04 Fare & Payment Service`
  5. `05 End-to-End Workflow`
  6. `06 Negative Scenarios`
- `environments/RideLink-Local.postman_environment.json`: Parameterized port variables (`account_url`, `driver_url`, `ride_url`, `payment_url`) and dynamic session tokens.

---

## 18. Testing Architecture

- **Service-Level Tests:**
  - `unit`: Domain logic, input validation, fare formulas, state transitions.
  - `integration`: Repository database operations and MockMvc web layer slice tests.
  - `security` / `lifecycle` / `contract`: Specialized test packages for RBAC authorization, state machine progression, and HTTP contract verification.
- **Root Integration Tests (`integration-tests/`):**
  - Seven designated workflow folders covering both positive end-to-end paths and required negative failure modes (no available driver, invalid state transition, unauthorized access, invalid input, failed payment).

---

## 19. CI Architecture

Configured via GitHub Actions in `.github/workflows/ci.yml`:
- Triggered on push and pull request against `main` and `develop`.
- Environment: Ubuntu Latest + Eclipse Temurin JDK 17 + Maven caching.
- Matrix Strategy: Parallel build and unit testing across all four microservices.
- Full Aggregator Verification: Validates complete project compilation.

---

## 20. Git Workflow

- **Branching Model:** Feature Branch Workflow based on `main` and `develop`.
- **Branches:**
  - `main`: Production release branch.
  - `develop`: Integration branch.
  - `feature/account-service`: Owned by Fernando B S C.
  - `feature/driver-vehicle-service`: Owned by Rathnakoon D A.
  - `feature/ride-management-service`: Owned by Herath H M S R.
  - `feature/fare-payment-service`: Owned by Sanjeewa H.D.U.S.
- **Quality Gates:** Feature branch -> Meaningful Commits -> Pull Request -> Peer Review -> Passing CI -> Merge to Develop.

---

## 21. Team Ownership

| Team Member | Student ID | Assigned Microservice | Primary Scope |
| :--- | :--- | :--- | :--- |
| **Fernando B S C** | IT24103775 | **Account Service** | Registration, login, JWT issuance, roles, profile management. |
| **Rathnakoon D A** | IT24300246 | **Driver & Vehicle Service** | Driver profile, vehicles, availability, simulated GPS location. |
| **Herath H M S R** | IT24103280 | **Ride Management Service** | Ride requests, state machine lifecycle, driver assignment, trip tracking. |
| **Sanjeewa H.D.U.S** | IT24101590 | **Fare & Payment Service** | Fare estimation, calculation formula, payments, receipts. |

---

## 22. Complete Final Directory Tree

```
d:\IT3130_AD_Group_Assignment_RideLink
├── .github
│   └── workflows
│       └── ci.yml
├── .env.example
├── .gitignore
├── pom.xml
├── README.md
├── RIDELINK_SPRING_BOOT_ARCHITECTURE_REPORT.md
│
├── account-service
│   ├── pom.xml
│   ├── README.md
│   ├── docs
│   │   └── README.md
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── com
│       │   │       └── ridelink
│       │   │           └── account
│       │   │               ├── AccountServiceApplication.java
│       │   │               ├── config
│       │   │               │   └── OpenApiConfig.java
│       │   │               ├── controller
│       │   │               │   └── package-info.java
│       │   │               ├── domain
│       │   │               │   └── package-info.java
│       │   │               ├── dto
│       │   │               │   ├── request
│       │   │               │   │   └── package-info.java
│       │   │               │   └── response
│       │   │               │       └── package-info.java
│       │   │               ├── entity
│       │   │               │   └── package-info.java
│       │   │               ├── exception
│       │   │               │   └── package-info.java
│       │   │               ├── integration
│       │   │               │   └── package-info.java
│       │   │               ├── mapper
│       │   │               │   └── package-info.java
│       │   │               ├── repository
│       │   │               │   └── package-info.java
│       │   │               ├── security
│       │   │               │   └── package-info.java
│       │   │               ├── service
│       │   │               │   └── package-info.java
│       │   │               └── validation
│       │   │                   └── package-info.java
│       │   └── resources
│       │       ├── application-local.yml.example
│       │       ├── application.yml
│       │       └── db
│       │           └── migration
│       │               └── README.md
│       └── test
│           └── java
│               └── com
│                   └── ridelink
│                       └── account
│                           ├── integration
│                           │   └── package-info.java
│                           ├── security
│                           │   └── package-info.java
│                           └── unit
│                               └── package-info.java
│
├── driver-vehicle-service
│   ├── pom.xml
│   ├── README.md
│   ├── docs
│   │   └── README.md
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── com
│       │   │       └── ridelink
│       │   │           └── driver
│       │   │               ├── DriverVehicleServiceApplication.java
│       │   │               ├── config
│       │   │               │   └── OpenApiConfig.java
│       │   │               ├── controller
│       │   │               │   └── package-info.java
│       │   │               ├── document
│       │   │               │   └── package-info.java
│       │   │               ├── domain
│       │   │               │   └── package-info.java
│       │   │               ├── dto
│       │   │               │   ├── request
│       │   │               │   │   └── package-info.java
│       │   │               └── response
│       │   │                   └── package-info.java
│       │   │               ├── entity
│       │   │               │   └── package-info.java
│       │   │               ├── exception
│       │   │               │   └── package-info.java
│       │   │               ├── integration
│       │   │               │   └── package-info.java
│       │   │               ├── mapper
│       │   │               │   └── package-info.java
│       │   │               ├── repository
│       │   │               │   └── package-info.java
│       │   │               ├── service
│       │   │               │   └── package-info.java
│       │   │               └── validation
│       │   │                   └── package-info.java
│       │   └── resources
│       │       ├── application-local.yml.example
│       │       └── application.yml
│       └── test
│           └── java
│               └── com
│                   └── ridelink
│                       └── driver
│                           ├── contract
│                           │   └── package-info.java
│                           ├── integration
│                           │   └── package-info.java
│                           └── unit
│                               └── package-info.java
│
├── ride-management-service
│   ├── pom.xml
│   ├── README.md
│   ├── docs
│   │   └── README.md
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── com
│       │   │       └── ridelink
│       │   │           └── ride
│       │   │               ├── RideManagementServiceApplication.java
│       │   │               ├── config
│       │   │               │   └── OpenApiConfig.java
│       │   │               ├── controller
│       │   │               │   └── package-info.java
│       │   │               ├── domain
│       │   │               │   └── package-info.java
│       │   │               ├── dto
│       │   │               │   ├── request
│       │   │               │   │   └── package-info.java
│       │   │               │   └── response
│       │   │               │       └── package-info.java
│       │   │               ├── entity
│       │   │               │   └── package-info.java
│       │   │               ├── exception
│       │   │               │   └── package-info.java
│       │   │               ├── integration
│       │   │               │   ├── account
│       │   │               │   │   └── package-info.java
│       │   │               │   ├── driver
│       │   │               │   │   └── package-info.java
│       │   │               │   ├── fare
│       │   │               │   │   └── package-info.java
│       │   │               │   └── package-info.java
│       │   │               ├── lifecycle
│       │   │               │   └── package-info.java
│       │   │               ├── mapper
│       │   │               │   └── package-info.java
│       │   │               ├── repository
│       │   │               │   └── package-info.java
│       │   │               ├── service
│       │   │               │   └── package-info.java
│       │   │               └── validation
│       │   │                   └── package-info.java
│       │   └── resources
│       │       ├── application-local.yml.example
│       │       ├── application.yml
│       │       └── db
│       │           └── migration
│       │               └── README.md
│       └── test
│           └── java
│               └── com
│                   └── ridelink
│                       └── ride
│                           ├── contract
│                           │   └── package-info.java
│                           ├── integration
│                           │   └── package-info.java
│                           ├── lifecycle
│                           │   └── package-info.java
│                           └── unit
│                               └── package-info.java
│
├── fare-payment-service
│   ├── pom.xml
│   ├── README.md
│   ├── docs
│   │   └── README.md
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── com
│       │   │       └── ridelink
│       │   │           └── payment
│       │   │               ├── FarePaymentServiceApplication.java
│       │   │               ├── config
│       │   │               │   └── OpenApiConfig.java
│       │   │               ├── controller
│       │   │               │   └── package-info.java
│       │   │               ├── domain
│       │   │               │   └── package-info.java
│       │   │               ├── dto
│       │   │               │   ├── request
│       │   │               │   │   └── package-info.java
│       │   │               │   └── response
│       │   │               │       └── package-info.java
│       │   │               ├── entity
│       │   │               │   └── package-info.java
│       │   │               ├── exception
│       │   │               │   └── package-info.java
│       │   │               ├── fare
│       │   │               │   └── package-info.java
│       │   │               ├── integration
│       │   │               │   └── package-info.java
│       │   │               ├── mapper
│       │   │               │   └── package-info.java
│       │   │               ├── payment
│       │   │               │   └── package-info.java
│       │   │               ├── receipt
│       │   │               │   └── package-info.java
│       │   │               ├── repository
│       │   │               │   └── package-info.java
│       │   │               ├── service
│       │   │               │   └── package-info.java
│       │   │               └── validation
│       │   │                   └── package-info.java
│       │   └── resources
│       │       ├── application-local.yml.example
│       │       ├── application.yml
│       │       └── db
│       │           └── migration
│       │               └── README.md
│       └── test
│           └── java
│               └── com
│                   └── ridelink
│                       └── payment
│                           ├── fare
│                           │   └── package-info.java
│                           ├── integration
│                           │   └── package-info.java
│                           ├── payment
│                           │   └── package-info.java
│                           └── unit
│                               └── package-info.java
│
├── integration-tests
│   ├── README.md
│   ├── configuration
│   │   └── README.md
│   ├── contracts
│   │   └── README.md
│   └── workflows
│       ├── account-access
│       │   └── README.md
│       ├── completion-payment
│       │   └── README.md
│       ├── driver-preparation
│       │   └── README.md
│       ├── fare-estimation
│       │   └── README.md
│       ├── negative-scenarios
│       │   └── README.md
│       ├── ride-lifecycle
│       │   └── README.md
│       └── ride-request-assignment
│           └── README.md
│
├── postman
│   ├── README.md
│   ├── collections
│   │   └── RideLink.postman_collection.json
│   └── environments
│       └── RideLink-Local.postman_environment.json
│
├── docs
│   ├── api
│   │   ├── account-service
│   │   │   └── README.md
│   │   ├── driver-vehicle-service
│   │   │   └── README.md
│   │   ├── fare-payment-service
│   │   │   └── README.md
│   │   └── ride-management-service
│   │       └── README.md
│   ├── architecture
│   │   ├── communication-design.md
│   │   ├── data-ownership.md
│   │   ├── README.md
│   │   ├── security-architecture.md
│   │   ├── service-boundaries.md
│   │   ├── system-context.md
│   │   └── technology-compliance.md
│   ├── ci
│   │   └── README.md
│   ├── contributions
│   │   └── README.md
│   ├── diagrams
│   │   ├── architecture
│   │   │   └── README.md
│   │   ├── data-ownership
│   │   │   └── README.md
│   │   ├── sequence
│   │   │   └── README.md
│   │   └── source
│   │       ├── ridelink-architecture.mmd
│   │       ├── ridelink-data-ownership.mmd
│   │       └── ridelink-ride-lifecycle.mmd
│   ├── git-workflow
│   │   └── README.md
│   └── testing
│       └── README.md
│
└── scripts
    ├── build-all.ps1
    ├── README.md
    └── test-all.ps1
```

---

## 23. Files Created

- **Root & Governance:** `pom.xml`, `.gitignore`, `.env.example`, `README.md`, `RIDELINK_SPRING_BOOT_ARCHITECTURE_REPORT.md`
- **CI / Workflows:** `.github/workflows/ci.yml`
- **Automation Scripts:** `scripts/build-all.ps1`, `scripts/test-all.ps1`, `scripts/README.md`
- **Account Service:** `account-service/pom.xml`, `README.md`, `docs/README.md`, `application.yml`, `application-local.yml.example`, `db/migration/README.md`, `AccountServiceApplication.java`, `OpenApiConfig.java`, and 14 `package-info.java` package markers.
- **Driver & Vehicle Service:** `driver-vehicle-service/pom.xml`, `README.md`, `docs/README.md`, `application.yml`, `application-local.yml.example`, `DriverVehicleServiceApplication.java`, `OpenApiConfig.java`, and 15 `package-info.java` package markers.
- **Ride Management Service:** `ride-management-service/pom.xml`, `README.md`, `docs/README.md`, `application.yml`, `application-local.yml.example`, `db/migration/README.md`, `RideManagementServiceApplication.java`, `OpenApiConfig.java`, and 18 `package-info.java` package markers.
- **Fare & Payment Service:** `fare-payment-service/pom.xml`, `README.md`, `docs/README.md`, `application.yml`, `application-local.yml.example`, `db/migration/README.md`, `FarePaymentServiceApplication.java`, `OpenApiConfig.java`, and 17 `package-info.java` package markers.
- **Integration Tests Harness:** `integration-tests/README.md`, `configuration/README.md`, `contracts/README.md`, and 7 workflow README files.
- **Postman Test Harness:** `postman/README.md`, `collections/RideLink.postman_collection.json`, `environments/RideLink-Local.postman_environment.json`.
- **System Documentation:** 7 documents in `docs/architecture/`, 3 Mermaid diagram sources in `docs/diagrams/source/`, 3 diagram documentation files, 4 API documentation files, and 4 QA/Process guides in `docs/`.

---

## 24. Files Modified

- None. (The workspace was completely fresh prior to project architecture initialization).

---

## 25. Existing Files Preserved

- No pre-existing legacy files existed to preserve or deprecate.

---

## 26. Remaining Non-Compliance Risks

1. **Risk of Accidental Shared Database Use:** Developers might be tempted to connect services to a single database instance during quick prototyping.  
   *Mitigation:* CI and PR checklists will verify each service connects only to its dedicated environment database variable (`ACCOUNT_DB_NAME`, `DRIVER_DB_NAME`, `RIDE_DB_NAME`, `PAYMENT_DB_NAME`).
2. **Risk of Frontend Scope Creep:** Attempting to build an interactive React or mobile application.  
   *Mitigation:* Documentation explicitly confirms frontend development carries zero marks; demonstration is strictly limited to Swagger UI and Postman.
3. **Risk of Node/Express Infiltration:** Introducing JavaScript scripts for backend processing.  
   *Mitigation:* Root Maven structure and GitHub Actions CI strictly enforce pure Java/Maven builds.

---

## 27. Next Implementation Phases

1. **Phase 1 – Entity & Persistence Layer (Week 1):**
   - Author JPA entities and Flyway migrations for `account_db`, `ride_db`, and `payment_db`.
   - Author MongoDB `@Document` models for `driver_db`.
   - Implement Spring Data repositories with query method contracts.
2. **Phase 2 – Domain Logic & Interservice Clients (Week 2):**
   - Implement Account Service BCrypt hashing and JWT generation.
   - Implement Ride State Machine lifecycle transitions in `ride-management-service`.
   - Implement upfront fare estimation and final calculation formula in `fare-payment-service`.
   - Configure Spring `RestClient` beans for synchronous interservice communication.
3. **Phase 3 – REST Controllers & OpenAPI Annotations (Week 2–3):**
   - Implement REST controllers and request/response DTOs.
   - Decorate endpoints with `@Operation`, `@ApiResponse`, and validation annotations (`@Valid`, `@NotNull`).
   - Verify Swagger UI rendering on ports 8081, 8082, 8083, and 8084.
4. **Phase 4 – Testing, Postman Population & CI Verification (Week 3):**
   - Populate `RideLink.postman_collection.json` with sample request bodies and test scripts.
   - Author JUnit 5 and Mockito unit tests across all four services.
   - Execute negative scenario verification (no driver, invalid transition, payment failure).
5. **Phase 5 – Final Report & Viva Preparation (Week 4):**
   - Compile individual contribution statements and architecture rationale into the final 8–12 page submission PDF.
   - Tag git release version (`v1.0.0-final`).

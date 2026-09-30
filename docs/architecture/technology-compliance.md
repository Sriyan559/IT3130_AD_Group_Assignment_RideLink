# Technology Compliance Specification

**Module:** IT3130 – Application Development  
**Project:** RideLink – Backend Microservices for a Ride-Sharing Platform  
**Document:** Technology Compliance & Architectural Constraints  
**Version:** 1.0.0

---

## 1. Executive Compliance Statement

This document defines the strict, non-negotiable architectural and technological boundaries mandated for the IT3130 RideLink Application Development Group Assignment. All team members must adhere strictly to these constraints.

---

## 2. Mandatory Technology Requirements

| Component / Concern | Mandated Technology / Standard | Status | Assessment Justification |
| :--- | :--- | :--- | :--- |
| **Backend Language** | **Java 17 (or Java 17+)** | **COMPLIANT** | Standard enterprise language aligned with IT3130 curriculum and lab requirements. |
| **Backend Framework** | **Spring Boot 3.3.x** | **COMPLIANT** | Industry-standard microservices framework providing Spring Web, Spring Data, and Spring Security. |
| **Number of Microservices** | **Exactly 4 Core Services** | **COMPLIANT** | 1. Account Service<br>2. Driver & Vehicle Service<br>3. Ride Management Service<br>4. Fare & Payment Service. |
| **MERN Backend Architecture** | **STRICTLY PROHIBITED** | **COMPLIANT** | MERN backend architecture is not permitted for the assignment submission. |
| **Node.js / Express.js Backend** | **STRICTLY PROHIBITED** | **COMPLIANT** | JavaScript/TypeScript backends as replacements for Spring Boot are non-compliant. |
| **Database Isolation** | **Independent DB per Service** | **COMPLIANT** | Each microservice maintains its own physical or logical data store boundary. |
| **Shared Database** | **STRICTLY PROHIBITED** | **COMPLIANT** | A single shared database across all four microservices is forbidden. No cross-service joins or repo sharing. |
| **MongoDB Usage** | **PERMITTED (Per Service)** | **COMPLIANT** | Permitted where appropriate. Utilized in Driver & Vehicle Service for location and vehicle attributes. |
| **Frontend Development** | **NOT REQUIRED (Out of Scope)** | **COMPLIANT** | Web/mobile UI is not required and carries no separate marks. |
| **Official Demonstration Client**| **Swagger UI / Postman** | **COMPLIANT** | Interactive API documentation (Springdoc OpenAPI) and shared Postman collection are the official test harness. |
| **Interservice Communication** | **RESTful HTTP / Contracts** | **COMPLIANT** | Synchronous REST interfaces with scalar identifiers (`passengerId`, `driverId`, `fareId`). |
| **Secret Management** | **Environment Variables / .gitignore** | **COMPLIANT** | Zero committed credentials, passwords, or production keys. `.env` and `application-local.yml` gitignored. |

---

## 3. Disallowed Architectural Patterns

1. **Monolithic or Shared Persistence:**
   - No microservice may connect to or query the database of another microservice.
   - Sharing JPA entities, Mongo document classes, or Spring Data repositories across services is strictly forbidden.
2. **Node.js / Express Backend Services:**
   - Any service written in Node.js, Express, Fastify, NestJS, or similar JS/TS frameworks is non-compliant with the IT3130 assignment brief.
3. **Frontend Application Sprawl:**
   - Developing React, Angular, Vue, or Next.js frontends is outside the project scope and diverts effort from backend microservice quality.
4. **Hardcoded Secrets:**
   - No database credentials, connection strings, or JWT HMAC secrets may be committed to version control.

---

## 4. Existing Repository Audit & Remediation

- **Audit Date:** 2026-09-24
- **Inspection Result:** The workspace `d:\IT3130_AD_Group_Assignment_RideLink` was audited and found completely clean prior to project initialization.
- **Node.js / Express Backend Status:** `NOT PRESENT`. No legacy or non-compliant Node/Express code was found.
- **Shared DB Status:** `NOT PRESENT`. No shared database existed.
- **Action Taken:** Initialized pure Java 17 + Spring Boot 3 multi-module Maven structure with 4 independent service directories and persistence boundaries.

---

## 5. Architectural Verification Checklist

- [x] Backend language is exclusively Java.
- [x] Backend framework is Spring Boot.
- [x] Exactly four independently executable microservices are defined.
- [x] No Node.js / Express backend exists or is used.
- [x] Each service owns its dedicated database (`account_db`, `driver_db`, `ride_db`, `payment_db`).
- [x] No shared database exists.
- [x] Swagger UI / OpenAPI structure is configured for each service.
- [x] Postman collection and environment structure is established.
- [x] Frontend is excluded as out-of-scope.
- [x] Interservice communication uses stable identifiers and network APIs.

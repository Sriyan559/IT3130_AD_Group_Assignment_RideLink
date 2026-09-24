# Service Boundaries & Responsibilities

**Module:** IT3130 – Application Development  
**Project:** RideLink – Backend Microservices for a Ride-Sharing Platform

---

## 1. Service Decomposition Overview

The RideLink platform is decomposed into **exactly four core microservices**, strictly matching the IT3130 assignment brief. Each service has a dedicated student owner who is primary responsible for its implementation, testing, and viva defense:

---

## 2. Microservice 1: Account Service
- **Primary Owner:** Fernando B S C (Student ID: IT24103775)
- **Base Package:** `com.ridelink.account`
- **Default Port:** `8081`
- **Persistence Boundary:** `account_db` (PostgreSQL)

### Responsibilities:
1. User registration for passengers and drivers.
2. User credential management and authentication.
3. JWT access token generation and cryptographic signing.
4. Role management (`ROLE_PASSENGER`, `ROLE_DRIVER`, `ROLE_ADMIN`).
5. User profile inspection and updates (name, phone, contact details).
6. Account status enforcement (`ACTIVE`, `SUSPENDED`, `DEACTIVATED`).

---

## 3. Microservice 2: Driver & Vehicle Service
- **Primary Owner:** Rathnakoon D A (Student ID: IT24300246)
- **Base Package:** `com.ridelink.driver`
- **Default Port:** `8082`
- **Persistence Boundary:** `driver_db` (MongoDB)

### Responsibilities:
1. Driver operational profile maintenance (license numbers, experience).
2. Vehicle registration and attribute tracking (make, model, license plate, seating capacity).
3. Driver availability status toggling (`AVAILABLE`, `ON_TRIP`, `OFFLINE`).
4. Operational service area definition (city zone or coverage polygon).
5. Simulated GPS location reporting (latitude and longitude updates).
6. Querying and filtering eligible, available drivers for ride matching.

---

## 4. Microservice 3: Ride Management Service
- **Primary Owner:** Herath H M S R (Student ID: IT24103280)
- **Base Package:** `com.ridelink.ride`
- **Default Port:** `8083`
- **Persistence Boundary:** `ride_db` (PostgreSQL)

### Responsibilities:
1. Ride booking request creation (specifying passenger, pickup, and destination coordinates).
2. Coordinating driver search by invoking Driver & Vehicle Service.
3. Driver assignment reference tracking (`driverId`).
4. Enforcing the strict Ride State Machine:
   - `REQUESTED` -> `ASSIGNED` -> `ACCEPTED` -> `IN_PROGRESS` -> `COMPLETED` / `CANCELLED`.
5. Preventing invalid state transitions and illegal cancellations.
6. Ride history and trip tracking queries.

---

## 5. Microservice 4: Fare & Payment Service
- **Primary Owner:** Sanjeewa H.D.U.S (Student ID: IT24101590)
- **Base Package:** `com.ridelink.payment`
- **Default Port:** `8084`
- **Persistence Boundary:** `payment_db` (PostgreSQL)

### Responsibilities:
1. Upfront fare estimation based on distance, duration estimate, and base tariff rules.
2. Deterministic final fare calculation upon trip completion.
3. Processing simulated payment transactions against ride IDs.
4. Payment transaction lifecycle tracking (`PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`).
5. Generating immutable payment receipts with itemized breakdown.
6. Receipt retrieval by receipt ID or ride ID.

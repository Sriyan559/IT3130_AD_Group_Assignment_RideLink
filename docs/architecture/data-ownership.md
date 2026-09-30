# Data Ownership & Persistence Boundaries

**Module:** IT3130 – Application Development  
**Project:** RideLink – Backend Microservices for a Ride-Sharing Platform

---

## 1. Non-Negotiable Rule: Database per Service

In strict accordance with the IT3130 specification:
> *"Each service must own its persistence boundary. One service must not directly query or modify another service’s database or tables."*

The architecture implements **strict Database-per-Service**. A single shared database across all four microservices is **forbidden**.

---

## 2. Data Ownership Matrix

| Microservice | Dedicated Database | Technology | Exclusively Owned Data Entities / Documents |
| :--- | :--- | :--- | :--- |
| **Account Service** | `account_db` | PostgreSQL (Relational) | • User accounts (passengers, drivers, admins)<br>• Password credentials & salts<br>• User roles & permissions<br>• User profile attributes<br>• Account status (`ACTIVE`, `SUSPENDED`) |
| **Driver & Vehicle Service** | `driver_db` | MongoDB (Document) | • Driver operational records<br>• Vehicle details & specifications<br>• Real-time availability (`AVAILABLE`, `OFFLINE`)<br>• Service area boundaries<br>• Simulated GPS coordinates (GeoJSON / lat-lng) |
| **Ride Management Service**| `ride_db` | PostgreSQL (Relational) | • Ride requests<br>• Pickup and drop-off coordinates/names<br>• Ride lifecycle states & transition logs<br>• External reference IDs (`passengerId`, `driverId`, `fareId`)<br>• Trip start/end timestamps |
| **Fare & Payment Service** | `payment_db` | PostgreSQL (Relational) | • Fare calculation rules & tariffs<br>• Upfront fare estimates<br>• Payment transaction records<br>• Payment status (`PENDING`, `COMPLETED`, `FAILED`)<br>• Immutable receipts & tax/fare breakdowns |

---

## 3. Data Ownership Diagram

```mermaid
graph LR
    subgraph "Account Domain"
        AS["Account Service"] -->|"Owns Exclusively"| D1[("account_db")]
        D1 --- E1["Users, Roles, Credentials, Profiles, Status"]
    end

    subgraph "Driver Domain"
        DS["Driver & Vehicle Service"] -->|"Owns Exclusively"| D2[("driver_db")]
        D2 --- E2["Vehicles, Driver Status, GPS Coordinates, Service Zones"]
    end

    subgraph "Ride Domain"
        RS["Ride Management Service"] -->|"Owns Exclusively"| D3[("ride_db")]
        D3 --- E3["Rides, State Transitions, Timestamps, Reference IDs"]
    end

    subgraph "Fare & Payment Domain"
        FS["Fare & Payment Service"] -->|"Owns Exclusively"| D4[("payment_db")]
        D4 --- E4["Tariffs, Estimates, Payments, Transaction Logs, Receipts"]
    end

    classDef forbidden stroke:#f00,stroke-width:2px,stroke-dasharray: 5 5;
```

---

## 4. Cross-Service Reference Rules

1. **No Shared JPA Entities or Repositories:**  
   Entities from one service's package (e.g. `com.ridelink.account.entity`) can never be imported into another service.
2. **Scalar External Identifiers:**  
   Cross-service relationships are maintained using stable scalar values (e.g., `UUID` or `String` identifiers such as `passengerId`, `driverId`, `fareId`).
3. **No Cross-Database Joins:**  
   SQL `JOIN` queries across databases or schemas are impossible and forbidden.
4. **Data Synchronization via APIs:**  
   When Ride Management needs to check if a driver is available, it makes an HTTP GET request to `Driver & Vehicle Service`'s API. It never reads `driver_db`.

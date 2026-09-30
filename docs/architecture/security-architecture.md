# Security Architecture & Secret Management

**Module:** IT3130 – Application Development  
**Project:** RideLink – Backend Microservices for a Ride-Sharing Platform

---

## 1. Security Overview

Security in RideLink is designed around the principles of least privilege, role-based access control (RBAC), stateless token-based authentication, and zero-leakage secret handling.

---

## 2. Authentication & Token Issuance

1. **User Authentication Authority:**
   - The **Account Service** acts as the centralized identity provider (IdP).
   - Passwords are encrypted using **BCrypt** with an adaptive work factor (default: 12 rounds) before persistence in `account_db`. Plaintext passwords are never logged or stored.
2. **JSON Web Tokens (JWT):**
   - Upon successful credentials verification, Account Service issues a cryptographically signed HMAC-SHA256 JWT.
   - Claims include:
     - `sub`: User ID (UUID)
     - `email`: User email address
     - `roles`: Assigned authority roles (`ROLE_PASSENGER`, `ROLE_DRIVER`, `ROLE_ADMIN`)
     - `iat`: Issued-at timestamp
     - `exp`: Expiration timestamp (e.g., 24 hours)

---

## 3. Role-Based Access Control (RBAC) Matrix

| Endpoint Area / Operation | Allowed Roles | Enforced By |
| :--- | :--- | :--- |
| User Profile Update | `PASSENGER`, `DRIVER`, `ADMIN` (self only) | Account Service |
| Driver Vehicle & Location Updates | `DRIVER` | Driver & Vehicle Service |
| Ride Creation | `PASSENGER` | Ride Management Service |
| Ride Status Progression (`ACCEPTED`, `IN_PROGRESS`) | `DRIVER` | Ride Management Service |
| Fare Calculation & Receipt Access | `PASSENGER`, `DRIVER`, `ADMIN` | Fare & Payment Service |
| Administrative Queries & Audit Logs | `ADMIN` | All Services |

---

## 4. Secret Management & Version Control Hygiene

In compliance with assignment marking criteria G4:
- **No Hardcoded Secrets:** Passwords, connection strings, JWT signing keys, and cloud tokens are strictly banned from Git commits.
- **Environment Variable Parameterization:** All credentials default to safe local placeholders and are injected via system environment variables (`ACCOUNT_DB_PASSWORD`, `JWT_SECRET`, etc.).
- **Ignored Files:** `.gitignore` actively prevents tracking of `.env`, `application-local.yml`, `secrets/`, and temporary build outputs.
- **Example Templates:** Non-sensitive templates (`.env.example`, `application-local.yml.example`) provide clear schemas without exposing confidential values.

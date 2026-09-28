# Ride Management Service Database Migrations

This directory contains Flyway database migration scripts (e.g. `V1__init_ride_schema.sql`).

Database: `ride_db` (PostgreSQL)
Persistence: Dedicated, independent relational database schema.
Cross-service access: **STRICTLY PROHIBITED**. Only Ride Management Service accesses `ride_db`.
External references: Stored as stable scalar identifiers (`passenger_id`, `driver_id`, `fare_id`), without database-level foreign keys across services.

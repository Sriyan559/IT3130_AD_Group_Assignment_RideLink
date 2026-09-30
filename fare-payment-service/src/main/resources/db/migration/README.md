# Fare & Payment Service Database Migrations

Flyway applies versioned schema changes at service startup. `V1__create_fare_payment_tables.sql`
creates the fare record and simulated payment tables in the dedicated PostgreSQL `payment_db`.

Only Fare & Payment Service connects to `payment_db`. Other services may pass stable ride and
passenger IDs over APIs but must not query or modify this database directly.

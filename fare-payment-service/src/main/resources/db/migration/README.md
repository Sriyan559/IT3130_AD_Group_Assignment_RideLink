# Fare & Payment Service Database Migrations
This directory contains Flyway database migration scripts (e.g. V1__init_payment_schema.sql).
Database: payment_db (PostgreSQL)
Persistence: Dedicated, independent relational database schema.
Cross-service access: STRICTLY PROHIBITED. Only Fare & Payment Service connects to payment_db.

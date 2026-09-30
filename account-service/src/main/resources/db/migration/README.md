# Account Service Database Migrations

This directory is designated for database migration scripts (e.g., Flyway versioned SQL scripts: `V1__init_account_schema.sql`).

Database: `account_db` (PostgreSQL)
Persistence: Dedicated, independent relational database schema.
Cross-service access: **STRICTLY PROHIBITED**. Only Account Service connects to `account_db`.

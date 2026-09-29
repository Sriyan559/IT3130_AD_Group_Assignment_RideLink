# Ride Management Service Database Migrations

MongoDB does not use Flyway SQL migrations. Ride documents are stored in the `rides` collection in `ride_db`.

Cross-service access: **STRICTLY PROHIBITED**. Only Ride Management Service accesses `ride_db`.
External references: Stored as stable scalar identifiers (`passenger_id`, `driver_id`, `fare_id`), without database-level foreign keys across services.

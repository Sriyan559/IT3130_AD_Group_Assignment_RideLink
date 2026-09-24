# Negative Scenarios Integration Tests

Validates error handling for:
1. No driver available in service radius.
2. Invalid ride lifecycle transitions (e.g. CANCELLED after COMPLETED).
3. Unauthorized operations or missing JWT.
4. Invalid input formats or coordinate boundaries.
5. Failed simulated payment and rollback behavior.

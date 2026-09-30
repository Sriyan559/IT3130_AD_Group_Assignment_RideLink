# Continuous integration

`.github/workflows/ci.yml` runs for pushes and pull requests targeting `main`,
`develop` and `integration`.

- The Java 17 matrix verifies each service with `mvn -B -pl <service> -am verify`.
  `-am` builds shared dependencies such as `service-support` in the same reactor;
  building Driver/Ride in isolation cannot resolve a fresh snapshot dependency.
- After the matrix succeeds, an aggregator job starts a local MongoDB service,
  enables `RIDELINK_MONGO_TESTS=true`, and runs `mvn -B verify`.
- The aggregator then runs `integration-tests/run-local-e2e.py` against real Account,
  Driver, Ride and Fare JARs with authentication enabled and isolated databases.
- CI E2E uses PostgreSQL 16 for Fare and MongoDB for the other services.
  Local E2E defaults to a separate H2 SQL file unless E2E_POSTGRES_DSN is provided.

Local equivalent (MongoDB required):

```powershell
$env:RIDELINK_MONGO_TESTS = 'true'
mvn -B verify
python -m pip install -r integration-tests/requirements.txt
python integration-tests/run-local-e2e.py
```

Hosted GitHub Actions results must be checked after publication; a successful local
run is not a claim that a hosted workflow has passed.

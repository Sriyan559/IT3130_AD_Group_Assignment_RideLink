CREATE TABLE final_fares (
 ride_id UUID PRIMARY KEY, passenger_id VARCHAR(100) NOT NULL,
 distance_kilometers NUMERIC(19,6) NOT NULL CHECK (distance_kilometers > 0),
 duration_minutes BIGINT NOT NULL CHECK (duration_minutes >= 0),
 base_fare NUMERIC(19,2) NOT NULL, distance_fare NUMERIC(19,2) NOT NULL,
 time_fare NUMERIC(19,2) NOT NULL, minimum_adjustment NUMERIC(19,2) NOT NULL,
 total NUMERIC(19,2) NOT NULL CHECK (total >= 0), currency VARCHAR(3) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, paid_payment_id UUID
);
CREATE TABLE payment_attempts (
 id UUID PRIMARY KEY, idempotency_key VARCHAR(100) NOT NULL UNIQUE,
 ride_id UUID NOT NULL REFERENCES final_fares(ride_id), passenger_id VARCHAR(100) NOT NULL,
 amount NUMERIC(19,2) NOT NULL CHECK (amount >= 0), currency VARCHAR(3) NOT NULL,
 payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('CARD','CASH')),
 status VARCHAR(20) NOT NULL CHECK (status IN ('SUCCESS','FAILED')),
 simulate_failure BOOLEAN NOT NULL, transaction_reference VARCHAR(80) NOT NULL UNIQUE,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, paid_at TIMESTAMP WITH TIME ZONE, receipt_id UUID
);
CREATE INDEX payment_ride_index ON payment_attempts(ride_id);
CREATE TABLE receipts (
 id UUID PRIMARY KEY, payment_id UUID NOT NULL UNIQUE REFERENCES payment_attempts(id),
 ride_id UUID NOT NULL UNIQUE REFERENCES final_fares(ride_id), passenger_id VARCHAR(100) NOT NULL,
 base_fare NUMERIC(19,2) NOT NULL, distance_fare NUMERIC(19,2) NOT NULL,
 time_fare NUMERIC(19,2) NOT NULL, minimum_adjustment NUMERIC(19,2) NOT NULL,
 total NUMERIC(19,2) NOT NULL, currency VARCHAR(3) NOT NULL,
 transaction_reference VARCHAR(80) NOT NULL, issued_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE rides (
    id UUID PRIMARY KEY,
    passenger_id BIGINT NOT NULL,
    driver_id BIGINT,
    pickup_latitude NUMERIC(9, 6) NOT NULL,
    pickup_longitude NUMERIC(9, 6) NOT NULL,
    pickup_address VARCHAR(255) NOT NULL,
    destination_latitude NUMERIC(9, 6) NOT NULL,
    destination_longitude NUMERIC(9, 6) NOT NULL,
    destination_address VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    distance_km NUMERIC(10, 2),
    duration_minutes BIGINT,
    fare_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_rides_passenger_created_at ON rides (passenger_id, created_at DESC);
CREATE INDEX idx_rides_driver_status ON rides (driver_id, status);
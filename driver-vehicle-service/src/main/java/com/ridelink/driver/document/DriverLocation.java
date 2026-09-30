package com.ridelink.driver.document;

import java.time.Instant;

public record DriverLocation(double latitude, double longitude, Instant updatedAt) {
}

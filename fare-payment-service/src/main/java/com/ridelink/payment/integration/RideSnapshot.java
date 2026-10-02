package com.ridelink.payment.integration;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The subset of Ride Management Service's ride response that this service needs.
 * Other fields in the JSON are ignored, so Ride can add fields without breaking us.
 */
public record RideSnapshot(
        UUID id,
        String passengerId,
        String status,
        BigDecimal distanceKm,
        Long durationMinutes) {

    public static final String COMPLETED = "COMPLETED";

    public boolean isCompleted() {
        return COMPLETED.equals(status);
    }

    public boolean hasTripMetrics() {
        return distanceKm != null && durationMinutes != null;
    }
}

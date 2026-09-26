package com.ridelink.ride.dto.response;

import com.ridelink.ride.domain.RideStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RideResponse(
        UUID id, Long passengerId, Long driverId,
        BigDecimal pickupLatitude, BigDecimal pickupLongitude, String pickupAddress,
        BigDecimal destinationLatitude, BigDecimal destinationLongitude, String destinationAddress,
        RideStatus status, BigDecimal distanceKm, Long durationMinutes, UUID fareId,
        Instant createdAt, Instant updatedAt
) { }
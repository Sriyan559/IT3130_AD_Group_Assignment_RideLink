package com.ridelink.ride.dto.request;

import com.ridelink.ride.domain.RideStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateRideStatusRequest(
        @NotNull RideStatus status,
        @DecimalMin("0.0") BigDecimal distanceKm,
        @DecimalMin("0") Long durationMinutes
) { }
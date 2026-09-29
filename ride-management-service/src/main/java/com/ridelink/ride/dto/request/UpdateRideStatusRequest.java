package com.ridelink.ride.dto.request;

import com.ridelink.ride.domain.RideStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Request payload for transitioning ride lifecycle status")
public record UpdateRideStatusRequest(
        @Schema(description = "Target status according to state machine transitions (REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED / CANCELLED)", example = "ACCEPTED")
        @NotNull(message = "status is required")
        RideStatus status,

        @Schema(description = "Actual trip distance in kilometers (typically provided on COMPLETED)", example = "8.5", nullable = true)
        @DecimalMin(value = "0.0", message = "distanceKm must be positive")
        BigDecimal distanceKm,

        @Schema(description = "Actual trip duration in minutes (typically provided on COMPLETED)", example = "22", nullable = true)
        @DecimalMin(value = "0", message = "durationMinutes must be positive")
        Long durationMinutes
) { }
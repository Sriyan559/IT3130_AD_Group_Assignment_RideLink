package com.ridelink.ride.dto.response;

import com.ridelink.ride.domain.RideStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Detailed response representation of a ride")
public record RideResponse(
        @Schema(description = "Unique UUID of the ride", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "ID of the passenger", example = "6abbfa6d095b5451ceb2f209")
        String passengerId,

        @Schema(description = "ID of the assigned driver", example = "6abbfa6d095b5451ceb2f20a", nullable = true)
        String driverId,

        @Schema(description = "Pickup latitude", example = "6.9271")
        BigDecimal pickupLatitude,

        @Schema(description = "Pickup longitude", example = "79.8612")
        BigDecimal pickupLongitude,

        @Schema(description = "Pickup address / landmark", example = "Colombo Fort Railway Station, Colombo")
        String pickupAddress,

        @Schema(description = "Destination latitude", example = "6.9147")
        BigDecimal destinationLatitude,

        @Schema(description = "Destination longitude", example = "79.8732")
        BigDecimal destinationLongitude,

        @Schema(description = "Destination address / landmark", example = "SLIIT Metropolitan Campus, Colombo 03")
        String destinationAddress,

        @Schema(description = "Current lifecycle status of the ride", example = "REQUESTED")
        RideStatus status,

        @Schema(description = "Trip distance in kilometers", example = "8.5", nullable = true)
        BigDecimal distanceKm,

        @Schema(description = "Trip duration in minutes", example = "22", nullable = true)
        Long durationMinutes,

        @Schema(description = "Associated Fare ID from Fare & Payment Service", nullable = true)
        UUID fareId,

        @Schema(description = "Timestamp when ride was requested/created")
        Instant createdAt,

        @Schema(description = "Timestamp of the last status/field update")
        Instant updatedAt
) { }
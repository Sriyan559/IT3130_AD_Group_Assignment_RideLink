package com.ridelink.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Request to calculate the final fare of a completed ride")
public record FinalFareRequest(
        @Schema(description = "ID of a COMPLETED ride from Ride Management Service",
                example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "rideId is required")
        UUID rideId) {
}

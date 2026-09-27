package com.ridelink.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FareEstimateRequest(
        @NotNull(message = "Distance is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Distance must be greater than zero")
        BigDecimal distanceKilometers) {
}
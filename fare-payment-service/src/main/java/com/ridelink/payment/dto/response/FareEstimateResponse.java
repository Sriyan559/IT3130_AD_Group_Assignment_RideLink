package com.ridelink.payment.dto.response;

import java.math.BigDecimal;

public record FareEstimateResponse(
        BigDecimal distanceKilometers,
        BigDecimal estimatedFare,
        String currency) {
}
package com.ridelink.payment.fare;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FareCalculator {

    private static final BigDecimal BASE_FARE = new BigDecimal("150.00");
    private static final BigDecimal RATE_PER_KILOMETER = new BigDecimal("100.00");

    private FareCalculator() {
    }

    public static BigDecimal calculateFare(BigDecimal distanceKilometers) {
        if (distanceKilometers == null || distanceKilometers.signum() <= 0) {
            throw new IllegalArgumentException("Distance must be greater than zero");
        }

        return BASE_FARE.add(RATE_PER_KILOMETER.multiply(distanceKilometers))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
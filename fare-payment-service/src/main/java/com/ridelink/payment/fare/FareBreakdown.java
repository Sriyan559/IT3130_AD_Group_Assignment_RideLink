package com.ridelink.payment.fare;

import java.math.BigDecimal;

/**
 * Itemised result of applying the fare rule. All money values have two decimal places.
 *
 * @param minimumAdjustment amount added so the total reaches the minimum fare (zero when not needed)
 */
public record FareBreakdown(
        BigDecimal distanceKilometers,
        long durationMinutes,
        BigDecimal baseFare,
        BigDecimal distanceFare,
        BigDecimal timeFare,
        BigDecimal minimumAdjustment,
        BigDecimal total,
        String currency) {
}

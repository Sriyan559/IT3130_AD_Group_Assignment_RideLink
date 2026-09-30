package com.ridelink.payment.fare;

import com.ridelink.payment.exception.ApiException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/**
 * Applies the documented RideLink fare rule:
 *
 * <pre>
 * subtotal = baseRate + (distanceKm x perKmRate) + (durationMinutes x perMinuteRate)
 * total    = max(minimumFare, subtotal)
 * </pre>
 *
 * Default rates (application.yml): base 150, 100 per km, 10 per minute, minimum 200 LKR.
 * Example: 8 km and 20 minutes = 150 + 800 + 200 = LKR 1150.00.
 * Each component is rounded HALF_UP to 2 decimal places before adding.
 * <p>
 * The class is stateless apart from the rates, so the same inputs always give the same fare.
 */
@Component
public class FareCalculator {

    public static final String CURRENCY = "LKR";
    /** Upper bounds reject obviously wrong input (e.g. a distance typed in metres). */
    public static final String MAX_DISTANCE_KM = "10000";
    public static final long MAX_DURATION_MINUTES = 100_000;

    private static final BigDecimal MAX_DISTANCE = new BigDecimal(MAX_DISTANCE_KM);

    private final FarePolicy policy;

    public FareCalculator(FarePolicy policy) {
        this.policy = policy;
    }

    public FareBreakdown calculate(BigDecimal distanceKilometers, Long durationMinutes) {
        validate(distanceKilometers, durationMinutes);

        BigDecimal baseFare = money(policy.baseRate());
        BigDecimal distanceFare = money(distanceKilometers.multiply(policy.perKmRate()));
        BigDecimal timeFare = money(policy.perMinuteRate().multiply(BigDecimal.valueOf(durationMinutes)));
        BigDecimal subtotal = baseFare.add(distanceFare).add(timeFare);

        BigDecimal minimumAdjustment = money(policy.minimumFare().subtract(subtotal).max(BigDecimal.ZERO));
        BigDecimal total = subtotal.add(minimumAdjustment);

        return new FareBreakdown(distanceKilometers, durationMinutes, baseFare, distanceFare, timeFare,
                minimumAdjustment, total, CURRENCY);
    }

    public FarePolicy getPolicy() {
        return policy;
    }

    private void validate(BigDecimal distanceKilometers, Long durationMinutes) {
        if (distanceKilometers == null || distanceKilometers.signum() <= 0
                || distanceKilometers.compareTo(MAX_DISTANCE) > 0) {
            throw ApiException.badRequest("INVALID_DISTANCE",
                    "Distance must be greater than 0 and at most " + MAX_DISTANCE_KM + " km");
        }
        if (durationMinutes == null || durationMinutes < 0 || durationMinutes > MAX_DURATION_MINUTES) {
            throw ApiException.badRequest("INVALID_DURATION",
                    "Duration must be between 0 and " + MAX_DURATION_MINUTES + " minutes");
        }
    }

    private static BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}

package com.ridelink.payment.fare;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ridelink.support.AccessFailure;

@Component
public class FareCalculator {
    private final BigDecimal base, perKm, perMinute, minimum;
    public record Breakdown(BigDecimal distanceKilometers, long durationMinutes,
            BigDecimal baseFare, BigDecimal distanceFare, BigDecimal timeFare,
            BigDecimal minimumAdjustment, BigDecimal total, String currency) {}
    public FareCalculator(@Value("${fare.base-rate:150}") BigDecimal base,
            @Value("${fare.per-km-rate:100}") BigDecimal perKm,
            @Value("${fare.per-minute-rate:10}") BigDecimal perMinute,
            @Value("${fare.minimum-fare:200}") BigDecimal minimum) {
        if (base.signum()<0 || perKm.signum()<0 || perMinute.signum()<0 || minimum.signum()<0)
            throw new IllegalArgumentException("Fare rates cannot be negative");
        this.base=base; this.perKm=perKm; this.perMinute=perMinute; this.minimum=minimum;
    }
    public Breakdown calculate(BigDecimal distance, Long minutes) {
        if (distance==null || distance.signum()<=0 || distance.compareTo(new BigDecimal("10000"))>0
                || minutes==null || minutes<0 || minutes>100000)
            throw new AccessFailure(400,"Positive distance up to 10000 km and duration 0-100000 minutes required");
        BigDecimal baseFare=money(base), distanceFare=money(distance.multiply(perKm)), timeFare=money(perMinute.multiply(BigDecimal.valueOf(minutes)));
        BigDecimal subtotal=baseFare.add(distanceFare).add(timeFare);
        BigDecimal adjustment=money(minimum.subtract(subtotal).max(BigDecimal.ZERO));
        return new Breakdown(distance,minutes,baseFare,distanceFare,timeFare,adjustment,subtotal.add(adjustment),"LKR");
    }
    private BigDecimal money(BigDecimal amount) { return amount.setScale(2,RoundingMode.HALF_UP); }
}

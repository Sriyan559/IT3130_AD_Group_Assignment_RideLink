package com.ridelink.payment.fare;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Fare rates loaded from the {@code fare.*} properties in application.yml.
 * <p>
 * Keeping the rates in configuration (not code) means the tariff can change without a
 * rebuild. Startup fails if any rate is missing or negative.
 *
 * @param baseRate      fixed charge for every ride (LKR)
 * @param perKmRate     charge per kilometre travelled (LKR)
 * @param perMinuteRate charge per minute of ride duration (LKR)
 * @param minimumFare   lowest total a ride can cost (LKR)
 */
@Validated
@ConfigurationProperties(prefix = "fare")
public record FarePolicy(
        @NotNull @PositiveOrZero BigDecimal baseRate,
        @NotNull @PositiveOrZero BigDecimal perKmRate,
        @NotNull @PositiveOrZero BigDecimal perMinuteRate,
        @NotNull @PositiveOrZero BigDecimal minimumFare) {

    /** Human-readable form of the rule, shown in estimate responses and Swagger. */
    public String describe() {
        return "max(%s, %s + %s x km + %s x minutes) LKR".formatted(
                minimumFare.toPlainString(), baseRate.toPlainString(),
                perKmRate.toPlainString(), perMinuteRate.toPlainString());
    }
}

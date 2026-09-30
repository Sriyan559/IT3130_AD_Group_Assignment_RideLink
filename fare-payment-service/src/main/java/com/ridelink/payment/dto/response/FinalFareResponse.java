package com.ridelink.payment.dto.response;

import com.ridelink.payment.entity.FinalFare;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Stored final fare of a completed ride")
public record FinalFareResponse(
        UUID rideId,
        String passengerId,
        BigDecimal distanceKilometers,
        long durationMinutes,
        BigDecimal baseFare,
        BigDecimal distanceFare,
        BigDecimal timeFare,
        BigDecimal minimumAdjustment,
        @Schema(example = "1150.00") BigDecimal total,
        String currency,
        Instant createdAt,
        @Schema(description = "Successful payment, null while unpaid", nullable = true) UUID paidPaymentId) {

    public static FinalFareResponse from(FinalFare fare) {
        return new FinalFareResponse(fare.getRideId(), fare.getPassengerId(), fare.getDistanceKilometers(),
                fare.getDurationMinutes(), fare.getBaseFare(), fare.getDistanceFare(), fare.getTimeFare(),
                fare.getMinimumAdjustment(), fare.getTotal(), fare.getCurrency(), fare.getCreatedAt(),
                fare.getPaidPaymentId());
    }
}

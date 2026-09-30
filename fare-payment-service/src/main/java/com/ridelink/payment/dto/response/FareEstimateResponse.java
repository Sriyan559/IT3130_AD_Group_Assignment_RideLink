package com.ridelink.payment.dto.response;

import com.ridelink.payment.fare.FareBreakdown;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Itemised fare estimate")
public record FareEstimateResponse(
        @Schema(example = "8") BigDecimal distanceKilometers,
        @Schema(example = "20") long durationMinutes,
        @Schema(example = "150.00") BigDecimal baseFare,
        @Schema(example = "800.00") BigDecimal distanceFare,
        @Schema(example = "200.00") BigDecimal timeFare,
        @Schema(description = "Added to reach the minimum fare", example = "0.00") BigDecimal minimumAdjustment,
        @Schema(example = "1150.00") BigDecimal total,
        @Schema(example = "LKR") String currency,
        @Schema(description = "The rule used", example = "max(200, 150 + 100 x km + 10 x minutes) LKR") String fareRule) {

    public static FareEstimateResponse from(FareBreakdown breakdown, String fareRule) {
        return new FareEstimateResponse(breakdown.distanceKilometers(), breakdown.durationMinutes(),
                breakdown.baseFare(), breakdown.distanceFare(), breakdown.timeFare(),
                breakdown.minimumAdjustment(), breakdown.total(), breakdown.currency(), fareRule);
    }
}

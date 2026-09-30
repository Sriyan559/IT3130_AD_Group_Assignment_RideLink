package com.ridelink.payment.dto.request;

import com.ridelink.payment.fare.FareCalculator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Trip figures used to estimate a fare before booking")
public record FareEstimateRequest(
        @Schema(description = "Trip distance in km (greater than 0, at most 10000)", example = "8")
        @NotNull(message = "distanceKilometers is required")
        @DecimalMin(value = "0", inclusive = false, message = "distanceKilometers must be greater than 0")
        @DecimalMax(value = FareCalculator.MAX_DISTANCE_KM, message = "distanceKilometers must be at most 10000")
        BigDecimal distanceKilometers,

        @Schema(description = "Expected duration in minutes (0-100000). Optional, defaults to 0", example = "20")
        @Min(value = 0, message = "durationMinutes must be 0 or more")
        @Max(value = FareCalculator.MAX_DURATION_MINUTES, message = "durationMinutes must be at most 100000")
        Long durationMinutes) {
}

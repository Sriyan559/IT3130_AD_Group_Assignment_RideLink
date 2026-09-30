package com.ridelink.payment.dto.request;

import com.ridelink.payment.domain.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Simulated payment for a completed ride. The amount charged is the ride's final fare.")
public record ProcessPaymentRequest(
        @Schema(description = "Ride being paid for", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "rideId is required")
        UUID rideId,

        @Schema(description = "Client-chosen unique key. Resending the same key returns the original attempt "
                + "instead of charging again", example = "ride-550e8400-attempt-1")
        @NotBlank(message = "idempotencyKey is required")
        @Size(max = 100, message = "idempotencyKey must be at most 100 characters")
        String idempotencyKey,

        @Schema(description = "CARD or CASH", example = "CARD")
        @NotNull(message = "paymentMethod is required (CARD or CASH)")
        PaymentMethod paymentMethod,

        @Schema(description = "true forces a FAILED attempt to demonstrate the failed-payment scenario",
                example = "false")
        boolean simulateFailure,

        @Schema(description = "Optional. The amount the passenger expects to pay; if given it must equal the "
                + "final fare, otherwise the payment is rejected", example = "1150.00", nullable = true)
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        @Digits(integer = 17, fraction = 2, message = "amount must have at most 2 decimal places")
        BigDecimal amount) {

    /** Request without a client-side amount check (used by Ride Service). */
    public ProcessPaymentRequest(UUID rideId, String idempotencyKey, PaymentMethod paymentMethod,
                                 boolean simulateFailure) {
        this(rideId, idempotencyKey, paymentMethod, simulateFailure, null);
    }
}

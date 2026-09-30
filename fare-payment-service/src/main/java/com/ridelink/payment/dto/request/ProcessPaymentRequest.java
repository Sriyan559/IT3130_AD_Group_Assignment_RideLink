package com.ridelink.payment.dto.request;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record ProcessPaymentRequest(@NotNull UUID rideId,
        @NotBlank @Size(max=100) String idempotencyKey,
        @NotNull Method paymentMethod, boolean simulateFailure) {
    public enum Method { CARD, CASH }
}

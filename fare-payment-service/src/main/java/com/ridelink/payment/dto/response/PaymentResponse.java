package com.ridelink.payment.dto.response;

import com.ridelink.payment.domain.PaymentMethod;
import com.ridelink.payment.domain.PaymentStatus;
import com.ridelink.payment.entity.PaymentAttempt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "A simulated payment attempt")
public record PaymentResponse(
        UUID id,
        String idempotencyKey,
        UUID rideId,
        String passengerId,
        @Schema(example = "1150.00") BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        @Schema(example = "SUCCESS") PaymentStatus status,
        boolean simulateFailure,
        @Schema(example = "SIM-3fa85f64-5717-4562-b3fc-2c963f66afa6") String transactionReference,
        Instant createdAt,
        @Schema(nullable = true) Instant paidAt,
        @Schema(description = "Receipt ID, null when the payment FAILED", nullable = true) UUID receiptId) {

    public static PaymentResponse from(PaymentAttempt payment) {
        return new PaymentResponse(payment.getId(), payment.getIdempotencyKey(), payment.getRideId(),
                payment.getPassengerId(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod(),
                payment.getStatus(), payment.isSimulateFailure(), payment.getTransactionReference(),
                payment.getCreatedAt(), payment.getPaidAt(), payment.getReceiptId());
    }
}

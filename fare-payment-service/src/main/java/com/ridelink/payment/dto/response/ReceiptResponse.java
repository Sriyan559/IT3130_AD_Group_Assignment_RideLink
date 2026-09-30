package com.ridelink.payment.dto.response;

import com.ridelink.payment.entity.Receipt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Itemised receipt for a successful payment")
public record ReceiptResponse(
        UUID id,
        UUID paymentId,
        UUID rideId,
        String passengerId,
        BigDecimal baseFare,
        BigDecimal distanceFare,
        BigDecimal timeFare,
        BigDecimal minimumAdjustment,
        @Schema(example = "1150.00") BigDecimal total,
        String currency,
        String transactionReference,
        Instant issuedAt) {

    public static ReceiptResponse from(Receipt receipt) {
        return new ReceiptResponse(receipt.getId(), receipt.getPaymentId(), receipt.getRideId(),
                receipt.getPassengerId(), receipt.getBaseFare(), receipt.getDistanceFare(), receipt.getTimeFare(),
                receipt.getMinimumAdjustment(), receipt.getTotal(), receipt.getCurrency(),
                receipt.getTransactionReference(), receipt.getIssuedAt());
    }
}

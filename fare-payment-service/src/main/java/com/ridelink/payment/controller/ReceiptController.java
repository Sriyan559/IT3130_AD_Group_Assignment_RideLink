package com.ridelink.payment.controller;

import com.ridelink.payment.dto.response.ReceiptResponse;
import com.ridelink.payment.exception.ApiErrorResponse;
import com.ridelink.payment.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Receipts", description = "Receipts for successful payments")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping({"/api/receipts/{receiptId}", "/api/v1/receipts/{receiptId}"})
    @Operation(summary = "Get a receipt by ID")
    @ApiResponse(responseCode = "200", description = "Receipt")
    @ApiResponse(responseCode = "404", description = "Receipt not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ReceiptResponse getReceipt(@PathVariable UUID receiptId) {
        return ReceiptResponse.from(receiptService.getReceipt(receiptId));
    }

    @GetMapping({"/api/payments/{paymentId}/receipt", "/api/v1/payments/{paymentId}/receipt"})
    @Operation(summary = "Get the receipt of a payment", description = "404 when the payment FAILED")
    @ApiResponse(responseCode = "200", description = "Receipt")
    @ApiResponse(responseCode = "404", description = "Payment not found or payment FAILED",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ReceiptResponse getReceiptForPayment(@PathVariable UUID paymentId) {
        return ReceiptResponse.from(receiptService.getReceiptForPayment(paymentId));
    }

    @GetMapping({"/api/receipts/ride/{rideId}", "/api/v1/receipts/ride/{rideId}"})
    @Operation(summary = "Get the receipt of a ride", description = "404 when the ride is unpaid or its payment failed")
    @ApiResponse(responseCode = "200", description = "Receipt")
    @ApiResponse(responseCode = "404", description = "No successful payment for this ride",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ReceiptResponse getReceiptForRide(@PathVariable UUID rideId) {
        return ReceiptResponse.from(receiptService.getReceiptForRide(rideId));
    }
}

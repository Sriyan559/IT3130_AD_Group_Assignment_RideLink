package com.ridelink.payment.controller;

import com.ridelink.payment.dto.request.ProcessPaymentRequest;
import com.ridelink.payment.dto.response.PaymentResponse;
import com.ridelink.payment.exception.ApiErrorResponse;
import com.ridelink.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Simulated payment endpoints. {@code /api/payments} and {@code /api/v1/payments/process} are equivalent. */
@RestController
@Tag(name = "Payments", description = "Simulated payments for completed rides")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping({"/api/payments", "/api/v1/payments/process"})
    @Operation(summary = "Pay for a completed ride (simulated)",
            description = "Charges the ride's final fare. simulateFailure=true records a FAILED attempt with no "
                    + "receipt. Re-sending the same idempotencyKey returns the original attempt.")
    @ApiResponse(responseCode = "200", description = "Payment attempt (status SUCCESS or FAILED)")
    @ApiResponse(responseCode = "400", description = "Invalid request fields",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Ride already paid, not completed, or key reused",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public PaymentResponse pay(@Valid @RequestBody ProcessPaymentRequest request) {
        return PaymentResponse.from(paymentService.pay(request));
    }

    @GetMapping({"/api/payments/{paymentId}", "/api/v1/payments/{paymentId}"})
    @Operation(summary = "Get a payment attempt and its status")
    @ApiResponse(responseCode = "200", description = "Payment attempt")
    @ApiResponse(responseCode = "404", description = "Payment not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public PaymentResponse getPayment(@PathVariable UUID paymentId) {
        return PaymentResponse.from(paymentService.getPayment(paymentId));
    }

    @GetMapping({"/api/payments/ride/{rideId}", "/api/v1/payments/ride/{rideId}"})
    @Operation(summary = "List all payment attempts for a ride (oldest first)")
    public List<PaymentResponse> getPaymentsForRide(@PathVariable UUID rideId) {
        return paymentService.getPaymentsForRide(rideId).stream().map(PaymentResponse::from).toList();
    }
}

package com.ridelink.payment.controller;

import com.ridelink.payment.dto.request.ProcessPaymentRequest;
import com.ridelink.payment.dto.response.PaymentResponse;
import com.ridelink.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service endpoints used by Ride Management Service when a ride is completed.
 * Protected by the shared X-Service-Token (see PaymentAccessFilter) and hidden from Swagger.
 */
@Hidden
@RestController
public class InternalPaymentController {

    private final PaymentService paymentService;

    public InternalPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/internal/payments/process")
    public PaymentResponse payForCompletedRide(@Valid @RequestBody ProcessPaymentRequest request) {
        return PaymentResponse.from(paymentService.payForCompletedRide(request));
    }

    @GetMapping("/internal/payments/ride/{rideId}/successful")
    public PaymentResponse getSuccessfulPayment(@PathVariable UUID rideId) {
        return PaymentResponse.from(paymentService.getSuccessfulPaymentForRide(rideId));
    }
}

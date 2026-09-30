package com.ridelink.payment.service;

import com.ridelink.payment.dto.request.ProcessPaymentRequest;
import com.ridelink.payment.entity.FinalFare;
import com.ridelink.payment.entity.PaymentAttempt;
import com.ridelink.payment.exception.ApiException;
import com.ridelink.payment.integration.RideServiceClient;
import com.ridelink.payment.integration.RideSnapshot;
import com.ridelink.payment.repository.FinalFareRepository;
import com.ridelink.payment.repository.PaymentRepository;
import com.ridelink.payment.security.PassengerAccessPolicy;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Payment use cases. Paying always makes sure the final fare exists first, then hands the
 * database work to {@link PaymentProcessor}.
 */
@Service
public class PaymentService {

    private final RideServiceClient rides;
    private final FareService fares;
    private final PaymentProcessor processor;
    private final PaymentRepository payments;
    private final FinalFareRepository finalFares;
    private final PassengerAccessPolicy access;

    public PaymentService(RideServiceClient rides, FareService fares, PaymentProcessor processor,
                          PaymentRepository payments, FinalFareRepository finalFares, PassengerAccessPolicy access) {
        this.rides = rides;
        this.fares = fares;
        this.processor = processor;
        this.payments = payments;
        this.finalFares = finalFares;
        this.access = access;
    }

    /** Public API: a passenger pays for their own completed ride. */
    public PaymentAttempt pay(ProcessPaymentRequest request) {
        RideSnapshot ride = rides.getRide(request.rideId());
        access.requireOwnerOrAdmin(ride.passengerId());
        fares.finalizeFor(ride);
        return processor.process(request);
    }

    /** Internal API: Ride Service charges the fare automatically when a ride is completed. */
    public PaymentAttempt payForCompletedRide(ProcessPaymentRequest request) {
        fares.finalizeFor(rides.getRide(request.rideId()));
        return processor.process(request);
    }

    public PaymentAttempt getPayment(UUID paymentId) {
        PaymentAttempt payment = payments.findById(paymentId)
                .orElseThrow(() -> ApiException.notFound("PAYMENT_NOT_FOUND", "Payment " + paymentId + " not found"));
        access.requireOwnerOrAdmin(payment.getPassengerId());
        return payment;
    }

    /** All attempts for a ride, oldest first (including FAILED ones). */
    public List<PaymentAttempt> getPaymentsForRide(UUID rideId) {
        access.requireOwnerOrAdmin(rides.getRide(rideId).passengerId());
        return payments.findByRideIdOrderByCreatedAtAsc(rideId);
    }

    /** Internal API: lets Ride Service find the successful payment after a retry or restart. */
    public PaymentAttempt getSuccessfulPaymentForRide(UUID rideId) {
        FinalFare fare = finalFares.findById(rideId)
                .orElseThrow(() -> ApiException.notFound("FINAL_FARE_NOT_FOUND", "No final fare for ride " + rideId));
        if (!fare.isPaid()) {
            throw ApiException.notFound("PAYMENT_NOT_FOUND", "Ride " + rideId + " has no successful payment");
        }
        return payments.findById(fare.getPaidPaymentId())
                .orElseThrow(() -> ApiException.notFound("PAYMENT_NOT_FOUND", "Payment record missing"));
    }
}

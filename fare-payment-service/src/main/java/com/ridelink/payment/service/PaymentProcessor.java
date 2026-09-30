package com.ridelink.payment.service;

import com.ridelink.payment.dto.request.ProcessPaymentRequest;
import com.ridelink.payment.entity.FinalFare;
import com.ridelink.payment.entity.PaymentAttempt;
import com.ridelink.payment.entity.Receipt;
import com.ridelink.payment.exception.ApiException;
import com.ridelink.payment.repository.FinalFareRepository;
import com.ridelink.payment.repository.PaymentRepository;
import com.ridelink.payment.repository.ReceiptRepository;
import java.time.Clock;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records one simulated payment as a single database transaction.
 * <p>
 * Kept separate from {@link PaymentService} so the transaction covers only local database work,
 * not the HTTP call to Ride Service. Steps:
 * <ol>
 *   <li>Lock the ride's final fare row (SELECT ... FOR UPDATE). Concurrent payments for the same
 *       ride wait here, so two requests can never both succeed.</li>
 *   <li>If the client sent an amount, it must equal the final fare (400 otherwise).</li>
 *   <li>If the idempotency key was used before, return that earlier attempt (safe retry).</li>
 *   <li>Reject a second payment for an already-paid ride (409).</li>
 *   <li>Save the attempt; on SUCCESS also issue the receipt and mark the fare as paid.</li>
 * </ol>
 */
@Service
public class PaymentProcessor {

    private final FinalFareRepository finalFares;
    private final PaymentRepository payments;
    private final ReceiptRepository receipts;
    private final Clock clock;

    public PaymentProcessor(FinalFareRepository finalFares, PaymentRepository payments,
                            ReceiptRepository receipts, Clock clock) {
        this.finalFares = finalFares;
        this.payments = payments;
        this.receipts = receipts;
        this.clock = clock;
    }

    @Transactional
    public PaymentAttempt process(ProcessPaymentRequest request) {
        FinalFare fare = finalFares.lockByRideId(request.rideId())
                .orElseThrow(() -> ApiException.notFound("FINAL_FARE_NOT_FOUND",
                        "Ride " + request.rideId() + " has no final fare to pay"));
        rejectAmountMismatch(request, fare);

        Optional<PaymentAttempt> previous = payments.findByIdempotencyKey(request.idempotencyKey());
        if (previous.isPresent()) {
            return replay(previous.get(), request);
        }
        if (fare.isPaid()) {
            throw ApiException.conflict("RIDE_ALREADY_PAID", "Ride " + fare.getRideId() + " has already been paid");
        }

        PaymentAttempt attempt = PaymentAttempt.simulate(fare, request.idempotencyKey(), request.paymentMethod(),
                request.simulateFailure(), clock.instant());
        payments.saveAndFlush(attempt);

        if (attempt.isSuccessful()) {
            receipts.saveAndFlush(Receipt.issue(attempt, fare));
            fare.markPaidBy(attempt.getId());
        }
        return attempt;
    }

    /** The fare is authoritative; a client-supplied amount is only accepted as a confirmation of it. */
    private void rejectAmountMismatch(ProcessPaymentRequest request, FinalFare fare) {
        if (request.amount() != null && request.amount().compareTo(fare.getTotal()) != 0) {
            throw ApiException.badRequest("AMOUNT_MISMATCH", "Amount " + request.amount().toPlainString()
                    + " does not match the final fare of " + fare.getTotal().toPlainString() + " " + fare.getCurrency());
        }
    }

    private PaymentAttempt replay(PaymentAttempt previous, ProcessPaymentRequest request) {
        if (!previous.isSameRequestAs(request.rideId(), request.paymentMethod(), request.simulateFailure())) {
            throw ApiException.conflict("IDEMPOTENCY_KEY_REUSED",
                    "Idempotency key '" + request.idempotencyKey() + "' was already used for a different request");
        }
        return previous;
    }
}

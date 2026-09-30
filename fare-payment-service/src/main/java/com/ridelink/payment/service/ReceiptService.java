package com.ridelink.payment.service;

import com.ridelink.payment.entity.PaymentAttempt;
import com.ridelink.payment.entity.Receipt;
import com.ridelink.payment.exception.ApiException;
import com.ridelink.payment.repository.PaymentRepository;
import com.ridelink.payment.repository.ReceiptRepository;
import com.ridelink.payment.security.PassengerAccessPolicy;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Read-only access to receipts. Receipts are created only by {@link PaymentProcessor}. */
@Service
public class ReceiptService {

    private final ReceiptRepository receipts;
    private final PaymentRepository payments;
    private final PassengerAccessPolicy access;

    public ReceiptService(ReceiptRepository receipts, PaymentRepository payments, PassengerAccessPolicy access) {
        this.receipts = receipts;
        this.payments = payments;
        this.access = access;
    }

    public Receipt getReceipt(UUID receiptId) {
        Receipt receipt = receipts.findById(receiptId)
                .orElseThrow(() -> ApiException.notFound("RECEIPT_NOT_FOUND", "Receipt " + receiptId + " not found"));
        access.requireOwnerOrAdmin(receipt.getPassengerId());
        return receipt;
    }

    public Receipt getReceiptForRide(UUID rideId) {
        Receipt receipt = receipts.findByRideId(rideId)
                .orElseThrow(() -> ApiException.notFound("RECEIPT_NOT_FOUND",
                        "Ride " + rideId + " has no receipt (it is unpaid or its payment failed)"));
        access.requireOwnerOrAdmin(receipt.getPassengerId());
        return receipt;
    }

    /** A FAILED payment never has a receipt, so the response must not suggest the ride was paid. */
    public Receipt getReceiptForPayment(UUID paymentId) {
        PaymentAttempt payment = payments.findById(paymentId)
                .orElseThrow(() -> ApiException.notFound("PAYMENT_NOT_FOUND", "Payment " + paymentId + " not found"));
        access.requireOwnerOrAdmin(payment.getPassengerId());
        if (!payment.isSuccessful()) {
            throw ApiException.notFound("RECEIPT_NOT_FOUND", "Payment " + paymentId + " has status "
                    + payment.getStatus() + "; receipts are only issued for successful payments");
        }
        return receipts.findById(payment.getReceiptId())
                .orElseThrow(() -> ApiException.notFound("RECEIPT_NOT_FOUND", "Receipt record missing"));
    }
}

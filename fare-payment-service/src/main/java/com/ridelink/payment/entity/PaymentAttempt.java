package com.ridelink.payment.entity;

import com.ridelink.payment.domain.PaymentMethod;
import com.ridelink.payment.domain.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * One simulated payment attempt against a ride's final fare (table {@code payment_attempts}).
 * <p>
 * Failed attempts are kept for history. The unique {@code idempotencyKey} lets a client resend
 * the same request safely (for example after a timeout) without being charged twice.
 */
@Entity
@Table(name = "payment_attempts")
public class PaymentAttempt {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(nullable = false)
    private UUID rideId;

    @Column(nullable = false, length = 100)
    private String passengerId;

    /** Always copied from the final fare, never taken from the client. */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false)
    private boolean simulateFailure;

    @Column(nullable = false, length = 80)
    private String transactionReference;

    @Column(nullable = false)
    private Instant createdAt;

    /** Set only for SUCCESS. */
    private Instant paidAt;

    /** Set only for SUCCESS. */
    private UUID receiptId;

    /** Required by JPA. */
    protected PaymentAttempt() {
    }

    /**
     * Simulates charging the fare. {@code simulateFailure = true} produces a FAILED attempt,
     * which is how the failed-payment scenario is demonstrated without a real gateway.
     */
    public static PaymentAttempt simulate(FinalFare fare, String idempotencyKey, PaymentMethod method,
                                          boolean simulateFailure, Instant now) {
        PaymentAttempt attempt = new PaymentAttempt();
        attempt.id = UUID.randomUUID();
        attempt.idempotencyKey = idempotencyKey;
        attempt.rideId = fare.getRideId();
        attempt.passengerId = fare.getPassengerId();
        attempt.amount = fare.getTotal();
        attempt.currency = fare.getCurrency();
        attempt.paymentMethod = method;
        attempt.simulateFailure = simulateFailure;
        attempt.transactionReference = "SIM-" + attempt.id;
        attempt.createdAt = now;
        if (simulateFailure) {
            attempt.status = PaymentStatus.FAILED;
        } else {
            attempt.status = PaymentStatus.SUCCESS;
            attempt.paidAt = now;
            attempt.receiptId = UUID.randomUUID();
        }
        return attempt;
    }

    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCESS;
    }

    /** True when a retried request with the same idempotency key asks for exactly the same thing. */
    public boolean isSameRequestAs(UUID otherRideId, PaymentMethod otherMethod, boolean otherSimulateFailure) {
        return rideId.equals(otherRideId) && paymentMethod == otherMethod && simulateFailure == otherSimulateFailure;
    }

    public UUID getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public UUID getRideId() {
        return rideId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public boolean isSimulateFailure() {
        return simulateFailure;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public UUID getReceiptId() {
        return receiptId;
    }
}

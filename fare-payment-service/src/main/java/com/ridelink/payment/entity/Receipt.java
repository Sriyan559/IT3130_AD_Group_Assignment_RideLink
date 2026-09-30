package com.ridelink.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Immutable;

/**
 * Itemised proof of a successful payment (table {@code receipts}).
 * <p>
 * It is a snapshot: fare components are copied at payment time and {@link Immutable} stops
 * Hibernate from ever updating the row. Unique ride and payment IDs allow one receipt per ride.
 */
@Entity
@Immutable
@Table(name = "receipts")
public class Receipt {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID paymentId;

    @Column(nullable = false, unique = true)
    private UUID rideId;

    @Column(nullable = false, length = 100)
    private String passengerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal baseFare;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal distanceFare;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal timeFare;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal minimumAdjustment;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 80)
    private String transactionReference;

    @Column(nullable = false)
    private Instant issuedAt;

    /** Required by JPA. */
    protected Receipt() {
    }

    public static Receipt issue(PaymentAttempt payment, FinalFare fare) {
        if (!payment.isSuccessful()) {
            throw new IllegalArgumentException("Receipts are only issued for successful payments");
        }
        Receipt receipt = new Receipt();
        receipt.id = payment.getReceiptId();
        receipt.paymentId = payment.getId();
        receipt.rideId = fare.getRideId();
        receipt.passengerId = fare.getPassengerId();
        receipt.baseFare = fare.getBaseFare();
        receipt.distanceFare = fare.getDistanceFare();
        receipt.timeFare = fare.getTimeFare();
        receipt.minimumAdjustment = fare.getMinimumAdjustment();
        receipt.total = fare.getTotal();
        receipt.currency = fare.getCurrency();
        receipt.transactionReference = payment.getTransactionReference();
        receipt.issuedAt = payment.getPaidAt();
        return receipt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public UUID getRideId() {
        return rideId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public BigDecimal getDistanceFare() {
        return distanceFare;
    }

    public BigDecimal getTimeFare() {
        return timeFare;
    }

    public BigDecimal getMinimumAdjustment() {
        return minimumAdjustment;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }
}

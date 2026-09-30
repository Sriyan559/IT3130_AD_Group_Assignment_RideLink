package com.ridelink.payment.entity;

import com.ridelink.payment.fare.FareBreakdown;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The authoritative fare for one completed ride (table {@code final_fares}).
 * <p>
 * The ride ID is the primary key, so each ride has at most one final fare. Amounts are
 * written once and never recalculated, so a later tariff change cannot alter what the
 * passenger owes. {@code paidPaymentId} links to the single successful payment.
 */
@Entity
@Table(name = "final_fares")
public class FinalFare {

    /** ID owned by Ride Management Service; stored here as a reference only. */
    @Id
    private UUID rideId;

    /** Account Service ID of the passenger who owes the fare. */
    @Column(nullable = false, length = 100)
    private String passengerId;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal distanceKilometers;

    @Column(nullable = false)
    private long durationMinutes;

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

    @Column(nullable = false)
    private Instant createdAt;

    private UUID paidPaymentId;

    /** Required by JPA. */
    protected FinalFare() {
    }

    public static FinalFare create(UUID rideId, String passengerId, FareBreakdown breakdown, Instant createdAt) {
        FinalFare fare = new FinalFare();
        fare.rideId = rideId;
        fare.passengerId = passengerId;
        fare.distanceKilometers = breakdown.distanceKilometers();
        fare.durationMinutes = breakdown.durationMinutes();
        fare.baseFare = breakdown.baseFare();
        fare.distanceFare = breakdown.distanceFare();
        fare.timeFare = breakdown.timeFare();
        fare.minimumAdjustment = breakdown.minimumAdjustment();
        fare.total = breakdown.total();
        fare.currency = breakdown.currency();
        fare.createdAt = createdAt;
        return fare;
    }

    public boolean isPaid() {
        return paidPaymentId != null;
    }

    public void markPaidBy(UUID paymentId) {
        if (isPaid()) {
            throw new IllegalStateException("Ride " + rideId + " is already paid");
        }
        this.paidPaymentId = paymentId;
    }

    public UUID getRideId() {
        return rideId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public BigDecimal getDistanceKilometers() {
        return distanceKilometers;
    }

    public long getDurationMinutes() {
        return durationMinutes;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UUID getPaidPaymentId() {
        return paidPaymentId;
    }
}

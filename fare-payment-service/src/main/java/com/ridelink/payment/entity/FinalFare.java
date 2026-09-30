package com.ridelink.payment.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="final_fares")
public class FinalFare {
    @Id public UUID rideId;
    @Column(nullable=false,length=100) public String passengerId;
    @Column(nullable=false,precision=19,scale=6) public BigDecimal distanceKilometers;
    @Column(nullable=false) public long durationMinutes;
    @Column(nullable=false,precision=19,scale=2) public BigDecimal baseFare;
    @Column(nullable=false,precision=19,scale=2) public BigDecimal distanceFare;
    @Column(nullable=false,precision=19,scale=2) public BigDecimal timeFare;
    @Column(nullable=false,precision=19,scale=2) public BigDecimal minimumAdjustment;
    @Column(nullable=false,precision=19,scale=2) public BigDecimal total;
    @Column(nullable=false,length=3) public String currency="LKR";
    @Column(nullable=false) public Instant createdAt;
    public UUID paidPaymentId;
}

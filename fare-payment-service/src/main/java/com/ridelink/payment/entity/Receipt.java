package com.ridelink.payment.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Entity @Immutable @Table(name="receipts")
public class Receipt {
    @Id public UUID id;
    @Column(nullable=false,unique=true) public UUID paymentId;
    @Column(nullable=false,unique=true) public UUID rideId;
    @Column(nullable=false,length=100) public String passengerId;
    @Column(nullable=false,precision=19,scale=2) public BigDecimal baseFare;
    @Column(nullable=false,precision=19,scale=2) public BigDecimal distanceFare;
    @Column(nullable=false,precision=19,scale=2) public BigDecimal timeFare;
    @Column(nullable=false,precision=19,scale=2) public BigDecimal minimumAdjustment;
    @Column(nullable=false,precision=19,scale=2) public BigDecimal total;
    @Column(nullable=false,length=3) public String currency="LKR";
    @Column(nullable=false,length=80) public String transactionReference;
    @Column(nullable=false) public Instant issuedAt;
}

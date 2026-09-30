package com.ridelink.payment.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="payment_attempts")
public class PaymentAttempt {
    @Id public UUID id;
    @Column(nullable=false,unique=true,length=100) public String idempotencyKey;
    @Column(nullable=false) public UUID rideId;
    @Column(nullable=false,length=100) public String passengerId;
    @Column(nullable=false,precision=19,scale=2) public BigDecimal amount;
    @Column(nullable=false,length=3) public String currency="LKR";
    @Column(nullable=false,length=20) public String paymentMethod;
    @Column(nullable=false,length=20) public String status;
    @Column(nullable=false) public boolean simulateFailure;
    @Column(nullable=false,length=80) public String transactionReference;
    @Column(nullable=false) public Instant createdAt;
    public Instant paidAt;
    public UUID receiptId;
}

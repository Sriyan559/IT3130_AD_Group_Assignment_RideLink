package com.ridelink.payment.repository;
import com.ridelink.payment.entity.PaymentAttempt;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PaymentRepository extends JpaRepository<PaymentAttempt,UUID> {
    Optional<PaymentAttempt> findByIdempotencyKey(String key);
    List<PaymentAttempt> findByRideIdOrderByCreatedAtAsc(UUID rideId);
}

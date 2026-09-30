package com.ridelink.payment.repository;
import com.ridelink.payment.entity.Receipt;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ReceiptRepository extends JpaRepository<Receipt,UUID> {
    Optional<Receipt> findByRideId(UUID rideId);
}

package com.ridelink.payment.repository;
import com.ridelink.payment.entity.FinalFare;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
public interface FinalFareRepository extends JpaRepository<FinalFare,UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FinalFare f where f.rideId = :rideId")
    Optional<FinalFare> lockByRideId(UUID rideId);
}

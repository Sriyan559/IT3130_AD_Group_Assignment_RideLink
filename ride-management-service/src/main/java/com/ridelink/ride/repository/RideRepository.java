package com.ridelink.ride.repository;

import com.ridelink.ride.domain.RideStatus;
import com.ridelink.ride.entity.Ride;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideRepository extends JpaRepository<Ride, UUID> {
    List<Ride> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);
    List<Ride> findByDriverIdAndStatus(Long driverId, RideStatus status);
}
package com.ridelink.ride.repository;

import com.ridelink.ride.domain.RideStatus;
import com.ridelink.ride.entity.Ride;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for Ride documents in 'rides' collection in ride_db.
 */
@Repository
public interface RideRepository extends MongoRepository<Ride, UUID> {
    List<Ride> findAllByOrderByCreatedAtDesc();
    List<Ride> findByStatusOrderByCreatedAtDesc(RideStatus status);
    List<Ride> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);
    List<Ride> findByDriverIdOrderByCreatedAtDesc(Long driverId);
    List<Ride> findByDriverIdAndStatus(Long driverId, RideStatus status);
}
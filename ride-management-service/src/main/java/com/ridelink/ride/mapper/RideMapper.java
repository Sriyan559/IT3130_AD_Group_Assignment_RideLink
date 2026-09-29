package com.ridelink.ride.mapper;

import com.ridelink.ride.dto.response.RideResponse;
import com.ridelink.ride.entity.Ride;
import org.springframework.stereotype.Component;

@Component
public class RideMapper {
    public RideResponse toResponse(Ride ride) {
        return new RideResponse(ride.getId(), ride.getPassengerId(), ride.getDriverId(),
                ride.getPickupLatitude(), ride.getPickupLongitude(), ride.getPickupAddress(),
                ride.getDestinationLatitude(), ride.getDestinationLongitude(), ride.getDestinationAddress(),
                ride.getStatus(), ride.getDistanceKm(), ride.getDurationMinutes(), ride.getFareId(),
                ride.getCreatedAt(), ride.getUpdatedAt());
    }
}
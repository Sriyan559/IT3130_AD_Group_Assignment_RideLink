package com.ridelink.ride.service;

import com.ridelink.ride.domain.RideStatus;
import com.ridelink.ride.dto.request.CreateRideRequest;
import com.ridelink.ride.dto.request.UpdateRideStatusRequest;
import com.ridelink.ride.dto.response.RideResponse;
import com.ridelink.ride.entity.Ride;
import com.ridelink.ride.exception.RideNotFoundException;
import com.ridelink.ride.lifecycle.RideLifecycle;
import com.ridelink.ride.mapper.RideMapper;
import com.ridelink.ride.repository.RideRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RideService {
    private final RideRepository repository;
    private final RideLifecycle lifecycle;
    private final RideMapper mapper;

    public RideService(RideRepository repository, RideLifecycle lifecycle, RideMapper mapper) {
        this.repository = repository;
        this.lifecycle = lifecycle;
        this.mapper = mapper;
    }

    @Transactional
    public RideResponse create(CreateRideRequest request) {
        Ride ride = new Ride();
        ride.setPassengerId(request.passengerId());
        ride.setDriverId(request.driverId());
        ride.setPickupLatitude(request.pickupLatitude());
        ride.setPickupLongitude(request.pickupLongitude());
        ride.setPickupAddress(request.pickupAddress());
        ride.setDestinationLatitude(request.destinationLatitude());
        ride.setDestinationLongitude(request.destinationLongitude());
        ride.setDestinationAddress(request.destinationAddress());
        ride.setStatus(request.driverId() == null ? RideStatus.REQUESTED : RideStatus.ASSIGNED);
        return mapper.toResponse(repository.save(ride));
    }

    @Transactional(readOnly = true)
    public RideResponse get(UUID rideId) { return mapper.toResponse(find(rideId)); }

    @Transactional(readOnly = true)
    public List<RideResponse> history(Long passengerId) {
        return repository.findByPassengerIdOrderByCreatedAtDesc(passengerId).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public RideResponse updateStatus(UUID rideId, UpdateRideStatusRequest request) {
        Ride ride = find(rideId);
        lifecycle.validate(ride.getStatus(), request.status());
        ride.setStatus(request.status());
        if (request.distanceKm() != null) ride.setDistanceKm(request.distanceKm());
        if (request.durationMinutes() != null) ride.setDurationMinutes(request.durationMinutes());
        return mapper.toResponse(repository.save(ride));
    }

    private Ride find(UUID rideId) {
        return repository.findById(rideId).orElseThrow(() -> new RideNotFoundException(rideId));
    }
}
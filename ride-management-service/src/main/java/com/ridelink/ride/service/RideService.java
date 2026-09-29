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

/**
 * Core business service for managing ride requests and coordinating the lifecycle state machine.
 */
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
        ride.prepareForSave();
        return mapper.toResponse(repository.save(ride));
    }

    public RideResponse get(UUID rideId) {
        return mapper.toResponse(find(rideId));
    }

    public List<RideResponse> getAll(RideStatus status) {
        List<Ride> rides = (status != null)
                ? repository.findByStatusOrderByCreatedAtDesc(status)
                : repository.findAllByOrderByCreatedAtDesc();
        return rides.stream().map(mapper::toResponse).toList();
    }

    public List<RideResponse> history(Long passengerId) {
        return repository.findByPassengerIdOrderByCreatedAtDesc(passengerId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<RideResponse> driverRides(Long driverId) {
        return repository.findByDriverIdOrderByCreatedAtDesc(driverId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public RideResponse assignDriver(UUID rideId, Long driverId) {
        Ride ride = find(rideId);
        if (ride.getStatus() == RideStatus.REQUESTED) {
            lifecycle.validate(ride.getStatus(), RideStatus.ASSIGNED);
            ride.setStatus(RideStatus.ASSIGNED);
        }
        ride.setDriverId(driverId);
        ride.prepareForSave();
        return mapper.toResponse(repository.save(ride));
    }

    public RideResponse updateStatus(UUID rideId, UpdateRideStatusRequest request) {
        Ride ride = find(rideId);
        lifecycle.validate(ride.getStatus(), request.status());
        ride.setStatus(request.status());
        if (request.distanceKm() != null) {
            ride.setDistanceKm(request.distanceKm());
        }
        if (request.durationMinutes() != null) {
            ride.setDurationMinutes(request.durationMinutes());
        }
        ride.prepareForSave();
        return mapper.toResponse(repository.save(ride));
    }

    public void cancel(UUID rideId) {
        Ride ride = find(rideId);
        lifecycle.validate(ride.getStatus(), RideStatus.CANCELLED);
        ride.setStatus(RideStatus.CANCELLED);
        ride.prepareForSave();
        repository.save(ride);
    }

    private Ride find(UUID rideId) {
        return repository.findById(rideId).orElseThrow(() -> new RideNotFoundException(rideId));
    }
}
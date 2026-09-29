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
    private com.ridelink.ride.integration.DriverGateway drivers;
    @org.springframework.beans.factory.annotation.Autowired
    public void setDriverGateway(com.ridelink.ride.integration.DriverGateway drivers) { this.drivers = drivers; }

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
        ride.setDriverId(null);
        ride.setPickupLatitude(request.pickupLatitude());
        ride.setPickupLongitude(request.pickupLongitude());
        ride.setPickupAddress(request.pickupAddress());
        ride.setDestinationLatitude(request.destinationLatitude());
        ride.setDestinationLongitude(request.destinationLongitude());
        ride.setDestinationAddress(request.destinationAddress());
        ride.setStatus(RideStatus.REQUESTED);
        ride.prepareForSave();
        ride = repository.save(ride);
        if (request.driverId() != null) {
            try { return assignDriver(ride.getId(), request.driverId()); }
            catch (com.ridelink.support.AccessFailure ex) {
                throw new com.ridelink.support.AccessFailure(ex.status(), ex.getMessage() + "; rideId=" + ride.getId());
            }
        }
        return mapper.toResponse(ride);
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

    public List<RideResponse> history(String passengerId) {
        return repository.findByPassengerIdOrderByCreatedAtDesc(passengerId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<RideResponse> driverRides(String driverId) {
        return repository.findByDriverIdOrderByCreatedAtDesc(driverId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public RideResponse assignDriver(UUID rideId, String driverId) {
        Ride ride = find(rideId);
        if (ride.getStatus() == RideStatus.ASSIGNED && driverId.equals(ride.getDriverId())) return mapper.toResponse(ride);
        if (ride.getStatus() != RideStatus.REQUESTED) throw new com.ridelink.support.AccessFailure(409, "Only REQUESTED rides can be assigned");
        if (ride.getPendingDriverId() != null && !driverId.equals(ride.getPendingDriverId()))
            throw new com.ridelink.support.AccessFailure(409, "Another assignment is pending");
        if (ride.getPendingDriverId() == null) {
            ride.setPendingDriverId(driverId);
            ride.prepareForSave();
            ride = repository.save(ride);
        }
        try {
            drivers.reserve(driverId, rideId);
        } catch (com.ridelink.support.AccessFailure ex) {
            // A timeout is ambiguous: retain the durable intent and retry the SAME reservation.
            if (ex.status() == 404 || ex.status() == 409) {
                ride.setPendingDriverId(null);
                repository.save(ride);
            }
            throw ex;
        }
        ride.setDriverId(driverId);
        ride.setPendingDriverId(null);
        ride.setStatus(RideStatus.ASSIGNED);
        ride.prepareForSave();
        return mapper.toResponse(repository.save(ride));
    }

    public RideResponse updateStatus(UUID rideId, UpdateRideStatusRequest request) {
        Ride ride = find(rideId);
        if (request.status() == RideStatus.ASSIGNED)
            throw new com.ridelink.support.AccessFailure(409, "Use assign-driver to reserve a driver");
        if (ride.getPendingDriverId() != null)
            throw new com.ridelink.support.AccessFailure(409, "Assignment pending; retry after reconciliation");
        if (ride.getStatus() == request.status() && terminal(ride)) {
            return mapper.toResponse(release(ride));
        }
        lifecycle.validate(ride.getStatus(), request.status());
        ride.setStatus(request.status());
        if (request.distanceKm() != null) {
            ride.setDistanceKm(request.distanceKm());
        }
        if (request.durationMinutes() != null) {
            ride.setDurationMinutes(request.durationMinutes());
        }
        ride.prepareForSave();
        ride.setReleasePending(terminal(ride) && ride.getDriverId() != null);
        return mapper.toResponse(release(repository.save(ride)));
    }

    public void cancel(UUID rideId) {
        Ride ride = find(rideId);
        if (ride.getPendingDriverId() != null)
            throw new com.ridelink.support.AccessFailure(409, "Assignment pending; retry after reconciliation");
        if (ride.getStatus() == RideStatus.CANCELLED) { release(ride); return; }
        lifecycle.validate(ride.getStatus(), RideStatus.CANCELLED);
        ride.setStatus(RideStatus.CANCELLED);
        ride.prepareForSave();
        ride.setReleasePending(ride.getDriverId() != null);
        release(repository.save(ride));
    }

    private boolean terminal(Ride ride) {
        return ride.getStatus() == RideStatus.COMPLETED || ride.getStatus() == RideStatus.CANCELLED;
    }
    private Ride release(Ride ride) {
        if (!ride.isReleasePending()) return ride;
        drivers.release(ride.getDriverId(), ride.getId());
        ride.setReleasePending(false);
        ride.prepareForSave();
        return repository.save(ride);
    }
    public void reconcile() {
        for (Ride ride : repository.findTop100ByPendingDriverIdNotNull()) {
            try { assignDriver(ride.getId(), ride.getPendingDriverId()); }
            catch (com.ridelink.support.AccessFailure | org.springframework.dao.DataAccessException ex) {
                // Persisted intent remains available for the next pass after transient failures.
            }
        }
        for (Ride ride : repository.findTop100ByReleasePendingTrue()) {
            try { release(ride); }
            catch (com.ridelink.support.AccessFailure | org.springframework.dao.DataAccessException ex) {
                // Release is scoped to rideId, so retry cannot release a later reservation.
            }
        }
    }

    private Ride find(UUID rideId) {
        return repository.findById(rideId).orElseThrow(() -> new RideNotFoundException(rideId));
    }
}

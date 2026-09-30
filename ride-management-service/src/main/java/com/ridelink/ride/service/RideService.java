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
    private com.ridelink.ride.integration.PaymentGateway payments;
    @org.springframework.beans.factory.annotation.Autowired
    public void setPaymentGateway(com.ridelink.ride.integration.PaymentGateway payments) {this.payments=payments;}
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
            return mapper.toResponse(settle(release(ride)));
        }
        lifecycle.validate(ride.getStatus(), request.status());
        if(request.status()==RideStatus.COMPLETED && (request.distanceKm()==null || request.distanceKm().signum()<=0
                || request.distanceKm().compareTo(new java.math.BigDecimal("10000"))>0
                || request.durationMinutes()==null || request.durationMinutes()<0 || request.durationMinutes()>100000))
            throw new com.ridelink.support.AccessFailure(400,"Completion requires distance 0-10000 km (exclusive zero) and duration 0-100000 minutes");
        ride.setStatus(request.status());
        if(request.status()==RideStatus.COMPLETED) {
            ride.setPaymentPending(true);ride.setSimulatePaymentFailure(request.simulatePaymentFailure());
        }
        if (request.distanceKm() != null) {
            ride.setDistanceKm(request.distanceKm());
        }
        if (request.durationMinutes() != null) {
            ride.setDurationMinutes(request.durationMinutes());
        }
        ride.prepareForSave();
        ride.setReleasePending(terminal(ride) && ride.getDriverId() != null);
        return mapper.toResponse(settle(release(repository.save(ride))));
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
        for(Ride ride:repository.findTop100ByPaymentPendingTrue()) {
            try {settle(ride);}
            catch(com.ridelink.support.AccessFailure | org.springframework.dao.DataAccessException ex) {
                // Same completion idempotency key prevents duplicate payment on recovery.
            }
        }
    }

    public RideResponse matchDriver(UUID rideId,java.math.BigDecimal radius) {
        if(radius==null || radius.signum()<=0 || radius.compareTo(new java.math.BigDecimal("50"))>0)
            throw new com.ridelink.support.AccessFailure(400,"Radius must be greater than zero and at most 50 km");
        Ride ride=find(rideId);
        if(ride.getStatus()==RideStatus.ASSIGNED) return mapper.toResponse(ride);
        if(ride.getStatus()!=RideStatus.REQUESTED) throw new com.ridelink.support.AccessFailure(409,"Only REQUESTED rides can be matched");
        if(ride.getPendingDriverId()!=null) return assignDriver(rideId,ride.getPendingDriverId());
        for(var candidate:drivers.eligible(ride.getPickupLatitude(),ride.getPickupLongitude(),radius)) {
            try {return assignDriver(rideId,candidate.driverId());}
            catch(com.ridelink.support.AccessFailure ex) {if(ex.status()!=409) throw ex;}
        }
        throw new com.ridelink.support.AccessFailure(409,"NO_AVAILABLE_DRIVER");
    }

    private Ride settle(Ride ride) {
        if(!ride.isPaymentPending()) return ride;
        var result=payments.process(ride.getId(),ride.isSimulatePaymentFailure());
        ride.setPaymentId(result.id());ride.setReceiptId(result.receiptId());ride.setPaymentStatus(result.status());
        ride.setPaymentPending(false);ride.prepareForSave();return repository.save(ride);
    }

    private Ride find(UUID rideId) {
        return repository.findById(rideId).orElseThrow(() -> new RideNotFoundException(rideId));
    }
}

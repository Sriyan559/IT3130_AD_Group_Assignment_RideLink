package com.ridelink.payment.service;

import com.ridelink.payment.entity.FinalFare;
import com.ridelink.payment.exception.ApiException;
import com.ridelink.payment.fare.FareBreakdown;
import com.ridelink.payment.fare.FareCalculator;
import com.ridelink.payment.integration.RideServiceClient;
import com.ridelink.payment.integration.RideSnapshot;
import com.ridelink.payment.repository.FinalFareRepository;
import com.ridelink.payment.security.PassengerAccessPolicy;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/** Fare estimation (before a ride) and final fare calculation (after a ride is COMPLETED). */
@Service
public class FareService {

    private final FareCalculator calculator;
    private final FinalFareRepository finalFares;
    private final RideServiceClient rides;
    private final PassengerAccessPolicy access;
    private final Clock clock;

    public FareService(FareCalculator calculator, FinalFareRepository finalFares, RideServiceClient rides,
                       PassengerAccessPolicy access, Clock clock) {
        this.calculator = calculator;
        this.finalFares = finalFares;
        this.rides = rides;
        this.access = access;
        this.clock = clock;
    }

    /** Estimate only; nothing is stored. A missing duration is treated as 0 minutes. */
    public FareBreakdown estimate(BigDecimal distanceKilometers, Long durationMinutes) {
        return calculator.calculate(distanceKilometers, durationMinutes == null ? 0L : durationMinutes);
    }

    public String describeRule() {
        return calculator.getPolicy().describe();
    }

    /** Passenger-facing: fetch the ride from Ride Service, check ownership, then calculate or return the fare. */
    public FinalFare finalizeFare(UUID rideId) {
        RideSnapshot ride = rides.getRide(rideId);
        access.requireOwnerOrAdmin(ride.passengerId());
        return finalizeFor(ride);
    }

    public FinalFare getFinalFare(UUID rideId) {
        FinalFare fare = finalFares.findById(rideId)
                .orElseThrow(() -> ApiException.notFound("FINAL_FARE_NOT_FOUND",
                        "No final fare has been calculated for ride " + rideId));
        access.requireOwnerOrAdmin(fare.getPassengerId());
        return fare;
    }

    /**
     * Returns the ride's final fare, creating it on first call.
     * <p>
     * Rules: the ride must be COMPLETED and have distance and duration recorded. Once stored the
     * fare never changes, so calling this again (or retrying) always gives the same amount.
     */
    public FinalFare finalizeFor(RideSnapshot ride) {
        if (!ride.isCompleted()) {
            throw ApiException.conflict("RIDE_NOT_COMPLETED",
                    "Final fare requires a COMPLETED ride; ride " + ride.id() + " is " + ride.status());
        }
        Optional<FinalFare> existing = finalFares.findById(ride.id());
        if (existing.isPresent()) {
            return existing.get();
        }
        if (!ride.hasTripMetrics()) {
            throw ApiException.conflict("RIDE_METRICS_MISSING",
                    "Completed ride " + ride.id() + " has no recorded distance or duration");
        }
        FareBreakdown breakdown = calculator.calculate(ride.distanceKm(), ride.durationMinutes());
        try {
            return finalFares.saveAndFlush(FinalFare.create(ride.id(), ride.passengerId(), breakdown, clock.instant()));
        } catch (DataIntegrityViolationException raceLost) {
            // Two requests for the same ride arrived together and the other one inserted first.
            // The primary key guarantees one row, so return the row that won.
            return finalFares.findById(ride.id())
                    .orElseThrow(() -> ApiException.conflict("CONCURRENT_FARE_CREATION",
                            "Final fare is being created by another request; retry"));
        }
    }
}

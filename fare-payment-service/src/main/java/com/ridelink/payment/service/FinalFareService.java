package com.ridelink.payment.service;

import com.ridelink.payment.entity.FinalFare;
import com.ridelink.payment.fare.FareCalculator;
import com.ridelink.payment.integration.RideGateway.Trip;
import com.ridelink.payment.repository.FinalFareRepository;
import com.ridelink.support.AccessFailure;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static java.util.Objects.requireNonNull;
@Service
public class FinalFareService {
    private final FinalFareRepository fares; private final FareCalculator calculator;
    public FinalFareService(FinalFareRepository fares,FareCalculator calculator) {this.fares=fares;this.calculator=calculator;}
    @Transactional
    public FinalFare ensure(Trip trip) {
        if(!"COMPLETED".equals(trip.status())) throw new AccessFailure(409,"Final fare requires a completed ride");
        var old=fares.findById(requireNonNull(trip.id()));
        if(old.isPresent()) return old.get();
        var b=calculator.calculate(trip.distanceKm(),trip.durationMinutes());
        FinalFare fare=new FinalFare(); fare.rideId=trip.id();fare.passengerId=trip.passengerId();
        fare.distanceKilometers=b.distanceKilometers();fare.durationMinutes=b.durationMinutes();
        fare.baseFare=b.baseFare();fare.distanceFare=b.distanceFare();fare.timeFare=b.timeFare();
        fare.minimumAdjustment=b.minimumAdjustment();fare.total=b.total();fare.createdAt=Instant.now();
        return fares.saveAndFlush(fare);
    }
}

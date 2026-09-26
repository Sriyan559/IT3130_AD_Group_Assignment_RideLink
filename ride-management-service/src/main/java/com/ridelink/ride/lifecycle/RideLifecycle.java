package com.ridelink.ride.lifecycle;

import com.ridelink.ride.domain.RideStatus;
import com.ridelink.ride.exception.InvalidRideTransitionException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class RideLifecycle {
    private final Map<RideStatus, Set<RideStatus>> transitions = new EnumMap<>(RideStatus.class);

    public RideLifecycle() {
        transitions.put(RideStatus.REQUESTED, EnumSet.of(RideStatus.ASSIGNED, RideStatus.CANCELLED));
        transitions.put(RideStatus.ASSIGNED, EnumSet.of(RideStatus.ACCEPTED, RideStatus.CANCELLED));
        transitions.put(RideStatus.ACCEPTED, EnumSet.of(RideStatus.IN_PROGRESS, RideStatus.CANCELLED));
        transitions.put(RideStatus.IN_PROGRESS, EnumSet.of(RideStatus.COMPLETED));
        transitions.put(RideStatus.COMPLETED, EnumSet.noneOf(RideStatus.class));
        transitions.put(RideStatus.CANCELLED, EnumSet.noneOf(RideStatus.class));
    }

    public void validate(RideStatus current, RideStatus next) {
        if (current == next || !transitions.getOrDefault(current, Set.of()).contains(next)) {
            throw new InvalidRideTransitionException(current, next);
        }
    }
}
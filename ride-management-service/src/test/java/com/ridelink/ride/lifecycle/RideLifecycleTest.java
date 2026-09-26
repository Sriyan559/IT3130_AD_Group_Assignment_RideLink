package com.ridelink.ride.lifecycle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ridelink.ride.domain.RideStatus;
import com.ridelink.ride.exception.InvalidRideTransitionException;
import org.junit.jupiter.api.Test;

class RideLifecycleTest {
    private final RideLifecycle lifecycle = new RideLifecycle();

    @Test
    void acceptsValidTransition() {
        assertDoesNotThrow(() -> lifecycle.validate(RideStatus.REQUESTED, RideStatus.ASSIGNED));
    }

    @Test
    void rejectsSkippingStates() {
        assertThrows(InvalidRideTransitionException.class,
                () -> lifecycle.validate(RideStatus.REQUESTED, RideStatus.COMPLETED));
    }

    @Test
    void rejectsCancellationAfterTripStarts() {
        assertThrows(InvalidRideTransitionException.class,
                () -> lifecycle.validate(RideStatus.IN_PROGRESS, RideStatus.CANCELLED));
    }
}
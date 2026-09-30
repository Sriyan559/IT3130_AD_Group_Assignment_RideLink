package com.ridelink.ride.lifecycle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ridelink.ride.domain.RideStatus;
import com.ridelink.ride.exception.InvalidRideTransitionException;
import org.junit.jupiter.api.Test;

class RideLifecycleTest {
    private final RideLifecycle lifecycle = new RideLifecycle();

    @Test
    void acceptsValidLifecyclePath() {
        assertDoesNotThrow(() -> lifecycle.validate(RideStatus.REQUESTED, RideStatus.ASSIGNED));
        assertDoesNotThrow(() -> lifecycle.validate(RideStatus.ASSIGNED, RideStatus.ACCEPTED));
        assertDoesNotThrow(() -> lifecycle.validate(RideStatus.ACCEPTED, RideStatus.IN_PROGRESS));
        assertDoesNotThrow(() -> lifecycle.validate(RideStatus.IN_PROGRESS, RideStatus.COMPLETED));
    }

    @Test
    void acceptsCancellationBeforeTripStarts() {
        assertDoesNotThrow(() -> lifecycle.validate(RideStatus.REQUESTED, RideStatus.CANCELLED));
        assertDoesNotThrow(() -> lifecycle.validate(RideStatus.ASSIGNED, RideStatus.CANCELLED));
        assertDoesNotThrow(() -> lifecycle.validate(RideStatus.ACCEPTED, RideStatus.CANCELLED));
    }

    @Test
    void rejectsSkippingStates() {
        assertThrows(InvalidRideTransitionException.class,
                () -> lifecycle.validate(RideStatus.REQUESTED, RideStatus.COMPLETED));
        assertThrows(InvalidRideTransitionException.class,
                () -> lifecycle.validate(RideStatus.REQUESTED, RideStatus.IN_PROGRESS));
    }

    @Test
    void rejectsCancellationAfterTripStarts() {
        assertThrows(InvalidRideTransitionException.class,
                () -> lifecycle.validate(RideStatus.IN_PROGRESS, RideStatus.CANCELLED));
    }

    @Test
    void rejectsTransitionsFromCompletedOrCancelled() {
        assertThrows(InvalidRideTransitionException.class,
                () -> lifecycle.validate(RideStatus.COMPLETED, RideStatus.REQUESTED));
        assertThrows(InvalidRideTransitionException.class,
                () -> lifecycle.validate(RideStatus.CANCELLED, RideStatus.REQUESTED));
    }

    @Test
    void rejectsTransitionToSameState() {
        assertThrows(InvalidRideTransitionException.class,
                () -> lifecycle.validate(RideStatus.REQUESTED, RideStatus.REQUESTED));
    }
}
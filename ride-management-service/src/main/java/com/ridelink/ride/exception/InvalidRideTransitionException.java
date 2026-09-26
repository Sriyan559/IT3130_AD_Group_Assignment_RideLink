package com.ridelink.ride.exception;

import com.ridelink.ride.domain.RideStatus;

public class InvalidRideTransitionException extends RuntimeException {
    public InvalidRideTransitionException(RideStatus current, RideStatus next) {
        super("Invalid ride status transition from " + current + " to " + next);
    }
}
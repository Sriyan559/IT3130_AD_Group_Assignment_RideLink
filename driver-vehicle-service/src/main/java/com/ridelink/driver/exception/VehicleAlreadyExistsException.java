package com.ridelink.driver.exception;

public class VehicleAlreadyExistsException extends RuntimeException {
    public VehicleAlreadyExistsException(Throwable cause) {
        super("A vehicle with this plate number already exists", cause);
    }
}

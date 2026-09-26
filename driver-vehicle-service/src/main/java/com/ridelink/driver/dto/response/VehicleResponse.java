package com.ridelink.driver.dto.response;

import com.ridelink.driver.document.Vehicle;

public record VehicleResponse(String id, String driverId, String plateNumber,
                              String make, String model, String vehicleClass, int capacity) {
    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(vehicle.id(), vehicle.driverId(), vehicle.plateNumber(),
                vehicle.make(), vehicle.model(), vehicle.vehicleClass(), vehicle.capacity());
    }
}

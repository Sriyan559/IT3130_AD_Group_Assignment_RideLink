package com.ridelink.driver.dto.response;

import com.ridelink.driver.document.DriverLocation;
import java.util.List;

public record EligibleDriverResponse(String driverId, DriverLocation location,
                                     double distanceKm, List<VehicleResponse> vehicles) {
}

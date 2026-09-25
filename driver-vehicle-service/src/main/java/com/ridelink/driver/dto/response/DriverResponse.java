package com.ridelink.driver.dto.response;

import com.ridelink.driver.document.Driver;
import com.ridelink.driver.domain.AvailabilityStatus;

public record DriverResponse(String id, String accountId, String licenseNumber,
                             String serviceArea, AvailabilityStatus availabilityStatus) {
    public static DriverResponse from(Driver driver) {
        return new DriverResponse(driver.id(), driver.accountId(), driver.licenseNumber(),
                driver.serviceArea(), driver.availabilityStatus());
    }
}

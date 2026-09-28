package com.ridelink.driver.dto.response;

import com.ridelink.driver.document.Driver;
import com.ridelink.driver.document.DriverLocation;
import com.ridelink.driver.domain.AvailabilityStatus;

public record DriverResponse(String id, String accountId, String licenseNumber,
                             String serviceArea, AvailabilityStatus availabilityStatus,
                             DriverLocation location) {
    public DriverResponse(String id, String accountId, String licenseNumber, String serviceArea,
                          AvailabilityStatus availabilityStatus) {
        this(id, accountId, licenseNumber, serviceArea, availabilityStatus, null);
    }

    public static DriverResponse from(Driver driver) {
        return new DriverResponse(driver.id(), driver.accountId(), driver.licenseNumber(),
                driver.serviceArea(), driver.availabilityStatus(), driver.location());
    }
}

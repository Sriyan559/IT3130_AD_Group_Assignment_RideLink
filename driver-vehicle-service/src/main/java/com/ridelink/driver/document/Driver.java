package com.ridelink.driver.document;

import com.ridelink.driver.domain.AvailabilityStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "drivers")
public record Driver(
        @Id String id,
        String accountId,
        String licenseNumber,
        String serviceArea,
        AvailabilityStatus availabilityStatus,
        DriverLocation location,
        String activeRideId,
        java.util.List<String> releasedRideIds) {
    @org.springframework.data.annotation.PersistenceCreator
    public Driver {
    }

    public Driver(String id, String accountId, String licenseNumber, String serviceArea,
                  AvailabilityStatus availabilityStatus, DriverLocation location) {
        this(id, accountId, licenseNumber, serviceArea, availabilityStatus, location, null, java.util.List.of());
    }

    public Driver(String id, String accountId, String licenseNumber, String serviceArea,
                  AvailabilityStatus availabilityStatus) {
        this(id, accountId, licenseNumber, serviceArea, availabilityStatus, null);
    }
}

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
        AvailabilityStatus availabilityStatus) {
}

package com.ridelink.driver.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "vehicles")
public record Vehicle(@Id String id, String driverId, String plateNumber,
                      String make, String model, String vehicleClass, int capacity) {
}

package com.ridelink.driver.repository;

import com.ridelink.driver.document.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VehicleRepository extends MongoRepository<Vehicle, String> {
}

package com.ridelink.driver.repository;

import com.ridelink.driver.document.Driver;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DriverRepository extends MongoRepository<Driver, String> {
}

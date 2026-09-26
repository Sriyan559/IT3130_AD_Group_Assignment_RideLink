package com.ridelink.driver.config;

import com.ridelink.driver.document.Vehicle;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

/** Enforces plate uniqueness even when registration requests run concurrently. */
@Component
public class VehicleIndexes implements ApplicationRunner {
    private final MongoTemplate mongoTemplate;

    public VehicleIndexes(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        mongoTemplate.indexOps(Vehicle.class).ensureIndex(new Index()
                .on("plateNumber", Sort.Direction.ASC).unique().named("vehicle_plate_unique"));
    }
}

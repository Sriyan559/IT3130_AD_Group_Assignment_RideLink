package com.ridelink.driver.service;

import com.ridelink.driver.document.Driver;
import com.ridelink.driver.domain.AvailabilityStatus;
import com.ridelink.driver.dto.request.UpdateAvailabilityRequest;
import com.ridelink.driver.dto.response.DriverResponse;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.exception.DriverOnTripException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class DriverAvailabilityService {
    private final MongoTemplate mongoTemplate;

    public DriverAvailabilityService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public DriverResponse update(String driverId, UpdateAvailabilityRequest request) {
        AvailabilityStatus target = AvailabilityStatus.valueOf(request.availabilityStatus());
        if (target == AvailabilityStatus.ON_TRIP) {
            throw new IllegalArgumentException("Availability must be AVAILABLE or OFFLINE");
        }
        // Check the current state in the write itself so an ON_TRIP driver cannot be overwritten.
        Query query = Query.query(Criteria.where("_id").is(driverId)
                .and("availabilityStatus").in(AvailabilityStatus.AVAILABLE, AvailabilityStatus.OFFLINE));
        Driver updated = mongoTemplate.findAndModify(query,
                new Update().set("availabilityStatus", target),
                FindAndModifyOptions.options().returnNew(true), Driver.class);
        if (updated != null) {
            return DriverResponse.from(updated);
        }
        if (!mongoTemplate.exists(Query.query(Criteria.where("_id").is(driverId)), Driver.class)) {
            throw new DriverNotFoundException();
        }
        throw new DriverOnTripException();
    }
}

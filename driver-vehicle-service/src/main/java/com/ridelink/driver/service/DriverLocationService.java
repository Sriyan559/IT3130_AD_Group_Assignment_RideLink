package com.ridelink.driver.service;

import com.ridelink.driver.document.Driver;
import com.ridelink.driver.document.DriverLocation;
import com.ridelink.driver.dto.request.UpdateLocationRequest;
import com.ridelink.driver.dto.response.DriverResponse;
import com.ridelink.driver.exception.DriverNotFoundException;
import java.time.Instant;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class DriverLocationService {
    private final MongoTemplate mongo;

    public DriverLocationService(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public DriverResponse update(String driverId, UpdateLocationRequest request) {
        var location = new DriverLocation(request.latitude().doubleValue(),
                request.longitude().doubleValue(), Instant.now());
        // Replace the coordinate pair together without overwriting concurrent status changes.
        Driver updated = mongo.findAndModify(Query.query(Criteria.where("_id").is(driverId)),
                new Update().set("location", location),
                FindAndModifyOptions.options().returnNew(true), Driver.class);
        if (updated == null) {
            throw new DriverNotFoundException();
        }
        return DriverResponse.from(updated);
    }
}

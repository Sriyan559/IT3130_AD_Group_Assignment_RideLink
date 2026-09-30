package com.ridelink.driver.service;

import com.ridelink.driver.document.*;
import com.ridelink.driver.domain.AvailabilityStatus;
import com.ridelink.support.AccessFailure;
import java.time.Clock;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;

@Service
public class DriverReservationService {
    private final MongoTemplate mongo;
    private final Clock clock;
    private final long maxAge;
    public DriverReservationService(MongoTemplate mongo, Clock clock,
            @Value("${driver.search.max-location-age-seconds:300}") long maxAge) {
        this.mongo = mongo; this.clock = clock; this.maxAge = maxAge;
    }
    public Driver reserve(String driverId, UUID rideId) {
        String ride = rideId.toString();
        Driver existing = get(driverId);
        if (ride.equals(existing.activeRideId())) return existing;
        if (!mongo.exists(Query.query(Criteria.where("driverId").is(driverId)), Vehicle.class))
            throw new AccessFailure(409, "Driver has no registered vehicle");
        var time = Criteria.where("location.updatedAt").ne(null).lte(clock.instant());
        if (maxAge > 0) time.gte(clock.instant().minusSeconds(maxAge));
        Query eligible = Query.query(new Criteria().andOperator(
                Criteria.where("_id").is(driverId),
                Criteria.where("availabilityStatus").is(AvailabilityStatus.AVAILABLE),
                Criteria.where("activeRideId").is(null),
                Criteria.where("releasedRideIds").ne(ride),
                Criteria.where("location.latitude").gte(-90).lte(90),
                Criteria.where("location.longitude").gte(-180).lte(180), time));
        Driver updated;
        try {
            updated = mongo.findAndModify(eligible,
                    new Update().set("availabilityStatus", AvailabilityStatus.ON_TRIP).set("activeRideId", ride),
                    FindAndModifyOptions.options().returnNew(true), Driver.class);
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            throw new AccessFailure(409, "Ride already holds another driver");
        }
        if (updated != null) return updated;
        existing = get(driverId);
        if (ride.equals(existing.activeRideId())) return existing;
        throw new AccessFailure(409, "Driver unavailable, location stale, or reservation already released");
    }
    public Driver release(String driverId, UUID rideId) {
        String ride = rideId.toString();
        Driver updated = mongo.findAndModify(Query.query(Criteria.where("_id").is(driverId)
                        .and("activeRideId").is(ride).and("availabilityStatus").is(AvailabilityStatus.ON_TRIP)),
                new Update().set("availabilityStatus", AvailabilityStatus.AVAILABLE).unset("activeRideId")
                        .addToSet("releasedRideIds", ride), FindAndModifyOptions.options().returnNew(true), Driver.class);
        if (updated != null) return updated;
        Driver current = get(driverId);
        if (current.releasedRideIds() != null && current.releasedRideIds().contains(ride)) return current;
        throw new AccessFailure(409, "Reservation does not belong to this ride");
    }
    public Driver get(String driverId) {
        Driver driver = mongo.findById(driverId, Driver.class);
        if (driver == null) throw new AccessFailure(404, "Driver not found");
        return driver;
    }
}

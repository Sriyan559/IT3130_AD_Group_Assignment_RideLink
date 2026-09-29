package com.ridelink.driver.service;

import com.ridelink.driver.document.Driver;
import com.ridelink.driver.document.DriverLocation;
import com.ridelink.driver.document.Vehicle;
import com.ridelink.driver.domain.AvailabilityStatus;
import com.ridelink.driver.dto.request.EligibleDriversRequest;
import com.ridelink.driver.dto.response.EligibleDriverResponse;
import com.ridelink.driver.dto.response.VehicleResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class EligibleDriversService {
    private final MongoTemplate mongo;
    private final Clock clock;
    private final long maxAgeSeconds;

    public EligibleDriversService(MongoTemplate mongo, Clock clock,
            @Value("${driver.search.max-location-age-seconds:300}") long maxAgeSeconds) {
        if (maxAgeSeconds < 0) {
            throw new IllegalArgumentException("Location max age cannot be negative");
        }
        this.mongo = mongo;
        this.clock = clock;
        this.maxAgeSeconds = maxAgeSeconds;
    }

    public List<EligibleDriverResponse> find(EligibleDriversRequest request) {
        Instant now = clock.instant();
        Criteria timestamp = Criteria.where("location.updatedAt").ne(null).lte(now);
        if (maxAgeSeconds > 0) {
            timestamp.gte(now.minusSeconds(maxAgeSeconds));
        }
        Query query = Query.query(new Criteria().andOperator(
                Criteria.where("availabilityStatus").is(AvailabilityStatus.AVAILABLE),
                Criteria.where("location.latitude").gte(-90).lte(90),
                Criteria.where("location.longitude").gte(-180).lte(180), timestamp));
        // Small-service implementation using the existing location schema; no data migration.
        List<EligibleDriverResponse> nearby = mongo.find(query, Driver.class).stream()
                .filter(driver -> driver.availabilityStatus() == AvailabilityStatus.AVAILABLE
                        && validLocation(driver.location(), now))
                .map(driver -> new EligibleDriverResponse(driver.id(), driver.location(),
                        GeoDistance.kilometres(request.lat().doubleValue(), request.lng().doubleValue(),
                                driver.location().latitude(), driver.location().longitude()), List.of()))
                .filter(driver -> driver.distanceKm() <= request.radius().doubleValue())
                .toList();
        if (nearby.isEmpty()) {
            return List.of();
        }
        // One bulk vehicle query, even when a driver owns multiple vehicles.
        var vehicles = mongo.find(Query.query(Criteria.where("driverId")
                        .in(nearby.stream().map(EligibleDriverResponse::driverId).toList())), Vehicle.class)
                .stream().sorted(Comparator.comparing(Vehicle::id))
                .map(VehicleResponse::from).collect(Collectors.groupingBy(VehicleResponse::driverId));
        return nearby.stream().filter(driver -> vehicles.containsKey(driver.driverId()))
                .map(driver -> new EligibleDriverResponse(driver.driverId(), driver.location(),
                        driver.distanceKm(), List.copyOf(vehicles.get(driver.driverId()))))
                .sorted(Comparator.comparingDouble(EligibleDriverResponse::distanceKm)
                        .thenComparing(EligibleDriverResponse::driverId))
                .toList();
    }

    private boolean validLocation(DriverLocation location, Instant now) {
        return location != null && Double.isFinite(location.latitude()) && Double.isFinite(location.longitude())
                && Math.abs(location.latitude()) <= 90 && Math.abs(location.longitude()) <= 180
                && location.updatedAt() != null && !location.updatedAt().isAfter(now)
                && (maxAgeSeconds == 0 || !location.updatedAt().isBefore(now.minusSeconds(maxAgeSeconds)));
    }
}

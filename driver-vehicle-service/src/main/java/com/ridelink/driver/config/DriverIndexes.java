package com.ridelink.driver.config;

import com.ridelink.driver.document.Driver;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

/** Database constraints also protect against simultaneous duplicate requests. */
@Component
public class DriverIndexes implements ApplicationRunner {
    private final MongoTemplate mongoTemplate;

    public DriverIndexes(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        var indexes = mongoTemplate.indexOps(Driver.class);
        indexes.ensureIndex(new Index().on("activeRideId", Sort.Direction.ASC).unique()
                .partial(org.springframework.data.mongodb.core.index.PartialIndexFilter.of(
                        org.springframework.data.mongodb.core.query.Criteria.where("activeRideId").type(2)))
                .named("driver_active_ride_unique"));
        indexes.ensureIndex(new Index().on("availabilityStatus", Sort.Direction.ASC)
                .on("location.updatedAt", Sort.Direction.DESC).named("driver_search_status_time"));
        indexes.ensureIndex(new Index().on("accountId", Sort.Direction.ASC)
                .unique().named("driver_account_unique"));
        indexes.ensureIndex(new Index().on("licenseNumber", Sort.Direction.ASC)
                .unique().named("driver_license_unique"));
    }
}

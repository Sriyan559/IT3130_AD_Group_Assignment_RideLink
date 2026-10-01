package com.ridelink.ride.contract;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.ridelink.ride.domain.RideStatus;
import com.ridelink.ride.entity.Ride;
import com.ridelink.ride.repository.RideRepository;
import java.util.UUID;
import org.bson.UuidRepresentation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static java.util.Objects.requireNonNull;

@EnabledIfEnvironmentVariable(named = "RIDELINK_MONGO_TESTS", matches = "true")
class ExternalIdMongoTest {
    @Test
    void stringIdsPersistAndHistoryQueriesFindThem() {
        String database = "ride_external_ids_test_" + UUID.randomUUID().toString().replace("-", "");
        var settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
                .uuidRepresentation(UuidRepresentation.STANDARD).build();
        try (var client = MongoClients.create(settings)) {
            var mongo = new MongoTemplate(requireNonNull(client), database);
            try {
                var repository = new MongoRepositoryFactory(mongo).getRepository(RideRepository.class);
                Ride ride = new Ride();
                ride.setPassengerId("6abbfa6d095b5451ceb2f209");
                ride.setDriverId("6abbfa6d095b5451ceb2f20a");
                ride.setStatus(RideStatus.ASSIGNED);
                ride.setDurationMinutes(22L);
                ride.prepareForSave();
                repository.save(ride);
                var stored = repository.findById(requireNonNull(ride.getId())).orElseThrow();
                assertThat(stored.getPassengerId()).isEqualTo(ride.getPassengerId());
                assertThat(stored.getDriverId()).isEqualTo(ride.getDriverId());
                assertThat(stored.getDurationMinutes()).isEqualTo(22L);
                assertThat(repository.findByPassengerIdOrderByCreatedAtDesc(ride.getPassengerId())).hasSize(1);
                assertThat(repository.findByDriverIdOrderByCreatedAtDesc(ride.getDriverId())).hasSize(1);
                assertThat(repository.findByDriverIdAndStatus(ride.getDriverId(), RideStatus.ASSIGNED)).hasSize(1);
                var raw = mongo.getCollection("rides").find().first();
                assertThat(raw.get("passengerId")).isInstanceOf(String.class);
                assertThat(raw.get("driverId")).isInstanceOf(String.class);
            } finally {
                assertThat(mongo.getDb().getName()).isEqualTo(database);
                mongo.getDb().drop();
            }
        }
    }
}

package com.ridelink.driver.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.ridelink.driver.document.Driver;
import com.ridelink.driver.domain.AvailabilityStatus;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static java.util.Objects.requireNonNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfEnvironmentVariable(named = "RIDELINK_MONGO_TESTS", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EligibleDriversMongoTest {
    private static final String DATABASE = "driver_eligible_test_" + UUID.randomUUID().toString().replace("-", "");
    @Autowired private TestRestTemplate http;
    @Autowired private MongoTemplate mongo;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.data.mongodb.host", () -> "localhost");
        properties.add("spring.data.mongodb.port", () -> 27017);
        properties.add("spring.data.mongodb.database", () -> DATABASE);
    }

    @AfterAll
    void removeIsolatedTestDatabase() {
        assertThat(mongo.getDb().getName()).isEqualTo(DATABASE);
        mongo.getDb().drop();
    }

    @Test
    void searchesStoredDataAndExcludesIneligibleDriversWithoutMutatingDocuments() {
        java.time.Instant now = java.time.Instant.now();
        Driver near = seed("near", 0, AvailabilityStatus.AVAILABLE, now, true);
        Driver far = seed("far", 0.02, AvailabilityStatus.AVAILABLE, now, true);
        seed("outside", 1, AvailabilityStatus.AVAILABLE, now, true);
        seed("offline", 0, AvailabilityStatus.OFFLINE, now, true);
        seed("trip", 0, AvailabilityStatus.ON_TRIP, now, true);
        seed("stale", 0, AvailabilityStatus.AVAILABLE, now.minusSeconds(301), true);
        seed("future", 0, AvailabilityStatus.AVAILABLE, now.plusSeconds(60), true);
        seed("no-vehicle", 0, AvailabilityStatus.AVAILABLE, now, false);
        mongo.insert(new Driver(null, "no-location", "no-location", "Colombo", AvailabilityStatus.AVAILABLE));
        seed("no-time", 0, AvailabilityStatus.AVAILABLE, null, true);
        mongo.insert(new com.ridelink.driver.document.Vehicle(null, near.id(), "second-vehicle", "Honda", "Fit", "CAR", 4));
        var beforeDrivers = mongo.findAll(Driver.class);
        var beforeVehicles = mongo.findAll(com.ridelink.driver.document.Vehicle.class);
        ResponseEntity<JsonNode> result = http.getForEntity("/api/drivers/eligible?lat=0&lng=0&radius=5", JsonNode.class);
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        var matches = requireNonNull(result.getBody());
        assertThat(matches).hasSize(2);
        JsonNode first = matches.get(0);
        JsonNode second = matches.get(1);
        assertThat(first.path("driverId").asText()).isEqualTo(near.id());
        assertThat(first.path("distanceKm").asDouble()).isZero();
        assertThat(first.path("vehicles")).hasSize(2);
        assertThat(second.path("driverId").asText()).isEqualTo(far.id());
        assertThat(second.path("distanceKm").asDouble()).isBetween(2.22,2.23);
        assertThat(mongo.findAll(Driver.class)).containsExactlyInAnyOrderElementsOf(beforeDrivers);
        assertThat(mongo.findAll(com.ridelink.driver.document.Vehicle.class)).containsExactlyInAnyOrderElementsOf(beforeVehicles);
        // Static /eligible route must not be interpreted as a driver ID.
        assertThat(http.getForEntity("/api/drivers/eligible?lat=50&lng=50&radius=1", JsonNode.class).getBody()).isEmpty();
        assertThat(http.getForEntity("/api/drivers/" + near.id(), JsonNode.class).getStatusCode().value()).isEqualTo(200);
        JsonNode spec = http.getForObject("/v3/api-docs", JsonNode.class);
        assertThat(spec.path("paths").has("/api/drivers/eligible")).isTrue();
        assertThat(mongo.indexOps(Driver.class).getIndexInfo()).extracting(i -> i.getName()).contains("driver_search_status_time");
        assertThat(mongo.indexOps(com.ridelink.driver.document.Vehicle.class).getIndexInfo()).extracting(i -> i.getName()).contains("vehicle_driver_lookup");
    }

    @Test
    void rejectsInvalidAndMissingParametersWithStructuredError() {
        for (String query : new String[]{"", "?lat=0&lng=0&radius=51", "?lat=NaN&lng=0&radius=5"}) {
            var response = http.getForEntity("/api/drivers/eligible" + query, JsonNode.class);
            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(requireNonNull(response.getBody()).path("error").asText()).isEqualTo("VALIDATION_ERROR");
        }
    }

    private Driver seed(String tag, double longitude, AvailabilityStatus status, java.time.Instant updatedAt, boolean vehicle) {
        Driver d = mongo.insert(new Driver(null, tag, tag, "Colombo", status,
                new com.ridelink.driver.document.DriverLocation(0,longitude,updatedAt)));
        if (vehicle) mongo.insert(new com.ridelink.driver.document.Vehicle(null, d.id(), tag, "Toyota", "Aqua", "CAR", 4));
        return d;
    }
}

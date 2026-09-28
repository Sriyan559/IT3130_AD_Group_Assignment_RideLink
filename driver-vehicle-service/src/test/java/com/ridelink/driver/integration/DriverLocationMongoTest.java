package com.ridelink.driver.integration;

import com.ridelink.driver.document.Driver;
import com.ridelink.driver.domain.AvailabilityStatus;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfEnvironmentVariable(named = "RIDELINK_MONGO_TESTS", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DriverLocationMongoTest {
    private static final String DATABASE = "driver_location_test_" + UUID.randomUUID().toString().replace("-", "");
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
    void locationsPersistForEveryStatusAndInvalidUpdatesPreserveData() {
        for (AvailabilityStatus state : AvailabilityStatus.values()) {
            // Insert an old-format document that has no location field.
            String id = new org.bson.types.ObjectId().toHexString();
            mongo.getCollection("drivers").insertOne(new org.bson.Document("_id", new org.bson.types.ObjectId(id))
                    .append("accountId", "location-" + state).append("licenseNumber", "LOC-" + state)
                    .append("serviceArea", "Colombo").append("availabilityStatus", state.name()));
            assertThat(mongo.findById(id, Driver.class).location()).isNull();
            for (double[] pair : new double[][]{{6.9271, 79.8612}, {0, 0}, {-90, -180}, {90, 180}, {90, 180}}) {
                java.time.Instant before = java.time.Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
                ResponseEntity<Map> result = update(id, pair[0], pair[1]);
                assertThat(result.getStatusCode().value()).isEqualTo(200);
                Driver stored = mongo.findById(id, Driver.class);
                assertThat(stored.location().latitude()).isEqualTo(pair[0]);
                assertThat(stored.location().longitude()).isEqualTo(pair[1]);
                assertThat(stored.location().updatedAt()).isBetween(before, java.time.Instant.now());
                assertThat(stored.availabilityStatus()).isEqualTo(state);
                assertThat(stored.accountId()).isEqualTo("location-" + state);
                assertThat(stored.licenseNumber()).isEqualTo("LOC-" + state);
                assertThat(stored.serviceArea()).isEqualTo("Colombo");
                Map profile = http.getForObject("/api/drivers/" + id, Map.class);
                assertThat(profile.get("location")).isEqualTo(result.getBody().get("location"));
            }
            Driver beforeInvalid = mongo.findById(id, Driver.class);
            assertThat(update(id, 91, 0).getStatusCode().value()).isEqualTo(400);
            assertThat(mongo.findById(id, Driver.class)).isEqualTo(beforeInvalid);
        }
        assertThat(http.getForEntity("/swagger-ui/index.html", String.class).getStatusCode().value()).isEqualTo(200);
        Map spec = http.getForObject("/v3/api-docs", Map.class);
        assertThat((Map) spec.get("paths")).containsKey("/api/drivers/{driverId}/location");
    }

    @Test
    void missingDriverIsNotUpserted() {
        String id = "000000000000000000000001";
        ResponseEntity<Map> result = update(id, 0, 0);
        assertThat(result.getStatusCode().value()).isEqualTo(404);
        assertThat(result.getBody().get("error")).isEqualTo("DRIVER_NOT_FOUND");
        assertThat(mongo.findById(id, Driver.class)).isNull();
    }

    @Test
    void concurrentAvailabilityAndLocationUpdatesBothSurvive() throws Exception {
        Driver driver = mongo.insert(new Driver(null, "concurrent-location", "CONCURRENT-LOC", "Colombo", AvailabilityStatus.OFFLINE));
        var executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            for (int i = 0; i < 10; i++) {
                double latitude = i;
                String status = i % 2 == 0 ? "AVAILABLE" : "OFFLINE";
                var start = new java.util.concurrent.CountDownLatch(1);
                var location = executor.submit(() -> {
                    start.await();
                    return update(driver.id(), latitude, 79);
                });
                var availability = executor.submit(() -> {
                    start.await();
                    return http.exchange("/api/drivers/" + driver.id() + "/availability", HttpMethod.PUT,
                            new HttpEntity<>(Map.of("availabilityStatus", status)), Map.class);
                });
                start.countDown();
                assertThat(location.get(15, java.util.concurrent.TimeUnit.SECONDS).getStatusCode().value()).isEqualTo(200);
                assertThat(availability.get(15, java.util.concurrent.TimeUnit.SECONDS).getStatusCode().value()).isEqualTo(200);
                Driver stored = mongo.findById(driver.id(), Driver.class);
                assertThat(stored.location().latitude()).isEqualTo(latitude);
                assertThat(stored.location().longitude()).isEqualTo(79);
                assertThat(stored.availabilityStatus().name()).isEqualTo(status);
                assertThat(stored.accountId()).isEqualTo(driver.accountId());
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private ResponseEntity<Map> update(String id, double latitude, double longitude) {
        return http.exchange("/api/drivers/" + id + "/location", HttpMethod.PUT,
                new HttpEntity<>(Map.of("latitude", latitude, "longitude", longitude)), Map.class);
    }
}

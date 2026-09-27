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
class DriverAvailabilityMongoTest {
    private static final String DATABASE = "driver_availability_test_" + UUID.randomUUID().toString().replace("-", "");
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
    void transitionsAndRepeatedUpdatesPersistWithoutChangingProfile() {
        ResponseEntity<Map> created = http.postForEntity("/api/drivers", Map.of(
                "accountId", "availability-test", "licenseNumber", "AVAIL-001", "serviceArea", "Malabe"), Map.class);
        assertThat(created.getStatusCode().value()).isEqualTo(201);
        String id = (String) created.getBody().get("id");
        assertThat(created.getBody().get("availabilityStatus")).isEqualTo("OFFLINE");
        for (String target : new String[]{"AVAILABLE", "AVAILABLE", "OFFLINE", "OFFLINE"}) {
            ResponseEntity<Map> result = update(id, target);
            assertThat(result.getStatusCode().value()).isEqualTo(200);
            assertThat(result.getBody().get("availabilityStatus")).isEqualTo(target);
            Map profile = http.getForObject("/api/drivers/" + id, Map.class);
            assertThat(profile).containsEntry("availabilityStatus", target)
                    .containsEntry("accountId", "availability-test")
                    .containsEntry("licenseNumber", "AVAIL-001").containsEntry("serviceArea", "Malabe");
            assertThat(mongo.findById(id, Driver.class).availabilityStatus().name()).isEqualTo(target);
        }
        ResponseEntity<Map> invalid = update(id, "ON_TRIP");
        assertThat(invalid.getStatusCode().value()).isEqualTo(400);
        assertThat(invalid.getBody().get("error")).isEqualTo("VALIDATION_ERROR");
        assertThat(mongo.findById(id, Driver.class).availabilityStatus()).isEqualTo(AvailabilityStatus.OFFLINE);
        assertThat(http.getForEntity("/swagger-ui/index.html", String.class).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void onTripDriverCannotBeOverwritten() {
        Driver driver = mongo.insert(new Driver(null, "trip-account", "TRIP-001", "Malabe", AvailabilityStatus.ON_TRIP));
        for (String target : new String[]{"AVAILABLE", "OFFLINE"}) {
            ResponseEntity<Map> result = update(driver.id(), target);
            assertThat(result.getStatusCode().value()).isEqualTo(409);
            assertThat(result.getBody().get("error")).isEqualTo("DRIVER_ON_TRIP");
            assertThat(mongo.findById(driver.id(), Driver.class)).isEqualTo(driver);
        }
    }

    @Test
    void missingDriverIsNotUpserted() {
        String id = "000000000000000000000001";
        ResponseEntity<Map> result = update(id, "AVAILABLE");
        assertThat(result.getStatusCode().value()).isEqualTo(404);
        assertThat(result.getBody().get("error")).isEqualTo("DRIVER_NOT_FOUND");
        assertThat(mongo.findById(id, Driver.class)).isNull();
    }

    private ResponseEntity<Map> update(String id, String target) {
        return http.exchange("/api/drivers/" + id + "/availability", HttpMethod.PUT,
                new HttpEntity<>(Map.of("availabilityStatus", target)), Map.class);
    }
}

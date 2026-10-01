package com.ridelink.driver.integration;

import com.fasterxml.jackson.databind.JsonNode;
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
import static java.util.Objects.requireNonNull;

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
        ResponseEntity<JsonNode> created = http.postForEntity("/api/drivers", Map.of(
                "accountId", "availability-test", "licenseNumber", "AVAIL-001", "serviceArea", "Malabe"), JsonNode.class);
        assertThat(created.getStatusCode().value()).isEqualTo(201);
        String id = requireNonNull(created.getBody()).path("id").asText();
        assertThat(requireNonNull(created.getBody()).path("availabilityStatus").asText()).isEqualTo("OFFLINE");
        for (String target : new String[]{"AVAILABLE", "AVAILABLE", "OFFLINE", "OFFLINE"}) {
            ResponseEntity<JsonNode> result = update(id, target);
            assertThat(result.getStatusCode().value()).isEqualTo(200);
            assertThat(requireNonNull(result.getBody()).path("availabilityStatus").asText()).isEqualTo(target);
            JsonNode profile = http.getForObject("/api/drivers/" + id, JsonNode.class);
            assertThat(profile.path("availabilityStatus").asText()).isEqualTo(target);
            assertThat(profile.path("accountId").asText()).isEqualTo("availability-test");
            assertThat(profile.path("licenseNumber").asText()).isEqualTo("AVAIL-001");
            assertThat(profile.path("serviceArea").asText()).isEqualTo("Malabe");
            assertThat(requireNonNull(mongo.findById(requireNonNull(id), Driver.class)).availabilityStatus().name()).isEqualTo(target);
        }
        ResponseEntity<JsonNode> invalid = update(id, "ON_TRIP");
        assertThat(invalid.getStatusCode().value()).isEqualTo(400);
        assertThat(requireNonNull(invalid.getBody()).path("error").asText()).isEqualTo("VALIDATION_ERROR");
        assertThat(requireNonNull(mongo.findById(requireNonNull(id), Driver.class)).availabilityStatus()).isEqualTo(AvailabilityStatus.OFFLINE);
        assertThat(http.getForEntity("/swagger-ui/index.html", String.class).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void onTripDriverCannotBeOverwritten() {
        Driver driver = mongo.insert(new Driver(null, "trip-account", "TRIP-001", "Malabe", AvailabilityStatus.ON_TRIP));
        for (String target : new String[]{"AVAILABLE", "OFFLINE"}) {
            ResponseEntity<JsonNode> result = update(driver.id(), target);
            assertThat(result.getStatusCode().value()).isEqualTo(409);
            assertThat(requireNonNull(result.getBody()).path("error").asText()).isEqualTo("DRIVER_ON_TRIP");
            assertThat(mongo.findById(requireNonNull(driver.id()), Driver.class)).isEqualTo(driver);
        }
    }

    @Test
    void missingDriverIsNotUpserted() {
        String id = "000000000000000000000001";
        ResponseEntity<JsonNode> result = update(id, "AVAILABLE");
        assertThat(result.getStatusCode().value()).isEqualTo(404);
        assertThat(requireNonNull(result.getBody()).path("error").asText()).isEqualTo("DRIVER_NOT_FOUND");
        assertThat(mongo.findById(id, Driver.class)).isNull();
    }

    private ResponseEntity<JsonNode> update(String id, String target) {
        return http.exchange("/api/drivers/" + id + "/availability", HttpMethod.PUT,
                new HttpEntity<>(requireNonNull(Map.of("availabilityStatus", target))), JsonNode.class);
    }
}

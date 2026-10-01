package com.ridelink.driver.integration;

import com.mongodb.client.MongoClients;
import com.ridelink.driver.config.DriverIndexes;
import com.ridelink.driver.document.*;
import com.ridelink.driver.domain.AvailabilityStatus;
import com.ridelink.driver.service.DriverReservationService;
import com.ridelink.support.AccessFailure;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.assertj.core.api.Assertions.*;
import static java.util.Objects.requireNonNull;

@EnabledIfEnvironmentVariable(named="RIDELINK_MONGO_TESTS", matches="true")
class DriverReservationMongoTest {
    private com.mongodb.client.MongoClient client;
    private MongoTemplate mongo;
    private DriverReservationService service;
    @BeforeEach void setup() {
        client = MongoClients.create("mongodb://localhost:27017");
        mongo = new MongoTemplate(requireNonNull(client),"driver_reservation_test_"+UUID.randomUUID().toString().replace("-",""));
        new DriverIndexes(mongo).run(null);
        service = new DriverReservationService(mongo, Clock.systemUTC(),300);
    }
    @AfterEach void cleanup() { mongo.getDb().drop(); client.close(); }
    private String seed(String name, Instant time, boolean vehicle) {
        var driver = mongo.insert(new Driver(null,name,name,"Colombo",AvailabilityStatus.AVAILABLE,
                new DriverLocation(6.9271,79.8612,time)));
        if (vehicle) mongo.insert(new Vehicle(null,driver.id(),name,"Toyota","Aqua","CAR",4));
        return driver.id();
    }
    @Test void simultaneousRidesCannotReserveSameDriver() throws Exception {
        String driver = seed("concurrent",Instant.now(),true);
        CountDownLatch start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i=0;i<2;i++) results.add(pool.submit(() -> {
                start.await();
                try { service.reserve(driver,UUID.randomUUID()); return true; }
                catch (AccessFailure ex) { assertThat(ex.status()).isEqualTo(409); return false; }
            }));
            start.countDown();
            assertThat(List.of(results.get(0).get(),results.get(1).get())).containsExactlyInAnyOrder(true,false);
        } finally { pool.shutdownNow(); }
    }
    @Test void retriesAndDelayedReleaseDoNotAffectNewRide() {
        String driver = seed("retry",Instant.now(),true);
        UUID first = UUID.randomUUID(), second=UUID.randomUUID();
        service.reserve(driver,first); service.reserve(driver,first);
        service.release(driver,first); service.release(driver,first);
        assertThatThrownBy(() -> service.reserve(driver,first)).isInstanceOf(AccessFailure.class);
        service.reserve(driver,second); service.release(driver,first);
        assertThat(service.get(driver).activeRideId()).isEqualTo(second.toString());
        assertThat(service.get(driver).availabilityStatus()).isEqualTo(AvailabilityStatus.ON_TRIP);
        assertThatThrownBy(() -> service.release(driver,UUID.randomUUID())).isInstanceOf(AccessFailure.class);
    }
    @Test void oneRideCannotHoldTwoDrivers() {
        String a=seed("a",Instant.now(),true), b=seed("b",Instant.now(),true);
        UUID ride=UUID.randomUUID(); service.reserve(a,ride);
        assertThatThrownBy(() -> service.reserve(b,ride)).isInstanceOf(AccessFailure.class);
        assertThat(service.get(b).availabilityStatus()).isEqualTo(AvailabilityStatus.AVAILABLE);
    }
    @Test void freshnessAndVehicleAreRequired() {
        String stale=seed("stale",Instant.now().minusSeconds(301),true);
        String noVehicle=seed("none",Instant.now(),false);
        assertThatThrownBy(() -> service.reserve(stale,UUID.randomUUID())).isInstanceOf(AccessFailure.class);
        assertThatThrownBy(() -> service.reserve(noVehicle,UUID.randomUUID())).isInstanceOf(AccessFailure.class);
    }
}

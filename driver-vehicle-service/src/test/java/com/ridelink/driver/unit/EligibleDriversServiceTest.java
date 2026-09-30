package com.ridelink.driver.unit;

import com.ridelink.driver.document.*;
import com.ridelink.driver.domain.AvailabilityStatus;
import com.ridelink.driver.dto.request.EligibleDriversRequest;
import com.ridelink.driver.service.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EligibleDriversServiceTest {
    private static final Instant NOW = Instant.parse("2026-09-29T12:00:00Z");
    private final MongoTemplate mongo = mock(MongoTemplate.class);
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final EligibleDriversService service = new EligibleDriversService(mongo, clock, 300);
    private final EligibleDriversRequest request = new EligibleDriversRequest(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(5));

    private Driver driver(String id, double longitude, Instant updated) {
        return new Driver(id, "account-" + id, "license-" + id, "Colombo", AvailabilityStatus.AVAILABLE,
                new DriverLocation(0, longitude, updated));
    }

    private Vehicle vehicle(String id, String driverId) {
        return new Vehicle(id, driverId, id, "Toyota", "Aqua", "CAR", 4);
    }

    @Test
    void sortsByDistanceThenIdAndGroupsVehiclesWithoutDuplicateDrivers() {
        when(mongo.find(any(Query.class), eq(Driver.class))).thenReturn(List.of(
                driver("far", 0.02, NOW), driver("b", 0, NOW), driver("a", 0, NOW),
                driver("outside", 1, NOW), driver("no-vehicle", 0, NOW)));
        when(mongo.find(any(Query.class), eq(Vehicle.class))).thenReturn(List.of(
                vehicle("v2", "a"), vehicle("v1", "a"), vehicle("v3", "b"), vehicle("v4", "far")));
        var results = service.find(request);
        assertThat(results).extracting(r -> r.driverId()).containsExactly("a", "b", "far");
        assertThat(results.get(0).vehicles()).extracting(v -> v.id()).containsExactly("v1", "v2");
        assertThat(results.get(2).distanceKm()).isCloseTo(2.224, within(0.001));
        verify(mongo, times(1)).find(any(Query.class), eq(Vehicle.class));
    }

    @Test
    void freshnessCutoffIsInclusiveAndFutureOrMalformedLocationsAreExcluded() {
        when(mongo.find(any(Query.class), eq(Driver.class))).thenReturn(List.of(
                driver("boundary", 0, NOW.minusSeconds(300)), driver("stale", 0, NOW.minusSeconds(301)),
                driver("future", 0, NOW.plusSeconds(1)), driver("no-time", 0, null),
                driver("nan", Double.NaN, NOW), driver("invalid", 181, NOW),
                new Driver("no-location", "a", "b", "Colombo", AvailabilityStatus.AVAILABLE),
                new Driver("offline", "a", "b", "Colombo", AvailabilityStatus.OFFLINE, new DriverLocation(0,0,NOW))));
        when(mongo.find(any(Query.class), eq(Vehicle.class))).thenReturn(List.of(vehicle("v", "boundary")));
        assertThat(service.find(request)).extracting(r -> r.driverId()).containsExactly("boundary");
    }

    @Test
    void radiusBoundaryIsInclusive() {
        when(mongo.find(any(Query.class), eq(Driver.class))).thenReturn(List.of(driver("edge", 0.01, NOW)));
        when(mongo.find(any(Query.class), eq(Vehicle.class))).thenReturn(List.of(vehicle("v", "edge")));
        double distance = GeoDistance.kilometres(0,0,0,0.01);
        var exact = new EligibleDriversRequest(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(distance));
        assertThat(service.find(exact)).hasSize(1);
        var smaller = new EligibleDriversRequest(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(distance - 0.000001));
        assertThat(service.find(smaller)).isEmpty();
    }

    @Test
    void demoConfigurationCanAllowOldLocations() {
        when(mongo.find(any(Query.class), eq(Driver.class))).thenReturn(List.of(driver("old", 0, NOW.minusSeconds(99999))));
        when(mongo.find(any(Query.class), eq(Vehicle.class))).thenReturn(List.of(vehicle("v", "old")));
        assertThat(new EligibleDriversService(mongo, clock, 0).find(request)).hasSize(1);
    }

    @Test
    void noCandidatesSkipsVehicleQuery() {
        when(mongo.find(any(Query.class), eq(Driver.class))).thenReturn(List.of());
        assertThat(service.find(request)).isEmpty();
        verify(mongo, never()).find(any(Query.class), eq(Vehicle.class));
    }

    @Test
    void driverDatabaseFailurePropagates() {
        when(mongo.find(any(Query.class), eq(Driver.class))).thenThrow(new DataAccessResourceFailureException("offline"));
        assertThatThrownBy(() -> service.find(request)).isInstanceOf(DataAccessResourceFailureException.class);
    }

    @Test
    void vehicleDatabaseFailureDoesNotReturnPartialSuccess() {
        when(mongo.find(any(Query.class), eq(Driver.class))).thenReturn(List.of(driver("d",0,NOW)));
        when(mongo.find(any(Query.class), eq(Vehicle.class))).thenThrow(new DataAccessResourceFailureException("offline"));
        assertThatThrownBy(() -> service.find(request)).isInstanceOf(DataAccessResourceFailureException.class);
    }

    @Test
    void negativeFreshnessConfigurationIsRejected() {
        assertThatThrownBy(() -> new EligibleDriversService(mongo,clock,-1)).isInstanceOf(IllegalArgumentException.class);
    }
}

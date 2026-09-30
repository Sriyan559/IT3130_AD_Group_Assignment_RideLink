package com.ridelink.driver.unit;

import com.ridelink.driver.document.Driver;
import com.ridelink.driver.document.DriverLocation;
import com.ridelink.driver.domain.AvailabilityStatus;
import com.ridelink.driver.dto.request.UpdateLocationRequest;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.service.DriverLocationService;
import java.math.BigDecimal;
import java.time.Instant;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DriverLocationServiceTest {
    private final MongoTemplate mongo = mock(MongoTemplate.class);
    private final DriverLocationService service = new DriverLocationService(mongo);
    private final UpdateLocationRequest request = new UpdateLocationRequest(new BigDecimal("6.9271"), new BigDecimal("79.8612"));

    @ParameterizedTest
    @EnumSource(AvailabilityStatus.class)
    void updatesOnlyLocationForEveryStatus(AvailabilityStatus status) {
        var stored = new DriverLocation(6.9271, 79.8612, Instant.parse("2026-09-28T00:00:00Z"));
        when(mongo.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Driver.class)))
                .thenReturn(new Driver("driver-1", "account-1", "LIC-1", "Colombo", status, stored));
        Instant before = Instant.now();
        var response = service.update("driver-1", request);
        assertThat(response.location()).isEqualTo(stored);
        assertThat(response.availabilityStatus()).isEqualTo(status);
        assertThat(response.accountId()).isEqualTo("account-1");
        var query = ArgumentCaptor.forClass(Query.class);
        var update = ArgumentCaptor.forClass(Update.class);
        var options = ArgumentCaptor.forClass(FindAndModifyOptions.class);
        verify(mongo).findAndModify(query.capture(), update.capture(), options.capture(), eq(Driver.class));
        assertThat(query.getValue().getQueryObject()).isEqualTo(new Document("_id", "driver-1"));
        Document change = update.getValue().getUpdateObject();
        assertThat(change).containsOnlyKeys("$set");
        Document fields = (Document) change.get("$set");
        assertThat(fields).containsOnlyKeys("location");
        DriverLocation location = (DriverLocation) fields.get("location");
        assertThat(location.latitude()).isEqualTo(6.9271);
        assertThat(location.longitude()).isEqualTo(79.8612);
        assertThat(location.updatedAt()).isBetween(before, Instant.now());
        assertThat(options.getValue().isReturnNew()).isTrue();
        assertThat(options.getValue().isUpsert()).isFalse();
        verifyNoMoreInteractions(mongo);
    }

    @Test
    void missingDriverReturnsNotFound() {
        assertThatThrownBy(() -> service.update("missing", request)).isInstanceOf(DriverNotFoundException.class);
    }

    @Test
    void databaseFailurePropagates() {
        when(mongo.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Driver.class)))
                .thenThrow(new DataAccessResourceFailureException("unavailable"));
        assertThatThrownBy(() -> service.update("driver-1", request)).isInstanceOf(DataAccessResourceFailureException.class);
    }
}

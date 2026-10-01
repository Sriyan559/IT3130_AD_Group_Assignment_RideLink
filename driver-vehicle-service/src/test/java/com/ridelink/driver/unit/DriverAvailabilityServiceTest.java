package com.ridelink.driver.unit;

import com.ridelink.driver.document.Driver;
import com.ridelink.driver.domain.AvailabilityStatus;
import com.ridelink.driver.dto.request.UpdateAvailabilityRequest;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.exception.DriverOnTripException;
import com.ridelink.driver.service.DriverAvailabilityService;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DriverAvailabilityServiceTest {
    private final MongoTemplate mongo = mock(MongoTemplate.class);
    private final DriverAvailabilityService service = new DriverAvailabilityService(mongo);

    @ParameterizedTest
    @ValueSource(strings = {"AVAILABLE", "OFFLINE"})
    // Mockito matchers/captors return null placeholders; the mock does not consume them.
    @SuppressWarnings("null")
    void atomicallyUpdatesOnlyAvailabilityAndReturnsPersistedProfile(String target) {
        var state = AvailabilityStatus.valueOf(target);
        when(mongo.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Driver.class)))
                .thenReturn(new Driver("driver-1", "account-1", "B1234567", "Malabe", state));

        var response = service.update("driver-1", new UpdateAvailabilityRequest(target));

        assertThat(response.id()).isEqualTo("driver-1");
        assertThat(response.accountId()).isEqualTo("account-1");
        assertThat(response.licenseNumber()).isEqualTo("B1234567");
        assertThat(response.serviceArea()).isEqualTo("Malabe");
        assertThat(response.availabilityStatus()).isEqualTo(state);
        var query = ArgumentCaptor.forClass(Query.class);
        var update = ArgumentCaptor.forClass(Update.class);
        var options = ArgumentCaptor.forClass(FindAndModifyOptions.class);
        verify(mongo).findAndModify(query.capture(), update.capture(), options.capture(), eq(Driver.class));
        assertThat(query.getValue().getQueryObject()).isEqualTo(new Document("_id", "driver-1")
                .append("availabilityStatus", new Document("$in", List.of(AvailabilityStatus.AVAILABLE, AvailabilityStatus.OFFLINE))));
        assertThat(update.getValue().getUpdateObject())
                .isEqualTo(new Document("$set", new Document("availabilityStatus", state)));
        assertThat(options.getValue().isReturnNew()).isTrue();
        assertThat(options.getValue().isUpsert()).isFalse();
        verifyNoMoreInteractions(mongo);
    }

    @Test
    // Mockito matchers/captors return null placeholders; the mock does not consume them.
    @SuppressWarnings("null")
    void missingDriverIsNotCreated() {
        assertThatThrownBy(() -> service.update("missing", new UpdateAvailabilityRequest("AVAILABLE")))
                .isInstanceOf(DriverNotFoundException.class);
        var query = ArgumentCaptor.forClass(Query.class);
        verify(mongo).exists(query.capture(), eq(Driver.class));
        assertThat(query.getValue().getQueryObject()).isEqualTo(new Document("_id", "missing"));
    }

    @Test
    // Mockito matchers/captors return null placeholders; the mock does not consume them.
    @SuppressWarnings("null")
    void existingIneligibleDriverReturnsConflict() {
        when(mongo.exists(any(Query.class), eq(Driver.class))).thenReturn(true);
        assertThatThrownBy(() -> service.update("driver-1", new UpdateAvailabilityRequest("OFFLINE")))
                .isInstanceOf(DriverOnTripException.class);
    }

    @Test
    // Mockito matchers/captors return null placeholders; the mock does not consume them.
    @SuppressWarnings("null")
    void writeFailurePropagatesWithoutFallbackWrite() {
        when(mongo.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Driver.class)))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));
        assertThatThrownBy(() -> service.update("driver-1", new UpdateAvailabilityRequest("AVAILABLE")))
                .isInstanceOf(DataAccessResourceFailureException.class);
        verify(mongo, never()).exists(any(Query.class), eq(Driver.class));
    }

    @Test
    // Mockito matchers/captors return null placeholders; the mock does not consume them.
    @SuppressWarnings("null")
    void existenceCheckFailurePropagates() {
        when(mongo.exists(any(Query.class), eq(Driver.class)))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));
        assertThatThrownBy(() -> service.update("driver-1", new UpdateAvailabilityRequest("OFFLINE")))
                .isInstanceOf(DataAccessResourceFailureException.class);
    }

    @Test
    void onTripTargetIsRejectedEvenForDirectServiceCall() {
        assertThatThrownBy(() -> service.update("driver-1", new UpdateAvailabilityRequest("ON_TRIP")))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(mongo);
    }
}

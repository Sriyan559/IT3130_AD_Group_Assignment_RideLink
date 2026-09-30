package com.ridelink.driver.contract;

import com.ridelink.driver.controller.DriverLocationController;
import com.ridelink.driver.document.DriverLocation;
import com.ridelink.driver.domain.AvailabilityStatus;
import com.ridelink.driver.dto.response.DriverResponse;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.service.DriverLocationService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DriverLocationController.class)
class DriverLocationControllerTest {
    @Autowired private MockMvc mvc;
    @MockBean private DriverLocationService service;
    private static final String PATH = "/api/drivers/driver-1/location";

    @ParameterizedTest
    @ValueSource(strings = {"{\"latitude\":6.9271,\"longitude\":79.8612}",
            "{\"latitude\":-90,\"longitude\":-180}", "{\"latitude\":90,\"longitude\":180}",
            "{\"latitude\":0,\"longitude\":0}"})
    void acceptsCoordinatesIncludingBoundariesAndZero(String body) throws Exception {
        when(service.update(eq("driver-1"), any())).thenAnswer(call -> {
            var request = call.getArgument(1, com.ridelink.driver.dto.request.UpdateLocationRequest.class);
            return new DriverResponse("driver-1", "account-1", "LIC-1", "Colombo", AvailabilityStatus.OFFLINE,
                    new DriverLocation(request.latitude().doubleValue(), request.longitude().doubleValue(),
                            Instant.parse("2026-09-28T00:00:00Z")));
        });
        mvc.perform(put(PATH).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value("driver-1"))
                .andExpect(jsonPath("$.availabilityStatus").value("OFFLINE"))
                .andExpect(jsonPath("$.location.latitude").isNumber())
                .andExpect(jsonPath("$.location.longitude").isNumber())
                .andExpect(jsonPath("$.location.updatedAt").value("2026-09-28T00:00:00Z"));
        verify(service).update(eq("driver-1"), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"latitude\":0}", "{\"longitude\":0}",
            "{\"latitude\":null,\"longitude\":null}",
            "{\"latitude\":90.0001,\"longitude\":0}", "{\"latitude\":-90.0001,\"longitude\":0}",
            "{\"latitude\":0,\"longitude\":180.0001}", "{\"latitude\":0,\"longitude\":-180.0001}"})
    void rejectsMissingAndOutOfRangeCoordinatesWithoutWriting(String body) throws Exception {
        mvc.perform(put(PATH).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "{bad", "{\"latitude\":[],\"longitude\":0}",
            "{\"latitude\":true,\"longitude\":0}", "{\"latitude\":\"NaN\",\"longitude\":0}",
            "{\"latitude\":0,\"longitude\":\"Infinity\"}"})
    void rejectsMalformedCoordinatesWithoutWriting(String body) throws Exception {
        mvc.perform(put(PATH).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
        verifyNoInteractions(service);
    }

    @Test
    void missingDriverReturns404() throws Exception {
        when(service.update(anyString(), any())).thenThrow(new DriverNotFoundException());
        error(404, "DRIVER_NOT_FOUND", "Driver profile not found");
    }

    @Test
    void databaseFailureReturnsSafe503() throws Exception {
        when(service.update(anyString(), any())).thenThrow(new DataAccessResourceFailureException("private details"));
        error(503, "DATABASE_UNAVAILABLE", "Driver data is temporarily unavailable");
    }

    private void error(int code, String error, String message) throws Exception {
        mvc.perform(put(PATH).contentType(MediaType.APPLICATION_JSON).content("{\"latitude\":0,\"longitude\":0}"))
                .andExpect(status().is(code)).andExpect(jsonPath("$.error").value(error))
                .andExpect(jsonPath("$.message").value(message)).andExpect(jsonPath("$.path").value(PATH));
    }
}

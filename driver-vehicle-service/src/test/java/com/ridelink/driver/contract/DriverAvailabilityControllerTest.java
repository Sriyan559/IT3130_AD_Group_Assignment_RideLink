package com.ridelink.driver.contract;

import com.ridelink.driver.controller.DriverController;
import com.ridelink.driver.domain.AvailabilityStatus;
import com.ridelink.driver.dto.request.UpdateAvailabilityRequest;
import com.ridelink.driver.dto.response.DriverResponse;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.exception.DriverOnTripException;
import com.ridelink.driver.service.DriverAvailabilityService;
import com.ridelink.driver.service.DriverService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static java.util.Objects.requireNonNull;

@WebMvcTest(DriverController.class)
class DriverAvailabilityControllerTest {
    @Autowired private MockMvc mvc;
    @MockBean private DriverService driverService;
    @MockBean private DriverAvailabilityService service;
    private static final String PATH = "/api/drivers/driver-1/availability";

    @ParameterizedTest
    @ValueSource(strings = {"AVAILABLE", "OFFLINE"})
    void validStatusReturnsUpdatedProfile(String target) throws Exception {
        var request = new UpdateAvailabilityRequest(target);
        when(service.update("driver-1", request)).thenReturn(new DriverResponse(
                "driver-1", "account-1", "B1234567", "Malabe", AvailabilityStatus.valueOf(target)));
        mvc.perform(put(PATH).contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"availabilityStatus\":\"" + target + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("driver-1"))
                .andExpect(jsonPath("$.accountId").value("account-1"))
                .andExpect(jsonPath("$.licenseNumber").value("B1234567"))
                .andExpect(jsonPath("$.serviceArea").value("Malabe"))
                .andExpect(jsonPath("$.availabilityStatus").value(target));
        verify(service).update("driver-1", request);
        verifyNoInteractions(driverService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"availabilityStatus\":null}", "{\"availabilityStatus\":\"\"}",
            "{\"availabilityStatus\":\" \"}", "{\"availabilityStatus\":\"ON_TRIP\"}",
            "{\"availabilityStatus\":\"available\"}", "{\"availabilityStatus\":\" AVAILABLE \"}",
            "{\"availabilityStatus\":\"UNKNOWN\"}", "{\"availabilityStatus\":1}"})
    void invalidStatusIsRejectedBeforeWriting(String body) throws Exception {
        mvc.perform(put(PATH).contentType(MediaType.APPLICATION_JSON_VALUE).content(requireNonNull(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value(PATH));
        verifyNoInteractions(service, driverService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "{bad", "{\"availabilityStatus\":[]}"})
    void malformedBodyIsRejected(String body) throws Exception {
        mvc.perform(put(PATH).contentType(MediaType.APPLICATION_JSON_VALUE).content(requireNonNull(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
        verifyNoInteractions(service, driverService);
    }

    @Test
    void missingDriverReturns404() throws Exception {
        when(service.update(eq("driver-1"), any())).thenThrow(new DriverNotFoundException());
        performError(404, "DRIVER_NOT_FOUND", "Driver profile not found");
    }

    @Test
    void onTripDriverReturns409() throws Exception {
        when(service.update(eq("driver-1"), any())).thenThrow(new DriverOnTripException());
        performError(409, "DRIVER_ON_TRIP", "Availability cannot be changed while the driver is on a trip");
    }

    @Test
    void databaseFailureReturnsSafe503() throws Exception {
        when(service.update(eq("driver-1"), any()))
                .thenThrow(new DataAccessResourceFailureException("internal database details"));
        performError(503, "DATABASE_UNAVAILABLE", "Driver data is temporarily unavailable");
    }

    private void performError(int statusCode, String code, String message) throws Exception {
        mvc.perform(put(PATH).contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"availabilityStatus\":\"AVAILABLE\"}"))
                .andExpect(status().is(statusCode))
                .andExpect(jsonPath("$.status").value(statusCode))
                .andExpect(jsonPath("$.error").value(code))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.path").value(PATH));
    }
}

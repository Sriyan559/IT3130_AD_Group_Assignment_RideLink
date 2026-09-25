package com.ridelink.driver.contract;

import com.ridelink.driver.controller.DriverController;
import com.ridelink.driver.domain.AvailabilityStatus;
import com.ridelink.driver.dto.request.CreateDriverRequest;
import com.ridelink.driver.dto.response.DriverResponse;
import com.ridelink.driver.service.DriverService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DriverController.class)
class DriverControllerTest {
    @Autowired private MockMvc mvc;
    @MockBean private DriverService service;

    private static final String VALID = """
            {"accountId":"account-1","licenseNumber":"B1234567","serviceArea":"Malabe"}
            """;

    @Test
    void validRequestReturnsCreatedDriver() throws Exception {
        when(service.create(any())).thenReturn(new DriverResponse("driver-1", "account-1",
                "B1234567", "Malabe", AvailabilityStatus.OFFLINE));
        mvc.perform(post("/api/drivers").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value("driver-1"))
                .andExpect(jsonPath("$.availabilityStatus").value("OFFLINE"));
        verify(service).create(new CreateDriverRequest("account-1", "B1234567", "Malabe"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"accountId\":\" \",\"licenseNumber\":\"B1\",\"serviceArea\":\"Malabe\"}",
            "{\"accountId\":\"a1\",\"licenseNumber\":\" \",\"serviceArea\":\"Malabe\"}",
            "{\"accountId\":\"a1\",\"licenseNumber\":\"B1\",\"serviceArea\":\" \"}"})
    void requiredFieldsRejectBlankOrMissingValues(String json) throws Exception {
        mvc.perform(post("/api/drivers").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/drivers"));
        verifyNoInteractions(service);
    }

    @Test
    void overlongLicenceIsRejected() throws Exception {
        mvc.perform(post("/api/drivers").contentType(MediaType.APPLICATION_JSON)
                        .content(VALID.replace("B1234567", "B".repeat(51))))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{bad", "", "null"})
    void malformedOrMissingBodyIsRejected(String json) throws Exception {
        mvc.perform(post("/api/drivers").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
        verifyNoInteractions(service);
    }

    @Test
    void duplicateIsConflictWithoutDatabaseDetails() throws Exception {
        when(service.create(any())).thenThrow(new DuplicateKeyException("internal database details"));
        mvc.perform(post("/api/drivers").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DRIVER_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value(
                        "A driver with this account ID or licence number already exists"));
    }

    @Test
    void databaseFailureIsServiceUnavailable() throws Exception {
        when(service.create(any())).thenThrow(new DataAccessResourceFailureException("internal details"));
        mvc.perform(post("/api/drivers").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("DATABASE_UNAVAILABLE"));
    }
}

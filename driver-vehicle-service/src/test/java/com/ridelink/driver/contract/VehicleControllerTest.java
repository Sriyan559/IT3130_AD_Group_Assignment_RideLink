package com.ridelink.driver.contract;

import com.ridelink.driver.controller.VehicleController;
import com.ridelink.driver.dto.request.RegisterVehicleRequest;
import com.ridelink.driver.dto.response.VehicleResponse;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.exception.VehicleAlreadyExistsException;
import com.ridelink.driver.service.VehicleService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
class VehicleControllerTest {
    @Autowired private MockMvc mvc;
    @MockBean private VehicleService service;
    private static final String URL = "/api/drivers/driver-1/vehicles";
    private static final String VALID = """
            {"plateNumber":"ABC-1234","make":"Toyota","model":"Prius","vehicleClass":"CAR","capacity":4}
            """;

    @Test
    void registersVehicleUnderPathDriver() throws Exception {
        when(service.register(eq("driver-1"), any())).thenReturn(new VehicleResponse(
                "vehicle-1", "driver-1", "ABC1234", "Toyota", "Prius", "CAR", 4));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("vehicle-1"))
                .andExpect(jsonPath("$.driverId").value("driver-1"))
                .andExpect(jsonPath("$.plateNumber").value("ABC1234"))
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Prius"))
                .andExpect(jsonPath("$.vehicleClass").value("CAR"))
                .andExpect(jsonPath("$.capacity").value(4));
        verify(service).register("driver-1", new RegisterVehicleRequest("ABC-1234", "Toyota", "Prius", "CAR", 4));
    }

    @ParameterizedTest
    @ValueSource(strings = {"plateNumber", "make", "model", "vehicleClass", "capacity"})
    void requiredFieldsCannotBeNull(String field) throws Exception {
        String json = VALID.replaceAll("\"" + field + "\":(?:\"[^\"]*\"|4)", "\"" + field + "\":null");
        rejectValidation(json);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABC-1234", "Toyota", "Prius", "CAR"})
    void textFieldsCannotBeBlank(String value) throws Exception {
        rejectValidation(VALID.replace(value, " "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void capacityMustBePositive(String value) throws Exception {
        rejectValidation(VALID.replace("\"capacity\":4", "\"capacity\":" + value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABC/1234", "---", "ABC_1234", "ABC@1234", "ABCDEFGHIJKLMNOPQRSTU"})
    void rejectsInvalidPlates(String plate) throws Exception {
        rejectValidation(VALID.replace("ABC-1234", plate));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Toyota", "Prius", "CAR"})
    void rejectsOverlongAttributes(String value) throws Exception {
        rejectValidation(VALID.replace(value, "A".repeat(51)));
    }

    @Test
    void missingFieldsAreRejected() throws Exception {
        rejectValidation("{}");
    }

    private void rejectValidation(String json) throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "{broken"})
    void rejectsMalformedBody(String json) throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
        verifyNoInteractions(service);
    }

    @Test
    void missingDriverReturns404() throws Exception {
        when(service.register(eq("driver-1"), any())).thenThrow(new DriverNotFoundException());
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("DRIVER_NOT_FOUND"));
    }

    @Test
    void duplicateVehicleReturnsSafe409() throws Exception {
        when(service.register(eq("driver-1"), any()))
                .thenThrow(new VehicleAlreadyExistsException(new DuplicateKeyException("internal details")));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("VEHICLE_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("A vehicle with this plate number already exists"))
                .andExpect(jsonPath("$.path").value(URL));
    }

    @Test
    void databaseUnavailableReturns503() throws Exception {
        when(service.register(eq("driver-1"), any()))
                .thenThrow(new DataAccessResourceFailureException("internal details"));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("DATABASE_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Driver data is temporarily unavailable"));
    }
}

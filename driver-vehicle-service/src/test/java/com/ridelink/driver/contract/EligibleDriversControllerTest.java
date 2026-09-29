package com.ridelink.driver.contract;

import com.ridelink.driver.controller.EligibleDriversController;
import com.ridelink.driver.document.DriverLocation;
import com.ridelink.driver.dto.response.EligibleDriverResponse;
import com.ridelink.driver.dto.response.VehicleResponse;
import com.ridelink.driver.service.EligibleDriversService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EligibleDriversController.class)
class EligibleDriversControllerTest {
    @Autowired private MockMvc mvc;
    @MockBean private EligibleDriversService service;
    private static final String PATH = "/api/drivers/eligible";

    @Test
    void returnsMatchingDriversAndVehiclesWithoutPrivateProfileFields() throws Exception {
        when(service.find(any())).thenReturn(List.of(new EligibleDriverResponse("d1",
                new DriverLocation(6.9271, 79.8612, Instant.parse("2026-09-29T00:00:00Z")), 0,
                List.of(new VehicleResponse("v1", "d1", "ABC123", "Toyota", "Aqua", "CAR", 4)))));
        mvc.perform(get(PATH).param("lat", "6.9271").param("lng", "79.8612").param("radius", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].driverId").value("d1"))
                .andExpect(jsonPath("$[0].distanceKm").value(0))
                .andExpect(jsonPath("$[0].location.latitude").value(6.9271))
                .andExpect(jsonPath("$[0].vehicles[0].id").value("v1"))
                .andExpect(jsonPath("$[0].accountId").doesNotExist())
                .andExpect(jsonPath("$[0].licenseNumber").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {"lat=90&lng=180&radius=50", "lat=-90&lng=-180&radius=0.1", "lat=0&lng=0&radius=5"})
    void boundariesReturn200AndEmptyList(String query) throws Exception {
        when(service.find(any())).thenReturn(List.of());
        mvc.perform(get(PATH + "?" + query)).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "lat=0&lng=0", "lng=0&radius=5", "lat=0&radius=5",
            "lat=91&lng=0&radius=5", "lat=-91&lng=0&radius=5", "lat=0&lng=181&radius=5",
            "lat=0&lng=-181&radius=5", "lat=0&lng=0&radius=0", "lat=0&lng=0&radius=-1",
            "lat=0&lng=0&radius=50.001", "lat=NaN&lng=0&radius=5", "lat=0&lng=Infinity&radius=5",
            "lat=0&lng=0&radius=abc", "lat=&lng=0&radius=5"})
    void invalidQueryReturnsStructured400BeforeDatabaseAccess(String query) throws Exception {
        mvc.perform(get(PATH + "?" + query)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value(PATH));
        verifyNoInteractions(service);
    }

    @Test
    void databaseFailureIsSafe503() throws Exception {
        when(service.find(any())).thenThrow(new DataAccessResourceFailureException("private internal data"));
        mvc.perform(get(PATH + "?lat=0&lng=0&radius=5")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("DATABASE_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Driver data is temporarily unavailable"));
    }
}

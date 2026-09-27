package com.ridelink.payment.unit;

import com.ridelink.payment.dto.request.FareEstimateRequest;
import com.ridelink.payment.dto.response.FareEstimateResponse;
import com.ridelink.payment.service.FareEstimationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FareController.class)
class FareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FareEstimationService fareEstimationService;

    @Test
    void returnsEstimateForPositiveDistance() throws Exception {
        when(fareEstimationService.estimate(any(FareEstimateRequest.class)))
                .thenReturn(new FareEstimateResponse(new BigDecimal("8"), new BigDecimal("950.00"), "LKR"));

        mockMvc.perform(post("/api/fares/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"distanceKilometers": 8}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedFare").value(950.00))
                .andExpect(jsonPath("$.currency").value("LKR"));
    }

    @Test
    void rejectsZeroDistanceWithValidationError() throws Exception {
        mockMvc.perform(post("/api/fares/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"distanceKilometers": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/fares/estimate"));
    }
}
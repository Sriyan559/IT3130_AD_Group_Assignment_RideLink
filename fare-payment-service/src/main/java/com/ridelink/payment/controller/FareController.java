package com.ridelink.payment.controller;

import com.ridelink.payment.dto.request.FareEstimateRequest;
import com.ridelink.payment.dto.response.FareEstimateResponse;
import com.ridelink.payment.service.FareEstimationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fares")
@Tag(name = "Fares", description = "Fare estimation and calculation")
public class FareController {

    private final FareEstimationService fareEstimationService;

    public FareController(FareEstimationService fareEstimationService) {
        this.fareEstimationService = fareEstimationService;
    }

    @PostMapping(value = "/estimate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Estimate a fare", description = "Calculates an LKR fare from a positive trip distance.")
    public FareEstimateResponse estimate(@Valid @RequestBody FareEstimateRequest request) {
        return fareEstimationService.estimate(request);
    }
}
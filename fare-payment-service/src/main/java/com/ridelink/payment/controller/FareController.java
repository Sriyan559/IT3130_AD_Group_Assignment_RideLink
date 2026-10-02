package com.ridelink.payment.controller;

import com.ridelink.payment.dto.request.FareEstimateRequest;
import com.ridelink.payment.dto.request.FinalFareRequest;
import com.ridelink.payment.dto.response.FareEstimateResponse;
import com.ridelink.payment.dto.response.FinalFareResponse;
import com.ridelink.payment.exception.ApiErrorResponse;
import com.ridelink.payment.service.FareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Fare endpoints. {@code /api/fares/...} and {@code /api/v1/fare/...} are equivalent. */
@RestController
@Tag(name = "Fares", description = "Fare estimate and final fare calculation")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @PostMapping({"/api/fares/estimate", "/api/v1/fare/estimate"})
    @Operation(summary = "Estimate a fare",
            description = "Applies max(minimum, base + km x perKm + minutes x perMinute). Nothing is stored.")
    @ApiResponse(responseCode = "200", description = "Itemised estimate")
    @ApiResponse(responseCode = "400", description = "Distance or duration out of range",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public FareEstimateResponse estimate(@Valid @RequestBody FareEstimateRequest request) {
        return FareEstimateResponse.from(
                fareService.estimate(request.distanceKilometers(), request.durationMinutes()),
                fareService.describeRule());
    }

    @PostMapping({"/api/fares/final", "/api/v1/fare/final"})
    @Operation(summary = "Calculate the final fare of a completed ride",
            description = "Distance and duration are read from Ride Management Service. Calling again returns "
                    + "the same stored fare.")
    @ApiResponse(responseCode = "200", description = "Final fare")
    @ApiResponse(responseCode = "403", description = "Ride belongs to another passenger",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Ride not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Ride is not COMPLETED",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public FinalFareResponse finalizeFare(@Valid @RequestBody FinalFareRequest request) {
        return FinalFareResponse.from(fareService.finalizeFare(request.rideId()));
    }

    @GetMapping({"/api/fares/final/{rideId}", "/api/v1/fare/final/{rideId}"})
    @Operation(summary = "Get the stored final fare of a ride")
    @ApiResponse(responseCode = "200", description = "Final fare")
    @ApiResponse(responseCode = "404", description = "No final fare yet",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public FinalFareResponse getFinalFare(@PathVariable UUID rideId) {
        return FinalFareResponse.from(fareService.getFinalFare(rideId));
    }
}
